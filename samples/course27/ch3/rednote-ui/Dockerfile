# 基础镜像
FROM node:22.17.0-alpine AS builder

# 工作目录
WORKDIR /app

# 复制项目依赖包
COPY package.json ./
COPY package-lock.json ./

# 安装依赖包
RUN npm install

# 复制项目源代码
COPY . .

# 构建项目
RUN npm run build

# 创建镜像
FROM nginx:1.29-alpine

# 复制构建好的项目
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动服务
CMD ["nginx", "-g", "daemon off;"]