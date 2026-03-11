# CLAUDE.md - 项目开发规范

> 本文档提取自 **Silence MQ Center** 项目的最佳实践，用于指导 AI 辅助开发和代码审查。

## 目录
1. [项目概览](#项目概览)
2. [架构模式](#架构模式)
3. [数据模型规范](#数据模型规范)
4. [常用工具栈](#常用工具栈)
5. [开发红线](#开发红线)
6. [代码规范](#代码规范)

---

## 项目概览

**项目名称**: Silence MQ Center (RocketMQ 管理中心)  
**技术栈**: Java 21 + Spring Boot 3.3.8  
**主要职责**: RocketMQ 集群管理、监控、消息追踪、消费管理等运维功能

### 核心特点
- **非数据库应用**: 主要通过 RocketMQ 管理客户端与消息队列交互
- **对象池模式**: 使用 GenericObjectPool 管理 MQAdminExt 连接
- **AOP 框架**: 通过 AspectJ 实现方法拦截和资源管理
- **缓存机制**: 使用 Guava LoadingCache 缓存集群/主题信息

---

## 架构模式

### 1. 三层架构 (Controller → Service → 工厂)

```
API 层 (Controller)
    ↓
业务层 (Service/ServiceImpl)
    ↓
工厂层 (Factory/客户端)
    ↓
RocketMQ 管理客户端 (MQAdminExt)
```

#### Controller 层职责
- **输入验证**: 使用 Guava `Preconditions.checkArgument()` 验证参数
- **API 暴露**: RESTful 接口定义，使用 `@RestController` + `@RequestMapping`
- **参数校验示例**:

```java
@PostMapping("/add")
public Boolean addAclConfig(@RequestBody PlainAccessConfig config) {
    // 必须验证所有输入参数
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(config.getAccessKey()), 
        "accessKey is null"
    );
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(config.getSecretKey()), 
        "secretKey is null"
    );
    aclService.addAclConfig(config);
    return true;
}
```

**验证方式（优先级）**:
1. `Preconditions.checkArgument()` - 最常用，参数不合法
2. `Preconditions.checkNotNull()` - 空指针检查
3. `StringUtils.isNotEmpty()` - 字符串检查
4. `CollectionUtils.isNotEmpty()` - 集合检查

#### Service 层职责  
- **业务逻辑**: 封装核心业务流程
- **异常处理**: 使用统一的 `ServiceException`
- **资源管理**: 通过工厂获取 MQAdminExt
- **缓存管理**: 维护业务相关的缓存

**Service 分类**:

| 类型 | 特点 | 示例 |
|------|------|------|
| 直接实现 | 简单业务逻辑 | `ClusterServiceImpl` |
| 继承 AbstractCommonService | 需要使用 MQAdminExt | `AclServiceImpl`, `TopicServiceImpl` |
| 组合依赖 | 协调多个 Service | `DashboardServiceImpl` |

#### Factory 层职责
- **连接管理**: GenericObjectPool 管理 MQAdminExt 实例
- **对象复用**: 减少连接创建开销
- **异常处理**: 连接异常时的重试逻辑

**工厂类结构**:
```java
@Configuration
public class MQAdminFactory {
    // 创建连接池
    @Bean
    public GenericObjectPool<MQAdminExt> mqAdminExtPool() {
        return new GenericObjectPool<>(
            new MQAdminPooledObjectFactory(...)
        );
    }
}

// 使用方式
@Service
public class DlqMessageServiceImpl implements DlqMessageService {
    private final MQAdminExt mqAdminExt;  // 直接注入
    
    public DlqMessageServiceImpl(MQAdminExt mqAdminExt) {
        this.mqAdminExt = mqAdminExt;
    }
}
```

### 2. AOP 框架 (AspectJ 拦截)

所有 MQAdminExt 调用通过 AOP 拦截，自动处理连接池的获取和归还：

```java
@Aspect
@Service
public class MQAdminAspect {
    private final GenericObjectPool<MQAdminExt> mqAdminExtPool;
    
    @Around("execution(* com.old.silence.mq.center.domain.service.client.MQAdminExtImpl..*(..))")
    public Object aroundMQAdminMethod(ProceedingJoinPoint joinPoint) {
        MQAdminExt mqAdminExt = mqAdminExtPool.borrowObject();
        try {
            return joinPoint.proceed();  // 业务方法
        } finally {
            mqAdminExtPool.returnObject(mqAdminExt);  // 确保归还
        }
    }
}
```

**关键点**:
- ✅ 自动的 try-finally 确保连接归还
- ✅ 异常发生时也会正确归还
- ✅ 业务代码中不需要手动管理连接

---

## 数据模型规范

### 1. 模型类设计

由于是非数据库应用，模型主要分为三类：

| 分类 | 职责 | 示例 |
|------|------|------|
| **VO (View Object)** | API 响应模型 | `MessageView`, `MessageTraceView` |
| **DTO (Data Transfer Object)** | API 请求/转换模型 | `MessageQueryByPage`, `DlqMessageRequest` |
| **Excel Model** | 批量导出模型 | `DlqMessageExcelModel` |

### 2. 模型类最佳实践

✅ **必须遵守**:

```java
public class MessageTraceView {
    // 1. 所有字段应该是 public getter/setter（JavaBean 规范）
    private String requestId;
    private String msgId;
    private long timeStamp;
    
    // 2. 提供无参构造器
    public MessageTraceView() {
    }
    
    // 3. 日期字段使用 long（毫秒时间戳）
    private long timeStamp;
    
    // 4. 枚举值转字符串用 .name() 或自定义枚举的 getStatus()
    private String status;  // 值来自 MessageTraceStatusEnum.SUCCESS.getStatus()
    
    // 5. 数组/集合字段处理
    private Map<String, String> properties;  // 存储消息标签
    
    // 6. 使用静态工厂方法进行对象转换
    public static MessageTraceView decodeFromTraceTransData(String key, MessageExt messageExt) {
        MessageTraceView view = new MessageTraceView();
        // ... 字段赋值
        return view;
    }
}
```

### 3. 对象复制规则

```java
// ✅ 推荐: Spring 的 BeanUtils
public static MessageView fromMessageExt(MessageExt messageExt) {
    MessageView messageView = new MessageView();
    BeanUtils.copyProperties(messageExt, messageView);
    
    // ✅ 特殊字段单独处理（如字节数组转字符串）
    if (messageExt.getBody() != null) {
        messageView.setMessageBody(
            new String(messageExt.getBody(), Charsets.UTF_8)
        );
    }
    return messageView;
}

// ❌ 避免手动逐个赋值（繁琐易出错）
```

### 4. 时间戳处理

```java
public class DlqMessageExcelModel implements Serializable {
    private String bornTimestamp;   // 格式化后的时间字符串
    private String storeTimestamp;
    
    // ✅ 正确的日期格式化方式
    private String format(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return instant.atOffset(ZoneOffset.UTC).format(formatter);
    }
    
    // ✅ 在构造器中调用
    public DlqMessageExcelModel(MessageExt messageExt) {
        this.bornTimestamp = format(messageExt.getBornTimestamp());
        this.storeTimestamp = format(messageExt.getStoreTimestamp());
    }
}
```

### 5. 序列化

```java
// ✅ Excel 导出模型必须实现 Serializable
public class DlqMessageExcelModel implements Serializable {
    private static final long serialVersionUID = 1L;  // 版本号
}

// ✅ ServiceException 也需要
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 9213584003139969215L;
}
```

---

## 常用工具栈

### 1. JSON 处理 (Jackson)

统一使用 `JsonUtil` 工具类：

```java
// 位置: com.old.silence.mq.center.util.JsonUtil

// ✅ 对象 → JSON 字符串
String json = JsonUtil.obj2String(obj);

// ✅ JSON 字符串 → 对象
MyClass obj = JsonUtil.string2Obj(json, MyClass.class);

// ✅ 处理泛型（如 List）
List<String> list = JsonUtil.string2Obj(json, new TypeReference<List<String>>() {});

// ✅ 字节数组处理
byte[] bytes = JsonUtil.obj2Byte(obj);
MyClass obj = JsonUtil.byte2Obj(bytes, MyClass.class);

// ✅ Map 转对象
MyClass obj = JsonUtil.map2Obj(map, MyClass.class);
```

**配置特点**:
- 枚举使用 toString() 方式序列化
- 忽略未知属性 (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES = false)
- 日期格式: `yyyy-MM-dd HH:mm:ss`
- 不序列化 null 和空值 (NON_EMPTY)

### 2. 日志框架 (SLF4J + Logback)

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    
    public void method() {
        // ✅ DEBUG: 最详细，用于开发调试
        logger.debug("op=method params={}", JsonUtil.obj2String(params));
        
        // ✅ INFO: 一般不用（Spring Boot 默认为 INFO 级别）
        // logger.info("Processing started");
        
        // ✅ WARN: 警告但不影响功能
        logger.warn("Refresh cluster info failed", e);
        
        // ✅ ERROR: 错误且需要关注
        logger.error("Failed to query message by Id:{}", msgId, e);
        
        // ✅ 关键：第二参数是异常对象，会自动打印堆栈
        try {
            // ...
        } catch (Exception e) {
            logger.error("op=methodName error", e);  // e 必须传入
        }
    }
}
```

**日志级别选择**:

| 级别 | 何时使用 | 示例 |
|------|---------|------|
| DEBUG | 流程追踪 | 入参、出参、中间结果 |
| WARN | 降级处理 | 缓存刷新失败但有默认值 |
| ERROR | 业务异常 | 数据库连接失败、业务操作失败 |

### 3. 缓存 (Guava LoadingCache)

用于缓存集群拓扑、主题列表等实时性要求不高的数据：

```java
// ✅ 创建缓存（自动加载 + 过期策略）
private LoadingCache<String, List<String>> brokerMap = CacheBuilder.newBuilder()
    .maximumSize(1000)                    // 最多缓存 1000 个 key
    .concurrencyLevel(10)                 // 并发度：10 个线程
    .recordStats()                        // 记录缓存统计信息
    .ticker(Ticker.systemTicker())        // 使用系统时钟
    .removalListener(notification -> {    // 元素移除监听
        logger.debug("Cache entry removed: {}", notification.getKey());
    })
    .build(new CacheLoader<String, List<String>>() {
        @Override
        public List<String> load(String key) throws Exception {
            // 缓存未命中时自动加载
            return loadDataFromRocketMQ(key);
        }
    });

// ✅ 使用
List<String> brokers = brokerMap.get("clusterName");  // 自动加载或返回缓存

// ✅ 手动刷新
brokerMap.refresh("clusterName");

// ✅ 手动清除
brokerMap.invalidate("clusterName");
```

### 4. 集合工具 (Guava Collections)

```java
// ✅ 创建 ImmutableSet
Set<String> immutable = ImmutableSet.of("a", "b", "c");

// ✅ Maps 工具
Map<String, String> map = Maps.newHashMap();
Map<String, String> map = Maps.newConcurrentMap();

// ✅ Lists 工具
List<String> list = Lists.newArrayList("a", "b");
List<String> list = Lists.newLinkedList();

// ✅ Sets 工具
Set<String> set = Sets.newHashSet();
Set<String> set = Sets.newConcurrentHashSet();
```

### 5. 文件操作 (Guava Files)

```java
import com.google.common.io.Files;

// ✅ 读文件
String content = Files.toString(file, Charsets.UTF_8);

// ✅ 写文件
Files.write(content, file, Charsets.UTF_8);

// ✅ 读取所有行
List<String> lines = Files.readLines(file, Charsets.UTF_8);
```

### 6. 异常处理 (Guava Throwables)

```java
import com.google.common.base.Throwables;

try {
    // RocketMQ 操作
    return mqAdminExt.examineTopicStats(topic);
} catch (Exception e) {
    // ✅ 如果是 Unchecked 异常，原样抛出；否则包装成 RuntimeException
    Throwables.throwIfUnchecked(e);
    throw new RuntimeException(e);
}
```

### 7. Excel 导出 (EasyExcel)

```java
// 原 Apache POI 已被 EasyExcel 4.0.3 取代
// 使用场景：导出 DLQ 失败消息列表

public List<DlqMessageExcelModel> exportDlqMessages(String topic) {
    // 返回 List<DlqMessageExcelModel>，由显示层负责 Excel 生成
    return dlqService.getDlqMessages(topic).stream()
        .map(DlqMessageExcelModel::new)
        .collect(Collectors.toList());
}
```

### 8. 连接池 (Apache Commons Pool2)

```java
import org.apache.commons.pool2.impl.GenericObjectPool;

// ✅ 配置
GenericObjectPool<MQAdminExt> pool = new GenericObjectPool<>(
    new MQAdminPooledObjectFactory(...),
    new GenericObjectPoolConfig<>()
        .setMaxTotal(10)        // 最大总数
        .setMaxIdle(5)          // 最大空闲
        .setMinIdle(1)          // 最小空闲
);

// ✅ 借用对象
MQAdminExt admin = pool.borrowObject();
try {
    admin.queryTopicStatsInfo(topic);
} finally {
    pool.returnObject(admin);  // 必须归还
}
```

### 9. 其他常用库

| 库 | 版本 | 用途 |
|----|------|------|
| commons-lang3 | 3.17.0 | `StringUtils.isNotEmpty()`, `StringUtils.join()` 等 |
| commons-collections4 | 4.4 | `CollectionUtils.isEmpty()`, `Iterables.isEmpty()` 等 |
| commons-io | 2.18.0 | 文件 I/O 操作 |
| fastjson | 2.0.0+ | 高性能 JSON（与 Jackson 共存） |
| hutool | - | 未直接依赖，但常用 |

---

## 开发红线

### ❌ 禁止事项（违者代码审查不通过）

#### 1. 异常处理相关

```java
// ❌ 禁止：吞掉异常堆栈
try {
    doSomething();
} catch (Exception e) {
    logger.error("Something wrong");  // 缺少 e，看不到堆栈
}

// ✅ 正确
try {
    doSomething();
} catch (Exception e) {
    logger.error("Something wrong", e);  // 传入异常对象
}

// ❌ 禁止：直接返回 null
try {
    return queryMessage(msgId);
} catch (Exception e) {
    return null;  // 调用方无法判断返回 null 的原因
}

// ✅ 正确：抛出自定义异常或返回 Collections.emptyList()
try {
    return queryMessage(msgId);
} catch (Exception e) {
    logger.error("Query message failed", e);
    return Collections.emptyList();
}
```

#### 2. 数据库查询相关

> 当项目集成数据库后，必须遵守

```java
// ❌ 禁止：在循环中查库
List<String> topics = getTopicList();
for (String topic : topics) {
    TopicStats stats = queryFromDB(topic);  // 大问题！N+1 查询
    process(stats);
}

// ✅ 正确：批量查询
List<String> topics = getTopicList();
List<TopicStats> statsList = queryFromDB_Batch(topics);
for (TopicStats stats : statsList) {
    process(stats);
}

// ❌ 禁止：不使用参数化查询
String sql = "SELECT * FROM topic WHERE name = '" + topicName + "'";
query(sql);  // SQL 注入漏洞！

// ✅ 正确：参数化查询
String sql = "SELECT * FROM topic WHERE name = ?";
query(sql, topicName);
```

#### 3. 资源管理相关

```java
// ❌ 禁止：不释放连接
MQAdminExt admin = pool.borrowObject();
admin.queryTopic(topic);
// 没有归还！导致连接泄漏

// ✅ 正确：try-finally 或 try-with-resources 确保释放
MQAdminExt admin = pool.borrowObject();
try {
    return admin.queryTopic(topic);
} finally {
    pool.returnObject(admin);
}

// ✅ 或通过 AOP 自动管理（已配置）
```

#### 4. 空指针检查相关

```java
// ❌ 禁止：不检查 null
public void procesMessage(MessageExt msg) {
    String body = new String(msg.getBody(), Charsets.UTF_8);  // msg 可能为 null
}

// ✅ 正确：参数验证
public void procesMessage(MessageExt msg) {
    Preconditions.checkNotNull(msg, "message must not be null");
    if (msg.getBody() == null) {
        logger.warn("Message body is null");
        return;
    }
    String body = new String(msg.getBody(), Charsets.UTF_8);
}

// ❌ 禁止：集合不判空
List<String> topics = queryTopics();
for (String topic : topics) {  // 如果 queryTopics() 返回 null，NPE！
    process(topic);
}

// ✅ 正确：使用 Guava 工具
List<String> topics = queryTopics();
if (CollectionUtils.isEmpty(topics)) {
    return;
}
// 或
List<String> topics = queryTopics();
if (topics == null) {
    topics = Collections.emptyList();
}
```

#### 5. 并发相关

```java
// ❌ 禁止：非线程安全的 HashMap 用在并发场景
private Map<String, String> cache = new HashMap<>();  // 竞态条件！

// ✅ 正确：使用 ConcurrentHashMap
private Map<String, String> cache = new ConcurrentHashMap<>();

// ✅ 或使用 Guava 的并发缓存
private LoadingCache<String, Data> cache = CacheBuilder.newBuilder()
    .concurrencyLevel(10)
    .build(loader);
```

#### 6. 字符编码相关

```java
// ❌ 禁止：使用系统默认编码
String str = new String(bytes);  // 可能在不同系统不兼容

// ✅ 正确：显式指定 UTF-8
String str = new String(bytes, Charsets.UTF_8);
String str = new String(bytes, "UTF-8");
```

#### 7. 日期时间相关

```java
// ❌ 禁止：使用 Date（过时）
public void setTime(Date date) {
    // ...
}

// ✅ 正确：使用 java.time（Java 8+）
public void setTime(long timestamp) {
    // 保存时间戳
}

// ✅ 或使用 LocalDateTime（如果有显示需求）
public String formatTime(long timestamp) {
    Instant instant = Instant.ofEpochMilli(timestamp);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return instant.atOffset(ZoneOffset.UTC).format(formatter);
}
```

#### 8. 输入验证相关

```java
// ❌ 禁止：不验证参数
@PostMapping("/create")
public void create(@RequestBody TopicCreate request) {
    topicService.create(request);  // 没验证 request 的字段
}

// ✅ 正确：Controller 层验证
@PostMapping("/create")
public void create(@RequestBody TopicCreate request) {
    Preconditions.checkArgument(
        StringUtils.isNotEmpty(request.getTopicName()), 
        "Topic name cannot be empty"
    );
    Preconditions.checkArgument(
        request.getQueueNum() > 0, 
        "Queue number must be positive"
    );
    topicService.create(request);
}
```

### 📊 代码审查检查清单

在提交代码前，自检以下项目：

- [ ] 异常捕获时是否传入异常对象用于打印堆栈？
- [ ] 是否有 null 指针检查（参数、返回值、集合）？
- [ ] 是否使用了 UTF-8 编码（字符串转换时）？
- [ ] 如果使用缓存，是否正确配置了并发度和大小？
- [ ] 如果获取了连接/资源，是否在 finally 中释放？
- [ ] Service 层是否只依赖接口而非实现类？
- [ ] 是否使用了 Guava 工具（Collections, Throwables）简化代码？
- [ ] 是否在 Controller 层进行了充分的参数验证？

---

## 代码规范

### 1. 包名结构

```
com.old.silence.mq.center
├── api                      # Controller 层
│   ├── AclController.java
│   ├── TopicController.java
│   └── config/              # 配置相关
│       ├── CollectExecutorConfig.java
│       └── RMQConfigure.java
├── domain                   # 业务核心
│   ├── model/               # 数据模型
│   │   ├── MessageView.java
│   │   ├── MessageTraceView.java
│   │   ├── request/         # 请求 DTO
│   │   └── trace/           # 追踪相关模型
│   └── service/             # 业务服务
│       ├── AclService.java  # 接口
│       ├── impl/            # 实现
│       │   ├── AclServiceImpl.java
│       │   └── ...
│       └── client/          # RocketMQ 客户端相关
├── aspect/                  # AOP 切面
│   └── MQAdminAspect.java
├── factory/                 # 工厂模式
│   ├── MQAdminFactory.java
│   └── MQAdminPooledObjectFactory.java
├── task/                    # 后台任务
│   ├── CollectTaskRunnable.java
│   └── DashboardCollectTask.java
├── exception/               # 自定义异常
│   └── ServiceException.java
├── util/                    # 工具类
│   ├── JsonUtil.java
│   ├── MatcherUtil.java
│   ├── MsgTraceDecodeUtil.java
│   └── ...
└── MqCenterApplication.java # 主类
```

### 2. 命名规范

```java
// 接口：使用 Service 后缀
public interface TopicService { }

// 实现类：使用 ServiceImpl 后缀
public class TopicServiceImpl implements TopicService { }

// Controller：使用 Controller 后缀
public class TopicController { }

// 工厂：使用 Factory 后缀
public class MQAdminFactory { }

// 常量：全大写，下划线分隔
private static final String LOG_PREFIX = "[MQ-CENTER] ";
private static final int DEFAULT_TIMEOUT = 5000;

// 私有字段：小驼峰
private String topicName;
private List<String> topicList;

// 静态字段：小驼峰（不必全大写）
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

// 方法参数：小驼峰
public void processTopic(String topicName, int queueNum) { }
```

### 3. 类和方法的组织顺序

```java
public class MyService {
    // 1. 静态常量
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    private static final String CACHE_KEY_PREFIX = "topic:";
    
    // 2. 实例字段（按依赖类型分组）
    private final MQAdminExt mqAdminExt;      // 注入
    private final OtherService otherService;
    private Map<String, Object> cache;        // 本地
    
    // 3. 构造器
    public MyService(MQAdminExt mqAdminExt, OtherService otherService) {
        this.mqAdminExt = mqAdminExt;
        this.otherService = otherService;
    }
    
    // 4. 公开方法（按业务重要性排序）
    public List<Topic> queryTopics() { }
    public TopicStats getTopicStats(String topicName) { }
    
    // 5. 私有方法（按调用关系排序）
    private void loadCache() { }
    private Topic buildTopic(TopicConfig config) { }
}
```

### 4. 注释规范

```java
/**
 * 查询主题统计信息。
 * 
 * 此方法会优先从缓存中读取，如果缓存未命中则从 RocketMQ 查询。
 * 
 * @param topicName 主题名称，不能为空
 * @return 主题统计信息，如果不存在返回 null
 * @throws ServiceException 如果查询失败
 * 
 * @see #queryTopicStats(String)
 */
public TopicStats getTopicStats(String topicName) throws ServiceException {
    // ...
}

// 行注释：用于解释"为什么"而非"是什么"
// ✅ 好
// 使用 synchronized 防止多线程下的缓存重复加载
synchronized (cacheLock) {
    if (cache.get(key) == null) {
        cache.put(key, loadData(key));
    }
}

// ❌ 不好（太明显了）
// 获取缓存
Map data = cache.get(key);
```

### 5. Import 组织顺序

```java
// 1. java 系列
import java.io.*;
import java.util.*;

// 2. javax 系列
import javax.servlet.*;

// 3. 组织外部库（按名称字母序）
import com.alibaba.fastjson.*;
import com.google.common.*;
import org.apache.commons.*;
import org.springframework.*;

// 4. 项目内部
import com.old.silence.mq.center.*;
```

---

## 附录：快速参考

### 常见 Guava 用法速查表

| 需求 | 代码 |
|------|------|
| 创建空列表 | `Lists.newArrayList()` |
| 创建列表（初始化元素） | `Lists.newArrayList("a", "b")` |
| 创建集合 | `Sets.newHashSet()` |
| 创建 Map | `Maps.newHashMap()` |
| 并发 Map | `Maps.newConcurrentMap()` |
| 参数非空 | `Preconditions.checkNotNull(obj)` |
| 参数有效性 | `Preconditions.checkArgument(condition, msg)` |
| 异常处理（保留堆栈） | `Throwables.throwIfUnchecked(e)` |
| 获取异常堆栈 | `Throwables.getStackTraceAsString(e)` |
| 读文件 | `Files.toString(file, Charsets.UTF_8)` |
| 写文件 | `Files.write(content, file, Charsets.UTF_8)` |
| 字符串非空 | `Strings.isNullOrEmpty(str)` |

### Spring 常用注解速查表

| 注解 | 用途 |
|------|------|
| `@RestController` | 声明 REST API 控制器 |
| `@Service` | 声明业务服务类 |
| `@Configuration` | 声明配置类 |
| `@Bean` | 在配置类中声明 Bean |
| `@Autowired` | 自动注入依赖（不推荐）|
| `@PostMapping` | POST 请求映射 |
| `@GetMapping` | GET 请求映射 |
| `@RequestBody` | 请求体自动反序列化 |
| `@RequestParam` | 请求参数解析 |
| `@Aspect` | AOP 切面类 |
| `@Around` | AOP 环绕通知 |
| `@PostConstruct` | Bean 初始化后调用 |

### SLF4J 日志参数化速查表

```java
// 支持占位符 {} 和 可变参数
logger.debug("Topic: {}, Queue: {}", topicName, queueNum);
logger.warn("Failed for topics: {}", topicList);
logger.error("Error occurred", exception);  // 最后一个参数是异常
```

---

**文档版本**: 1.0  
**最后更新**: 2026-03-11  
**作者**: AI Code Generator  

> 本文档旨在保持代码质量和一致性。在审查代码或编写新功能时，请参考本文档。
