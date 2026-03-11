# 配置文件快速参考

## 📋 快速导航

| 配置文件 | 用途 | 何时加载 | 何时修改 |
|---------|------|---------|---------|
| `bootstrap.yml` | 公共基础配置 | **最先** — 应用启动时 | 需要修改所有环境的公共配置 |
| `application.yml` | 默认配置 | 最后 — fallback | 很少修改 |
| `application-dev.yml` | 开发环境 | 当 `profile=dev` | 修改开发环境配置 |
| `application-prd.yml` | 生产环境 | 当 `profile=prd` | 修改生产环境配置 |

---

## 🚀 最常用命令

### 启动开发环境
```bash
# 方式1（推荐）
java -jar app.jar -Dspring.profiles.active=dev

# 方式2
java -jar app.jar --spring.profiles.active=dev

# 方式3
java -D spring.profiles.active=dev -jar app.jar
```

### 启动生产环境
```bash
# 需要设置环境变量
export NACOS_SERVER_ADDR=nacos.prod.example.com:8848
export DB_URL=jdbc:mysql://prod-db:3306/mq
export DB_USERNAME=mq_user
export DB_PASSWORD=<secure_password>

java -jar app.jar -Dspring.profiles.active=prd
```

---

## 📝 什么时候编辑哪个文件？

### 场景1: 修改所有环境都需要的配置
**编辑**: `bootstrap.yml`

示例: 修改应用名称、添加全局拦截器
```yaml
spring:
  application:
    name: mq-center  # 所有环境都使用这个名称
```

### 场景2: 修改开发环境配置
**编辑**: `application-dev.yml`

示例: 修改本地数据库地址
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/silence_mq_dev
```

### 场景3: 修改生产环境配置
**编辑**: `application-prd.yml`

示例: 修改生产RocketMQ地址
```yaml
rocketmq:
  config:
    namesrvAddrs:
      - rocketmq-1.prod.example.com:9876
      - rocketmq-2.prod.example.com:9876
```

### 场景4: 修改默认/全局行为
**编辑**: `application.yml`

示例: 修改服务器端口（所有环境未覆盖时使用）
```yaml
server:
  port: 8099
```

---

## 🔍 调试配置问题

### 查看当前激活的profile
```bash
# 启动时查看日志
java -jar app.jar -Dspring.profiles.active=dev | grep "active"
# 输出: The following profiles are active: dev
```

### 查看加载的配置源
```bash
# 启用DEBUG日志
java -jar app.jar -Dspring.profiles.active=dev -Dspring.boot.logging.level=DEBUG

# 查看哪个文件被加载了
# 日志会显示:
# Loaded config from file:application-dev.yml
# Loaded config from file:bootstrap.yml
```

### 查看某个属性的值
```bash
# 通过Actuator检查配置
curl http://localhost:8099/actuator/configprops | grep "datasource" -A 10
```

---

## 🔐 环境变量配置

### 需要配置的环境变量

#### 开发环境的环境变量 (可选，有默认值)
```bash
NACOS_SERVER_ADDR=localhost:8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
DB_URL=jdbc:mysql://localhost:3306/silence_mq
DB_USERNAME=root
DB_PASSWORD=123456
```

#### 生产环境必需的环境变量
```bash
NACOS_SERVER_ADDR=<生产Nacos地址>
NACOS_USERNAME=<用户名>
NACOS_PASSWORD=<密码>
DB_URL=<数据库连接字符串>
DB_USERNAME=<数据库用户>
DB_PASSWORD=<数据库密码>
ROCKETMQ_ACCESS_KEY=<RocketMQ Access Key>
ROCKETMQ_SECRET_KEY=<RocketMQ Secret Key>
```

### 设置环境变量的方式

#### 方式1: shell中设置
```bash
export DB_URL=jdbc:mysql://prod-db:3306/mq
export DB_USERNAME=mq_user
export DB_PASSWORD=secret

java -jar app.jar -Dspring.profiles.active=prd
```

#### 方式2: 在JVM参数中设置
```bash
java \
  -DDB_URL=jdbc:mysql://prod-db:3306/mq \
  -DDB_USERNAME=mq_user \
  -DDB_PASSWORD=secret \
  -Dspring.profiles.active=prd \
  -jar app.jar
```

#### 方式3: 配置文件中引用
```yaml
# application-prd.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

#### 方式4: Docker中设置
```bash
docker run -e DB_URL=... -e DB_USERNAME=... myapp:latest
```

---

## ✅ 配置校验检查清单

启动应用后，检查以下内容：

- [ ] 日志显示正确的profile: `active: dev` 或 `active: prd`
- [ ] 应用分别连接了正确的Nacos服务
- [ ] 应用分别连接了正确的数据库
- [ ] 应用分别连接了正确的RocketMQ集群
- [ ] 日志级别符合预期 (dev=DEBUG, prd=WARN)
- [ ] 线程池大小符合预期 (dev=5-10, prd=20-50)

---

## 📚 完整配置对照表

### 开发环境 (dev)

```yaml
# bootstrap.yml (共享)
spring:
  application:
    name: mq-center
  profiles:
    active: dev

# application-dev.yml (开发特定)
server:
  port: 8099
  tomcat:
    threads:
      max: 50
      min-spare: 5

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/silence_mq_dev
    username: root
    password: 123456
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev

rocketmq:
  config:
    namesrvAddrs:
      - localhost:9876

logging:
  level:
    root: INFO
    com.old.silence.mq.center: DEBUG
```

### 生产环境 (prd)

```yaml
# bootstrap.yml (共享)
spring:
  application:
    name: mq-center
  profiles:
    active: prd  # 需要通过-D参数覆盖

# application-prd.yml (生产特定)
server:
  port: 8099
  tomcat:
    threads:
      max: 200
      min-spare: 20

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR}
        namespace: prd

rocketmq:
  config:
    namesrvAddrs:
      - ${ROCKETMQ_NAME_SRV_1}
      - ${ROCKETMQ_NAME_SRV_2}

logging:
  level:
    root: WARN
    com.old.silence.mq.center: INFO
  file:
    name: /data/logs/mq-center.log
    max-size: 100MB
```

---

## 🐛 常见问题

### Q: 为什么我的配置没有生效？
**A**: 检查以下几点：
1. 是否指定了正确的profile? `java -jar app.jar -Dspring.profiles.active=prd`
2. 配置是否在正确的文件中? 开发配置应该在 `application-dev.yml`
3. 配置文件是否有语法错误? (YAML缩进很重要)

### Q: bootstrap.yml 和 application.yml 的区别是什么？
**A**: 
- `bootstrap.yml` - **应用启动最早阶段加载**，用于配置中心、服务发现等基础设施
- `application.yml` - **应用启动后期加载**，用于应用特定的配置

### Q: 为什么我的环境变量没有被读取？
**A**: 确保：
1. 环境变量已正确导出: `export VAR_NAME=value`
2. 配置文件中使用了正确的语法: `${VAR_NAME}`
3. 应用启动时环境变量仍在作用域中

### Q: 能否同时启用dev和prd配置？
**A**: 不推荐。profile应该是互斥的。如果需要多个profile，使用逗号分隔:
```bash
java -jar app.jar -Dspring.profiles.active=dev,custom
```

---

## 📖 更多信息

- 完整配置迁移指南: [CONFIGURATION_MIGRATION_GUIDE.md](CONFIGURATION_MIGRATION_GUIDE.md)
- 项目开发规范: [CLAUDE.md](CLAUDE.md)
- Spring Boot官方文档: https://spring.io/projects/spring-boot

