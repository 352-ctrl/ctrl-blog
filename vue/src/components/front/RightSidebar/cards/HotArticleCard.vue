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

      <SimpleArticleCard
          v-else
          :articles="hotList"
          :item-class="getDynamicRowClass"
          @click="goToArticle"
      >
        <template #prefix="{ index }">
          <div :class="['rank-badge', `rank-${index + 1}`]">
            {{ index + 1 }}
          </div>
        </template>
      </SimpleArticleCard>

    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { getHotArticles } from "@/api/front/article.js";

const router = useRouter();
const route = useRoute();
const hotList = ref([]);

onMounted(() => {
  loadHotArticles();
});

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

// 动态决定哪一项应该附加高亮 class
const getDynamicRowClass = (item) => {
  return route.params.id === String(item.id) ? 'is-active-hot' : '';
};

const goToArticle = (id) => {
  router.push(`/post/${id}`);
};
</script>

<style scoped lang="scss">
/* 1. 卡片基础样式 */
.hot-article-card {
  border: 1px solid var(--el-border-color-light);
  margin: 0;
  border-radius: 8px;
  background: var(--el-bg-color-overlay);

  :deep(.el-card__header) {
    padding: 15px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
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

      .icon-hot {
        margin-right: 10px;
        color: var(--el-color-danger);
        font-size: 18px;
        animation: swing 2s infinite linear;
        transform-origin: bottom center;
      }
    }
  }

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
}

/* =======================================
   ✨ 2. 注入到子组件的特定业务样式
   使用 :deep() 穿透到 MiniArticleList 内部
   ======================================= */

/* 排名徽章设计 */
:deep(.rank-badge) {
  font-family: 'SmileySans', sans-serif;
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

/* 热门榜单专属的 Active 高亮状态 */
:deep(.is-active-hot) {
  background-color: var(--el-color-primary-light-9);
  border-bottom-color: transparent !important;

  .article-info .title {
    color: var(--el-color-primary) !important;
    font-weight: 600 !important;
  }

  .meta-row .date {
    color: var(--el-color-primary-light-3) !important;
  }

  /* 左侧主题色指示条 */
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

/* 动画 Keyframes */
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