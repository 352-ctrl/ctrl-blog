<template>
  <el-card class="tag-main-card" shadow="never" v-loading="tagLoading">
    <div class="tag-header">
      <el-icon class="header-icon"><CollectionTag /></el-icon>
      <span class="header-title">标签云</span>
    </div>

    <div class="tag-cloud-box">
      <div
          v-for="item in tagList"
          :key="item.id"
          class="custom-tag-item"
          :class="{ 'is-active': activeTab === item.id.toString() }"
          @click="handleTagClick(item.id.toString())"
          :style="{ '--random-color': item.color }"
      >
        <el-icon class="tag-icon"><PriceTag /></el-icon>
        <span class="tag-name">{{ item.name }}</span>
      </div>
    </div>
  </el-card>

  <div class="article-list-container" v-loading="articleLoading">
    <ArticleListCard :articles="articleList" @click="navToArticle" />
    <el-empty v-if="!articleLoading && articleList.length === 0" description="该标签下暂无文章" />
  </div>

  <div class="pagination-wrapper" v-if="total > 0">
    <FrontPagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getArticlePage } from "@/api/front/article/article.js";
import { getTagList } from "@/api/front/article/tag.js";
import { useRoute, useRouter } from "vue-router";
import { useTable } from "@/composables/useTable";
import { useRequest } from "@/composables/useRequest";

const route = useRoute();
const router = useRouter();
const activeTab = ref('');

const colorPalette = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#9c27b0', '#ff9800'];

// 1. 列表 Hooks
const {
  loading: articleLoading,
  list: articleList,
  total,
  query,
  loadData,
  handlePageChange
} = useTable(getArticlePage, { tagIds: [] });

// 2. 标签 Hooks
const { loading: tagLoading, data: tagList, execute: fetchTags } = useRequest(getTagList);

const handleTagClick = async (tagKey) => {
  if (activeTab.value === tagKey) {
    activeTab.value = '';
    query.tagIds = [];
  } else {
    activeTab.value = tagKey;
    query.tagIds = [tagKey];
  }
  query.pageNum = 1;
  await loadData();
};

const navToArticle = (id) => {
  router.push({ path: `/post/${id}` });
}

onMounted(async () => {
  // 1. 获取标签并染色
  const rawTags = await fetchTags();
  if (rawTags) {
    tagList.value = rawTags.map(tag => ({
      ...tag,
      color: colorPalette[Math.floor(Math.random() * colorPalette.length)]
    }));
  }

  // 2. 处理初始参数
  const queryId = route.query.id;
  if (queryId) {
    activeTab.value = queryId.toString();
    query.tagIds = [parseInt(queryId, 10)];
  }

  await loadData();
});
</script>

<style scoped lang="scss">
/* ==========================================
 * 卡片容器与标题样式
 * ========================================== */
.tag-main-card {
  margin-top: 0;
  /* 增加轻微边框，适配暗黑模式下阴影不可见的问题 */
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-light);
  margin-bottom: 20px;
  transition: background-color 0.3s, border-color 0.3s, box-shadow 0.3s;

  :deep(.el-card__body) {
    padding: 25px 30px;
  }
}

.tag-header {
  font-family: 'SmileySans', sans-serif;
  display: flex;
  align-items: center;
  justify-content: center; /* 标题居中 */
  margin-bottom: 25px;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  transition: color 0.3s;

  .header-icon {
    margin-right: 8px;
    font-size: 22px;
    color: var(--el-color-primary);
  }
}

.article-list-container {
  min-height: 200px;
}

/* ==========================================
 * 优化的标签样式
 * ========================================== */
.tag-cloud-box {
  display: flex;
  flex-wrap: wrap;
  justify-content: center; /* 标签也整体居中 */
  gap: 12px 16px;
  align-items: center;
}

.custom-tag-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  font-size: 14px;
  color: var(--el-text-color-regular);
  /* 优化底色，在明暗模式下都能保持绝佳的内部对比度 */
  background-color: var(--el-fill-color-blank);

  /* 边框颜色使用动态注入的 CSS 变量 */
  border: 1px solid var(--random-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  user-select: none;

  .tag-icon {
    margin-right: 6px;
    font-size: 14px;
    /* 图标颜色使用动态注入的 CSS 变量 */
    color: var(--random-color);
    transition: color 0.3s;
  }

  .tag-name {
    font-weight: 500;
  }

  /* 悬浮状态 */
  &:hover {
    color: var(--random-color); /* 悬浮时字体也变成对应颜色 */
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow-light);
  }

  /* 激活(选中)状态 */
  &.is-active {
    background-color: var(--random-color);
    border-color: var(--random-color);
    /* 激活时背景被随机颜色填充，为了保证对比度，文字强制设为白色 */
    color: #ffffff;
    transform: translateY(-2px);
    box-shadow: var(--el-box-shadow);

    .tag-icon {
      color: #ffffff; /* 激活时图标变成白色 */
    }
  }
}

/* ==========================================
 * 分页容器
 * ========================================== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  margin-top: 10px;
}

/* ==========================================
 * 移动端适配
 * ========================================== */
@media screen and (max-width: 768px) {
  .tag-main-card {
    border-radius: 8px;
    margin-bottom: 15px;

    :deep(.el-card__body) {
      padding: 20px 15px;
    }
  }

  .tag-header {
    font-size: 18px;
    margin-bottom: 18px;
  }

  .tag-cloud-box {
    gap: 10px;
  }

  .custom-tag-item {
    padding: 6px 14px;
    font-size: 13px;
    border-radius: 6px;
  }
}
</style>