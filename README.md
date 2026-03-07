# 🚀 CtrlBlog

![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.9-green.svg)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)

**CtrlBlog** 是一个基于 Spring Boot 3 和 Vue 3 的前后端分离的现代化博客系统。系统不仅提供了丰富的文章创作与互动功能，还集成了 AI 辅助、定时任务调度、敏感词过滤及详细的系统监控与日志管理，致力于为开发者提供一个“完全掌控”的个人内容发布平台。

## ✨ 核心特性

* **📝 沉浸式创作**：支持 Markdown 高级编辑（MdEditorV3），支持 Emoji 表情、图片裁剪上传。
* **🤖 AI 赋能**：集成 Spring AI (OpenAI)，支持 AI 自动生成文章摘要等智能化体验。
* **💬 丰富互动**：支持文章点赞、收藏、多级评论（带 Emoji 支持）、以及文章阅读量统计。
* **🛡️ 安全与风控**：内置图形验证码、敏感词过滤（DFA算法）、恶意内容举报机制，保障社区健康。
* **📊 数据与监控**：集成 Echarts 图表展示每日访问量；内置操作日志、登录日志记录；集成 IP2Region 实现 IP 属地定位。
* **⚙️ 强大后台**：包含用户管理、分类/标签管理、系统消息通知、意见反馈处理，并内置 Quartz 定时任务调度中心。

## 🛠️ 技术栈

### 后端 (Backend)
* **核心框架**：Spring Boot 3.5.9 / Java 21
* **持久层**：MyBatis-Plus 3.5.7 / MySQL 8.0
* **缓存与安全**：Redis / JWT (Auth0) / Spring Security Crypto (BCrypt)
* **工具与增强**：Lombok / MapStruct / Hutool / Apache POI
* **接口文档**：Knife4j (OpenAPI 3)
* **其他组件**：Quartz (定时任务) / FreeMarker (模板引擎) / Spring Boot Actuator

### 前端 (Frontend)
* **核心框架**：Vue 3.5.x (Composition API) / Vite
* **状态与路由**：Pinia / Vue Router
* **UI 组件库**：Element Plus
* **网络请求**：Axios
* **特色插件**：Echarts / md-editor-v3 / vue-cropper / xss (防注入) / crypto-js

## 💻 环境要求

在运行该项目之前，请确保您的开发环境具备以下条件：

* **Java**: JDK 21+ (当前使用 21.0.9)
* **Node.js**: v20.19.0+ 或 v22.12.0+ (当前使用 v22.21.1)
* **MySQL**: 8.0+ (当前使用 8.0.44)
* **Redis**: 5.0+ (当前使用 5.0.14)

## 🚀 快速开始

### 1. 数据库准备
1. 创建数据库 `blog_db`。
2. 运行项目根目录下的 `blog_db.sql` 脚本导入数据表结构。

### 2. 后端启动
1. 在 IDE (如 IntelliJ IDEA) 中打开后端项目。
2. 修改 `application.yml` 中的 MySQL、Redis 连接配置以及邮件、AI 的相关密钥。
3. 运行主启动类启动 Spring Boot 服务。
4. 接口文档访问地址（默认）：`http://localhost:8080/doc.html`

### 3. 前端启动
```bash
# 进入前端目录
cd frontend-directory

# 安装依赖
npm install

# 启动本地开发服务
npm run dev
