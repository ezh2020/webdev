webdev Docker 简易使用说明（Windows + IDEA）
=========================================

这份说明给「小白」使用，环境假设：
- 操作系统：Windows（装了 Docker Desktop）
- IDE：IntelliJ IDEA（终端用 IDEA 自带 Terminal 或 PowerShell）

只记下面几条命令就够了。

1. 第一次启动（构建 + 运行）
---------------------------

步骤：
1) 在 IDEA 中打开本项目  
2) 打开 IDEA 的 Terminal（或在 Windows 上打开 PowerShell），进入项目根目录（有 `docker-compose.yml` 的目录），例如：

```bash
cd C:\path\to\your\project   # 用你自己的路径
```

3) 第一次启动，用这条（会自动构建镜像 + 启动两个容器）：

```bash
docker compose up -d --build
```

如果提示找不到 `docker compose` 命令，可以改用：

```bash
docker-compose up -d --build
```

执行完后，会有两个容器在运行：
- `mysql`：数据库容器，会自动用项目里的 SQL 初始化一次库
- `webdev`：Java 开发环境容器（JDK17 + Maven），不自动跑项目

2. 访问网页（登录、注册等）
-------------------------

在浏览器里直接访问（前提是你在容器里已经跑起来 Spring Boot，应答 8080 端口）：

- 首页：`http://localhost:8080/` 或 `http://localhost:8080/index.html`
- 登录页：`http://localhost:8080/login.html`
- 注册页：`http://localhost:8080/register.html`
- 教师页：`http://localhost:8080/teacher.html`

页面文件位置在项目里：`src/main/resources/static/*.html`。

3. 在 webdev 容器里启动 Spring Boot（可选）
---------------------------------------

webdev 容器只是准备好 JDK + Maven，不会自动运行你的项目。  
如果你想在容器里启动项目：

```bash
# 进入 webdev 容器
docker compose exec webdev bash
# 或旧命令：
# docker-compose exec webdev bash

# 在容器里执行（等同于本机 ./mvnw spring-boot:run）
./mvnw spring-boot:run
```

看到 Spring Boot 启动成功后，就可以用浏览器打开上面那些地址。

4. 暂时关闭容器（下次还要用）
---------------------------

关闭当前运行的容器，但保留数据和配置：

```bash
docker compose down
# 或：docker-compose down
```

这会：
- 停掉并删除当前的 `webdev` 和 `mysql` 容器
- 但是会保留数据库的数据（存在 Docker 的 volume 里）

5. 下次再启动（不重新初始化数据）
-----------------------------

下次要再用，只需要：

```bash
cd C:\path\to\your\project
docker compose up -d
# 或：docker-compose up -d
```

不会重新跑初始化 SQL，之前的数据库数据会保留。

6. 重新构建（改了 Dockerfile 或 SQL 时）
------------------------------------

如果你改了：
- `Dockerfile`
- `Dockerfile.mysql`
- 数据库初始化 SQL 文件

想让容器用新的内容重新来一遍：

```bash
cd C:\path\to\your\project
docker compose down
docker compose up -d --build
```

同样，如果 `docker compose` 报错，就用 `docker-compose` 替换。
