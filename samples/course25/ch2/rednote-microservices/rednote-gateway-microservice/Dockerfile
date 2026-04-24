# 指定基础镜像
FROM openjdk:24-ea-17-slim

# 创建目录，作为工作目录
RUN mkdir -p /rednote-gateway-microservice
WORKDIR /rednote-gateway-microservice

# 将文件复制到镜像中
COPY ./target/rednote-gateway-microservice-0.0.1-SNAPSHOT.jar app.jar

# 设置TZ时区
ENV TZ=Asia/Shanghai

# 声明要暴露的端口
EXPOSE 8080

# 启动应用
CMD ["java", "-jar", "app.jar"]