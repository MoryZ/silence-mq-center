# RocketMQ Console 改进方案 - 依赖配置

## 📦 需要添加的依赖

### pom.xml 配置

```xml
<!-- 现有依赖中可能已经有这个，如果没有请添加 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.11.1</version>
</dependency>

<!-- 可选：用于监控和指标收集 -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
    <version>1.10.0</version>
    <optional>true</optional>
</dependency>

<!-- 可选：用于异步操作 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <optional>true</optional>
</dependency>
```

## 🔧 Maven 命令

### 检查依赖冲突

```bash
mvn dependency:tree | grep commons-pool2
```

### 更新依赖

```bash
mvn clean dependency:resolve
```

## ✅ 依赖检查清单

- [ ] commons-pool2 已添加到 pom.xml
- [ ] 与现有依赖没有版本冲突
- [ ] 项目可以成功编译
- [ ] 单元测试可以运行

## 🚀 配置检查

### 1. 创建 pool 子包

```
src/main/java/com/old/silence/mq/center/domain/service/
├── pool/
│   ├── MQClientConnectionPool.java
│   └── MQProducerFactory.java
└── template/
    └── RocketMQOperationTemplate.java
```

### 2. 验证 Bean 自动装配

```java
@SpringBootTest
public class PoolConfigTest {
    @Autowired
    private MQClientConnectionPool pool;
    
    @Test
    public void testPoolInitialization() {
        assertNotNull(pool);
        assertFalse(pool.getPoolStats().isEmpty());
    }
}
```

### 3. 检查 Spring Boot 自动配置

```
src/main/resources/META-INF/spring.factories
// 添加（如果使用自定义自动配置）
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.old.silence.mq.center.config.PoolAutoConfiguration
```

---

## 📋 项目结构

改进完成后的项目结构：

```
silence-mq-center/
├── src/main/java/com/old/silence/mq/center/
│   ├── api/
│   │   └── *.Controller.java          # API 层（保持不变）
│   ├── domain/
│   │   ├── model/                    # 数据模型（保持不变）
│   │   ├── service/
│   │   │   ├── *.Service.java        # Service 接口（保持不变）
│   │   │   ├── impl/
│   │   │   │   └── *.ServiceImpl.java # Service 实现（逐步改进）
│   │   │   ├── client/               # Client 封装（保持不变）
│   │   │   ├── pool/                 # ✨ 新增：连接池
│   │   │   │   ├── MQClientConnectionPool.java
│   │   │   │   └── MQProducerFactory.java
│   │   │   └── template/             # ✨ 新增：操作模板
│   │   │       └── RocketMQOperationTemplate.java
│   │   ├── exception/                # 异常处理（保持不变）
│   │   ├── factory/                  # 工厂类（保持不变）
│   │   ├── task/                     # 定时任务（保持不变）
│   │   └── util/                     # 工具类（保持不变）
│   ├── aspect/                       # AOP 切面（保持不变）
│   └── MqCenterApplication.java      # 启动类（保持不变）
├── src/main/resources/
│   ├── application.yml               # 应用配置（新增池配置）
│   └── ...
├── pom.xml                           # ✨ 修改：添加 commons-pool2
├── CLAUDE.md                         # 开发规范
├── MODERNIZATION_GUIDE.md            # ✨ 新增：改进指南
├── ARCHITECTURE_ANALYSIS.md          # ✨ 新增：详细分析
├── IMPROVEMENT_EXAMPLES.md           # ✨ 新增：代码示例
└── README.md
```

---

## 🧪 测试清单

### 单元测试

```java
@SpringBootTest
public class MQClientConnectionPoolTest {
    
    @Test
    public void testBorrowAndReturn() throws Exception {
        // 测试从池中借用和归还
    }
    
    @Test
    public void testPoolExhaustion() throws Exception {
        // 测试池满时的行为
    }
    
    @Test
    public void testConnectionValidation() throws Exception {
        // 测试连接验证机制
    }
}
```

### 性能测试

```bash
# 运行基准测试（改进前后对比）
mvn clean test -Dtest=PerformanceBenchmarkTest
```

### 集成测试

```bash
# 确保改进不会破坏现有功能
mvn clean test -Dgroups=integration
```

---

## 📊 验收标准

- [ ] 所有单元测试通过
- [ ] 集成测试通过
- [ ] 性能测试满足预期（5倍以上提升）
- [ ] 代码审查通过
- [ ] 文档完整
- [ ] 灰度上线 24 小时无问题

---

## 🔗 参考文档

- [Apache Commons Pool2](https://commons.apache.org/proper/commons-pool/)
- [RocketMQ Admin Tool API](https://github.com/apache/rocketmq/wiki/RMQ4CPP_ADMIN_TOOL)
- [Spring Boot 连接池最佳实践](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
