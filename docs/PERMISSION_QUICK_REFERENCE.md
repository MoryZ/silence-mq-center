# 权限系统快速参考

## 🚀 5 分钟快速开始

### 1️⃣ 初始化数据库（1 分钟）

```bash
# 执行 SQL 脚本
mysql -u root -p your_database < docs/permission_schema.sql
```

### 2️⃣ 添加 Maven 依赖（30 秒）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 3️⃣ 添加注解到 Service 方法（1 分钟）

```java
@Service
public class ProducerService {
    
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void produceMessage(Long topicId, String message) {
        // 业务代码保持不变
        // 权限检查由 AOP 自动进行
    }
}
```

### 4️⃣ 为用户赋予权限（2 分钟）

```sql
-- 为用户赋予权限
INSERT INTO user_permission (user_id, user_name, topic_id, permission_code, granted_by_id, status) VALUES
(1, 'user1', 1, 'PRODUCE', 999, 'ACTIVE');
```

✅ 完成！权限系统已启用。

---

## 📚 常用 API

### 权限操作

| 操作 | 代码 | 说明 |
|-----|------|------|
| **检查权限** | `permissionService.hasPermission(userId, topicId, "PRODUCE")` | 返回 boolean |
| **检查（异常）** | `permissionService.checkPermission(userId, topicId, "PRODUCE")` | 无权限则抛异常 |
| **申请权限** | `permissionService.requestPermission(userId, userName, topicId, "PRODUCE", reason)` | 创建待审批 |
| **审批通过** | `permissionService.approvePermission(requestId, approverId, name, reason, expireTime)` | 赋予权限 |
| **撤销权限** | `permissionService.revokePermission(userId, topicId, "PRODUCE")` | 立即失效 |
| **直接赋予** | `permissionService.grantPermission(userId, name, topicId, "PRODUCE", granterId, name, expireTime)` | 无需申请 |

### 权限查询

| 查询 | 代码 |
|-----|------|
| 用户权限列表 | `permissionService.getUserPermissions(userId)` |
| Topic 权限列表 | `permissionService.getTopicPermissions(topicId)` |
| 待审批申请 | `permissionService.getPendingRequests()` |
| 用户申请历史 | `permissionService.getUserRequests(userId)` |

---

## 🎯 注解用法速查

### 全局权限（不需要 Topic）

```java
@RequirePermission("CREATE_TOPIC")
public void createTopic(String topicName) { }
```

### Topic 级权限（推荐）

```java
@RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
public void produceMessage(Long topicId, String message) { }
```

### 自定义参数名

```java
@RequirePermission(value = "VIEW_MESSAGE", topicIdParamName = "tid")
public void viewMessage(Long tid) { }
```

### 自定义用户 ID 参数

```java
@RequirePermission(
    value = "RESET_OFFSET",
    userIdParamName = "userId",
    topicIdParamName = "topicId"
)
public void resetOffset(Long userId, Long topicId) { }
```

### 自定义错误消息

```java
@RequirePermission(
    value = "MANAGE_ACL",
    errorMessage = "您没有权限管理此 Topic 的 ACL"
)
public void manageAcl(Long topicId) { }
```

### 开发模式（禁用检查）

```java
@RequirePermission(value = "PRODUCE", enabled = false)
public void testProduce(Long topicId) { }
```

### 静默模式（不记录日志）

```java
@RequirePermission(value = "PRODUCE", logOnDeny = false)
public void batchProduce(Long topicId) { }
```

---

## 🔌 REST API 速查

### 申请权限
```http
POST /api/permissions/request
Content-Type: application/json

{
  "topicId": 1,
  "permissionCode": "PRODUCE",
  "reason": "用于生产业务消息"
}
```

### 审批权限
```http
POST /api/permissions/approve
Content-Type: application/json

{
  "requestId": 1,
  "approvalReason": "已确认用户身份",
  "expireTime": "2025-01-01T00:00:00"
}
```

### 查询我的权限
```http
GET /api/permissions/my-permissions
```

### 检查权限
```http
GET /api/permissions/check?userId=1&topicId=1&permissionCode=PRODUCE
```

### 撤销权限
```http
POST /api/permissions/revoke
Content-Type: application/x-www-form-urlencoded

userId=1&topicId=1&permissionCode=PRODUCE
```

---

## 🔑 权限代码列表

| 代码 | 名称 | 场景 |
|-----|------|------|
| `CREATE_TOPIC` | 创建 Topic | 允许创建新 Topic |
| `DELETE_TOPIC` | 删除 Topic | 允许删除 Topic |
| `UPDATE_TOPIC` | 修改 Topic | 允许修改 Topic 配置 |
| `PRODUCE` | 生产消息 | 允许发送消息 |
| `CONSUME` | 消费消息 | 允许消费消息 |
| `SUBSCRIBE_TOPIC` | 订阅 Topic | 允许订阅 Topic |
| `MANAGE_ACL` | 管理 ACL | 允许管理访问控制 |

---

## ⚙️ 配置建议

### application.yml

```yaml
spring:
  aop:
    auto: true
    proxy-target-class: true

logging:
  level:
    com.old.silence.mq.center.domain.service.permission: DEBUG
```

---

## ❌ 错误排查

### 问题 1: PermissionDeniedException

**错误**：`用户 1 在Topic 1 上没有 PRODUCE 权限`

**原因**：用户没有权限或权限已过期

**解决**：
```java
// 查询用户权限
List<UserPermission> permissions = permissionService.getUserPermissions(1L);
// 为用户申请/赋予权限
permissionService.grantPermission(1L, "user1", 1L, "PRODUCE", 999L, "admin", null);
```

### 问题 2: 无法获取用户信息

**错误**：`无法获取当前用户信息，请先登录`

**原因**：SecurityContext 中没有认证信息

**解决**：
```java
// 1. 确保使用了 Spring Security
// 2. 或在注解中指定 userIdParamName
@RequirePermission(value = "PRODUCE", userIdParamName = "userId")
public void produce(Long userId, Long topicId) { }

// 3. 或在请求头中添加 X-User-Id
```

### 问题 3: AOP 拦截器没有生效

**检查清单**：
- ✓ pom.xml 中有 `spring-boot-starter-aop`？
- ✓ `@RequirePermission` 注解已添加？
- ✓ PermissionCheckAspect 被 Spring 扫描？
- ✓ 查看 DEBUG 日志确认拦截？

---

## 📊 权限状态流程

```
PENDING (申请中)
    ↓
    ├─→ APPROVED (通过) → ACTIVE (激活)
    │                        ↓
    │                    EXPIRED (过期)
    │                        或
    │                    REVOKED (撤销)
    │
    └─→ REJECTED (拒绝)
```

---

## 🧪 快速测试

### 测试权限检查

```bash
# 1. 赋予用户权限
curl -X POST http://localhost:8080/api/permissions/grant \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "userName": "user1",
    "topicId": 1,
    "permissionCode": "PRODUCE",
    "expireTime": "2025-12-31"
  }'

# 2. 调用受保护方法（应该成功）
curl -X POST http://localhost:8080/api/topics/1/produce \
  -H "Authorization: Bearer token" \
  -d "message=test"

# 3. 撤销权限
curl -X POST http://localhost:8080/api/permissions/revoke \
  -d "userId=1&topicId=1&permissionCode=PRODUCE"

# 4. 调用受保护方法（应该被拒绝）
curl -X POST http://localhost:8080/api/topics/1/produce \
  -d "message=test"
# 返回: 403 Forbidden - 用户 1 在Topic 1 上没有 PRODUCE 权限
```

---

## 📖 详细文档

- 📘 [完整使用指南](PERMISSION_SYSTEM_INTEGRATION.md)
- 📗 [AOP 拦截器指南](PERMISSION_INTERCEPTOR_GUIDE.md)
- 📙 [项目完成总结](PERMISSION_SYSTEM_IMPLEMENTATION.md)
- 📕 [SQL 初始化脚本](permission_schema.sql)

---

## 💡 最佳实践

### ✅ 推荐做法

1. **使用 Topic 级权限** - 大多数场景都需要 Topic 粒度
   ```java
   @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
   public void produce(Long topicId) { }
   ```

2. **设置权限过期时间** - 避免权限永久有效
   ```java
   permissionService.grantPermission(
       userId, userName, topicId, "PRODUCE",
       granterId, granter, 
       LocalDateTime.now().plusYears(1)  // 设置 1 年有效期
   );
   ```

3. **定期清理过期权限** - 运维定期任务
   ```java
   permissionService.expireExpiredPermissions();
   ```

4. **记录权限操作** - 用于审计追踪（自动进行）

### ❌ 避免做法

1. **不要硬编码用户 ID** - 使用参数或 SecurityContext
   ```java
   // ❌ 错误
   Long userId = 999L;  // 硬编码
   
   // ✅ 正确
   @RequirePermission(userIdParamName = "userId")
   public void produce(Long userId) { }
   ```

2. **不要混合权限检查方式** - 选择一种方式
   ```java
   // ❌ 避免
   permissionService.checkPermission(...);  // 手动检查
   @RequirePermission(...)  // 注解检查
   ```

3. **不要忽视权限过期** - 定期维护过期权限

---

## 🎓 学习路径

1. **入门** (5 分钟)
   - 阅读本快速参考
   - 执行快速开始 4 步

2. **进阶** (30 分钟)
   - 阅读 AOP 拦截器指南
   - 学习所有注解用法

3. **精通** (2 小时)
   - 阅读完整集成指南
   - 查看源代码实现
   - 编写测试用例

---

**最后更新**: 2024-01-01  
**文档版本**: 1.0  
**维护者**: Silence

