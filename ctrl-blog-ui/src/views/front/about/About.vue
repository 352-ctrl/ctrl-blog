<template>
  <div class="about-container animate__animated animate__fadeIn">
    <el-card class="about-card" shadow="never" v-loading="loading">

      <div class="about-header">
        <div class="header-title">
          <el-icon class="header-icon"><User /></el-icon>
          关于我
        </div>
      </div>

      <div class="profile-section">
        <div class="profile-avatar-box">
          <el-avatar class="author-avatar" :size="120" :src="webmaster.avatar">
            <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png" alt="默认头像"/>
          </el-avatar>
        </div>
        <div class="profile-info-box">
          <div class="author-name">{{ webmaster.nickname || '神秘站长' }}</div>
          <div class="author-bio">{{ webmaster.bio || '探索技术，记录生活，保持热爱。' }}</div>

          <div class="social-links" v-if="webmaster.github">
            <a :href="webmaster.github" target="_blank" class="github-btn" title="访问我的 GitHub">
              <el-icon><Link /></el-icon><span>前往 GitHub 交流</span>
            </a>
          </div>
        </div>
      </div>

      <el-divider border-style="solid" class="custom-divider" />

      <div class="content-section">
        <h3 class="section-title">
          <el-icon class="title-icon"><Monitor /></el-icon>
          关于本站
        </h3>
        <p class="section-text">
          欢迎来到我的个人博客！<strong>ctrl-blog</strong> 是我从零开始设计与开发的个人博客系统。在这里，我会记录日常的技术学习笔记、项目开发踩坑经验，偶尔也会分享一些生活感悟。希望我的文字能对你有所帮助或启发。
        </p>
      </div>

      <div class="content-section">
        <h3 class="section-title">
          <el-icon class="title-icon"><MagicStick /></el-icon>
          技术栈 & 兴趣
        </h3>
        <div class="tech-tags">
          <el-tag class="custom-tech-tag is-vue">Vue 3</el-tag>
          <el-tag class="custom-tech-tag is-spring">Spring Boot</el-tag>
          <el-tag class="custom-tech-tag is-element">Element Plus</el-tag>
          <el-tag class="custom-tech-tag is-mysql">MySQL</el-tag>
          <el-tag class="custom-tech-tag is-redis">Redis</el-tag>
        </div>
        <p class="section-text">
          目前主要专注于前后端全栈开发。喜欢折腾新鲜的架构和好玩的轮子，致力于写出优雅且易维护的代码。
        </p>
      </div>

      <div class="content-section">
        <h3 class="section-title">
          <el-icon class="title-icon"><ChatDotRound /></el-icon>
          联系方式
        </h3>
        <p class="section-text">
          期待与你的交流！无论是探讨技术难题、分享有趣的想法，还是反馈本站的 Bug，你都可以随时前往 <el-link type="primary" href="/message">留言板</el-link> 畅所欲言，或者通过 GitHub 给我提 Issue。
        </p>
      </div>

    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { User, Link, Monitor, MagicStick, ChatDotRound } from '@element-plus/icons-vue';
import { getWebmasterInfo } from "@/api/front/system/site.js";
import { useRequest } from "@/composables/useRequest.js";

const webmaster = ref({});

const { loading, execute: fetchWebmasterInfo } = useRequest(getWebmasterInfo);

onMounted(async () => {
  const data = await fetchWebmasterInfo();
  if (data) {
    webmaster.value = data;
  }
});
</script>

<style scoped lang="scss">
/* ====================================
   外层容器与卡片
   ==================================== */
.about-container {
  max-width: 800px;
  margin: 0 auto;
  padding-top: 10px;
}

.about-card {
  border-radius: 12px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  min-height: 500px;

  :deep(.el-card__body) {
    padding: 40px 50px;
  }
}

/* ====================================
   头部标题
   ==================================== */
.about-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid var(--el-border-color-lighter);
  padding-bottom: 15px;
  margin-bottom: 40px;
}

.header-title {
  font-family: 'SmileySans', sans-serif;
  font-size: 26px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: var(--el-color-primary);
  font-size: 28px;
}

/* ====================================
   个人资料展示区
   ==================================== */
.profile-section {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 10px 0 20px 0;
}

.profile-avatar-box {
  flex-shrink: 0;

  .author-avatar {
    border: 4px solid var(--el-bg-color);
    box-shadow: 0 0 0 2px var(--el-border-color-lighter), var(--el-box-shadow-light);
    transition: transform 0.4s ease, box-shadow 0.4s ease;

    &:hover {
      transform: translateY(-5px) scale(1.02);
      box-shadow: 0 0 0 2px var(--el-color-primary-light-5), 0 10px 20px rgba(0, 0, 0, 0.1);
    }
  }
}

.profile-info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .author-name {
    font-size: 28px;
    font-weight: bold;
    font-family: 'SmileySans', sans-serif;
    color: var(--el-text-color-primary);
    margin-bottom: 10px;
    letter-spacing: 1px;
  }

  .author-bio {
    font-size: 15px;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
    margin-bottom: 20px;
  }
}

.social-links {
  display: flex;

  .github-btn {
    display: inline-flex;
    align-items: center;
    padding: 10px 22px;
    border-radius: 8px;
    background-color: transparent;
    color: var(--el-text-color-regular);
    border: 1px solid var(--el-border-color-darker);
    text-decoration: none;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

    .el-icon {
      margin-right: 8px;
      font-size: 18px;
    }

    &:hover {
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
      transform: translateY(-2px);
      box-shadow: var(--el-box-shadow-light);
    }
  }
}

.custom-divider {
  margin: 30px 0;
  border-color: var(--el-border-color-lighter);
}

/* ====================================
   内容介绍区
   ==================================== */
.content-section {
  margin-bottom: 35px;

  .section-title {
    display: flex;
    align-items: center;
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 18px;
    padding-left: 12px;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 18px;
      background-color: var(--el-color-primary);
      border-radius: 2px;
    }

    .title-icon {
      margin-right: 8px;
      color: var(--el-text-color-regular);
      font-size: 20px;
    }
  }

  .section-text {
    font-size: 15px;
    color: var(--el-text-color-regular);
    line-height: 1.8;
    text-align: justify;

    strong {
      color: var(--el-color-primary);
      font-family: 'SmileySans', sans-serif;
      margin: 0 4px;
      font-weight: normal;
    }

    .el-link {
      font-size: 15px;
      vertical-align: baseline;
    }
  }
}

/* ====================================
   技术栈标签
   ==================================== */
.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 15px;

  .custom-tech-tag {
    padding: 8px 16px;
    height: auto;
    font-size: 14px;
    font-weight: bold;
    border-radius: 6px;
    background-color: var(--el-fill-color-blank);
    color: var(--tag-color, var(--el-text-color-regular));
    border: 1px solid var(--tag-color, var(--el-border-color));
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    cursor: default;

    &:hover {
      transform: translateY(-3px);
      background-color: var(--tag-color);
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &.is-vue { --tag-color: #41b883; }
    &.is-spring { --tag-color: #6db33f; }
    &.is-element { --tag-color: #409eff; }
    &.is-mysql { --tag-color: #00758f; }
    &.is-redis { --tag-color: #d82c20; }
  }
}

/* ====================================
   移动端响应式适配
   ==================================== */
@media screen and (max-width: 768px) {
  .about-card {
    :deep(.el-card__body) {
      padding: 25px 20px;
    }
  }

  .header-title {
    font-size: 22px;
  }

  .profile-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .profile-info-box {
    align-items: center;

    .author-name {
      font-size: 24px;
    }
  }
}
</style>