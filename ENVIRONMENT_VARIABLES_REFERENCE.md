# 环境变量参考指南

本文档列出了应用在不同环境下所需的所有环境变量。

---

## 🔧 开发环境配置 (.env.dev)

开发环境下大多数配置有默认值，以下是可选的环境变量配置：

### 基础配置
```bash
# 应用环境标识
SPRING_PROFILES_ACTIVE=dev

# JVM内存设置
JAVA_OPTS=-Xmx512m -Xms256m
```

### Nacos配置（服务发现和配置中心）
```bash
# Nacos服务地址 (默认: localhost:8848)
NACOS_SERVER_ADDR=localhost:8848

# Nacos认证信息
NACOS_USERNAME=nacos          # 默认用户名
NACOS_PASSWORD=nacos          # 默认密码

# Nacos命名空间
NACOS_NAMESPACE=dev           # 开发命名空间

# Nacos分组
NACOS_GROUP=DEV_GROUP
```

### 数据库配置（如果项目使用数据库）
```bash
# MySQL数据库
DB_URL=jdbc:mysql://localhost:3306/silence_mq
DB_USERNAME=root
DB_PASSWORD=123456
DB_DRIVER=com.mysql.cj.jdbc.Driver

# PostgreSQL数据库（可选）
DB_URL=jdbc:postgresql://localhost:5432/silence_mq
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### RocketMQ配置
```bash
# RocketMQ NameServer地址 (默认: localhost:9876)
ROCKETMQ_NAMESRV_ADDR=localhost:9876

# RocketMQ 命名空间
ROCKETMQ_NAMESPACE=dev

# RocketMQ访问凭证（可选，默认无认证）
ROCKETMQ_ACCESS_KEY=
ROCKETMQ_SECRET_KEY=

# RocketMQ数据路径
ROCKETMQ_DATA_PATH=~/.mq-console/dev/data
```

### Redis配置（如果使用缓存）
```bash
# Redis服务器
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0
```

### 日志配置
```bash
# 日志级别
LOG_LEVEL=DEBUG

# 日志输出路径
LOG_PATH=./logs/dev/

# 日志文件大小和保留天数
LOG_FILE_MAX_SIZE=10MB
LOG_FILE_MAX_DAYS=7
```

### 应用配置
```bash
# 应用名称
APP_NAME=mq-center

# 应用版本
APP_VERSION=1.0.0

# 应用描述
APP_DESCRIPTION=RocketMQ管理中心-开发环境
```

---

## 🏭 生产环境配置 (.env.prd)

**⚠️ 重要**: 生产环境下以下环境变量**必需**配置，不能使用默认值。

### 必需的基础配置
```bash
# 应用环境标识（必需）
SPRING_PROFILES_ACTIVE=prd

# JVM内存设置（必需）
JAVA_OPTS=-Xmx2g -Xms2g -XX:+UseG1GC -XX:+ParallelRefProcEnabled
```

### 必需的Nacos配置
```bash
# Nacos服务地址（必需）- 支持多节点
NACOS_SERVER_ADDR=nacos-1.prod.example.com:8848,nacos-2.prod.example.com:8848,nacos-3.prod.example.com:8848

# Nacos认证信息（必需）
NACOS_USERNAME=<prod_nacos_username>
NACOS_PASSWORD=<prod_nacos_password>

# Nacos命名空间（必需）
NACOS_NAMESPACE=prd

# Nacos分组
NACOS_GROUP=PROD_GROUP

# Nacos配置文件ID
NACOS_CONFIG_DATA_ID=mq-center-prd
```

### 必需的数据库配置
```bash
# 数据库连接字符串（必需）
DB_URL=jdbc:mysql://prod-db-1.example.com:3306/silence_mq?useSSL=true&serverTimezone=UTC

# 数据库认证信息（必需）
DB_USERNAME=<prod_db_username>
DB_PASSWORD=<prod_db_password>

# 数据库驱动
DB_DRIVER=com.mysql.cj.jdbc.Driver

# 数据库连接池大小
DB_POOL_MAX_SIZE=20
DB_POOL_MIN_IDLE=5
DB_POOL_CONNECTION_TIMEOUT=30000
```

### 必需的RocketMQ配置
```bash
# RocketMQ NameServer地址（必需）- 多节点HA
ROCKETMQ_NAMESRV_ADDR=rocketmq-1.prod.example.com:9876,rocketmq-2.prod.example.com:9876,rocketmq-3.prod.example.com:9876,rocketmq-4.prod.example.com:9876

# RocketMQ 命名空间（必需）
ROCKETMQ_NAMESPACE=prd

# RocketMQ 访问凭证（必需）
ROCKETMQ_ACCESS_KEY=<prod_rocketmq_access_key>
ROCKETMQ_SECRET_KEY=<prod_rocketmq_secret_key>

# RocketMQ 是否使用VIP通道
ROCKETMQ_IS_VIP_CHANNEL=true

# RocketMQ 是否启用TLS
ROCKETMQ_USE_TLS=true

# RocketMQ 是否需要登录
ROCKETMQ_LOGIN_REQUIRED=true

# RocketMQ 数据路径（必需）
ROCKETMQ_DATA_PATH=/data/mq-console/prd/data
```

### 可选的Redis配置（用于缓存）
```bash
# Redis主从或集群地址
REDIS_MASTER_HOST=redis-master.prod.example.com
REDIS_MASTER_PORT=6379

# Redis从节点（可选，用于读取）
REDIS_SLAVE_HOST=redis-slave.prod.example.com
REDIS_SLAVE_PORT=6379

# Redis认证信息
REDIS_USERNAME=<redis_username>
REDIS_PASSWORD=<redis_password>

# Redis使用的库
REDIS_DATABASE=0

# Redis连接池
REDIS_POOL_MAX_ACTIVE=20
REDIS_POOL_MAX_IDLE=10
REDIS_POOL_MIN_IDLE=5
```

### 生产日志配置
```bash
# 日志级别（生产环境应为WARN或INFO）
LOG_LEVEL=WARN

# 日志输出路径（必需）
LOG_PATH=/data/logs/mq-center/

# 日志文件大小
LOG_FILE_MAX_SIZE=100MB

# 日志保留天数
LOG_FILE_MAX_DAYS=30

# 日志压缩
LOG_FILE_COMPRESS=true

# 最大日志数量
LOG_FILE_MAX_HISTORY=10
```

### 安全配置
```bash
# HTTPS/TLS证书路径
TLS_CERT_PATH=/etc/ssl/mq-center/cert.pem
TLS_KEY_PATH=/etc/ssl/mq-center/key.pem

# API密钥（用于认证）
API_SECRET_KEY=<secure_api_key>

# CORS允许的域名
CORS_ALLOWED_ORIGINS=https://dashboard.prod.example.com

# 会话超时时间（分钟）
SESSION_TIMEOUT=30
```

### 监控和告警配置
```bash
# Prometheus指标暴露端口
PROMETHEUS_PORT=9090

# Prometheus指标暴露路径
PROMETHEUS_PATH=/metrics

# APM Agent配置
APM_SERVER_URL=https://apm.prod.example.com:8200

# 应用性能监控
APM_SERVICE_NAME=mq-center-prod
APM_ENVIRONMENT=production
```

### 应用配置
```bash
# 应用名称
APP_NAME=mq-center

# 应用版本
APP_VERSION=1.0.0

# 应用描述
APP_DESCRIPTION=RocketMQ管理中心-生产环境

# 构建时间
BUILD_TIME=$(date -u +'%Y-%m-%dT%H:%M:%SZ')

# Git提交ID
GIT_COMMIT=<git_commit_hash>
```

### 高级性能配置
```bash
# 线程池配置
THREAD_POOL_CORE_SIZE=20
THREAD_POOL_MAX_SIZE=50
THREAD_POOL_QUEUE_CAPACITY=1000

# 缓存配置
CACHE_MAX_SIZE=10000
CACHE_EXPIRE_MINUTES=30

# 连接超时时间
CONNECTION_TIMEOUT=5000

# 读取超时时间
READ_TIMEOUT=30000

# 写入超时时间
WRITE_TIMEOUT=30000
```

---

## 📋 环境变量模板

### 开发环境模板文件 (.env.dev.template)
```bash
# === 开发环境(DEV)配置模板 ===
# 使用说明: 
#   1. 复制此文件为 .env.dev
#   2. 根据实际情况填写下面的值
#   3. 在启动时加载: source .env.dev && java -jar app.jar -Dspring.profiles.active=dev

# 基础配置
SPRING_PROFILES_ACTIVE=dev
JAVA_OPTS=-Xmx512m -Xms256m

# Nacos配置
NACOS_SERVER_ADDR=localhost:8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
NACOS_NAMESPACE=dev
NACOS_GROUP=DEV_GROUP

# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/silence_mq
DB_USERNAME=root
DB_PASSWORD=123456

# RocketMQ配置
ROCKETMQ_NAMESRV_ADDR=localhost:9876
ROCKETMQ_NAMESPACE=dev

# 日志配置
LOG_LEVEL=DEBUG
LOG_PATH=./logs/dev/
```

### 生产环境模板文件 (.env.prd.template)
```bash
# === 生产环境(PRD)配置模板 ===
# ⚠️ 重要注意事项:
#   1. 请勿将真实密码提交到版本控制
#   2. 使用此模板在生产服务器上创建 .env.prd
#   3. 确保只有授权人员能访问此文件
#   4. 定期审计环境变量配置

# === 必需配置 ===

# 基础配置
SPRING_PROFILES_ACTIVE=prd
JAVA_OPTS=-Xmx2g -Xms2g -XX:+UseG1GC

# Nacos配置（必需）
NACOS_SERVER_ADDR=<nacos-server-1:8848,nacos-server-2:8848>
NACOS_USERNAME=<your_nacos_username>
NACOS_PASSWORD=<your_nacos_password>
NACOS_NAMESPACE=prd
NACOS_GROUP=PROD_GROUP

# 数据库配置（必需）
DB_URL=<jdbc:mysql://db-server:3306/silence_mq>
DB_USERNAME=<your_db_username>
DB_PASSWORD=<your_db_password>
DB_POOL_MAX_SIZE=20

# RocketMQ配置（必需）
ROCKETMQ_NAMESRV_ADDR=<namesrv-1:9876,namesrv-2:9876,namesrv-3:9876,namesrv-4:9876>
ROCKETMQ_NAMESPACE=prd
ROCKETMQ_ACCESS_KEY=<your_rocketmq_access_key>
ROCKETMQ_SECRET_KEY=<your_rocketmq_secret_key>
ROCKETMQ_USE_TLS=true
ROCKETMQ_DATA_PATH=/data/mq-console/prd/data

# 日志配置（必需）
LOG_LEVEL=WARN
LOG_PATH=/data/logs/mq-center/
LOG_FILE_MAX_SIZE=100MB
LOG_FILE_MAX_DAYS=30

# === 可选配置 ===

# Redis配置（如需缓存）
REDIS_MASTER_HOST=<redis-server>
REDIS_MASTER_PORT=6379
REDIS_PASSWORD=<your_redis_password>

# 监控配置
APM_SERVER_URL=https://apm.example.com:8200
APM_SERVICE_NAME=mq-center-prod
```

---

## 🚀 启动脚本示例

### 使用环境变量文件启动 (start-dev.sh)
```bash
#!/bin/bash

# 加载环境变量
source .env.dev

# 启动应用
java -jar mq-center.jar
```

### 使用环境变量文件启动 (start-prd.sh)
```bash
#!/bin/bash

# 加载环境变量
set -o allexport
source /etc/mq-center/.env.prd
set +o allexport

# 启动应用
java \
  -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
  $JAVA_OPTS \
  -jar /opt/mq-center/mq-center.jar

# 等待应用启动
sleep 5

# 验证应用是否启动成功
curl -f http://localhost:8099/actuator/health || exit 1
echo "应用启动成功"
```

---

## ✅ 环境变量配置检查清单

### 开发环境检查
- [ ] NACOS_SERVER_ADDR 指向本地Nacos
- [ ] DB_URL 指向本地数据库
- [ ] ROCKETMQ_NAMESRV_ADDR 指向本地RocketMQ
- [ ] LOG_LEVEL 设置为DEBUG
- [ ] JAVA_OPTS 内存设置不超过256MB

### 生产环境检查
- [ ] NACOS_SERVER_ADDR 指向生产Nacos集群（多节点）
- [ ] DB_URL 使用生产数据库，启用SSL
- [ ] ROCKETMQ_NAMESRV_ADDR 指向生产RocketMQ集群（HA配置）
- [ ] ROCKETMQ_ACCESS_KEY 和 ROCKETMQ_SECRET_KEY 已配置
- [ ] LOG_LEVEL 设置为WARN
- [ ] ROCKETMQ_USE_TLS 设置为true
- [ ] LOG_PATH 指向生产日志目录
- [ ] JAVA_OPTS 内存设置为2GB
- [ ] 所有密码和密钥已从版本控制中排除

---

## 🔐 安全建议

1. **不要将真实密码提交到Git**
   ```bash
   # 在 .gitignore 中添加
   echo ".env.prd" >> .gitignore
   echo ".env.*.local" >> .gitignore
   ```

2. **使用密钥管理系统存储敏感信息**
   - AWS Secrets Manager
   - HashiCorp Vault
   - Azure Key Vault
   - 其他企业级密钥管理系统

3. **定期轮换密钥**
   - RocketMQ访问密钥
   - 数据库密码
   - API密钥

4. **限制文件访问权限**
   ```bash
   chmod 600 .env.prd
   chmod 600 /etc/mq-center/.env.prd
   ```

5. **审计环境变量使用**
   - 记录谁访问了哪些环境变量
   - 监控异常的环境变量访问

---

## 📚 相关文档

- [CONFIG_QUICK_REFERENCE.md](CONFIG_QUICK_REFERENCE.md) - 配置快速参考
- [CONFIGURATION_MIGRATION_GUIDE.md](CONFIGURATION_MIGRATION_GUIDE.md) - 完整配置迁移指南
- [CLAUDE.md](CLAUDE.md) - 项目开发规范

