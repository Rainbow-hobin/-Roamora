"""API 业务层：会话状态管理与人机流程调用"""
import asyncio
import time
from collections import OrderedDict
from dataclasses import dataclass
from typing import Any, AsyncIterator, Dict, List, Optional, Tuple

from langchain_core.messages import AIMessage, BaseMessage, HumanMessage

from chat_history_manager import get_chat_history_manager
from graph.state import GlobalState
from graph.workflow import travel_graph
from user_profile_manager import profile_scope
from api.answer_checks import check_answer


class SessionNotFound(Exception):
    """会话不存在"""


@dataclass
class ChatResult:
    """对话结果"""
    session_id: str
    answer: str
    current_agent: Optional[str]
    needs_clarification: bool
    elapsed_ms: int


def build_initial_state(history: Optional[List[BaseMessage]] = None) -> GlobalState:
    """构造一轮对话的初始状态，history 用于恢复多轮上下文"""
    return {
        "messages": history or [],
        "user_query": None,
        "planner_context": None,
        "executor_context": None,
        "summarizer_context": None,
        "current_agent": None,
        "next_agent": None,
        "is_complete": False,
        "needs_clarification": False,
        "needs_replan": False,
        "feedback_type": None,
        "confirmation_message": None,
        "preference_updates": None,
    }


def extract_answer(result: Dict[str, Any]) -> str:
    """从最终状态中取出要展示给用户的回答"""
    planner = result.get("planner_context") or {}
    if planner.get("needs_clarification"):
        return planner.get("clarification_question") or "请提供更多信息"

    summarizer = result.get("summarizer_context") or {}
    if summarizer.get("final_summary"):
        return summarizer["final_summary"]

    for msg in reversed(result.get("messages", [])):
        if isinstance(msg, AIMessage) or getattr(msg, "type", None) == "ai":
            return msg.content

    return "处理完成，但没有生成回答。"


class ChatService:
    """编排会话、状态与 Agent 调用"""

    def __init__(self, max_cached_sessions: int = 200):
        self.chat_manager = get_chat_history_manager()
        self.max_cached_sessions = max_cached_sessions
        self._states: "OrderedDict[str, GlobalState]" = OrderedDict()
        self._locks: Dict[str, asyncio.Lock] = {}

    # ---------- 会话管理 ----------

    def create_session(self, title: Optional[str] = None
                       , user_id: Optional[str] = None) -> str:
        session_id = self.chat_manager.create_session(user_id=user_id, title=title)
        self._states[session_id] = build_initial_state()
        return session_id

    def delete_session(self, session_id: str) -> None:
        self.chat_manager.delete_session(session_id)
        self._states.pop(session_id, None)
        self._locks.pop(session_id, None)

    def get_session(self, session_id: str):
        return self.chat_manager.get_session(session_id)

    def list_sessions(self, user_id: Optional[str] = None, limit: int = 50):
        return self.chat_manager.get_user_sessions(user_id, limit=limit)

    def list_messages(self, session_id: str) -> List[Dict[str, Any]]:
        rows = self.chat_manager.get_session_messages(session_id)
        return [
            {
                "role": "user" if row.message_type == "user" else "assistant",
                "content": row.content,
                "timestamp": row.timestamp,
                "metadata": row.metadata,
            }
            for row in rows
        ]

    def health(self) -> Dict[str, Any]:
        sessions = self.chat_manager.get_user_sessions(limit=1)
        return {"sessions": len(self._states), "last_session": sessions[0].session_id if sessions else None}

    # ---------- 内部状态 ----------

    def _lock_for(self, session_id: str) -> asyncio.Lock:
        lock = self._locks.get(session_id)
        if lock is None:
            lock = asyncio.Lock()
            self._locks[session_id] = lock
        return lock

    def _load_state(self, session_id: str) -> GlobalState:
        """优先用内存状态（保留工具结果复用能力），否则从数据库恢复对话历史"""
        state = self._states.get(session_id)
        if state is not None:
            self._states.move_to_end(session_id)
            return state

        history: List[BaseMessage] = []
        for msg in self.chat_manager.get_session_messages(session_id):
            if msg.message_type == "user":
                history.append(HumanMessage(content=msg.content))
            else:
                history.append(AIMessage(content=msg.content))

        state = build_initial_state(history)
        self._states[session_id] = state
        self._evict_states()
        return state

    def _evict_states(self) -> None:
        while len(self._states) > self.max_cached_sessions:
            old_id, _ = self._states.popitem(last=False)
            self._locks.pop(old_id, None)

    def _prepare_turn(self, session_id: Optional[str], message: str,
                      user_id: Optional[str]) -> Tuple[str, GlobalState]:
        """校验会话、写入用户消息、重置本轮控制字段"""
        if session_id:
            if not self.chat_manager.session_exists(session_id):
                raise SessionNotFound(session_id)
            sid = session_id
        else:
            sid = self.create_session(user_id=user_id)

        profile_scope.set("web_session_" + sid)
        state = self._load_state(sid)

        # 与 Streamlit 版本保持一致：保留历史对话，重置子 Agent 上下文
        state["user_query"] = message
        state["planner_context"] = None
        state["executor_context"] = None
        state["summarizer_context"] = None
        state["current_agent"] = None
        state["next_agent"] = None
        state["is_complete"] = False
        state["needs_clarification"] = False
        state["needs_replan"] = False
        state["feedback_type"] = None
        state["confirmation_message"] = None
        state["preference_updates"] = None

        self.chat_manager.add_message(sid, "user", message, user_id=user_id)
        state["messages"].append(HumanMessage(content=message))
        return sid, state

    def _finalize_turn(self, session_id: str, state: GlobalState,
                       user_id: Optional[str]) -> ChatResult:
        answer = check_answer(extract_answer(state))

        # Main 节点直接回答时已经写入了 AIMessage；其它节点只写了各自的 context，
        # 这里补齐 AI 消息，保证内存状态与数据库历史一致，下一轮能正确带上。
        messages = state.get("messages") or []
        already_added = any(
            isinstance(msg, AIMessage) and msg.content == answer
            for msg in messages
        )
        if not already_added:
            messages.append(AIMessage(content=answer))
        state["messages"] = messages

        self._states[session_id] = state
        self.chat_manager.add_message(session_id, "ai", answer, user_id=user_id)
        return ChatResult(
            session_id=session_id,
            answer=answer,
            current_agent=state.get("current_agent"),
            needs_clarification=bool((state.get("planner_context") or {}).get("needs_clarification")),
            elapsed_ms=0,
        )

    # ---------- 对话 ----------

    async def chat(self, message: str, session_id: Optional[str] = None,
                   user_id: Optional[str] = None) -> ChatResult:
        """同步执行一次完整对话"""
        started = time.perf_counter()
        lock = self._lock_for(session_id) if session_id else None
        sid: Optional[str] = None

        if lock:
            await lock.acquire()
        try:
            sid, state = self._prepare_turn(session_id, message, user_id)
            result = await travel_graph.ainvoke(state)
            chat_result = self._finalize_turn(sid, result, user_id)
            chat_result.elapsed_ms = int((time.perf_counter() - started) * 1000)
            return chat_result
        except Exception:
            # 失败时丢弃内存状态，下一轮从数据库重建，避免半成品状态被复用
            if sid:
                self._states.pop(sid, None)
            raise
        finally:
            if lock:
                lock.release()

    async def chat_stream(self, message: str, session_id: Optional[str] = None,
                          user_id: Optional[str] = None) -> AsyncIterator[Dict[str, Any]]:
        """流式执行，按节点推送进度事件"""
        started = time.perf_counter()
        lock = self._lock_for(session_id) if session_id else None

        if lock:
            await lock.acquire()
        sid = session_id
        try:
            sid, state = self._prepare_turn(session_id, message, user_id)
            yield {"event": "start", "data": {"session_id": sid}}

            last_snapshot: Dict[str, Any] = state
            last_agent = None
            async for snapshot in travel_graph.astream(state, stream_mode="values"):
                last_snapshot = snapshot
                agent = snapshot.get("current_agent")
                if agent and agent != last_agent:
                    last_agent = agent
                    yield {
                        "event": "agent",
                        "data": {
                            "agent": agent,
                            "is_complete": bool(snapshot.get("is_complete")),
                        },
                    }

            result = self._finalize_turn(sid, last_snapshot, user_id)
            yield {
                "event": "final",
                "data": {
                    "session_id": sid,
                    "answer": result.answer,
                    "current_agent": result.current_agent,
                    "needs_clarification": result.needs_clarification,
                    "elapsed_ms": int((time.perf_counter() - started) * 1000),
                },
            }
        except Exception as exc:
            if sid:
                self._states.pop(sid, None)
            yield {"event": "error", "data": {"message": str(exc)}}
        finally:
            if lock:
                lock.release()
