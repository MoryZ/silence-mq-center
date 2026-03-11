# Silence MQ Center

#### 项目简介

**Silence MQ Center** 是一个基于 Java 21 + Spring Boot 3.3.8 的 RocketMQ 管理中心，提供集群管理、监控、消息追踪、消费管理等全面的运维功能。

**特点**: 
- 🎯 非数据库应用，通过 RocketMQ 管理客户端与消息队列交互
- 🔄 对象池模式管理 MQAdminExt 连接，优化资源利用
- ⚡ AOP 框架自动处理资源获取和释放
- 💾 Guava LoadingCache 缓存机制，性能优化

**相关文档**: 详见 [CLAUDE.md](./CLAUDE.md) - 完整的开发规范和最佳实践

---

## 目录

1. [快速开始](#快速开始)
2. [项目架构](#项目架构)
3. [核心模块](#核心模块)
4. [开发规范](#开发规范)
5. [环境要求](#环境要求)
6. [参与贡献](#参与贡献)

---

## 快速开始

### 环境要求

- Java 21+
- Spring Boot 3.3.8
- Maven 3.6+
- RocketMQ 5.1.0+

### 项目结构

```
com.old.silence.mq.center
├── api                      # Controller 层 (REST API)
├── domain                   # 业务核心层
│   ├── model/              # 数据模型 (VO/DTO)
│   └── service/            # 业务服务
├── aspect/                  # AOP 切面层
├── factory/                 # 工厂模式 (连接池管理)
├── task/                    # 后台任务
├── exception/               # 自定义异常
├── util/                    # 工具类
└── config/                  # 配置类
```

---

## 项目架构

### 三层架构设计

```
┌─────────────────────────┐
│   API Controller 层     │ (请求入口 + 参数验证)
├─────────────────────────┤
│   Service 业务层        │ (业务逻辑 + 缓存管理)
├─────────────────────────┤
│   Factory 工厂层        │ (对象池 + 资源管理)
├─────────────────────────┤
│   RocketMQ 客户端       │ (MQAdminExt)
└─────────────────────────┘
```

### AOP 资源自动管理

所有 MQAdminExt 调用通过 AspectJ AOP 自动处理连接的获取和释放：

```java
@Aspect
@Service
public class MQAdminAspect {
    @Around("execution(* com.old.silence.mq.center.domain.service.client.MQAdminExtImpl..*(..))")
    public Object aroundMQAdminMethod(ProceedingJoinPoint joinPoint) {
        MQAdminExt mqAdminExt = pool.borrowObject();
        try {
            return joinPoint.proceed();  // 执行业务方法
        } finally {
            pool.returnObject(mqAdminExt);  // 确保归还连接
        }
    }
}
```

**优势**:
- ✅ 自动的 try-finally 确保连接归还
- ✅ 异常发生时也会正确归还
- ✅ 业务代码无需手动管理连接

---

## 核心模块

### 1. 数据模型 (Model)

模型分为三类：

| 分类 | 用途 | 示例 |
|------|------|------|
| **VO** | API 响应 | `MessageView`, `MessageTraceView` |
| **DTO** | API 请求 | `MessageQueryByPage`, `DlqMessageRequest` |
| **Excel** | 批量导出 | `DlqMessageExcelModel` |

**关键规范**:
```java
// ✅ 提供无参构造器
public MessageView() {}

// ✅ 使用 long 存储时间戳
private long bornTimestamp;

// ✅ 使用 BeanUtils.copyProperties() 进行对象转换
public static MessageView fromMessageExt(MessageExt messageExt) {
    MessageView view = new MessageView();
    BeanUtils.copyProperties(messageExt, view);
    return view;
}

// ✅ Excel 模型必须实现序列化
public class DlqMessageExcelModel implements Serializable {
    private static final long serialVersionUID = 1L;
}
```

### 2. Service 业务层

三种 Service 类型：

| 类型 | 特点 | 继承关系 |
|------|------|--------|
| 直接实现 | 简单业务 | implements Service |
| 继承 AbstractCommonService | 需要 MQAdminExt | extends AbstractCommonService |
| 组合依赖 | 多个服务协调 | 注入多个 Service |

**示例**:
```java
@Service
public class AclServiceImpl extends AbstractCommonService implements AclService {
    protected AclServiceImpl(MQAdminExt mqAdminExt) {
        super(mqAdminExt);
    }
}
```

### 3. 工厂模式 (Factory)

使用 GenericObjectPool 管理 MQAdminExt 连接：

```java
@Configuration
public class MQAdminFactory {
    @Bean
    public GenericObjectPool<MQAdminExt> mqAdminExtPool() {
        return new GenericObjectPool<>(
            new MQAdminPooledObjectFactory(...),
            new GenericObjectPoolConfig<>()
                .setMaxTotal(10)
                .setMaxIdle(5)
                .setMinIdle(1)
        );
    }
}
```

---

## 开发规范

### 必用工具栈

| 工具 | 版本 | 用途 |
|------|------|------|
| Jackson | 内置 | JSON 序列化（JsonUtil 封装） |
| SLF4J | 3.x | 日志框架 |
| Guava | 33.2.0 | 集合、缓存、异常处理 |
| Commons Pool2 | 2.12.0 | 对象池管理 |
| AspectJ | 1.9.21 | AOP 处理 |
| EasyExcel | 4.0.3 | Excel 导出 |

### JSON 处理 - JsonUtil

```java
// 对象 → JSON
String json = JsonUtil.obj2String(obj);

// JSON → 对象
MyClass obj = JsonUtil.string2Obj(json, MyClass.class);

// 处理泛型
List<String> list = JsonUtil.string2Obj(json, 
    new TypeReference<List<String>>() {});
```

### 日志规范

```java
// DEBUG: 流程追踪
logger.debug("op=queryTopics params={}", JsonUtil.obj2String(params));

// WARN: 警告但不影响功能
logger.warn("Refresh cluster failed", e);

// ERROR: 错误（必须传入异常对象）
logger.error("Query failed", e);  // ✅ 第二参数是异常
```

### 缓存规范 - Guava LoadingCache

```java
private LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)           // 最多缓存 1000 个
    .concurrencyLevel(10)        // 并发度 10
    .build(new CacheLoader<String, List<String>>() {
        @Override
        public List<String> load(String key) throws Exception {
            return loadDataFromRocketMQ(key);
        }
    });

// 使用
List<String> data = cache.get(key);  // 自动加载

// 手动刷新
cache.refresh(key);

// 手动清除
cache.invalidate(key);
```

### 参数验证 - Preconditions

```java
@PostMapping("/create")
public void create(@RequestBody TopicConfig config) {
    // ✅ 必须验证所有参数
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(config.getTopicName()), 
        "Topic name cannot be empty"
    );
    Preconditions.checkArgument(
        config.getQueueNum() > 0, 
        "Queue number must be positive"
    );
    topicService.create(config);
}
```

---

## 开发红线 (禁止事项)

### ❌ 异常处理

```java
// ❌ 禁止：吞掉异常堆栈
logger.error("Failed");  // 缺少异常对象

// ✅ 正确
logger.error("Failed", e);  // 传入异常对象
```

### ❌ 资源管理

```java
// ❌ 禁止：不释放连接
MQAdminExt admin = pool.borrowObject();
admin.queryTopic(topic);
// 没有归还！连接泄漏

// ✅ 正确：通过 AOP 自动管理
```

### ❌ 空指针检查

```java
// ❌ 禁止：不检查 null
List<String> topics = getTopics();
for (String topic : topics) {  // NPE if null
    process(topic);
}

// ✅ 正确
if (CollectionUtils.isEmpty(topics)) {
    return;
}
```

### ❌ 字符编码

```java
// ❌ 禁止：使用默认编码
String str = new String(bytes);

// ✅ 正确：显式指定 UTF-8
String str = new String(bytes, Charsets.UTF_8);
```

### ❌ 并发相关

```java
// ❌ 禁止：非线程安全的 HashMap
private Map<String, String> cache = new HashMap<>();

// ✅ 正确：使用 ConcurrentHashMap
private Map<String, String> cache = new ConcurrentHashMap<>();
```

---

## 代码审查检查清单

提交代码前，请自检：

- [ ] 异常捕获时是否传入异常对象？
- [ ] 是否有 null 指针检查？
- [ ] 是否使用了 UTF-8 编码？
- [ ] 缓存配置是否包括并发度？
- [ ] 是否在 finally 中释放资源？
- [ ] Service 层是否只依赖接口？
- [ ] 是否在 Controller 层进行了参数验证？

---

## 参与贡献

1.  Fork 本仓库
2.  新建 `feature/xxx` 分支
3.  提交修改（遵循上述规范）
4.  新建 Pull Request

### 提交要求

- 遵循 [CLAUDE.md](./CLAUDE.md) 中的开发规范
- 异常处理必须完整（包括日志）
- 添加必要的代码注释
- 通过代码审查检查清单

---

## 快速链接

- 📘 [完整开发规范 - CLAUDE.md](./CLAUDE.md)
- 🏗️ [项目架构设计]()
- 📚 [API 文档]()
- 🐛 [常见问题解答]()

---

**版本**: 2.0.1-SNAPSHOT  
**Java**: 21  
**Spring Boot**: 3.3.8  
**RocketMQ**: 5.1.0  

更多详情请参考 [CLAUDE.md](./CLAUDE.md) 了解完整的开发规范、工具栈和开发红线。
