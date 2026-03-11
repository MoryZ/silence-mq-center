# Silence MQ Center

#### Project Description

**Silence MQ Center** is a RocketMQ management center built with Java 21 + Spring Boot 3.3.8, providing comprehensive operational functions including cluster management, monitoring, message tracking, and consumer management.

**Features**:
- 🎯 Database-free application, interacts with RocketMQ via management client
- 🔄 Object pool pattern for MQAdminExt connection management and resource optimization
- ⚡ AOP framework automatically handles resource acquisition and release
- 💾 Guava LoadingCache for performance optimization

**Development Guidelines**: See [CLAUDE.md](./CLAUDE.md) - Complete development standards and best practices

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Project Architecture](#project-architecture)
3. [Core Modules](#core-modules)
4. [Development Standards](#development-standards)
5. [Requirements](#requirements)
6. [Contributing](#contributing)

---

## Quick Start

### Requirements

- Java 21+
- Spring Boot 3.3.8
- Maven 3.6+
- RocketMQ 5.1.0+

### Project Structure

```
com.old.silence.mq.center
├── api                      # Controller layer (REST API)
├── domain                   # Business core layer
│   ├── model/              # Data models (VO/DTO)
│   └── service/            # Business services
├── aspect/                  # AOP aspect layer
├── factory/                 # Factory pattern (connection pool)
├── task/                    # Background tasks
├── exception/               # Custom exceptions
├── util/                    # Utility classes
└── config/                  # Configuration classes
```

---

## Project Architecture

### Three-Layer Architecture

```
┌─────────────────────────┐
│   API Controller Layer  │ (Entry point + parameter validation)
├─────────────────────────┤
│   Service Business Layer│ (Business logic + caching)
├─────────────────────────┤
│   Factory Layer         │ (Object pool + resource management)
├─────────────────────────┤
│   RocketMQ Client       │ (MQAdminExt)
└─────────────────────────┘
```

### AOP Automatic Resource Management

All MQAdminExt calls are automatically managed for connection acquisition and release via AspectJ AOP:

```java
@Aspect
@Service
public class MQAdminAspect {
    @Around("execution(* com.old.silence.mq.center.domain.service.client.MQAdminExtImpl..*(..))")
    public Object aroundMQAdminMethod(ProceedingJoinPoint joinPoint) {
        MQAdminExt mqAdminExt = pool.borrowObject();
        try {
            return joinPoint.proceed();  // Execute business method
        } finally {
            pool.returnObject(mqAdminExt);  // Ensure connection return
        }
    }
}
```

**Advantages**:
- ✅ Automatic try-finally ensures connection return
- ✅ Connections are properly returned even on exception
- ✅ No manual connection management needed in business code

---

## Core Modules

### 1. Data Models

Models are divided into three categories:

| Category | Purpose | Examples |
|----------|---------|----------|
| **VO** | API Response | `MessageView`, `MessageTraceView` |
| **DTO** | API Request | `MessageQueryByPage`, `DlqMessageRequest` |
| **Excel** | Bulk Export | `DlqMessageExcelModel` |

**Key Standards**:
```java
// ✅ Provide no-argument constructor
public MessageView() {}

// ✅ Use long for timestamps
private long bornTimestamp;

// ✅ Use BeanUtils.copyProperties() for object conversion
public static MessageView fromMessageExt(MessageExt messageExt) {
    MessageView view = new MessageView();
    BeanUtils.copyProperties(messageExt, view);
    return view;
}

// ✅ Excel models must implement Serializable
public class DlqMessageExcelModel implements Serializable {
    private static final long serialVersionUID = 1L;
}
```

### 2. Service Business Layer

Three types of Service classes:

| Type | Characteristics | Inheritance |
|------|-----------------|-------------|
| Direct Implementation | Simple business logic | implements Service |
| Extends AbstractCommonService | Requires MQAdminExt | extends AbstractCommonService |
| Composition Dependency | Multi-service coordination | Inject multiple Services |

**Example**:
```java
@Service
public class AclServiceImpl extends AbstractCommonService implements AclService {
    protected AclServiceImpl(MQAdminExt mqAdminExt) {
        super(mqAdminExt);
    }
}
```

### 3. Factory Pattern

Uses GenericObjectPool to manage MQAdminExt connections:

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

## Development Standards

### Required Toolstack

| Tool | Version | Purpose |
|------|---------|---------|
| Jackson | Built-in | JSON serialization (JsonUtil wrapper) |
| SLF4J | 3.x | Logging framework |
| Guava | 33.2.0 | Collections, caching, exception handling |
| Commons Pool2 | 2.12.0 | Object pool management |
| AspectJ | 1.9.21 | AOP handling |
| EasyExcel | 4.0.3 | Excel export |

### JSON Processing - JsonUtil

```java
// Object → JSON
String json = JsonUtil.obj2String(obj);

// JSON → Object
MyClass obj = JsonUtil.string2Obj(json, MyClass.class);

// Handle generics
List<String> list = JsonUtil.string2Obj(json, 
    new TypeReference<List<String>>() {});
```

### Logging Standards

```java
// DEBUG: Process tracing
logger.debug("op=queryTopics params={}", JsonUtil.obj2String(params));

// WARN: Warning but not affecting functionality
logger.warn("Refresh cluster failed", e);

// ERROR: Error (must pass exception object)
logger.error("Query failed", e);  // ✅ Second parameter is exception
```

### Caching Standards - Guava LoadingCache

```java
private LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)           // Max 1000 entries
    .concurrencyLevel(10)        // Concurrency level 10
    .build(new CacheLoader<String, List<String>>() {
        @Override
        public List<String> load(String key) throws Exception {
            return loadDataFromRocketMQ(key);
        }
    });

// Usage
List<String> data = cache.get(key);  // Auto-load

// Manual refresh
cache.refresh(key);

// Manual evict
cache.invalidate(key);
```

### Parameter Validation - Preconditions

```java
@PostMapping("/create")
public void create(@RequestBody TopicConfig config) {
    // ✅ Must validate all parameters
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

## Development Red Lines (Prohibited)

### ❌ Exception Handling

```java
// ❌ Forbidden: Swallow exception stack trace
logger.error("Failed");  // Missing exception object

// ✅ Correct
logger.error("Failed", e);  // Pass exception object
```

### ❌ Resource Management

```java
// ❌ Forbidden: Don't return connection
MQAdminExt admin = pool.borrowObject();
admin.queryTopic(topic);
// No return! Connection leak

// ✅ Correct: Auto-managed by AOP
```

### ❌ Null Pointer Checks

```java
// ❌ Forbidden: Don't check null
List<String> topics = getTopics();
for (String topic : topics) {  // NPE if null
    process(topic);
}

// ✅ Correct
if (CollectionUtils.isEmpty(topics)) {
    return;
}
```

### ❌ Character Encoding

```java
// ❌ Forbidden: Use default encoding
String str = new String(bytes);

// ✅ Correct: Explicitly specify UTF-8
String str = new String(bytes, Charsets.UTF_8);
```

### ❌ Concurrency

```java
// ❌ Forbidden: Non-thread-safe HashMap
private Map<String, String> cache = new HashMap<>();

// ✅ Correct: Use ConcurrentHashMap
private Map<String, String> cache = new ConcurrentHashMap<>();
```

---

## Code Review Checklist

Before submitting code, self-check:

- [ ] Is exception object passed to exception logging?
- [ ] Are null pointer checks present?
- [ ] Is UTF-8 encoding explicitly used?
- [ ] Do cache configs include concurrency level?
- [ ] Are resources released in finally blocks?
- [ ] Does Service layer depend only on interfaces?
- [ ] Are parameters validated in Controller layer?

---

## Contributing

1.  Fork the repository
2.  Create a `feature/xxx` branch
3.  Commit your changes (follow the standards above)
4.  Submit a Pull Request

### Submission Requirements

- Follow development standards in [CLAUDE.md](./CLAUDE.md)
- Exception handling must be complete (including logging)
- Add necessary code comments
- Pass the code review checklist

---

## Quick Links

- 📘 [Complete Development Standards - CLAUDE.md](./CLAUDE.md)
- 🏗️ [Project Architecture Design]()
- 📚 [API Documentation]()
- 🐛 [FAQ]()

---

**Version**: 2.0.1-SNAPSHOT  
**Java**: 21  
**Spring Boot**: 3.3.8  
**RocketMQ**: 5.1.0  

For more details, refer to [CLAUDE.md](./CLAUDE.md) to understand complete development standards, toolstack, and development red lines.
