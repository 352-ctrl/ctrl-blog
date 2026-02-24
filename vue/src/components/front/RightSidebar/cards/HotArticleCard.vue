<template>
  <el-card class="hot-article-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="icon-hot"><TrendCharts /></el-icon>
          热门文章
        </span>
      </div>
    </template>

    <div class="card-body">
      <div v-if="hotList.length === 0" class="empty-state">
        <el-empty description="暂无热门文章" :image-size="60" />
      </div>

      <div v-else class="hot-list">
        <div
            v-for="(item, index) in hotList"
            :key="item.id"
            :class="[
              'hot-item',
              'animate__animated',
              'animate__fadeIn',
              { 'is-active': isActive(item.id) }
            ]"
            @click="goToArticle(item.id)"
        >
          <div :class="['rank-badge', `rank-${index + 1}`]">
            {{ index + 1 }}
          </div>

          <el-image
              v-if="item.cover"
              class="article-cover"
              :src="item.cover"
              fit="cover"
              lazy
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div v-else class="article-cover empty-cover">
            <el-icon><Picture /></el-icon>
          </div>

          <div class="article-info">
            <div class="title" :title="item.title">{{ item.title }}</div>
            <div class="date">{{ item.createTime?.substring(0, 10) }}</div>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { TrendCharts, Picture } from '@element-plus/icons-vue';
import { getHotArticles } from "@/api/front/article.js";

const router = useRouter();
const route = useRoute(); // 引入 route 用于获取当前页面参数
const hotList = ref([]);

onMounted(() => {
  loadHotArticles();
});

// 加载热门文章
const loadHotArticles = async () => {
  try {
    const res = await getHotArticles();
    if (res.code === 200) {
      hotList.value = res.data || [];
    } else {
      ElMessage.error(res.msg || "获取热门文章失败");
    }
  } catch (error) {
    console.error("Failed to load hot articles:", error);
  }
};

// 判断是否是当前正在查看的文章 (被选中状态)
const isActive = (id) => {
  return route.params.id === String(id);
};

// 点击跳转到文章详情页
const goToArticle = (id) => {
  router.push(`/post/${id}`);
};
</script>

<style scoped lang="scss">
/* 1. 卡片整体样式复用 */
.hot-article-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color-overlay);
  margin-bottom: 20px;

  /* 2. 头部样式复用 */
  :deep(.el-card__header) {
    padding: 15px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .icon-hot {
        margin-right: 10px;
        color: var(--el-color-danger);
        font-size: 18px;
        animation: swing 2s infinite linear;
        transform-origin: bottom center;
      }
    }
  }

  /* 3. 内容区域 */
  .card-body {
    min-height: 100px;
  }

  .empty-state {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    :deep(.el-empty__description) {
      margin-top: 10px;
    }
  }

  /* 4. 热门文章列表特有样式 */
  .hot-list {
    display: flex;
    flex-direction: column;
  }

  .hot-item {
    display: flex;
    align-items: center;
    padding: 12px 10px;
    margin: 0 -10px;
    border-radius: 8px;
    border-bottom: 1px dashed transparent;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;

    &:not(:last-child) {
      border-bottom-color: var(--el-border-color-lighter);
    }

    /* ====== 鼠标悬浮特效 ====== */
    &:hover {
      background-color: var(--el-fill-color-light);
      border-bottom-color: transparent;
      transform: translateX(4px);

      .article-info .title {
        color: var(--el-color-primary);
      }
      .article-cover {
        transform: scale(1.05);
      }
    }

    /* ====== 当前选中(Active)特效 ====== */
    &.is-active {
      background-color: var(--el-color-primary-light-9);
      border-bottom-color: transparent;

      .article-info .title {
        color: var(--el-color-primary);
        font-weight: 600;
      }

      .date {
        color: var(--el-color-primary-light-3);
      }

      /* 左侧指示条 */
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 20%;
        height: 60%;
        width: 3px;
        background-color: var(--el-color-primary);
        border-radius: 0 4px 4px 0;
      }
    }

    /* 排名徽章设计 */
    .rank-badge {
      width: 24px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      border-radius: 6px;
      background-color: var(--el-fill-color-darker);
      color: var(--el-text-color-secondary);
      font-size: 13px;
      font-weight: bold;
      margin-right: 12px;
      flex-shrink: 0;

      &.rank-1 { background-color: var(--el-color-danger); color: #fff; }
      &.rank-2 { background-color: var(--el-color-warning); color: #fff; }
      &.rank-3 { background-color: var(--el-color-warning-light-3); color: #fff; }
    }

    /* 封面图样式 */
    .article-cover {
      width: 60px;
      height: 45px;
      border-radius: 4px;
      margin-right: 12px;
      flex-shrink: 0;
      transition: transform 0.3s ease;

      &.empty-cover, .image-slot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        background: var(--el-fill-color-light);
        color: var(--el-text-color-secondary);
        font-size: 18px;
        border-radius: 4px;
      }
    }

    /* 文章文本信息 */
    .article-info {
      flex: 1;
      overflow: hidden;

      .title {
        font-size: 14px;
        color: var(--el-text-color-primary);
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        transition: color 0.3s;
      }

      .date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        transition: color 0.3s;
      }
    }
  }
}

/* 5. 动画 Keyframes */
@keyframes swing {
  0% { transform: rotate(0deg); }
  10% { transform: rotate(15deg); }
  20% { transform: rotate(-10deg); }
  30% { transform: rotate(5deg); }
  40% { transform: rotate(-5deg); }
  50% { transform: rotate(0deg); }
  100% { transform: rotate(0deg); }
}
</style>