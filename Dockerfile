FROM maven:3.9.9-eclipse-temurin-17

ENV DEBIAN_FRONTEND=noninteractive \
    MYSQL_ROOT_PASSWORD=test123456 \
    MYSQL_DATABASE=course_management_platform

RUN apt-get update && \
    apt-get install -y mysql-server && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 直接将项目代码复制进镜像，其他电脑只需拉镜像即可还原开发环境
COPY . /app

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080 3306

ENTRYPOINT ["docker-entrypoint.sh"]
