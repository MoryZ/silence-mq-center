# silence-mq-center

> RocketMQ Management Console - Comprehensive monitoring, management and operations for message queues

##  Project Introduction

silence-mq-center is a Spring Boot-based RocketMQ management console that provides comprehensive monitoring, diagnostics, and management features for message queues to operations teams and developers.

### Core Features

-  **Cluster Monitoring**: Real-time monitoring of RocketMQ cluster status, Brokers, and NameServers
-  **Message Querying**: Support for querying messages by topic, message ID, and message content
-  **Consumer Management**: View consumer groups, consumption progress, and reset consumption offsets
-  **Performance Analysis**: Message trace analysis and performance metrics
-  **Operations**: Topic management, message resending, and dead letter queue handling
-  **Access Control**: ACL-based permission management

##  Project Architecture

Implements the classic three-layer architecture pattern:

### Controller Layer (API Layer)
- Handle HTTP requests and parameter validation
- Invoke Service layer for business logic
- Return unified API responses

### Service Layer (Business Logic Layer)
- Encapsulate core business logic
- Call RocketMQ Admin API
- Handle exceptions and errors

### Client Layer (Client Encapsulation)
- Wrap RocketMQ Admin API
- Provide unified interface for API calls
- Handle API exception conversion

See [CLAUDE.md](docs/CLAUDE.md) for complete development specifications

##  Tech Stack

| Technology | Version | Description |
|-----------|---------|-------------|
| Spring Boot | 2.x | Backend framework |
| RocketMQ | 4.x+ | Message queue engine |
| MyBatis-Plus | Latest | ORM framework |
| Guava | Latest | Google utilities library |
| Jackson | 2.x | JSON serialization |
| Hutool | Latest | Java utilities |
| SLF4J + Logback | Latest | Logging framework |

##  Quick Start

### Prerequisites

- JDK 8 or higher
- Maven 3.6+
- RocketMQ 4.x or higher

### Build

``bash
# Clone the repository
git clone https://gitee.com/your-org/silence-mq-center.git
cd silence-mq-center

# Build with Maven
mvn clean package -DskipTests

# JAR file will be in the target directory
``

### Run the Application

``bash
# Method 1: Run JAR directly
java -jar target/silence-mq-center.jar

# Method 2: Run with Maven
mvn spring-boot:run

# Method 3: Run with Docker
docker build -t silence-mq-center .
docker run -p 8080:8080 silence-mq-center
``

### Configuration

Edit `src/main/resources/application.yml`:

``yaml
spring:
  application:
    name: silence-mq-center
  
  server:
    port: 8080

rocketmq:
  namesrv-addr: localhost:9876  # NameServer address
  acl-enabled: false             # Enable ACL
  dashboard-data-path: /data/    # Data storage path
``

##  Development Guide

### Project Structure

``
src/main/java/com/old/silence/mq/center/
 api/                      # Controller layer
    ConsumerController.java
    TopicController.java
    ...
 domain/
    model/               # Data models/DTOs
    service/             # Service layer
       ConsumerService.java
       impl/
    service/client/      # Client layer
 exception/               # Exception handling
 factory/                 # Object pool factory
 task/                    # Scheduled tasks
 util/                    # Utilities
 aspect/                  # AOP aspects
 MqCenterApplication.java # Entry point
``

### Development Specifications

See [CLAUDE.md](docs/CLAUDE.md) for details, including:

- ✅ **Naming Conventions**: Rules for classes, methods, and variables
- ✅ **Layered Architecture**: Controller/Service/DAO responsibilities
- ✅ **Exception Handling**: No swallowing exceptions, use custom exceptions
- ✅ **Logging Standards**: Use SLF4J with parameterized logging
- ✅ **Performance Standards**: No database queries in loops, timely resource cleanup
- ✅ **Thread Safety**: ThreadPool usage, volatile keyword

### API Documentation

After starting the application, visit `http://localhost:8080/swagger-ui.html` for API documentation

Common API endpoints:

- `GET /api/v1/cluster` - Get cluster information
- `GET /api/v1/topic/list` - Get topic list
- `GET /api/v1/consumer/groupList` - Get consumer group list
- `GET /api/v1/message/queryById` - Query message by ID

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Review Checklist

Before submitting a PR, ensure:

- [ ] Code follows [CLAUDE.md](docs/CLAUDE.md) development specifications
- [ ] Added appropriate logging and exception handling
- [ ] Includes necessary unit tests
- [ ] Updated relevant documentation
- [ ] No DEBUG code or System.out statements

## 📝 License

This project is licensed under the MIT License

## 📧 Contact

- Issue Tracker: [Issues](https://gitee.com/your-org/silence-mq-center/issues)
- Discussions: [Discussions](https://gitee.com/your-org/silence-mq-center/discussions)

## 🙏 Acknowledgments

Thanks to the RocketMQ open source community and all contributors

---

For more details, please refer to [CLAUDE.md](docs/CLAUDE.md)
