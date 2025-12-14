FROM maven:3.9.11-eclipse-temurin-17

WORKDIR /app

# 将项目代码复制进镜像，提供完整的构建/运行环境
COPY . /app

EXPOSE 8080

# 默认不自动启动应用，仅提供开发环境依赖（JDK + Maven）
CMD ["bash", "-lc", "sleep infinity"]
