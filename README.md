# silence-mq-center

> RocketMQ 管理控制台 - 提供消息队列的监控、管理和运维功能

##  项目介绍

silence-mq-center 是一个基于 Spring Boot 的 RocketMQ 管理控制台，为运维人员和开发者提供全面的消息队列监控、诊断和管理功能。

### 核心功能

-  **集群监控**：实时监控 RocketMQ 集群状态、Broker 和 NameServer
-  **消息查询**：支持按主题、消息ID、消息内容查询消息
-  **消费者管理**：查看消费者组、消费进度、消费偏移量重置
-  **性能分析**：消息链路追踪、性能指标分析
-  **运维操作**：主题管理、消息重发、死信队列处理
-  **权限控制**：支持 ACL 权限管理

##  项目架构

采用经典的三层架构模式：

### Controller 层（API层）
- 处理 HTTP 请求和参数校验
- 调用 Service 层实现业务逻辑
- 统一返回 API 响应

### Service 层（业务逻辑层）
- 封装核心业务逻辑
- 调用 RocketMQ Admin API
- 处理异常和错误情况

### Client 层（客户端层）
- 封装 RocketMQ Admin API
- 提供统一的调用接口
- 处理 API 异常转换

详见 [CLAUDE.md](docs/CLAUDE.md) - 完整的开发规范文档

##  技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.x | 后端框架 |
| RocketMQ | 4.x+ | 消息队列引擎 |
| MyBatis-Plus | 最新 | ORM 框架 |
| Guava | 最新 | Google 工具库 |
| Jackson | 2.x | JSON 序列化 |
| Hutool | 最新 | Java 工具库 |
| SLF4J + Logback | 最新 | 日志框架 |

##  快速开始

### 前置要求

- JDK 8 或更高版本
- Maven 3.6+
- RocketMQ 4.x 或更高版本

### 编译构建

``bash
# 克隆项目
git clone https://gitee.com/your-org/silence-mq-center.git
cd silence-mq-center

# 编译打包
mvn clean package -DskipTests

# 生成的 JAR 在 target 目录下
``

### 运行应用

``bash
# 方式一：直接运行 JAR
java -jar target/silence-mq-center.jar

# 方式二：使用 Maven 运行
mvn spring-boot:run

# 方式三：Docker 运行
docker build -t silence-mq-center .
docker run -p 8080:8080 silence-mq-center
``

### 配置说明

编辑 `src/main/resources/application.yml`：

``yaml
spring:
  application:
    name: silence-mq-center
  
  server:
    port: 8080

rocketmq:
  namesrv-addr: localhost:9876  # NameServer 地址
  acl-enabled: false             # 是否启用 ACL
  dashboard-data-path: /data/    # 数据存储路径
``

## 📖 开发指南

### 项目结构

``
src/main/java/com/old/silence/mq/center/
 api/                      # Controller 层
    ConsumerController.java
    TopicController.java
    ...
 domain/
    model/               # 数据模型/DTO
    service/             # Service 层
       ConsumerService.java
       impl/
    service/client/      # Client 层
 exception/               # 异常处理
 factory/                 # 对象池工厂
 task/                    # 定时任务
 util/                    # 工具类
 aspect/                  # AOP 切面
 MqCenterApplication.java # 启动类
``

### 开发规范

详见 [CLAUDE.md](docs/CLAUDE.md)，包含：

- ✅ **命名规范**：类、方法、变量命名规则
- ✅ **架构分层**：Controller/Service/DAO 职责划分
- ✅ **异常处理**：禁止吞掉异常，使用自定义异常
- ✅ **日志规范**：使用 SLF4J，参数化日志
- ✅ **性能规范**：禁止循环查库，资源及时释放
- ✅ **并发安全**：线程池使用、volatile 关键字

### API 文档

启动应用后，访问 `http://localhost:8080/swagger-ui.html` 查看 API 文档

常用 API 端点：

- `GET /api/v1/cluster` - 获取集群信息
- `GET /api/v1/topic/list` - 获取主题列表
- `GET /api/v1/consumer/groupList` - 获取消费者组列表
- `GET /api/v1/message/queryById` - 查询消息

## 🤝 参与贡献

1. Fork 本仓库
2. 新建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码审查清单

提交 PR 前，请确保：

- [ ] 代码遵循 [CLAUDE.md](docs/CLAUDE.md) 的开发规范
- [ ] 添加了适当的日志和异常处理
- [ ] 包含必要的单元测试
- [ ] 更新了相关文档
- [ ] 没有 DEBUG 代码和 System.out

## 📝 许可证

本项目采用 MIT 许可证

## 📧 联系方式

- 问题反馈：[Issues](https://gitee.com/your-org/silence-mq-center/issues)
- 讨论交流：[Discussions](https://gitee.com/your-org/silence-mq-center/discussions)

## 🙏 致谢

感谢 RocketMQ 开源社区和所有贡献者

---

更多详细信息请参考 [CLAUDE.md](docs/CLAUDE.md)
