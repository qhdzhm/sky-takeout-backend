# 多阶段构建
FROM openjdk:17-jdk-slim as builder

# 安装Maven
RUN apt-get update && apt-get install -y maven

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 构建应用
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jre-slim

WORKDIR /app

# 复制jar文件
COPY --from=builder /app/sky-server/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /opt/sky-takeout/logs

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
