# 权限检查拦截器完整指南

## 🎯 概述

实现了一个**低侵入性**的权限检查拦截器，使用 AOP + 注解模式，无需修改现有 Service 代码。

---

## 📦 核心组件

### 1. @RequirePermission 注解

**位置**：`com.old.silence.mq.center.domain.service.permission.annotation.RequirePermission`

**属性**：

| 属性 | 类型 | 必需 | 说明 |
|-----|------|------|-----|
| `value` | String | ✓ | 所需权限代码（如 PRODUCE、CONSUME） |
| `topicIdParamName` | String | ✗ | Topic ID 参数名（默认：topicId；空字符串表示全局权限） |
| `userIdParamName` | String | ✗ | 用户 ID 参数名（默认：从 SecurityContext 获取） |
| `logOnDeny` | boolean | ✗ | 权限不足时是否记录 WARN 日志（默认：true） |
| `errorMessage` | String | ✗ | 权限不足时的错误消息 |
| `enabled` | boolean | ✗ | 是否启用权限检查（默认：true，可用于开发环境禁用） |

---

### 2. PermissionCheckAspect 切面

**位置**：`com.old.silence.mq.center.domain.service.permission.aspect.PermissionCheckAspect`

**功能**：

- 拦截所有 @RequirePermission 注解的方法
- 在方法执行前进行权限检查
- 自动获取用户信息
- 灵活处理参数名称

**用户 ID 获取优先级**：

```
1. 方法参数（userIdParamName 指定的参数）
   ↓
2. SecurityContext（Spring Security）
   ↓
3. 请求头（X-User-Id Header）
```

**Topic ID 获取方式**：

```
从方法参数中查找指定名称的参数（topicIdParamName）
```

---

## 🚀 使用方式

### 方式1：全局权限检查（无 Topic）

```java
@Service
public class TopicService {
    
    @RequirePermission("CREATE_TOPIC")
    public void createTopic(String topicName, String clusterName) {
        // 创建 Topic 的逻辑
        // 权限检查在方法执行前自动进行
    }
    
    @RequirePermission("DELETE_TOPIC")
    public void deleteTopic(String topicName) {
        // 删除 Topic 的逻辑
    }
}
```

**权限检查流程**：
```
请求 createTopic() → AOP 拦截 → 获取当前用户 ID → 检查 CREATE_TOPIC 权限 → 允许/拒绝
```

---

### 方式2：Topic 级权限检查（推荐）

```java
@Service
public class ProducerService {
    
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void produceMessage(Long topicId, String message) {
        // 生产消息的逻辑
    }
}
```

**权限检查流程**：
```
请求 produceMessage(topicId=1, message="test") 
  ↓
AOP 拦截 → 获取当前用户 ID → 从参数提取 topicId=1 
  ↓
检查用户在 Topic 1 上的 PRODUCE 权限 
  ↓
允许/拒绝
```

---

### 方式3：自定义参数名称

```java
@RequirePermission(value = "VIEW_MESSAGE", topicIdParamName = "tid")
public void viewMessage(Long tid, String messageId) {
    // 查看消息的逻辑
}

@RequirePermission(value = "RESET_OFFSET", 
                   userIdParamName = "userId", 
                   topicIdParamName = "topicId")
public void resetOffset(Long userId, Long topicId, String group) {
    // 重置偏移量的逻辑
}
```

---

### 方式4：自定义错误消息

```java
@RequirePermission(
    value = "MANAGE_ACL",
    topicIdParamName = "topicId",
    errorMessage = "您没有权限管理此 Topic 的 ACL"
)
public void manageAcl(Long topicId, String accessKey, String permissions) {
    // 管理 ACL 的逻辑
}
```

---

### 方式5：开发环境禁用权限检查

```java
// 在开发环境临时禁用权限检查
@RequirePermission(
    value = "PRODUCE",
    topicIdParamName = "topicId",
    enabled = false  // 禁用权限检查
)
public void produceTestData(Long topicId, String testData) {
    // 测试代码
}
```

---

### 方式6：禁用日志输出

```java
@RequirePermission(
    value = "PRODUCE",
    topicIdParamName = "topicId",
    logOnDeny = false  // 权限不足时不记录日志
)
public void batchProduceMessage(Long topicId, List<String> messages) {
    // 批量生产消息
}
```

---

## 📊 权限检查流程图

```
1. 用户调用 Service 方法
   ├─ produceMessage(1L, "test")
   ├─ modifyTopicConfig(1L, "key", "value")
   └─ deleteSubscriptionGroup(1L)

2. AOP 拦截（@RequirePermission 检查）
   ├─ 获取权限注解信息
   ├─ 提取 permissionCode
   ├─ 获取 userId（从参数、SecurityContext、请求头）
   └─ 获取 topicId（从参数）

3. 调用 PermissionService 检查权限
   ├─ userPermissionRepository.findValidPermission(userId, topicId, permissionCode)
   ├─ 检查权限是否存在
   ├─ 检查权限是否过期
   └─ 检查权限状态

4. 权限检查结果
   ├─ ✓ 权限有效 → 继续执行方法
   └─ ✗ 权限无效 → 抛出 PermissionDeniedException
       └─ 方法不执行，直接返回错误
```

---

## 🔍 异常处理

### PermissionDeniedException

当权限不足时，会抛出 `PermissionDeniedException`：

```java
class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(Long userId, Long topicId, String permissionCode) {
        super(String.format("用户 %d 在Topic %d 上没有 %s 权限", userId, topicId, permissionCode));
    }
}
```

**处理异常**（在 Controller 层）：

```java
@RestController
@RequestMapping("/api/topics")
public class TopicController {
    
    @PostMapping
    public ApiResponse createTopic(@RequestBody TopicRequest request) {
        try {
            topicService.createTopic(request.getName(), request.getCluster());
            return ApiResponse.success("Topic 创建成功");
        } catch (PermissionService.PermissionDeniedException e) {
            return ApiResponse.error(403, e.getMessage());
        }
    }
}
```

---

## 📝 集成到现有 Service 的示例

### 示例1：TopicService

**修改前**（需要手动检查权限）：
```java
@Service
public class TopicServiceImpl implements TopicService {
    
    @Autowired
    private PermissionService permissionService;
    
    public void createTopic(Long userId, Topic topic) {
        // 手动检查权限（侵入性强）
        permissionService.checkPermission(userId, null, "CREATE_TOPIC");
        
        // 创建 Topic 逻辑
        // ...
    }
}
```

**修改后**（使用注解）：
```java
@Service
public class TopicServiceImpl implements TopicService {
    
    // 无需注入 PermissionService
    
    @RequirePermission("CREATE_TOPIC")
    public void createTopic(Topic topic) {
        // 权限检查由 AOP 自动进行
        
        // 创建 Topic 逻辑保持不变
        // ...
    }
    
    @RequirePermission("DELETE_TOPIC")
    public void deleteTopic(String topicName) {
        // 删除逻辑
    }
}
```

### 示例2：ProducerService

```java
@Service
public class ProducerServiceImpl implements ProducerService {
    
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void sendMessage(Long topicId, String message) {
        // 生产消息逻辑
    }
    
    @RequirePermission(value = "PRODUCE", topicIdParamName = "topicId")
    public void sendBatch(Long topicId, List<String> messages) {
        // 批量生产逻辑
    }
}
```

### 示例3：ConsumerService

```java
@Service
public class ConsumerServiceImpl implements ConsumerService {
    
    @RequirePermission(value = "CONSUME", topicIdParamName = "topicId")
    public ConsumeStats consumeMessage(Long topicId, String consumerGroup) {
        // 消费消息逻辑
    }
    
    @RequirePermission(value = "SUBSCRIBE_TOPIC", topicIdParamName = "topicId")
    public void subscribeToTopic(Long topicId, String consumerGroup) {
        // 订阅逻辑
    }
}
```

---

## 🎯 与 Spring Security 集成

如果项目使用了 Spring Security，AOP 会自动从 `SecurityContext` 中获取用户信息：

```java
// SecurityContext 中的用户信息将自动被 AOP 获取
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
Object principal = auth.getPrincipal(); // 应该是用户 ID 或自定义 UserDetails
```

**配置 Spring Security**：

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // 权限检查由 @RequirePermission 注解处理
                .anyRequest().authenticated()
            .and()
            .formLogin();
    }
}
```

---

## 🔧 配置建议

### pom.xml（依赖项）

```xml
<!-- 已有依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- 如果使用 Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- AspectJ -->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
</dependency>
```

### application.yml（配置）

```yaml
# 启用 AOP
spring:
  aop:
    auto: true
    proxy-target-class: true

# 日志级别
logging:
  level:
    com.old.silence.mq.center.domain.service.permission: DEBUG
```

---

## ✨ 优点总结

✓ **侵入性低** - 只需添加注解，无需修改 Service 逻辑  
✓ **声明式** - 权限需求清晰易懂  
✓ **灵活** - 支持多种参数获取方式  
✓ **可扩展** - 可添加更多参数支持  
✓ **可配置** - 支持开发环境禁用  
✓ **自动化** - AOP 自动处理权限检查  

---

## ⚠️ 注意事项

1. **参数名称必须准确** - topicIdParamName 必须与实际参数名称一致
2. **用户 ID 获取** - 如果没有配置 userIdParamName，必须使用 Spring Security
3. **性能** - AOP 会增加一小部分性能开销，但可以忽略
4. **日志** - 权限检查失败会记录 WARN 日志，可通过 logOnDeny 禁用
5. **异常处理** - Controller 层需要捕获 PermissionDeniedException 并返回 403

---

## 🚀 下一步

1. ✓ 实现权限检查拦截器（已完成）
2. → 在现有 Service 中添加 @RequirePermission 注解
3. → 创建 PermissionController REST API
4. → 编写单元测试验证权限检查

