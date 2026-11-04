# Roamora · 智途星

**让旅行灵感，成为可执行的行程。**

Roamora 是一个面向桌面浏览器的智能旅行平台，集目的地探索、AI 行程规划、旅行社区与用户私聊于一体。前端采用 Vue 3，业务后端采用 Spring Boot，AI 规划服务采用 FastAPI 与 LangGraph。

An intelligent travel platform for discovering destinations, planning journeys with AI, sharing travel stories, and connecting through real-time private messaging.

## 功能

- **目的地探索**：浏览与搜索景点，查看地址、开放信息、攻略及公开资料来源，收藏感兴趣的目的地。
- **智能规划**：以自然语言描述需求，通过多轮对话、流式回答和多 Agent 工作流生成旅行建议。
- **行程管理**：创建和查看个人行程，管理旅行笔记。
- **旅行社区**：按板块浏览攻略、发布内容、参与评论，联系帖子作者。
- **实时私聊**：WebSocket 双向通信、多连接同步、未读计数、历史分页、失败重试与消息去重。消息先写入 MySQL，再推送给双方；支持离线后查看。
- **账号与管理**：验证码登录注册、JWT 鉴权、个人资料，以及用户、景点、评论和统计管理页面。

当前界面针对桌面浏览器设计，建议使用至少 1100px 宽的窗口。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| Web | Vue 3、TypeScript、Vite、Vue Router、Lucide |
| 业务 API | Java 17、Spring Boot 3.2.5、Spring Security、JPA、JDBC、JWT |
| 实时通信 | 原生 WebSocket、MySQL 消息持久化 |
| AI 服务 | Python、FastAPI、LangGraph、LangChain、DeepSeek、Qwen、MCP |
| 数据 | MySQL 8；AI 服务本地 SQLite 会话存储；可选 Chroma 检索 |

```text
桌面浏览器 / Vue
  ├─ /api、/uploads ── Spring Boot :8080 ── MySQL
  ├─ /ws/chat ──────── WebSocket 私聊 ──── MySQL
  └─ /agent ────────── FastAPI :8000 ───── LangGraph / 模型 / MCP
```

## 目录

```text
zhituxing-ui/                   Vue 前端
zhituxing-server/               Spring Boot 业务后端
  src/main/resources/db/        数据库初始化与私聊增量建表
  CHAT.md                       私聊接口、协议与测试说明
travel-agent/travel-agent/       AI 子项目（保留原目录与许可证）
  multi-agents/                 FastAPI、Agent、工具与会话管理
content/                        整理后的公开资料与来源
scripts/                        内容导入与聊天回归检查
```

## 本地启动

需要 Node.js 22.12+、Java 17、Maven 3.9+、MySQL 8 和兼容依赖的 Python 环境。以下命令以 PowerShell 为例；在项目根目录打开终端。

### 1. 数据库与后端

在 MySQL 客户端执行 `zhituxing-server/src/main/resources/db/schema.sql`。该脚本创建 `zhituxing` 数据库、业务表、演示数据及私聊表，演示数据只适合空库首次导入。已有数据库只新增私聊功能时执行 `db/chat.sql`。

```powershell
$env:MYSQL_PORT = "3307" # 按本机设置修改
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "your-database-password"
$env:JWT_SECRET = [guid]::NewGuid().ToString("N") + [guid]::NewGuid().ToString("N")
cd zhituxing-server
mvn spring-boot:run
```

后端默认监听 `8080`。发布版本要求通过环境变量提供数据库密码和 JWT 密钥；JWT 密钥至少 32 字节，部署时应固定并妥善保管。演示库含已知测试账号，正式部署前应删除或替换；数据库脚本中的演示评分、评论与浏览量不是实际用户反馈。

### 2. 前端

另开终端：

```powershell
cd zhituxing-ui
npm ci
npm run dev
```

打开 `http://localhost:23306`。Vite 已配置业务 API、上传图片、WebSocket 和 AI 服务代理。

### 3. AI 规划服务（可选）

基础业务和私聊不依赖 AI 服务；使用智能规划时启动此服务，并提供所需模型凭据。

```powershell
cd travel-agent/travel-agent
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r multi-agents/requirements.txt
Copy-Item .env.example .env
Copy-Item multi-agents/config/servers_config.example.json multi-agents/config/servers_config.json
```

编辑 `.env` 填入自己的模型 API Key，并按需配置 MCP 地址，再启动：

```powershell
cd multi-agents
python -m uvicorn api.main:app --host 127.0.0.1 --port 8000
```

AI 的在线模型和 MCP 查询依赖外部服务。Chroma 知识库不随仓库分发；其配置路径见 `config/settings.py`，需要检索功能时自行准备兼容的向量库。公开导览清单不会自动成为模型知识库。

## 公开导览内容

`content/catalog.json` 记录经整理的目的地资料、编辑攻略、来源链接和核对日期。相关说明见 [内容说明](content/README.md)。示意封面明确标示为示意图；票务、预约与开放信息请以官方最新公告为准。

初始化演示库后，可以在根目录执行以下命令补充导览内容（需要前面设置的 MySQL 环境变量）：

```powershell
python -m pip install pymysql bcrypt
python scripts/prepare-content.py
python scripts/import-content.py
```

导入器使用事务、同名检查和备份机制。备份包含原有记录，仅保存在本地，不应提交到仓库。

## 构建与检查

```powershell
npm --prefix zhituxing-ui run build
mvn -f zhituxing-server/pom.xml -DskipTests package
node scripts/test-chat-state.cjs
```

聊天状态回归覆盖跨标签身份变更、过期重登录、旧响应隔离、历史分页、请求乱序及账号缓存清理。真实接口与 WebSocket 检查另见 `scripts/test-chat-integration.py` 和 [私聊说明](zhituxing-server/CHAT.md)；运行该脚本需先启动 MySQL、后端和前端代理，并安装 `requests pymysql bcrypt websocket-client`。集成检查会创建临时账号并在结束后清理。

## 部署范围与说明

- 当前私聊的在线连接与限流信息保存在单个后端进程内；多实例部署需补充 Redis Pub/Sub 或消息代理。历史消息始终保存在 MySQL。
- 私聊采用服务端文本存储，不提供端到端加密。
- 上线需配置 HTTPS、WebSocket Upgrade 代理、实际前端允许来源、持久化上传目录及独立数据库账号。
- AI 服务的会话标识不能替代账号鉴权；公开部署前需在网关或服务端补齐账号授权与访问限制。
- 仓库不包含本地密钥、用户聊天数据库、用户画像、上传文件、数据库备份及本机工具。

## 来源与许可

AI 子项目在原有实现基础上集成和修改，保留其 [MIT 许可证与原版权声明](travel-agent/travel-agent/LICENSE)。公开导览资料的原始来源见内容清单；第三方资料和素材的权利归各自权利人所有。其他目录尚未单独声明开源许可证。
