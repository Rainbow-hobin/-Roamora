"""FastAPI 服务入口

启动方式（在 multi-agents 目录下执行）：
    uvicorn api.main:app --host 0.0.0.0 --port 8000 --reload
或：
    python api/main.py
"""
import asyncio
import json
import os
import sys
from contextlib import asynccontextmanager, suppress
from dataclasses import asdict
from pathlib import Path
from typing import Optional

# 让 api 包与项目内模块（graph/config/tools）都能被导入
BASE_DIR = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE_DIR))

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse

from api.schemas import (
    ChatRequest,
    ChatResponse,
    HealthOut,
    MessageOut,
    SessionCreateRequest,
    SessionDetailOut,
    SessionOut,
    SessionUpdateRequest,
    ToolOut,
)
from api.service import ChatService, SessionNotFound
import tools.mcp_tools as mcp_module
from tools.mcp_tools import get_mcp_manager
from tools.tool_registry import get_all_tools
from user_profile_manager import get_profile_manager

chat_service = ChatService()


@asynccontextmanager
async def lifespan(_: FastAPI):
    """服务关闭时释放 MCP 连接"""
    yield
    manager = mcp_module._mcp_manager
    if manager is not None:
        with suppress(Exception):
            await manager.cleanup()


app = FastAPI(
    title="智能旅行规划助手 API",
    description="基于 LangGraph Multi-Agent 的旅行规划服务",
    version="1.0.0",
    lifespan=lifespan,
)

_origins = os.getenv("CORS_ORIGINS", "*")
app.add_middleware(
    CORSMiddleware,
    allow_origins=[origin.strip() for origin in _origins.split(",")],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


def _sse(event: str, data: dict) -> str:
    """格式化一条 SSE 消息"""
    payload = json.dumps(data, ensure_ascii=False)
    return f"event: {event}\ndata: {payload}\n\n"


@app.get("/")
async def root():
    return {
        "name": "智能旅行规划助手 API",
        "docs": "/docs",
        "endpoints": ["/api/health", "/api/sessions", "/api/chat", "/api/chat/stream", "/api/tools"],
    }


@app.get("/api/health", response_model=HealthOut)
async def health():
    detail = chat_service.health()
    return HealthOut(status="ok", sessions=detail["sessions"], detail=detail)


@app.get("/api/mcp/status")
async def mcp_status():
    """查看 MCP 服务连接状态（首次调用会触发建连）"""
    try:
        manager = await asyncio.wait_for(get_mcp_manager(), timeout=45)
    except Exception as exc:
        partial = mcp_module._mcp_manager
        if partial is not None:
            with suppress(Exception):
                await partial.cleanup()
            mcp_module._mcp_manager = None
        raise HTTPException(status_code=503, detail=f"MCP 初始化失败: {exc}")
    return {
        "connected": list(manager.mcp_servers.keys()),
        "connected_count": len(manager.mcp_servers),
    }


@app.get("/api/tools", response_model=list[ToolOut])
async def tools():
    """列出提供给模型的工具定义"""
    return [
        ToolOut(
            name=tool.name,
            description=tool.description,
            tool_type=tool.tool_type,
            parameters=tool.parameters,
        )
        for tool in get_all_tools()
    ]


@app.get("/api/profile")
async def profile(user_id: Optional[str] = Query(None)):
    """当前用户的偏好画像"""
    return get_profile_manager().load_profile(user_id)


@app.get("/api/sessions", response_model=list[SessionOut])
async def list_sessions(
    user_id: Optional[str] = Query(None),
    limit: int = Query(50, ge=1, le=200),
):
    return [SessionOut(**asdict(item)) for item in chat_service.list_sessions(user_id, limit)]


@app.post("/api/sessions", response_model=SessionOut)
async def create_session(body: SessionCreateRequest):
    session_id = chat_service.create_session(title=body.title, user_id=body.user_id)
    session = chat_service.get_session(session_id)
    return SessionOut(**asdict(session))


@app.get("/api/sessions/{session_id}", response_model=SessionDetailOut)
async def get_session(session_id: str):
    session = chat_service.get_session(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="会话不存在")
    messages = [MessageOut(**item) for item in chat_service.list_messages(session_id)]
    return SessionDetailOut(**asdict(session), messages=messages)


@app.patch("/api/sessions/{session_id}", response_model=SessionOut)
async def rename_session(session_id: str, body: SessionUpdateRequest):
    if chat_service.get_session(session_id) is None:
        raise HTTPException(status_code=404, detail="会话不存在")
    chat_service.chat_manager.update_session_title(session_id, body.title)
    return SessionOut(**asdict(chat_service.get_session(session_id)))


@app.delete("/api/sessions/{session_id}")
async def delete_session(session_id: str):
    if chat_service.get_session(session_id) is None:
        raise HTTPException(status_code=404, detail="会话不存在")
    chat_service.delete_session(session_id)
    return {"deleted": True, "session_id": session_id}


@app.post("/api/chat", response_model=ChatResponse)
async def chat(body: ChatRequest):
    """阻塞式对话：等 Agent 全部执行完才返回"""
    try:
        result = await chat_service.chat(body.message, body.session_id, body.user_id)
    except SessionNotFound:
        raise HTTPException(status_code=404, detail="会话不存在")
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"处理失败: {exc}")

    return ChatResponse(
        session_id=result.session_id,
        answer=result.answer,
        current_agent=result.current_agent,
        needs_clarification=result.needs_clarification,
        elapsed_ms=result.elapsed_ms,
    )


@app.post("/api/chat/stream")
async def chat_stream(body: ChatRequest):
    """流式对话：按 Agent 节点推送 SSE 事件

    事件顺序：start -> agent(可多次) -> final；失败时推送 error。
    前端请使用 fetch + ReadableStream 读取（EventSource 不支持 POST）。
    """
    if body.session_id and chat_service.get_session(body.session_id) is None:
        raise HTTPException(status_code=404, detail="会话不存在")

    async def event_generator():
        async for item in chat_service.chat_stream(body.message, body.session_id, body.user_id):
            yield _sse(item["event"], item["data"])

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "api.main:app",
        host=os.getenv("API_HOST", "0.0.0.0"),
        port=int(os.getenv("API_PORT", "8000")),
        reload=False,
    )
