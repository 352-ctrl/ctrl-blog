# 🚀 CtrlBlog

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue.svg" alt="Java">
  <img src="https://img.shields.io/badge/SpringBoot-3.5.9-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D.svg" alt="Vue">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-5.0-dc382d.svg" alt="Redis">
</p>

<p align="center">
  <strong>CtrlBlog</strong> 是一个基于 Spring Boot 3 和 Vue 3 的前后端分离的现代化博客系统。系统不仅提供了丰富的文章创作与互动功能，还集成了 AI 辅助、定时任务调度、敏感词过滤及详细的系统监控与日志管理，致力于为开发者提供一个“完全掌控”的个人内容发布平台。
</p>

<p align="center">
  <a href="http://47.86.42.165/"><b>👉 在线体验 (Live Demo)</b></a> | 
  <a href="http://47.86.42.165/doc.html"><b>📖 接口文档</b></a>
</p>

---

## 📸 项目截图

| 前台首页 | 后台仪表盘 |
| :---: | :---: |
| <img src="http://tbobxfar9.hn-bkt.clouddn.com/blog/article/1774168582128_image.png" width="400" alt="前台首页"/> | <img src="http://tbobxfar9.hn-bkt.clouddn.com/blog/article/1774168609096_image.png" width="400" alt="后台仪表盘"/> |
| **文章详情与目录** | **弹幕留言墙** |
| <img src="http://tbobxfar9.hn-bkt.clouddn.com/blog/article/1774168651374_image.png" width="400" alt="文章详情"/> | <img src="http://tbobxfar9.hn-bkt.clouddn.com/blog/article/1774169346428_image.png" width="400" alt="弹幕留言墙"/> |

---

## ✨ 核心特性

* **📝 沉浸式创作**：支持 Markdown 高级编辑（MdEditorV3），支持 Emoji 表情、图片裁剪上传。
* **🤖 AI 赋能**：集成 Spring AI (OpenAI)，支持 AI 自动生成文章摘要等智能化体验。
* **💬 丰富互动**：支持文章点赞、收藏、多级评论（带 Emoji 支持）、以及弹幕留言墙。
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
* **核心框架**：Vue 3.5.25 (Composition API) / Vite
* **状态与路由**：Pinia / Vue Router
* **UI 组件库**：Element Plus
* **网络请求**：Axios
* **特色插件**：Echarts / md-editor-v3 / vue-cropper / xss (防注入) / crypto-js

---

## 💻 环境要求

在运行该项目之前，请确保您的开发环境具备以下条件（括号内为测试通过的稳定版本）：

* **Java**: JDK 21+ (`v21.0.9`)
* **Node.js**: v20.19.0+ / v22.12.0+ (`v22.21.1`)
* **MySQL**: 8.0+ (`v8.0.44`)
* **Redis**: 5.0+ (`v5.0.14`)

---

## 🚀 快速开始

### 方式一：本地开发环境运行

**1. 数据库准备**
* 创建数据库 `blog_db`，字符集 `utf8mb4`。
* 运行项目根目录下的 `blog_db.sql` 脚本导入数据表结构。

**2. 后端启动**
* 在 IDE (如 IntelliJ IDEA) 中打开后端项目。
* 修改 `application.yml` 中的 MySQL、Redis 连接配置以及邮件、七牛云、DeepSeek 的相关密钥。
* 运行主启动类启动 Spring Boot 服务。
* 接口文档访问地址（默认）：`http://localhost:8080/doc.html`

**3. 前端启动**
```bash
# 进入前端目录
cd ctrl-blog-ui

# 安装依赖
npm install

# 启动本地开发服务
npm run dev
```

### 方式二：Docker / CI/CD 生产环境自动化部署 (推荐)

本项目已实现基于 **GitHub Actions** 和 **阿里云 ACR (容器镜像服务)** 的企业级自动化部署流水线。服务器实现“零源码”纯净运行。

**1. 云端自动构建与推送**
* 开发者只需将代码 `git push` 至 GitHub `main` 分支。
* GitHub Actions 将自动触发多阶段构建 (Multi-stage Build)，分别打包 Spring Boot 和 Vue 项目。
* 自动构建 Docker 镜像并推送到阿里云 ACR 私有仓库。

**2. 服务器极速拉取运行**
在生产服务器上，仅需两个配置文件即可秒级启动整个高可用集群：

```bash
# 1. 准备目录与配置文件
mkdir -p /root/ctrl-blog && cd /root/ctrl-blog
# 请在此目录下放置 docker-compose.yml 和包含敏感信息的 .env 文件

# 2. 登录阿里云私有镜像仓库 (VPC内网极速拉取)
sudo docker login --username=您的用户名 crpi-xxx-vpc.cn-hongkong.personal.cr.aliyuncs.com

# 3. 一键拉取最新镜像并平滑启动
sudo docker compose pull
sudo docker compose up -d
