# 第一阶段：Node 构建环境
FROM node:22-alpine AS builder
WORKDIR /app
# 启用 corepack 以支持 pnpm/yarn (如果使用 npm 则不需要)
# RUN corepack enable
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 第二阶段：Nginx 运行环境
FROM nginx:alpine
# 移除默认的 Nginx 静态文件
RUN rm -rf /usr/share/nginx/html/*
# 复制 Vite 构建输出的 dist 文件夹到 Nginx 静态目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制自定义的 Nginx 配置文件 (解决 Vue Router History 模式刷新 404 的问题)
# 请在前端目录下创建一个 nginx.conf 文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]