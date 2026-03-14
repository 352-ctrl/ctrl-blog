# 第一阶段：构建环境
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
# 优化构建缓存：先复制 pom.xml 下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 复制源代码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行环境
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 设置时区为亚洲/上海
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 从 builder 阶段复制构建好的 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露 Spring Boot 默认端口
EXPOSE 8080

# 启动参数，支持动态传入环境变量
ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]