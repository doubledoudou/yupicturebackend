# 使用官方的 Maven 镜像进行构建阶段
FROM maven:3.8.6-openjdk-8-slim AS build

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和源码
COPY pom.xml .
COPY src ./src

# 构建项目，跳过测试以加快构建速度
RUN mvn clean package -DskipTests

# 运行阶段 - 使用较小的 JDK 镜像
FROM openjdk:8-jre-slim

# 安装必要的工具（如 curl 用于健康检查）
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 从构建阶段复制生成的 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露应用端口
EXPOSE 8123

# 添加卷用于日志等数据持久化
VOLUME ["/tmp", "/logs"]

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
