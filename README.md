# webdev · 课程管理平台


## 0. 如何使用（从零跑起来）

> 下面以 Docker 方式为主（不需要本机安装 MySQL），只要你有 Docker Desktop 即可。

1) 进入你希望存放代码的工作目录
```bash
cd /path/to/your/workdir
```

2) 克隆本项目并进入项目根目录
```bash
git clone https://github.com/ezh2020/webdev.git
cd webdev
```

3) 启动 MySQL + webdev 开发容器（首次会构建镜像）
```bash
docker compose up -d --build
```

4) 在 `webdev` 容器里启动 Spring Boot
```bash
docker compose exec webdev bash -lc "./mvnw spring-boot:run"
```

5) 浏览器访问
- `http://localhost:8080/`
- `http://localhost:8080/login.html`

## 1. 功能概览（按角色）

### 学生端（`src/main/resources/static/student.html`）
- 课程中心：浏览课程、筛选/排序、查看课程简介（Markdown 预览）。
- 已选课程：选课/退课、查看课程与教师信息。
- 作业与提交：按课程/状态筛选作业；上传/更新作业附件；查看打分状态。
- 通知中心：接收课程通知与成绩发布通知；支持 WebSocket 实时推送。

### 教师端（`src/main/resources/static/teacher.html`）
- 我的课程：创建/更新/删除课程；防前端伪造教师归属（后端强制绑定教师身份）。
- 作业管理：发布/更新/删除作业；查看提交记录；打分与评语；统计平均分；学生成绩趋势图（ECharts）。
- 课程资料：上传课程资源（课件/图片/附件等）、更新元信息、下载/预览。
- 总评发布：发布课程最终成绩（CourseSelection finalScore），并触发通知推送。
- 通知中心：创建/发布通知，前端提供 WebSocket 连接状态与重连。

### 管理员端（`src/main/resources/static/admin.html`）
- 账号管理：查看用户/教师/学生档案并编辑。
- 通知管理：全局通知查看与维护。
- 课程分析：基于前端聚合数据构建可视化（ECharts：选课热度柱状图、学院/部门饼图）。
- 作业管理：查看全量作业列表。

### 1.1 典型访问链路（页面跳转与鉴权）
- 入口页 `index.html` 通过 meta refresh 直接跳转 `login.html`。
- 登录页调用 `POST /api/user/login`，登录成功后按 `user.role` 跳转：
  - `TEACHER -> /teacher.html`
  - `STUDENT -> /student.html`
  - `ADMIN -> /admin.html`
- 登录态依赖 `HttpSession + JSESSIONID Cookie`：同域部署时浏览器自动携带 Cookie，后端通过 `AuthConstants.SESSION_USER_ID = "LOGIN_USER_ID"` 读取登录用户。

### 1.2 项目亮点（作业答辩可直接照念）
- **“能跑”到“能用”的闭环**：账号体系 + 角色分端（学生/教师/管理员）+ 课程/作业/资料/通知全链路，所有页面均可在 `static/*.html` 直接打开使用。
- **权限模型不是口号**：不仅做 `role` 校验，还做“资源归属校验”与“防参数伪造”（例如教师查课忽略前端传入的 `teacherId`，强制绑定当前会话身份）。
- **业务规则可证明**：课程号唯一、容量约束、学期合法性等在 Service 层校验，并在 `src/test/java/com/webdev/webdev` 有对应测试用例覆盖。
- **文件系统落地的真实工程问题**：50MB 上传限制、UUID 文件名避免冲突、DB 存相对路径、磁盘持久化、课程资料下载/删除做路径安全处理。
- **实时能力**：WebSocket `/ws/notification` 将“选课/退课/成绩发布/系统通知”与前端通知中心联动，做到“操作即提醒”。
- **工程化意识**：Docker 一键起 MySQL + 初始化演示数据；CI 做配置守卫避免把真实密码写进仓库。

## 2. 技术栈与关键依赖

### 后端（Spring Boot 2.7 / Java 17）
- Web：`spring-boot-starter-web`（REST + multipart 上传）
- WebSocket：`spring-boot-starter-websocket`（`/ws/notification`）
- ORM：MyBatis-Plus `3.5.6`（Mapper + ServiceImpl）
- 连接池：Druid `1.2.23`
- 数据库：MySQL 8
- 密码加密：jBCrypt（注册加盐哈希、登录校验）
- 测试：`spring-boot-starter-test`（JUnit5、MockMvc）

### 前端（纯静态 HTML，运行在 Spring Boot 的 static 下）
- Vue2（CDN）
- Element UI（CDN）
- Axios（API 调用统一 `axios.defaults.baseURL = '/api'`）
- ECharts（数据可视化）
- Vditor（Markdown 编辑/预览：教师编辑、学生只读预览）
- vue-i18n（中英双语切换，语言存储在 `localStorage['webdev_locale']`）

### 2.1 版本与实现约定（与代码对齐）
- Spring Boot 版本以 `pom.xml` 为准：`2.7.18`；JDK：`17`。
- 鉴权未引入 Spring Security：以 `HttpSession` 保存登录态，Controller 层显式做角色与归属校验（见 `src/main/java/com/webdev/webdev/controller/*`）。
- 持久层采用 MyBatis-Plus 注解映射（`@TableName/@TableField/@TableId`），实体位于 `src/main/java/com/webdev/webdev/model`，Mapper 位于 `src/main/java/com/webdev/webdev/mapper`。
- 前端不做构建产物：所有页面直接放在 `src/main/resources/static`，依赖通过 CDN 引入，便于开箱即用（但离线/内网环境需要自行镜像依赖）。

### 2.2 架构一图（便于写实验报告/答辩 PPT）

```mermaid
flowchart LR
  Browser[Browser\nVue2 + ElementUI + Axios\nstatic/*.html] -->|REST /api/*\nJSESSIONID| Spring[Spring Boot 2.7\nController/Service/Mapper]
  Browser <-->|WebSocket /ws/notification| Spring
  Spring -->|MyBatis-Plus| MySQL[(MySQL 8\nusers/courses/...)]
  Spring -->|Disk files| FS[(course-files/\nsubmission-files/)]
  Spring -->|broadcast()| WS[NotificationWebSocketHandler]
```

## 3. 代码结构（前后端尽可能完整）

### 3.1 后端目录结构（`src/main/java/com/webdev/webdev`）

```
com.webdev.webdev
├── WebdevApplication.java                 # Spring Boot 启动类
├── AuthConstants.java                     # Session Key：LOGIN_USER_ID
├── Result.java                            # 统一返回体 { success, message, data }
├── *Request.java                          # Login/Register/ChangePassword/… DTO
├── controller/                            # REST Controller（权限校验主要在这里）
│   ├── UserController.java                # 注册/登录/登出/改密/管理员用户列表
│   ├── StudentController.java             # 学生档案 me / updateProfile / listAll(管理员)
│   ├── TeacherController.java             # 教师档案 updateProfile / listAll(管理员)
│   ├── CourseController.java              # 课程 CRUD / 搜索 / 教师课程列表（含防伪造）
│   ├── CourseSelectionController.java     # 选课/退课/成绩发布/名单查询（含归属校验）
│   ├── AssignmentController.java          # 作业 CRUD / 按条件查询（含归属校验）
│   ├── AssignmentSubmissionController.java# 提交/更新/删除/打分/统计/附件下载（含归属校验）
│   ├── CourseResourceController.java      # 课程资料上传/下载/预览/删除/改名
│   ├── NotificationController.java        # 通知发布/更新/查询/删除
│   ├── DefaultController.java             # 简单演示接口
│   └── GlobalExceptionHandler.java        # 上传异常统一处理（50MB）
├── service/                               # 业务接口
│   ├── UserService.java / impl/UserServiceImpl.java
│   ├── CourseService.java / impl/CourseServiceImpl.java
│   ├── CourseSelectionService.java / impl/CourseSelectionServiceImpl.java
│   ├── AssignmentService.java / impl/AssignmentServiceImpl.java
│   ├── AssignmentSubmissionService.java / impl/AssignmentSubmissionServiceImpl.java
│   ├── CourseResourceService.java / impl/CourseResourceServiceImpl.java
│   └── NotificationService.java / impl/NotificationServiceImpl.java
├── mapper/                                # MyBatis-Plus Mapper
│   ├── UserMapper.java / StudentMapper.java / TeacherMapper.java
│   ├── CourseMapper.java / CourseSelectionMapper.java
│   ├── AssignmentMapper.java / AssignmentSubmissionMapper.java
│   ├── CourseResourceMapper.java / NotificationMapper.java
├── model/                                 # 实体（User/Course/Selection/Assignment/…）
└── websocket/                             # WebSocket Handler
    └── NotificationWebSocketHandler.java  # 广播 JSON 消息：{type,message,time}
```

### 3.2 前端目录结构（`src/main/resources/static`）

```
static/
├── index.html       # 入口/引导页（跳转登录）
├── login.html       # 登录页（vue-i18n；axios /api；成功后跳转到角色页）
├── register.html    # 注册页（学生/教师）
├── student.html     # 学生端主页面（课程/作业/通知；Markdown 预览；WS）
├── teacher.html     # 教师端主页面（课程/作业/资料/成绩；Markdown 编辑；WS；ECharts）
└── admin.html       # 管理员端主页面（账号/通知/分析；ECharts）
```

> 说明：该项目不使用前端工程化构建（无 Node/Vite/Webpack），所有依赖通过 CDN 引入，便于演示与部署。

### 3.3 项目根目录与运行时文件

```
.
├── docker-compose.yml                         # 本地开发：MySQL + webdev 开发容器
├── Dockerfile                                 # webdev 开发容器（JDK17 + Maven）
├── Dockerfile.mysql                           # MySQL 8（内置 init.sql）
├── Course Management Platform_sql_20251019 .sql# 建库建表 + 演示数据（Docker 首次启动会导入）
├── src/
│   ├── main/
│   │   ├── java/com/webdev/webdev             # 后端代码（controller/service/mapper/model/websocket）
│   │   └── resources/
│   │       ├── application.yml                # Spring Boot 配置（数据源/上传限制/Session）
│   │       └── static/                        # 前端静态页面（Vue2 + ElementUI + CDN）
│   └── test/java/com/webdev/webdev            # 自动化测试（SpringBootTest + MockMvc）
├── course-files/                              # 课程资料附件（运行时写入）
└── submission-files/                          # 作业提交附件（运行时写入）
```

## 4. 关键实现细节（技术重点）

### 4.1 精细化权限管理（Role + 归属校验 + 防参数伪造）
项目使用「Controller 层显式校验」的方式实现权限控制，核心思想：
- **角色校验**：根据 `User.role`（`STUDENT` / `TEACHER` / `ADMIN`）决定是否允许访问，如：
  - 学生才能提交/更新自己的作业提交：`AssignmentSubmissionController`
  - 教师/管理员才能发布作业、打分、发布成绩：`AssignmentController`、`AssignmentSubmissionController`、`CourseSelectionController`
  - 管理员才能查看全量用户/学生/教师档案：`UserController`、`StudentController`、`TeacherController`
- **资源归属校验**：不仅看角色，还要确认“是不是你的课/你的提交/你的选课记录”，典型场景：
  - 教师打分：只能打分自己课程下的提交（通过 `assignment -> course -> teacherId` 链路校验）。
  - 教师课程列表：忽略前端传入的 `teacherId`，强制使用当前登录教师身份查询（防伪造）。
  - 学生提交更新/删除：只能操作自己 `studentId` 对应的提交记录。

### 4.2 会话安全（HttpSession + 30 分钟滑动过期 + 显式登出）
- 登录成功后将 `userId` 写入 `HttpSession`：`AuthConstants.SESSION_USER_ID = "LOGIN_USER_ID"`。
- `application.yml` 配置 `server.servlet.session.timeout: 30m`，30 分钟无请求自动失效（滑动过期）。
- 登出接口调用 `session.invalidate()`，清理会话与登录态。

> 说明：前端与后端同域部署时，浏览器会自动携带 `JSESSIONID` Cookie；前端统一以 `/api` 为基地址调用接口。

### 4.3 密码安全（BCrypt 加盐哈希）
- 注册：`UserServiceImpl.register` 使用 `BCrypt.hashpw(plain, BCrypt.gensalt())` 存储哈希。
- 登录：`BCrypt.checkpw(plain, hashed)` 校验。
- 修改密码：`UserController.changePassword` 校验旧密码后生成新哈希。

### 4.4 文件上传/下载（50MB + 磁盘持久化 + 路径安全）
项目包含两类附件存储，均以“数据库存相对路径 + 磁盘存文件”的方式实现：

1) **课程资料**（`CourseResourceServiceImpl` / `CourseResourceController`）
- 上传接口：`POST /api/courseResource/upload`（multipart）
- 存储路径：`course-files/{courseId}/{uuid}.{ext}`（随机文件名避免冲突）
- 大小限制：服务层 `MAX_FILE_SIZE = 50MB` + `application.yml` multipart 限制 + 全局异常兜底
- 下载/预览：`/api/courseResource/download/{id}`、`/api/courseResource/view/{id}`
- **路径穿越防护**：下载/删除时将路径 normalize，并强制落在 `course-files/` 目录下（越界会回退到安全路径）

2) **作业提交附件**（`AssignmentSubmissionServiceImpl` / `AssignmentSubmissionController`）
- 提交接口：`POST /api/assignmentSubmission/submit`（multipart）
- 更新接口：`POST /api/assignmentSubmission/update`（multipart，强制新附件）
- 存储路径：`submission-files/{assignmentId}/{studentId}/{uuid}.{ext}`
- 更新时会先尝试删除旧附件，再写入新附件，避免磁盘堆积

### 4.5 Markdown 简介（教师编辑 + 学生预览）
- 教师端集成 Vditor 作为 Markdown 编辑器，用于课程简介/作业描述的富文本编辑体验。
- 学生端使用 Vditor 的 preview 能力只读渲染（含目录 toc）。

### 4.6 成绩管理（作业成绩 + 最终成绩 + 统计）
- 作业提交打分：`POST /api/assignmentSubmission/grade`，服务层校验 `score <= assignment.maxScore`。
- 平均分：`GET /api/assignmentSubmission/averageScore?assignmentId=...`（后端聚合）。
- 学生成绩趋势：`GET /api/assignmentSubmission/studentTrend?studentId=...&courseId=...`（为 ECharts 提供数据）。
- 最终成绩发布：`POST /api/courseSelection/publishGrade`（写入 `finalScore`，并触发通知）。

### 4.7 数据可视化（ECharts）
- 管理员端：课程选课热度（柱状图）与学院/部门选课聚合（饼图），数据来自 `/api/course/listAll` + `/api/courseSelection/listAll` 的前端聚合。
- 教师端：学生成绩趋势折线图（单学生在课程维度的作业得分走势），数据来自 `studentTrend` 接口。

### 4.8 消息推送（WebSocket /ws/notification）
- WebSocket 端点：`/ws/notification`（`WebSocketConfig` 注册）
- 推送协议：`NotificationWebSocketHandler.broadcast(type, message)` 广播 JSON：
  `{"type":"ENROLL|DROP|GRADE|SYSTEM","message":"...","time":"yyyy-MM-dd HH:mm:ss"}`
- 触发来源：
  - `NotificationServiceImpl.publishNotification(...)`：发布通知后即时推送
  - `CourseSelectionServiceImpl`：选课/退课/成绩发布后推送对应事件类型

### 4.9 国际化（前端 i18n）
- `login.html / register.html / student.html / teacher.html / admin.html` 内置中英双语 messages。
- 语言切换写入 `localStorage['webdev_locale']`，页面刷新后保留选择。

### 4.10 测试覆盖（JUnit5 + SpringBootTest + MockMvc）
测试位于 `src/test/java/com/webdev/webdev`：
- `CourseServiceTest`：验证课程核心业务规则（学期校验、课程号唯一、容量约束等），`@Transactional` 回滚数据。
- `PermissionIntegrationTest`：基于 `@SpringBootTest + @AutoConfigureMockMvc` 的端到端权限集成测试，覆盖“教师不能修改他人课程、不能给他人课程打分、学生不能改删他人提交”等场景。

> 注意：当前测试依赖真实数据库连接（沿用 datasource 配置），运行测试前请先准备好本地 MySQL 或使用 Docker。

### 4.11 统一返回体与前端处理方式
- 后端统一返回 `Result<T>`：`{ success, message, data }`（见 `src/main/java/com/webdev/webdev/Result.java`）。
- 前端页面（`student.html/teacher.html/admin.html`）统一设置 `axios.defaults.baseURL = '/api'`，并以 `body.success` 作为成功判断；失败时优先展示 `body.message`。

### 4.12 数据模型与表结构（与 SQL 对齐）
初始化 SQL：`Course Management Platform_sql_20251019 .sql`，核心表关系：
- `users`：统一账号（`role=ADMIN|TEACHER|STUDENT`）
- `students/teachers`：档案表，通过 `user_id -> users.id` 关联
- `courses`：课程，通过 `teacher_id -> teachers.id` 关联
- `course_selections`：选课记录（`student_id + course_id` 唯一），支持 `final_score`
- `assignments`：课程作业，通过 `course_id` 关联
- `assignment_submissions`：作业提交（`assignment_id + student_id` 唯一），支持附件与打分
- `course_resources`：课程资料（磁盘文件 + DB 相对路径）
- `notifications`：通知（课程维度 + 发布者）

### 4.13 课程搜索/排序的实现（后端组合查询 + 内存余量处理）
- 搜索接口：`POST /api/course/search`，请求体为 `CourseSearchRequest`（学期、创建时间区间、学分区间、余量、排序字段/顺序）。
- 实现策略（见 `CourseServiceImpl.searchCourses`）：
  - 时间/学分筛选与排序优先用 MyBatis-Plus `LambdaQueryWrapper` 下推到数据库；
  - 余量筛选/排序（`maxStudents-currentStudents`）在内存中处理，避免写复杂 SQL（兼容性更好）。

### 4.14 学生作业筛选的实现（只允许查看已选课程）
- 学生端作业筛选接口：`GET /api/assignmentSubmission/studentFilter`。
- 服务层逻辑（见 `AssignmentSubmissionServiceImpl.filterForStudent`）：
  - 先从 `course_selections` 取出该学生已选/已修完课程（`SELECTED/COMPLETED`）；
  - 再按课程、截止时间筛选 `assignments`；
  - 最后合并该学生在这些作业下的 `assignment_submissions`，产出“作业 +（可选）提交摘要”的视图列表。

### 4.15 数据库时间字段与默认值
`Course Management Platform_sql_20251019 .sql` 中多数字段使用 `DEFAULT CURRENT_TIMESTAMP`（如 `users.created_at`、`courses.created_at`、`course_selections.selected_at`、`notifications.publish_time`），因此即使未显式赋值也能得到合理的时间戳默认值。

### 4.16 实时通知的数据流（业务事件 -> DB 落库 -> WS 推送）
这套通知链路不是“单纯 WebSocket Demo”，而是业务事件驱动：
- 业务触发点：
  - 选课/退课/成绩发布：`CourseSelectionServiceImpl` 在关键动作成功后调用 `notificationService.publishNotification(...)` 并携带 `type=ENROLL|DROP|GRADE`。
  - 教师发布通知：`NotificationServiceImpl.publishNotification(...)`（`type=SYSTEM` 或自定义）。
- 推送落点：`NotificationWebSocketHandler.broadcast(type, message)` 向所有在线会话广播 JSON（`type/message/time`），前端 `student.html/teacher.html` 解析后用 Element UI Notification 弹出并刷新列表。
- 前端体验：页面展示 `wsStatus`（连接中/已连接/未连接/出错），便于演示“实时性”而不是黑盒。

### 4.17 数据一致性：Service 校验 + 数据库约束双保险
- Service 层校验（业务规则）：如 `CourseServiceImpl.addCourse/updateCourse` 的课程号唯一、学分/容量/学期合法性检查。
- 数据库约束（强一致）：SQL 中包含多处 `UNIQUE`（`courses.course_code`、`course_selections(student_id,course_id)`、`assignment_submissions(assignment_id,student_id)`），即使出现并发/绕过，也能在数据库层兜底。

### 4.18 安全模型（作业常问：你怎么防“伪造/越权/弱口令/路径穿越”？）
- **越权/伪造**：以 `HttpSession` 为唯一可信身份源，Controller 显式校验角色与资源归属，避免“前端传什么就信什么”。
- **口令**：注册/改密采用 BCrypt 加盐哈希（`BCrypt.hashpw + gensalt`），登录用 `BCrypt.checkpw` 校验。
- **文件**：multipart 全局 50MB 限制 + 课程资料下载/删除路径 normalize 并限制必须落在 `course-files/` 下（防路径穿越）。

### 4.19 可观测性（演示/排查友好）
- `application.yml` 启用 MyBatis 日志输出：`mybatis-plus.configuration.log-impl: StdOutImpl`，接口调用时可直接看到 SQL。
- WebSocket Handler 输出连接/断开/发送失败日志，答辩时可展示“实时连接确实建立、确实推送”。

### 4.20 设计取舍（讲清楚为什么这么做，也很“加分”）
- 没上 Spring Security：用 `HttpSession` + 明确的 Controller 校验快速落地 RBAC 与归属控制，代码路径清晰、易演示；代价是需要自己维护鉴权一致性（通过集成测试兜底）。
- 前端不工程化：纯 `static/*.html + CDN` 让项目“开箱即用”；代价是依赖网络与缓存策略，生产化可迁移到 Vite/webpack 再做资源构建与版本管理。

## 5. 配置与运行

### 5.1 配置（环境变量驱动的数据库连接）
`src/main/resources/application.yml` 使用环境变量注入数据源（并提供本地默认值）：
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

默认本地开发建议（仅本机演示用）：
- 用户名：`root`
- 密码：`test123456`
- 库名：`course_management_platform`

### 5.2 本机运行（推荐）
```bash
./mvnw spring-boot:run
```
然后访问：
- `http://localhost:8080/`（入口）
- `http://localhost:8080/login.html`（登录）

### 5.3 Docker 运行（MySQL + 开发容器）
> 项目提供 `docker-compose.yml`：一个 MySQL 容器 + 一个包含 JDK17/Maven 的开发容器。

1) 启动容器（构建 + 运行）
```bash
docker compose up -d --build
```

2) 在 webdev 容器内启动 Spring Boot
```bash
docker compose exec webdev bash
./mvnw spring-boot:run
```

3) 关闭（保留数据卷）
```bash
docker compose down
```

### 5.4 数据库初始化与演示账号（Docker 首次启动）
- MySQL 容器使用 `Dockerfile.mysql` 将 `Course Management Platform_sql_20251019 .sql` 拷贝为 `/docker-entrypoint-initdb.d/init.sql`：
  - **仅首次初始化数据目录时执行**（即第一次创建 `webdev-mysql-data` 卷时导入）。
  - 若你需要“重新导入演示数据”，可先删除数据卷再启动：`docker compose down -v && docker compose up -d --build`。
- 演示数据包含管理员账号 `admin` 与若干教师/学生账号（见 SQL 中的 `INSERT INTO users ...`）。

### 5.5 构建打包（生成可运行 JAR）
```bash
./mvnw clean package
```
产物在 `target/` 目录下；运行时仍需提供可用的 MySQL 数据源配置（环境变量或 `application.yml` 默认值）。

## 6. CI（GitHub Actions）
工作流位置：`.github/workflows/ci.yml`
- 目标：作为“配置守卫”，校验 `application.yml` 的 datasource 账号密码使用环境变量占位符，避免把真实密码硬编码进仓库。
- 说明：该 CI 不做构建/测试（按当前需求精简），仅做配置检查。

## 7. API 快速索引（按模块）
> 说明：多数接口需要已登录（依赖 `HttpSession`），且不同角色有权限限制；上传类接口为 `multipart/form-data`，其余多为 JSON 或 query 参数。

### 7.0 接口风格（方便老师快速理解）
- 统一返回：`Result<T>`（`success/message/data`），前端按 `success` 走 happy path。
- URL 分组：`/api/{module}/...`（如 `course/assignment/notification`），与后端 `@RequestMapping("/api/xxx")` 一一对应。
- 上传下载：上传走 `multipart/form-data`；下载/预览走 `GET` 并返回文件流（浏览器可直接打开/保存）。

### 7.1 用户与档案
- `POST /api/user/register`（学生/教师注册）
- `POST /api/user/login`、`POST /api/user/logout`、`GET /api/user/me`、`POST /api/user/changePassword`
- `GET /api/student/me`、`POST /api/student/updateProfile`、`GET /api/student/listAll`（管理员）
- `POST /api/teacher/updateProfile`、`GET /api/teacher/listAll`（管理员）

### 7.2 课程与选课
- `POST /api/course/add`、`POST /api/course/update`、`DELETE /api/course/{id}`
- `GET /api/course/listAll`、`GET /api/course/{id}`、`GET /api/course/listBySemester`
- `GET /api/course/listByTeacher`（教师会忽略入参 `teacherId`，只查自己的课）
- `POST /api/course/search`、`GET /api/course/teacherInfo`
- `POST /api/courseSelection/add`、`DELETE /api/courseSelection/{id}`、`POST /api/courseSelection/publishGrade`
- `GET /api/courseSelection/listByStudent`、`GET /api/courseSelection/listByTeacher`、`GET /api/courseSelection/listByCourse*`

### 7.3 作业与提交
- `POST /api/assignment/add`、`POST /api/assignment/update`、`DELETE /api/assignment/{id}`、`GET /api/assignment/listByCourseAndDeadline`
- `POST /api/assignmentSubmission/submit`、`POST /api/assignmentSubmission/update`、`DELETE /api/assignmentSubmission/{id}`
- `POST /api/assignmentSubmission/grade`、`GET /api/assignmentSubmission/averageScore`、`GET /api/assignmentSubmission/studentTrend`
- `GET /api/assignmentSubmission/downloadAttachment/{id}`、`GET /api/assignmentSubmission/studentFilter`

### 7.4 课程资料与通知
- `POST /api/courseResource/upload`、`GET /api/courseResource/listByCourse`、`POST /api/courseResource/updateMeta`、`DELETE /api/courseResource/{id}`
- `GET /api/courseResource/download/{id}`、`GET /api/courseResource/view/{id}`
- `POST /api/notification/create`、`POST /api/notification/update`、`GET /api/notification/listByCourse`、`GET /api/notification/listAll`、`DELETE /api/notification/{id}`

## 8. 答辩展示建议（2 分钟“秀肌肉”脚本）
- **先展示角色分端**：用 `admin/teacher/student` 三个账号登录，演示页面跳转与权限不同。
- **再展示越权拦截**：用教师 A 尝试修改教师 B 的课程 / 给 B 课程的提交打分，展示后端返回 `success=false`（并指出“归属校验链路”）。
- **最后展示实时推送**：学生选课/退课或教师发布成绩 -> 另一个浏览器窗口立刻弹通知（WebSocket），并刷新通知列表。
