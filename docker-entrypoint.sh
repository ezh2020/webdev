#!/usr/bin/env bash
set -e

MYSQL_DATA_DIR="/var/lib/mysql"

init_mysql() {
  if [ ! -d "${MYSQL_DATA_DIR}/mysql" ]; then
    echo "Initializing MySQL data directory..."
    mysqld --initialize-insecure --user=mysql --datadir="${MYSQL_DATA_DIR}"

    echo "Starting MySQL for initial setup..."
    mysqld --user=mysql --datadir="${MYSQL_DATA_DIR}" --skip-networking &
    MYSQL_TEMP_PID=$!

    # 等待 MySQL 启动
    for i in {30..0}; do
      if mysqladmin ping --silent >/dev/null 2>&1; then
        break
      fi
      echo "Waiting for MySQL to start... ($i)"
      sleep 1
    done

    if ! mysqladmin ping --silent >/dev/null 2>&1; then
      echo "MySQL failed to start for initialization."
      exit 1
    fi

    echo "Configuring root user and database..."
    mysql -uroot <<-EOSQL
      ALTER USER 'root'@'localhost' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}';
      CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
      FLUSH PRIVILEGES;
EOSQL

    # 导入初始数据库数据（如果 SQL 文件存在）
    SQL_FILE="/app/Course Management Platform_sql_20251019 .sql"
    if [ -f "${SQL_FILE}" ]; then
      echo "Importing initial data from ${SQL_FILE}..."
      mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "${SQL_FILE}" || {
        echo "Failed to import initial SQL data from ${SQL_FILE}"
      }
    else
      echo "SQL file ${SQL_FILE} not found, skipping initial data import."
    fi

    # 关闭临时 MySQL
    mysqladmin -uroot -p"${MYSQL_ROOT_PASSWORD}" shutdown || true
    wait "${MYSQL_TEMP_PID}" || true
  fi
}

start_mysql() {
  echo "Starting MySQL server..."
  mysqld --user=mysql --datadir="${MYSQL_DATA_DIR}" &
  MYSQL_MAIN_PID=$!

  # 等待 MySQL 就绪
  for i in {30..0}; do
    if mysqladmin -uroot -p"${MYSQL_ROOT_PASSWORD}" ping --silent >/dev/null 2>&1; then
      break
    fi
    echo "Waiting for MySQL to be ready... ($i)"
    sleep 1
  done

  if ! mysqladmin -uroot -p"${MYSQL_ROOT_PASSWORD}" ping --silent >/dev/null 2>&1; then
    echo "MySQL failed to become ready."
    exit 1
  fi
}

init_mysql
start_mysql

echo "Starting Spring Boot application..."
cd /app
exec mvn -B spring-boot:run
