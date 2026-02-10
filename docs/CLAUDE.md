# CLAUDE.md - RocketMQ Console 项目开发规范

## 项目概述

本项目是一个基于 Spring Boot 的 RocketMQ 管理控制台，提供消息队列的监控、管理和运维功能。

## 架构模式

### 分层架构

项目采用经典的三层架构模式：

#### 1. Controller层（API层）
- **职责**：处理HTTP请求，参数校验，调用Service层，返回响应
- **命名规范**：`XxxController`
- **路径规范**：`/api/v1/{module}`
- **示例**：[ConsumerController](../src/main/java/com/old/silence/mq/center/api/ConsumerController.java)

**最佳实践**：
```java
@RestController
@RequestMapping("/api/v1/consumer")
public class ConsumerController {
    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping(value = "/groupList")
    public List<GroupConsumeInfo> list(@RequestParam(required = false) boolean skipSysGroup, String address) {
        return consumerService.queryGroupList(skipSysGroup, address);
    }
}
```

**关键点**：
- 使用构造器注入依赖（推荐），避免 `@Autowired` 字段注入
- 方法返回具体类型，避免返回 `Object`
- 使用 `@RequestParam` 明确指定参数是否必填
- 使用 `@RequestBody` 处理复杂请求体

#### 2. Service层（业务逻辑层）
- **职责**：封装业务逻辑，调用MQAdminExt等客户端API，处理异常
- **命名规范**：`XxxService` (接口) + `XxxServiceImpl` (实现)
- **示例**：[ConsumerServiceImpl](../src/main/java/com/old/silence/mq/center/domain/service/impl/ConsumerServiceImpl.java)

**最佳实践**：
```java
@Service
public class ConsumerServiceImpl extends AbstractCommonService implements ConsumerService {
    private final Logger logger = LoggerFactory.getLogger(ConsumerServiceImpl.class);
    private final ProxyAdmin proxyAdmin;
    private final RMQConfigure configure;
    private final ClusterInfoService clusterInfoService;

    protected ConsumerServiceImpl(MQAdminExt mqAdminExt, ProxyAdmin proxyAdmin, 
                                  RMQConfigure configure, ClusterInfoService clusterInfoService) {
        super(mqAdminExt);
        this.proxyAdmin = proxyAdmin;
        this.configure = configure;
        this.clusterInfoService = clusterInfoService;
    }

    @Override
    public List<GroupConsumeInfo> queryGroupList(boolean skipSysGroup, String address) {
        // 业务逻辑实现
    }
}
```

**关键点**：
- 继承 `AbstractCommonService` 获取通用能力
- 使用 `protected` 构造器注入所有依赖
- 添加详细的日志记录
- 合理使用缓存机制（如 [`LoadingCache`](../src/main/java/com/old/silence/mq/center/domain/service/impl/DashboardCollectServiceImpl.java)）

#### 3. Client层（客户端封装）
- **职责**：封装RocketMQ Admin API，提供统一的调用接口
- **示例**：[MQAdminExtImpl](../src/main/java/com/old/silence/mq/center/domain/service/client/MQAdminExtImpl.java)

**最佳实践**：
- 实现 `MQAdminExt` 接口
- 未实现的方法抛出 `UnsupportedOperationException`
- 添加适当的异常转换和日志

## 开发规范

### 1. 命名规范

#### 类命名
- Controller: `{模块名}Controller`
- Service接口: `{模块名}Service`
- Service实现: `{模块名}ServiceImpl`
- Model/DTO: 描述性名称，如 `GroupConsumeInfo`, `MessageView`

#### 方法命名
- 查询单个: `get{Entity}`, `query{Entity}`
- 查询列表: `list{Entity}`, `query{Entity}List`
- 刷新: `refresh{Entity}`
- 创建/更新: `createOrUpdate{Entity}`
- 删除: `delete{Entity}`

#### 变量命名
- 使用有意义的名称，避免单字母变量（循环除外）
- 集合类型使用复数形式，如 `brokerList`, `topicMap`
- 布尔类型使用 `is/has/can` 前缀

### 2. 注解使用规范

#### Controller层
```java
@RestController
@RequestMapping("/api/v1/module")
public class ModuleController {
    
    @GetMapping(value = "/query")
    public Result query(@RequestParam String param) { }
    
    @PostMapping(value = "/create")
    public Result create(@RequestBody Request request) { }
}
```

#### Service层
```java
@Service
public class ModuleServiceImpl implements ModuleService {
    // 实现逻辑
}
```

### 3. 异常处理规范

**原则**：
1. **禁止吞掉异常**：必须记录日志或重新抛出
2. **使用自定义异常**：如 [`ServiceException`](../src/main/java/com/old/silence/mq/center/exception/ServiceException.java)
3. **异常信息要详细**：包含足够的上下文信息

**正确示例**：
```java
try {
    MessageExt messageExt = mqAdminExt.viewMessage(topic, msgId);
} catch (Exception e) {
    throw new ServiceException(-1, String.format("Failed to query message by Id: %s", msgId));
}
```

**错误示例**：
```java
// ❌ 禁止：吞掉异常
try {
    // ...
} catch (Exception e) {
    // 什么都不做
}

// ❌ 禁止：只打印日志不抛出
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();
}
```

### 4. 日志规范

**日志级别**：
- `ERROR`: 系统错误，需要立即处理
- `WARN`: 警告信息，可能影响功能
- `INFO`: 关键业务流程信息
- `DEBUG`: 详细调试信息

**最佳实践**：
```java
// ✅ 使用参数化日志
logger.info("op=look resetOffsetRequest:{}", JsonUtil.obj2String(resetOffsetRequest));

// ✅ 记录详细上下文
logger.debug("Broker Collected Data in memory = {}", JsonUtil.obj2String(map));

// ❌ 避免字符串拼接
logger.info("User: " + userName + " executed action: " + action);
```

### 5. 并发处理规范

本项目使用线程池处理并发任务，参考 [`ConsumerServiceImpl`](../src/main/java/com/old/silence/mq/center/domain/service/impl/ConsumerServiceImpl.java)：

```java
private ExecutorService executorService;

@Override
public void afterPropertiesSet() {
    executorService = new ThreadPoolExecutor(
        10, 10, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ConsumerServiceThread_" + threadIndex.incrementAndGet());
            }
        }
    );
}

@Override
public void destroy() {
    if (executorService != null) {
        executorService.shutdown();
    }
}
```

**关键点**：
- 实现 `InitializingBean` 和 `DisposableBean` 管理线程池生命周期
- 使用有意义的线程名称便于调试
- 应用关闭时优雅关闭线程池

## 常用工具栈

### 核心依赖

#### 1. RocketMQ Admin API
```java
// 使用 MQAdminExt 管理 RocketMQ
private final MQAdminExt mqAdminExt;
ClusterInfo clusterInfo = mqAdminExt.examineBrokerClusterInfo();
```

#### 2. Google Guava
```java
// Maps 工具类
Map<String, Object> resultMap = Maps.newHashMap();

// Lists 工具类
List<String> list = Lists.newArrayList();

// LoadingCache 缓存
LoadingCache<String, List<String>> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(new CacheLoader<String, List<String>>() {
        @Override
        public List<String> load(String key) {
            return Collections.emptyList();
        }
    });

// Throwables 异常处理
Throwables.throwIfUnchecked(e);
```

#### 3. Jackson (JSON处理)
```java
// 通过 JsonUtil 工具类统一处理
String json = JsonUtil.obj2String(object);
Object obj = JsonUtil.str2Obj(json, Object.class);
```

#### 4. Commons Collections
```java
// 集合判空
CollectionUtils.isNotEmpty(list)
```

#### 5. Commons Lang
```java
// 字符串判空
StringUtils.isNotEmpty(str)
```

### 工具类使用

#### JsonUtil
参考：[JsonUtil](../src/main/java/com/old/silence/mq/center/util/JsonUtil.java)

```java
// 对象转JSON
String json = JsonUtil.obj2String(object);

// JSON转对象
MyObject obj = JsonUtil.str2Obj(json, MyObject.class);

// JSON转集合
Map<String, Object> map = JsonUtil.str2Obj(json, new TypeReference<Map<String, Object>>() {});
```

## 开发红线

### 1. 性能相关

#### ❌ 禁止在循环中查询数据库/远程服务
```java
// ❌ 错误示例
for (String topic : topicList) {
    TopicRouteData route = mqAdminExt.examineTopicRouteInfo(topic);
}

// ✅ 正确做法：批量查询或使用缓存
```

#### ❌ 禁止无限制的集合大小
```java
// ❌ 错误
new LinkedBlockingQueue<>(); // 无界队列

// ✅ 正确
new LinkedBlockingQueue<>(1000); // 限制大小
```

### 2. 异常处理相关

#### ❌ 禁止吞掉异常
```java
// ❌ 错误
try {
    // ...
} catch (Exception e) {
    // 什么都不做
}

// ✅ 正确
try {
    // ...
} catch (Exception e) {
    logger.error("Error occurred", e);
    throw new ServiceException(-1, "Error message");
}
```

#### ❌ 禁止在循环中捕获异常
```java
// ❌ 错误
for (String item : list) {
    try {
        process(item);
    } catch (Exception e) {
        // 继续循环，可能导致大量异常被忽略
    }
}
```

### 3. 资源管理相关

#### ✅ 必须关闭资源
```java
// 实现 DisposableBean 或使用 @PreDestroy
@Override
public void destroy() {
    if (executorService != null) {
        executorService.shutdown();
    }
}
```

#### ✅ 文件操作必须处理异常
```java
try {
    Files.createParentDirs(file);
    file.createNewFile();
} catch (IOException e) {
    Throwables.throwIfUnchecked(e);
    throw new RuntimeException(e);
}
```

### 4. 并发相关

#### ✅ 使用线程安全的集合
```java
// ✅ 正确
private final List<GroupConsumeInfo> cacheList = Collections.synchronizedList(new ArrayList<>());
private final Map<String, Object> configMap = new ConcurrentHashMap<>();
```

#### ✅ 使用 volatile 保证可见性
```java
private volatile boolean isCacheBeingBuilt = false;
```

### 5. 日志相关

#### ❌ 禁止使用 System.out/err
```java
// ❌ 错误
System.out.println("debug info");

// ✅ 正确
logger.debug("debug info");
```

#### ❌ 禁止日志中包含敏感信息
```java
// ❌ 错误
logger.info("User password: {}", password);

// ✅ 正确
logger.info("User login, username: {}", username);
```

## 配置管理

### RMQConfigure
参考：项目使用 [`RMQConfigure`](../src/main/java/com/old/silence/mq/center/api/config/RMQConfigure.java) 管理配置

```java
@Component
public class MyService {
    private final RMQConfigure configure;
    
    public MyService(RMQConfigure configure) {
        this.configure = configure;
    }
    
    public void doSomething() {
        String dataPath = configure.getRocketMqDashboardDataPath();
        boolean aclEnabled = configure.isACLEnabled();
    }
}
```

## 数据持久化

### 文件存储
项目使用文件系统存储监控数据和配置：

```java
// 数据文件路径规范
String brokerFile = dataPath + date + ".json";
String topicFile = dataPath + date + "_topic.json";

// 使用 MixAll.string2File 写入
MixAll.string2File(dataStr, filePath);

// 使用 Guava Files 读取
List<String> lines = Files.readLines(file, Charsets.UTF_8);
```

## 定时任务

### 使用 @Scheduled
参考：[DashboardCollectTask](../src/main/java/com/old/silence/mq/center/task/DashboardCollectTask.java)

```java
@Component
public class MyTask {
    
    @Scheduled(cron = "0/5 * * * * ?")
    public void scheduledTask() {
        // 任务逻辑
    }
}
```

**注意**：
- 任务方法必须是无参的
- 使用 cron 表达式精确控制执行时间
- 添加开关控制任务是否执行

## 测试规范

### 单元测试
- 使用 JUnit 5
- Mock 外部依赖
- 测试覆盖率 > 70%

### 接口测试
- 使用 Postman/RestAssured
- 验证正常流程和异常情况

## 代码审查清单

- [ ] 是否遵循分层架构
- [ ] 是否正确处理异常
- [ ] 是否添加必要的日志
- [ ] 是否使用构造器注入
- [ ] 是否有潜在的性能问题
- [ ] 是否有并发安全问题
- [ ] 是否正确关闭资源
- [ ] 是否符合命名规范
- [ ] 是否添加必要的参数校验

## 扩展阅读

- RocketMQ 官方文档：https://rocketmq.apache.org/
- Google Guava 文档：https://github.com/google/guava/wiki
- Spring Boot 最佳实践：https://docs.spring.io/spring-boot/docs/current/reference/html/

---

**文档版本**：1.0  
**最后更新**：2026-02-03