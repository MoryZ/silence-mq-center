# 配置文件架构重组说明

**完成日期**: 2026-03-11  
**改进**: 从单一配置 → Spring Boot标准的多环境配置管理  

---

## 配置文件结构升级

### 📁 原始结构
```
src/main/resources/
├── application.yml           ❌ 包含所有配置，难以维护
├── application-prd.yml       ❌ 配置重复，环境隔离不清
└── logback.xml
```

### ✅ 新的标准结构
```
src/main/resources/
├── bootstrap.yml             ✅ 公共配置（所有环境）
├── application.yml           ✅ 默认配置（最小化）
├── application-dev.yml       ✅ 开发环境配置
├── application-prd.yml       ✅ 生产环境配置
└── logback.xml               ✅ 日志配置
```

---

## 配置文件说明

### 1. bootstrap.yml - 应用启动最早加载

**用途**: 应用启动最早阶段的公共配置  
**加载时机**: 最先加载（Spring Cloud Boot Strap阶段）  
**作用**: 配置中心、服务发现等基础设施

**包含内容**:
- ✅ 应用名称 (`spring.application.name`)
- ✅ 活跃profile配置 (`spring.profiles.active`)
- ✅ Nacos服务发现配置
- ✅ RocketMQ公共配置
- ✅ 线程池公共配置
- ✅ 平台标识符
- ✅ 日志级别配置

**配置语法**:
```yaml
spring:
  application:
    name: mq-service
  profiles:
    active: dev  # 指定活跃profile
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}  # 支持环境变量
        username: ${NACOS_USERNAME:nacos}
```

### 2. application.yml - 默认配置

**用途**: 如果未指定profile时使用的默认配置  
**加载时机**: 最后加载（可被environment覆盖）  
**建议**: 保持最小化，仅包含必要的默认值

**包含内容**:
- ✅ 服务器端口 (8099)
- ✅ 应用名称
- ✅ 默认profile配置

**特点**:
- 最小化
- 包含注释说明加载顺序
- 用于文档目的

### 3. application-dev.yml - 开发环境配置

**加载条件**: `spring.profiles.active=dev` 时加载  
**启动命令**: `java -jar app.jar -Dspring.profiles.active=dev`

**配置特点**:
```yaml
# ✅ 开发环境特定
server:
  port: 8099

# ✅ 本地开发数据库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/silence_mq
    username: root
    password: 123456

# ✅ 本地Nacos
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev

# ✅ 本地RocketMQ
rocketmq:
  config:
    namesrvAddrs:
      - localhost:9876
    dataPath: ~/.mq-console/dev/data
    loginRequired: false

# ✅ 详细日志用于调试
logging:
  level:
    root: INFO
    com.old.silence.mq.center: DEBUG
    org.springframework: DEBUG
    
# ✅ 启用Swagger
springdoc:
  swagger-ui:
    enabled: true
```

### 4. application-prd.yml - 生产环境配置

**加载条件**: `spring.profiles.active=prd` 时加载  
**启动命令**: `java -jar app.jar -Dspring.profiles.active=prd`

**配置特点**:
```yaml
# ✅ 生产环境优化
server:
  tomcat:
    threads:
      max: 200
      min-spare: 20

# ✅ 生产数据库（通过环境变量）
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# ✅ 生产Nacos
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: prd

# ✅ 生产RocketMQ
rocketmq:
  config:
    isVIPChannel: true
    loginRequired: true
    useTLS: true
    dataPath: /data/mq-console/prd

# ✅ 生产日志只记录重要信息
logging:
  level:
    root: WARN
    com.old.silence.mq.center: INFO
  file:
    name: /data/logs/mq-console/mq-console.log
    max-size: 100MB

# ✅ 禁用Swagger
springdoc:
  swagger-ui:
    enabled: false
```

---

## 配置加载优先级

```
优先级 (从高到低):
1. 命令行参数  (-Dspring.profiles.active=prd)
2. 环境变量    (SPRING_PROFILES_ACTIVE=prd)
3. application-{profile}.yml
4. bootstrap.yml
5. application.yml (默认)
```

**示例**:
```bash
# 方式1: JVM参数
java -jar app.jar -Dspring.profiles.active=prd

# 方式2: Spring参数
java -jar app.jar --spring.profiles.active=prd

# 方式3: 环境变量
export SPRING_PROFILES_ACTIVE=prd
java -jar app.jar

# 方式4: 配置文件中指定（bootstrap.yml）
spring:
  profiles:
    active: prd
```

---

## 环境变量配置

### 开发环境 (.env.dev)
```bash
# Nacos
NACOS_SERVER_ADDR=localhost:8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
NACOS_NAMESPACE=dev
NACOS_GROUP=DEV_GROUP

# Database
DB_URL=jdbc:mysql://localhost:3306/silence_mq
DB_USERNAME=root
DB_PASSWORD=123456

# RocketMQ
ROCKETMQ_ACCESS_KEY=
ROCKETMQ_SECRET_KEY=
```

### 生产环境 (.env.prd)
```bash
# Nacos
NACOS_SERVER_ADDR=nacos.prod.example.com:8848
NACOS_USERNAME=${PROD_NACOS_USERNAME}
NACOS_PASSWORD=${PROD_NACOS_PASSWORD}
NACOS_NAMESPACE=prd
NACOS_GROUP=PROD_GROUP

# Database
DB_URL=${PROD_DB_URL}
DB_USERNAME=${PROD_DB_USERNAME}
DB_PASSWORD=${PROD_DB_PASSWORD}

# RocketMQ
ROCKETMQ_ACCESS_KEY=${PROD_ROCKETMQ_KEY}
ROCKETMQ_SECRET_KEY=${PROD_ROCKETMQ_SECRET}
```

---

## 启动脚本示例

### 开发环境启动脚本 (dev.sh)
```bash
#!/bin/bash
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER_ADDR=localhost:8848
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=nacos

java -jar mq-service.jar
```

### 生产环境启动脚本 (prd.sh)
```bash
#!/bin/bash
# 加载环境变量
source /etc/mq-service/prd.env

# 启动应用
java -Xmx2g -Xms2g \
  -Dspring.profiles.active=prd \
  -DNACOS_SERVER_ADDR=$NACOS_SERVER_ADDR \
  -DDB_URL=$DB_URL \
  -DDB_USERNAME=$DB_USERNAME \
  -DDB_PASSWORD=$DB_PASSWORD \
  -DROCKETMQ_ACCESS_KEY=$ROCKETMQ_ACCESS_KEY \
  -DROCKETMQ_SECRET_KEY=$ROCKETMQ_SECRET_KEY \
  -jar /opt/mq-service/mq-service.jar
```

---

## Docker Compose 示例

### docker-compose.dev.yml
```yaml
version: '3.8'

services:
  mq-service:
    build: .
    ports:
      - "8099:8099"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - NACOS_SERVER_ADDR=nacos:8848
      - NACOS_USERNAME=nacos
      - NACOS_PASSWORD=nacos
    depends_on:
      - nacos
      - rocketmq

  nacos:
    image: nacos/nacos-server:latest
    ports:
      - "8848:8848"
    environment:
      - MODE=standalone
```

### docker-compose.prd.yml
```yaml
version: '3.8'

services:
  mq-service:
    build: .
    ports:
      - "8099:8099"
    environment:
      - SPRING_PROFILES_ACTIVE=prd
      - NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR}
      - DB_URL=${DB_URL}
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '2'
          memory: 2G
```

---

## Kubernetes 部署示例

### ConfigMap (开发环境)
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mq-service-dev-config
  namespace: default
data:
  application-dev.yml: |
    server:
      port: 8099
    spring:
      profiles:
        active: dev
```

### Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mq-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: mq-service
        image: mq-service:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prd
        - name: NACOS_SERVER_ADDR
          valueFrom:
            configMapKeyRef:
              name: mq-service-config
              key: nacos-addr
        volumeMounts:
        - name: config
          mountPath: /app/config
      volumes:
      - name: config
        configMap:
          name: mq-service-prd-config
```

---

## 最佳实践

### ✅ DO（应该做）

1. **始终指定profile**
   ```bash
   java -jar app.jar -Dspring.profiles.active=dev
   ```

2. **使用环境变量存储敏感信息**
   ```yaml
   password: ${DB_PASSWORD}  # 从环境变量读取
   ```

3. **应用-specific配置分离**
   ```
   bootstrap.yml         - 应用基础设置
   application-dev.yml   - 开发特定
   application-prd.yml   - 生产特定
   ```

4. **为每个environment准备启动脚本**
   ```bash
   ./scripts/start-dev.sh
   ./scripts/start-prd.sh
   ```

### ❌ DON'T（不应该做）

1. ❌ 在配置文件中硬编码密码
   ```yaml
   # 错误方式
   password: admin123
   ```

2. ❌ 混合多个环境配置到一个文件
   ```yaml
   # 错误方式
   spring:
     datasource:
       dev-url: jdbc:mysql://localhost:3306/dev_db
       prd-url: jdbc:mysql://prod.example.com/prd_db
   ```

3. ❌ 依赖默认profile
   ```bash
   # 错误方式
   java -jar app.jar  # 不指定profile
   ```

4. ❌ 在bootstrap.yml中包含环境特定配置
   ```yaml
   # 错误方式（在bootstrap.yml中）
   server:
     port: 8099  # 应该放在application-dev.yml
   ```

---

## 迁移检查清单

- [x] 创建 `bootstrap.yml` - 公共配置
- [x] 创建 `application-dev.yml` - 开发环境
- [x] 更新 `application-prd.yml` - 生产环境
- [x] 调整 `application.yml` - 最小化
- [ ] 编写启动脚本 (`scripts/start-dev.sh`, `scripts/start-prd.sh`)
- [ ] 创建环境变量模板 (`.env.dev.template`, `.env.prd.template`)
- [ ] 更新Docker支持 (`Dockerfile`, `docker-compose.yml`)
- [ ] 更新Kubernetes支持 (ConfigMaps, Deployments)
- [ ] 团队培训 - 新配置管理方式
- [ ] 更新文档 - 开发者指南

---

## 迁移验证

### 验证开发环境配置
```bash
# 启动时应显示使用的profile
INFO ... The following profiles are active: dev

# 检查加载的配置源
java -Dspring.boot.logging.level=DEBUG -jar app.jar -Dspring.profiles.active=dev
```

### 验证生产环境配置
```bash
# 确保使用正确的database/nacos地址
java -jar app.jar -Dspring.profiles.active=prd

# 检查日志级别
curl http://localhost:8099/actuator/env | grep logging.level
```

---

## 迁移影响分析

### ✅ 优势
1. **清晰的环境隔离** - 开发/生产配置完全分离
2. **易于维护** - 修改某个环境不影响其他
3. **安全性提升** - 敏感信息通过环境变量管理
4. **符合规范** - Spring Boot官方推荐的最佳实践
5. **容器友好** - Docker/K8s部署更简单

### ⚠️ 注意事项
1. **向后兼容** - 现有代码无需改动
2. **启动参数** - 需要指定 `-Dspring.profiles.active`
3. **环境变量** - 生产环境需要配置环境变量
4. **文档更新** - 更新开发者指南

---

## 相关文档

- Spring Boot [Profile-specific Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- Spring Cloud [Config Server](https://spring.io/projects/spring-cloud-config)
- [CLAUDE.md](./CLAUDE.md) - 项目开发规范

---

**配置文件迁移完成！** ✅

现在你的应用支持：
- 开发环境: `java -jar app.jar -Dspring.profiles.active=dev`
- 生产环境: `java -jar app.jar -Dspring.profiles.active=prd`

