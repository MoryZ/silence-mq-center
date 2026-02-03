# 权限系统集成指南

## 📋 目录

1. [系统概述](#系统概述)
2. [快速开始](#快速开始)
3. [集成现有服务](#集成现有服务)
4. [API 参考](#api-参考)
5. [常见问题](#常见问题)
6. [测试用例](#测试用例)

---

## 系统概述

### 架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      前端应用 / API 客户端                        │
└──────────────────────────┬──────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                     REST API 层                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ PermissionController（权限管理接口）                      │   │
│  │ TopicController / ProducerController / ConsumerController│   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                     AOP 拦截层                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ PermissionCheckAspect（@RequirePermission 拦截器）       │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Service 层（业务逻辑）                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ PermissionServiceImpl                                     │   │
│  │ TopicServiceImpl / ProducerServiceImpl / ConsumerServiceImpl│   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Repository 层（数据访问）                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ UserPermissionRepository / PermissionRequestRepository   │   │
│  │ PermissionTypeRepository / TopicRepository / ...         │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   MySQL 数据库                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ permission_type / topic / permission_request / ...       │   │
│  │ user_permission / permission_audit_log                  │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 工作流程

```
申请权限流程：
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   用户申请   │─────▶│  待审批      │─────▶│   管理员     │
│ PERMISSION  │      │  状态        │      │   审批      │
│  REQUEST    │      │  PENDING     │      │             │
└─────────────┘      └──────────────┘      └─────────────┘
                                                  │
                                ┌─────────────────┼─────────────────┐
                                ▼                 ▼                 ▼
                            ┌────────┐       ┌────────┐       ┌────────┐
                            │  批准   │       │  拒绝   │       │  自动   │
                            │APPROVED │       │REJECTED │      │过期     │
                            └────────┘       └────────┘       │EXPIRED  │
                                │                              └────────┘
                                ▼
                            ┌────────────┐
                            │  赋予权限   │
                            │ ACTIVE     │
                            │ 权限有效   │
                            └────────────┘
```

---

## 快速开始

### 1. 初始化数据库

执行 `docs/permission_schema.sql` 脚本创建权限相关表：

```bash
# 登录 MySQL
mysql -u root -p

# 选择数据库
USE your_database;

# 执行 SQL 脚本
SOURCE /path/to/permission_schema.sql;
```

### 2. 配置项目

#### 1. 添加依赖（pom.xml）

```xml
<!-- AOP 支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Spring Security（可选，用于认证） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- AspectJ -->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
</dependency>

<!-- JSON 处理 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

#### 2. 配置 application.yml

```yaml
spring:
  # AOP 配置
  aop:
    auto: true
    proxy-target-class: true
  
  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

# 日志配置
logging:
  level:
    com.old.silence.mq.center.domain.service.permission: DEBUG
    com.old.silence.mq.center.domain.service.permission.aspect: DEBUG
```

### 3. 在 Service 中添加 @RequirePermission 注解

**TopicService 示例**：

```java
@Service
public class TopicServiceImpl implements TopicService {
    
    // Topic 创建权限检查
    @RequirePermission("CREATE_TOPIC")
    public void createTopic(Topic topic) {
        // 创建 Topic 逻辑...
    }
    
    // Topic 删除权限检查
    @RequirePermission("DELETE_TOPIC")
    public void deleteTopic(String topicName) {
        // 删除 Topic 逻辑...
    }
    
    // Topic 级别权限检查
    @RequirePermission(value = "MODIFY_TOPIC", topicIdParamName = "topicId")
    public void modifyTopic(Long topicId, TopicConfig config) {
        // 修改 Topic 配置...
    }
}
```

**ProducerService 示例**：

```java
@Service
public class ProducerServiceImpl implements ProducerService {
    
    // 生产权限检查
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void sendMessage(Long topicId, Message message) {
        // 发送消息...
    }
    
    // 批量生产权限检查
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void batchSendMessages(Long topicId, List<Message> messages) {
        // 批量发送消息...
    }
}
```

**ConsumerService 示例**：

```java
@Service
public class ConsumerServiceImpl implements ConsumerService {
    
    // 消费权限检查
    @RequirePermission(value = "CONSUME", topicIdParamName = "topicId")
    public ConsumeStats consumeMessages(Long topicId, String consumerGroup) {
        // 消费消息...
    }
    
    // 订阅权限检查
    @RequirePermission(value = "SUBSCRIBE_TOPIC", topicIdParamName = "topicId")
    public void subscribeToTopic(Long topicId, String consumerGroup) {
        // 订阅 Topic...
    }
}
```

---

## 集成现有服务

### 第一步：审视现有代码

在集成前，审查需要添加权限检查的 Service 方法：

```
TopicService:
  - createTopic() → CREATE_TOPIC
  - deleteTopic() → DELETE_TOPIC
  - updateTopic() → UPDATE_TOPIC
  - queryTopics() → QUERY_TOPIC

ProducerService:
  - sendMessage() → PRODUCE
  - batchSendMessages() → PRODUCE
  - queryProducerStats() → QUERY_PRODUCER

ConsumerService:
  - consumeMessages() → CONSUME
  - subscribeToTopic() → SUBSCRIBE_TOPIC
  - queryConsumerStats() → QUERY_CONSUMER
```

### 第二步：添加注解

在需要权限检查的方法上添加 `@RequirePermission` 注解：

**修改前**：

```java
public void sendMessage(Long topicId, Message message) {
    // 业务逻辑...
}
```

**修改后**：

```java
@RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
public void sendMessage(Long topicId, Message message) {
    // 业务逻辑保持不变...
}
```

### 第三步：初始化权限数据

```sql
-- 插入权限类型
INSERT INTO permission_type (permission_code, permission_name, description) VALUES
('CREATE_TOPIC', '创建 Topic', '允许创建新的 Topic'),
('DELETE_TOPIC', '删除 Topic', '允许删除已有的 Topic'),
('PRODUCE', '生产消息', '允许向 Topic 发送消息'),
('CONSUME', '消费消息', '允许从 Topic 消费消息'),
('SUBSCRIBE_TOPIC', '订阅 Topic', '允许订阅 Topic'),
('QUERY_TOPIC', '查询 Topic', '允许查询 Topic 信息'),
('MODIFY_TOPIC', '修改 Topic', '允许修改 Topic 配置'),
('MANAGE_ACL', '管理 ACL', '允许管理 Topic 的访问控制列表');

-- 为用户赋予权限
INSERT INTO user_permission (user_id, user_name, topic_id, permission_code, granted_by_id, granted_by_name, granted_time, expire_time, status) VALUES
(1, 'user1', NULL, 'CREATE_TOPIC', 999, 'admin', NOW(), NULL, 'ACTIVE'),
(1, 'user1', 1, 'PRODUCE', 999, 'admin', NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 'ACTIVE'),
(2, 'user2', 1, 'CONSUME', 999, 'admin', NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 'ACTIVE');
```

---

## API 参考

### 权限申请相关

#### 申请权限

**请求**：
```http
POST /api/permissions/request
Content-Type: application/json

{
  "topicId": 1,
  "permissionCode": "PRODUCE",
  "reason": "需要生产消息用于业务需求"
}
```

**响应**：
```json
{
  "success": true,
  "code": 200,
  "message": "权限申请已提交，请等待管理员审批",
  "data": {
    "requestId": 1,
    "userId": 10,
    "userName": "user1",
    "topicId": 1,
    "permissionCode": "PRODUCE",
    "status": "PENDING",
    "reason": "需要生产消息用于业务需求",
    "requestTime": "2024-01-01T10:00:00"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 审批权限

**请求**：
```http
POST /api/permissions/approve
Content-Type: application/json

{
  "requestId": 1,
  "approvalReason": "已确认用户身份和需求",
  "expireTime": "2025-01-01T00:00:00"
}
```

**响应**：
```json
{
  "success": true,
  "code": 200,
  "message": "权限申请已通过，权限已赋予申请者",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 拒绝权限

**请求**：
```http
POST /api/permissions/reject
Content-Type: application/json

{
  "requestId": 1,
  "rejectionReason": "请求的权限范围过大"
}
```

### 权限赋予相关

#### 直接赋予权限（管理员）

**请求**：
```http
POST /api/permissions/grant
Content-Type: application/json

{
  "userId": 10,
  "userName": "user1",
  "topicId": 1,
  "permissionCode": "PRODUCE",
  "expireTime": "2025-01-01T00:00:00"
}
```

#### 撤销权限（管理员）

**请求**：
```http
POST /api/permissions/revoke
Content-Type: application/x-www-form-urlencoded

userId=10&topicId=1&permissionCode=PRODUCE
```

### 权限查询相关

#### 获取当前用户的权限列表

**请求**：
```http
GET /api/permissions/my-permissions
```

**响应**：
```json
{
  "success": true,
  "code": 200,
  "message": "权限列表获取成功",
  "data": [
    {
      "permissionId": 1,
      "userId": 10,
      "userName": "user1",
      "topicId": 1,
      "permissionCode": "PRODUCE",
      "status": "ACTIVE",
      "grantedTime": "2024-01-01T10:00:00",
      "expireTime": "2025-01-01T00:00:00",
      "expired": false
    }
  ],
  "timestamp": "2024-01-01T10:00:00"
}
```

#### 获取指定用户的权限列表

**请求**：
```http
GET /api/permissions/user/10
```

#### 获取 Topic 的权限列表

**请求**：
```http
GET /api/permissions/topic/1
```

#### 检查用户权限

**请求**：
```http
GET /api/permissions/check?userId=10&topicId=1&permissionCode=PRODUCE
```

**响应**：
```json
{
  "success": true,
  "code": 200,
  "message": "权限检查完成",
  "data": {
    "userId": 10,
    "topicId": 1,
    "permissionCode": "PRODUCE",
    "hasPermission": true
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

---

## 常见问题

### Q1: 用户调用受保护方法时得到权限错误

**问题**：
```
PermissionDeniedException: 用户 10 在Topic 1 上没有 PRODUCE 权限
```

**解决方案**：
1. 检查用户是否已申请或被赋予权限
2. 检查权限是否已过期
3. 查询权限列表：`GET /api/permissions/user/10`
4. 如果需要权限，调用申请接口：`POST /api/permissions/request`

### Q2: 无法从 SecurityContext 获取用户 ID

**问题**：
```
ServiceException: 无法获取当前用户信息，请先登录
```

**解决方案**：
1. 确保使用了 Spring Security 并正确认证
2. 在注解中明确指定 `userIdParamName` 参数
3. 使用 `X-User-Id` 请求头作为备选方案

### Q3: AOP 拦截器似乎没有生效

**检查清单**：
1. ✓ 确认 Spring AOP 已启用（pom.xml 中有 spring-boot-starter-aop）
2. ✓ 确认 @EnableAspectJAutoProxy 已配置或 auto: true
3. ✓ 确认注解 @RequirePermission 存在于方法上
4. ✓ 确认 PermissionCheckAspect 被 Spring 检测到（@Component）
5. ✓ 查看 DEBUG 日志确认拦截器是否被触发

### Q4: 如何在开发环境禁用权限检查？

**解决方案**：
```java
@RequirePermission(value = "PRODUCE", enabled = false)
public void produceTestData(Long topicId, String data) {
    // 开发期间禁用权限检查
}
```

或在 application.yml 中配置：
```yaml
permission:
  check:
    enabled: false  # 全局禁用（需要在 AOP 中添加支持）
```

### Q5: 权限过期后如何续期？

**解决方案**：
1. 调用撤销接口：`POST /api/permissions/revoke`
2. 重新申请权限：`POST /api/permissions/request`
3. 或由管理员调用赋予接口重新赋予权限

---

## 测试用例

### 单元测试示例

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class PermissionCheckAspectTest {
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private ProducerService producerService;
    
    private Long testUserId = 1L;
    private Long testTopicId = 1L;
    
    @Before
    public void setUp() {
        // 为测试用户赋予权限
        permissionService.grantPermission(
            testUserId, "testUser", testTopicId, "PRODUCE", 999L, "admin", null
        );
    }
    
    @Test
    public void testHasPermission_Success() {
        // 用户有权限，应该成功执行
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUserId, null)
        );
        
        // 应该不抛出异常
        producerService.sendMessage(testTopicId, "test message");
    }
    
    @Test(expected = PermissionDeniedException.class)
    public void testNoPermission_Failure() {
        Long unauthorizedUserId = 999L;
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(unauthorizedUserId, null)
        );
        
        // 应该抛出 PermissionDeniedException
        producerService.sendMessage(testTopicId, "test message");
    }
    
    @Test
    public void testPermissionExpired() {
        // 创建已过期的权限
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(1);
        permissionService.grantPermission(
            testUserId, "testUser", testTopicId, "CONSUME", 999L, "admin", expiredTime
        );
        
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(testUserId, null)
        );
        
        // 应该拒绝因为权限已过期
        // assertThrows(PermissionDeniedException.class, ...);
    }
}
```

### 集成测试示例

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PermissionControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private PermissionService permissionService;
    
    @Test
    public void testRequestPermissionFlow() {
        // 1. 用户申请权限
        PermissionRequestDTO request = new PermissionRequestDTO();
        request.setTopicId(1L);
        request.setPermissionCode("PRODUCE");
        request.setReason("测试申请");
        
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/permissions/request",
            request,
            ApiResponse.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }
}
```

---

## 📊 权限类型参考

| 权限代码 | 权限名称 | 应用场景 |
|---------|--------|--------|
| CREATE_TOPIC | 创建 Topic | 允许创建新 Topic |
| DELETE_TOPIC | 删除 Topic | 允许删除 Topic |
| UPDATE_TOPIC | 修改 Topic | 允许修改 Topic 配置 |
| QUERY_TOPIC | 查询 Topic | 允许查询 Topic 信息 |
| PRODUCE | 生产消息 | 允许向 Topic 发送消息 |
| CONSUME | 消费消息 | 允许从 Topic 消费消息 |
| SUBSCRIBE_TOPIC | 订阅 Topic | 允许订阅 Topic |
| MANAGE_ACL | 管理 ACL | 允许管理 Topic 的 ACL |

---

## 总结

权限系统通过 AOP 拦截器和注解提供了**低侵入性**、**灵活**、**易于维护**的权限管理能力。

关键要点：
- ✓ 最小化现有代码改动（只添加注解）
- ✓ 支持灵活的权限模型（全局、Topic 级）
- ✓ 完整的权限生命周期管理
- ✓ 详细的审计日志追踪

