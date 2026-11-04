"""API 请求与响应模型"""
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    """发送一条消息"""
    message: str = Field(..., min_length=1, max_length=4000, description="用户输入")
    session_id: Optional[str] = Field(None, description="会话 ID，不传则自动新建")
    user_id: Optional[str] = Field(None, description="用户 ID，默认 default_user")


class ChatResponse(BaseModel):
    """一次对话的完整结果"""
    session_id: str
    answer: str
    current_agent: Optional[str] = None
    needs_clarification: bool = False
    elapsed_ms: int


class SessionCreateRequest(BaseModel):
    title: Optional[str] = Field(None, max_length=100)
    user_id: Optional[str] = None


class SessionUpdateRequest(BaseModel):
    title: str = Field(..., min_length=1, max_length=100)


class MessageOut(BaseModel):
    role: str
    content: str
    timestamp: str
    metadata: Optional[str] = None


class SessionOut(BaseModel):
    session_id: str
    user_id: str
    title: str
    created_at: str
    updated_at: str
    message_count: int


class SessionDetailOut(SessionOut):
    messages: List[MessageOut]


class ToolOut(BaseModel):
    name: str
    description: str
    tool_type: str
    parameters: Dict[str, Any]


class HealthOut(BaseModel):
    status: str
    sessions: int
    detail: Dict[str, Any] = {}
