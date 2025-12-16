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

## 6. CI（GitHub Actions）
工作流位置：`.github/workflows/ci.yml`
- 目标：作为“配置守卫”，校验 `application.yml` 的 datasource 账号密码使用环境变量占位符，避免把真实密码硬编码进仓库。
- 说明：该 CI 不做构建/测试（按当前需求精简），仅做配置检查。
