# 1. 使用 JDK 21 的 Alpine 镜像
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 2. 创建非 root 用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 3. 复制 JAR 包
COPY --chown=appuser:appgroup target/silence-mq-center-2.0.1-SNAPSHOT.jar app.jar

# 4. 设置环境变量默认值 (使用方案二：host.docker.internal)
# 这些变量名需要对应你 application-dev.yml 中的配置项
ENV SPRING_PROFILES_ACTIVE=dev \
    NACOS_SERVER_ADDR=host.docker.internal:8848 \
    DB_URL="jdbc:mysql://host.docker.internal:3306/silence-platform?useSSL=false"

# 5. 切换用户
USER appuser

# 6. 暴露端口
EXPOSE 8099

# 7. 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8099/actuator/health || exit 1

# 8. 启动命令
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "app.jar"]