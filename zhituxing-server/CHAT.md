# 用户私聊

入口：桌面导航「消息」(`/messages`)，也可在非平台编辑的社区帖子详情点击「私聊作者」，按用户名搜索后开启对话。未登录访问会进入登录页，登录后返回。

## 数据库与启动

首次升级已有数据库需执行 `src/main/resources/db/chat.sql`。全新数据库的 `schema.sql` 已包含此表。现有本地开发库已经应用本次增量迁移，原内容不删除。

启动 Spring Boot 服务与 Vue/Vite 开发服务。Vite 的 `/ws` 代理启用 WebSocket 转发到8080，REST仍使用`/api`代理。生产反向代理也需为`/ws/chat`转发Upgrade/Connection头；前端HTTPS会自动使用wss。

允许的浏览器来源通过 `zhituxing.chat.allowed-origins` 配置，以逗号分隔；默认仅 `http://localhost:23306,http://127.0.0.1:23306`。生产环境须设置为实际前端来源，不使用任意来源通配。

## 接口与协议

REST接口位于`/api/v1/chat`，均需已有Bearer JWT。

- `GET /users?q=用户名`：最少2字，最多20条，只返回ID、名称、头像，不返回手机号。排除自己、禁用账号与平台资料编辑账号。
- `GET /users/{id}`：取得对方公开聊天资料。
- `GET /conversations`：最近100段会话、最后一条消息和各自未读数。
- `GET /unread`：当前用户全部未读数。
- `GET /messages/{peer}?before=消息ID`：仅取得当前用户与peer的私聊，每页至多50条，按ID升序返回。before为排他游标，不传取最新一页。
- `POST /read/{peer}`，JSON `{"through":123}`：只将该对方向当前用户发送且ID不大于游标的消息标记已读，不会误清除之后到达的消息。

WebSocket地址`/ws/chat`。握手后第一帧发送`{"type":"auth","token":"JWT"}`，服务端返回`{"type":"ready","userId":用户ID}`。JWT不放URL或查询参数。未认证连接约5秒后关闭，已认证连接到JWT过期时关闭，每次发送还会核验账号状态。

发送帧：`{"type":"send","to":接收者ID,"content":"内容","clientId":"UUID"}`。发送者身份由JWT确定，服务端不采信客户端senderId。

保存成功后，双方所有在线连接收到`{"type":"message","message":消息对象}`。消息对象包含id/senderId/recipientId/content/clientId/createdAt/readAt。错误帧包含type=error、message、clientId。`{"type":"ping"}`收到pong；已读变化给当前用户其他连接广播read通知。

发送限制为1–2000个Java字符，每个连接每分钟最多60条发送请求。数据库对(sender_id,client_id)建立唯一约束，超时重试使用同一clientId；相同编号改发不同内容会被拒绝。客户端收到服务端持久化确认后才移除“发送中”，未确认支持重试。重新连接后补取当前会话历史；早期消息可分页读取。

## 验证与范围

`scripts/test-chat-integration.py`使用真实登录接口、3个独立临时账号、多个原生WebSocket客户端验证，含Vite代理、鉴权、跨来源拒绝、双向实时推送、多连接同步、第三方隔离、离线持久化、去重、已读游标、历史分页、超长/自聊限制和账号禁用，共18项检查通过。脚本结束会删除它自己创建的测试消息和账号。运行需通过环境变量提供本地MYSQL_PASSWORD。

前端TypeScript/Vite构建、后端Maven编译通过。本次按用户选择未运行浏览器技能，未声称已经完成浏览器交互或视觉验收。

在线连接表和限流窗口保存在进程内，当前适用于单后端实例。扩展多实例时，需增加Redis或消息代理转发，数据库保存和会话历史查询可继续复用。消息不是端到端加密内容，服务端会以文本保存在MySQL中。

## 前端状态回归

执行 `node scripts/test-chat-state.cjs`（项目根目录）。测试加载真实 TypeScript 模块、Vue 响应式状态与消息页脚本，用内存中的网络和WebSocket替身控制事件顺序，不访问数据库。10项检查覆盖过期重登录、跨标签令牌更新与退出、旧连接帧、旧401响应、重连历史游标、历史加载期间的新消息、会话请求乱序和账号缓存清理。

令牌变化会同步关闭旧连接、清空旧账号私信状态并重建连接。HTTP响应返回后会核对发起请求时的令牌，旧账号响应不会覆盖新身份；分页游标只来自服务端历史页，不从混合后的显示列表推导。
