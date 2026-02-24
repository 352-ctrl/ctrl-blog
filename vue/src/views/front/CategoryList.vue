<template>
  <el-card class="home-main-card" shadow="never">
    <div class="tabs-container">
      <el-tabs
          v-model="data.activeTab"
          @tab-change="handleTabChange"
          class="custom-category-tabs"
      >
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane
            v-for="item in data.categoryList"
            :key="item.id"
            :label="item.name"
            :name="item.id.toString()"
        ></el-tab-pane>
      </el-tabs>
    </div>

    <div class="article-list-wrapper">
      <ArticleListCard
          :articles="data.articleList"
          @click="navToArticle"
      />

      <el-empty
          v-if="data.articleList.length === 0"
          description="该分类下暂无文章"
          :image-size="100"
      />
    </div>

    <div class="pagination-container">
      <FrontPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { getArticlePage } from "@/api/front/article.js";
import { getCategoryList } from '@/api/front/category.js';
import { getHomepageNotices } from "@/api/front/notice.js";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  activeTab: 'all',
  categoryId: null,
  articleList: [],
  categoryList: [],
  noticeList: []
});

const navToArticle = (id) => {
  router.push({
    path: `/post/${id}`
  });
};

// 初始化加载
onMounted(async () => {
  await loadCategory();
  // 检查 URL 是否带有 id 参数
  const queryId = route.query.id;
  if (queryId) {
    // 如果有 ID，设置当前激活的 Tab 和查询参数
    data.activeTab = queryId.toString();
    data.categoryId = queryId;
  } else {
    // 如果没有，默认全部
    data.activeTab = 'all';
    data.categoryId = '';
  }
  await loadPage();
});

// 加载文章数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
  };
  if (data.categoryId) {
    params.categoryId = data.categoryId;
  }
  return getArticlePage(params).then(res => {
    if (res.code === 200) {
      data.articleList = res.data.records || [];
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载分类列表
const loadCategory = () => {
  return getCategoryList().then(res => {
    if (res.code === 200) {
      data.categoryList = res.data || [];
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 分类切换处理
const handleTabChange = (tabName) => {
  data.pageNum = 1;
  data.categoryId = tabName === 'all' ? null : tabName;
  loadPage();
};
</script>

<style scoped lang="scss">
/* ==========================================
 * 主容器卡片优化
 * ========================================== */
.home-main-card {
  /* 增加极其轻微的边框，防止暗黑模式下阴影失效导致卡片与背景融为一体 */
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-light);
  transition: background-color 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    /* 悬浮加深阴影 */
    box-shadow: var(--el-box-shadow);
  }

  :deep(.el-card__body) {
    /* 这里的 padding-bottom 控制最后一个卡片距离分隔线的距离 */
    padding: 20px 24px 0px 24px;
  }
}

/* ==========================================
 * 文章列表区域
 * ========================================== */
.article-list-wrapper {
  min-height: 200px; /* 给予一个最小高度，防止切换 Tab 时高度塌陷导致闪烁 */
}

/* ==========================================
 * Tabs 分类导航美化
 * ========================================== */
.tabs-container {
  margin-bottom: 8px;

  :deep(.custom-category-tabs) {
    .el-tabs__nav-wrap::after {
      display: none; /* 隐藏默认的灰色底部长线 */
    }

    .el-tabs__header {
      margin-bottom: 4px;
    }

    .el-tabs__item {
      font-size: 16px;
      color: var(--el-text-color-regular); /* 自动适配暗黑模式的常规文本色 */
      padding: 0 20px;
      height: 40px;
      line-height: 40px;
      transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

      &:hover {
        color: var(--el-color-primary);
      }

      &.is-active {
        font-size: 17px;
        font-weight: 600;
        color: var(--el-text-color-primary); /* 激活态使用更亮/更深的主文本色 */
      }
    }

    .el-tabs__active-bar {
      height: 4px;
      border-radius: 2px;
      background-color: var(--el-color-primary);
      /* 利用品牌色的半透明版本作为发光阴影，适配暗黑/明亮双模式 */
      box-shadow: 0 2px 4px var(--el-color-primary-light-5);
      bottom: 0px;
    }
  }
}

/* ==========================================
 * 分页容器
 * ========================================== */
.pagination-container {
  border-top: 1px solid var(--el-border-color-lighter); /* 使用淡色边框衔接 */
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.3s ease;

  padding: 20px 0;
  margin-top: 0;

  :deep(> div),
  :deep(.custom-pagination),
  :deep(.el-pagination) {
    margin: 0 !important;
    padding-top: 0 !important;
    padding-bottom: 0 !important;
  }
}

/* ==========================================
 * 移动端适配
 * ========================================== */
@media screen and (max-width: 768px) {
  .home-main-card {
    :deep(.el-card__body) {
      padding: 12px 12px 0 12px;
    }
  }

  .pagination-container {
    padding: 15px 0;
  }
}
</style>