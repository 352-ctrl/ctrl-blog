<template>
  <el-card class="webmaster-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="icon-user icon-swing"><UserFilled /></el-icon>
          关于站长
        </span>
      </div>
    </template>

    <div class="card-body" v-loading="loading">
      <div class="author-info">
        <el-avatar class="author-avatar" :size="64" :src="webmaster.avatar">
          <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png" alt="默认头像"/>
        </el-avatar>
        <div class="author-name">{{ webmaster.nickname || '神秘站长' }}</div>
        <div class="author-bio">{{ webmaster.bio || '这个人很懒，什么都没有留下~' }}</div>
      </div>

      <div class="site-stats">
        <div class="stat-item"><div class="stat-label">文章</div><div class="stat-value">{{ webmaster.articleCount || 0 }}</div></div>
        <div class="stat-item"><div class="stat-label">分类</div><div class="stat-value">{{ webmaster.categoryCount || 0 }}</div></div>
        <div class="stat-item"><div class="stat-label">标签</div><div class="stat-value">{{ webmaster.tagCount || 0 }}</div></div>
      </div>

      <div class="social-links" v-if="webmaster.github">
        <a :href="webmaster.github" target="_blank" class="github-btn" title="去我的 GitHub 看看">
          <el-icon><Link /></el-icon><span>GitHub</span>
        </a>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { UserFilled, Link } from '@element-plus/icons-vue';
import { getWebmasterInfo } from "@/api/front/system/site.js";
import { useRequest } from "@/composables/useRequest.js"; // 引入 Hook

const webmaster = ref({});

// ======= Hook 接入 =======
const { loading, execute: fetchWebmasterInfo } = useRequest(getWebmasterInfo);

onMounted(async () => {
  const data = await fetchWebmasterInfo();
  // 成功时 Hook 会直接返回 res.data
  if (data) webmaster.value = data;
});
</script>

<style scoped lang="scss">
/* 1. 卡片基础样式 */
.webmaster-card {
  border: 1px solid var(--el-border-color-light);
  margin: 0;
  border-radius: 8px;
  background: var(--el-bg-color-overlay);

  /* 减小头部的内边距 */
  :deep(.el-card__header) {
    padding: 12px 15px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  /* 核心：覆盖 el-card 自带的 20px body padding，大幅减少高度 */
  :deep(.el-card__body) {
    padding: 15px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-family: 'SmileySans', sans-serif;
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .icon-user {
        margin-right: 10px;
        color: var(--el-color-primary);
        font-size: 18px;
        transform-origin: bottom center;
      }
    }
  }

  .card-body {
    padding: 0;
  }
}

/* 2. 站长信息区 */
.author-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 15px;

  .author-avatar {
    border: 2px solid var(--el-border-color-lighter);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.5s ease;

    &:hover {
      transform: rotate(360deg);
    }
  }

  .author-name {
    margin-top: 10px;
    font-size: 16px;
    font-weight: bold;
    color: var(--el-text-color-primary);
  }

  .author-bio {
    margin-top: 5px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.4;
    padding: 0 10px;
    word-break: break-word;
  }
}

/* 3. 数据统计区 */
.site-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 15px;
  padding: 8px 0;
  border-top: 1px dashed var(--el-border-color-lighter);
  border-bottom: 1px dashed var(--el-border-color-lighter);

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-label {
      font-size: 12px;
      color: var(--el-text-color-regular);
      margin-bottom: 2px;
    }

    .stat-value {
      font-size: 16px;
      font-weight: bold;
      color: var(--el-text-color-primary);
      font-family: 'SmileySans', sans-serif;
    }
  }
}

/* 4. 社交链接区 */
.social-links {
  display: flex;
  justify-content: center;

  .github-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    padding: 8px 0;
    border-radius: 6px;
    background-color: var(--el-fill-color-light);
    color: var(--el-text-color-regular);
    text-decoration: none;
    font-size: 13px;
    font-weight: 500;
    transition: all 0.3s;

    .el-icon {
      margin-right: 6px;
      font-size: 15px;
    }

    &:hover {
      background-color: var(--el-color-primary);
      color: white;
    }
  }
}
</style>