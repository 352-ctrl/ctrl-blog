<template>
  <el-card class="tag-main-card" shadow="never">
    <div class="tag-header">
      <el-icon class="header-icon"><CollectionTag /></el-icon>
      <span class="header-title">标签云</span>
    </div>

    <div class="tag-cloud-box">
      <div
          v-for="item in data.tagList"
          :key="item.id"
          class="custom-tag-item"
          :class="{ 'is-active': data.activeTab === item.id.toString() }"
          @click="handleTagClick(item.id.toString())"
          :style="{ '--random-color': item.color }"
      >
        <el-icon class="tag-icon"><PriceTag /></el-icon>
        <span class="tag-name">{{ item.name }}</span>
      </div>
    </div>
  </el-card>

  <div class="article-list-container">
    <ArticleListCard
        :articles="data.articleList"
        @click="navToArticle"
    />

    <el-empty
        v-if="data.articleList.length === 0"
        description="该标签下暂无文章"
        :image-size="100"
    />
  </div>

  <div class="pagination-wrapper">
    <FrontPagination
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :total="data.total"
        @change="loadPage"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { getArticlePage } from "@/api/front/article.js";
import { ElMessage } from "element-plus";
import { getTagList } from "@/api/front/tag.js";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

// 精选的一套好看的 UI 色板，避免纯随机产生亮瞎眼的颜色
const colorPalette = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
  '#9c27b0', '#ff9800', '#00bcd4', '#8bc34a',
  '#e91e63', '#3f51b5', '#009688', '#795548',
  '#FA541C', '#13C2C2', '#2F54EB', '#722ED1'
];

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  activeTab: '',
  tagId: '',
  articleList: [],
  tagList:[],
})

const navToArticle = (id) => {
  router.push({
    path: `/post/${id}`
  });
}

// 初始化加载
onMounted(async () => {
  await loadTag();
  const queryId = route.query.id;
  if (queryId) {
    data.activeTab = queryId.toString();
    data.tagId = queryId;
  } else {
    data.activeTab = '';
    data.tagId = '';
  }
  await loadPage();
});

// 加载数据
const loadPage = async () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
  };
  const tagIdNum = Number(data.tagId);
  if (tagIdNum && tagIdNum > 0) {
    params.tagIds = [tagIdNum];
  }
  try {
    const res = await getArticlePage(params);
    if (res.code === 200) {
      data.articleList = res.data.records || [];
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    console.error(error);
  }
};

const loadTag = async () => {
  try {
    const res = await getTagList();
    if (res.code === 200) {
      // 在这里为每一个标签随机分配一个颜色
      data.tagList = (res.data || []).map(tag => {
        const randomHex = colorPalette[Math.floor(Math.random() * colorPalette.length)];
        return {
          ...tag,
          color: randomHex
        };
      });
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    console.error(error);
  }
};

// 标签切换处理
const handleTagClick = (tagKey) => {
  if (data.activeTab === tagKey) {
    data.activeTab = '';
    data.tagId = '';
  } else {
    data.activeTab = tagKey;
    data.tagId = tagKey;
  }
  data.pageNum = 1;
  loadPage();
};
</script>

<style scoped lang="scss">
/* ==========================================
 * 卡片容器与标题样式
 * ========================================== */
.tag-main-card {
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