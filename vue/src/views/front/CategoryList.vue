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

    <div>
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
      <Pagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          layout="total, prev, pager, next"
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
import {useRoute, useRouter} from "vue-router";

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
.home-main-card {
  border: none;
  transition: all 0.3s;

  // 移除 card 默认内边距的影响，方便 tabs 贴边或自定义
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.tabs-container {
  margin-bottom: 15px;
  border-bottom: 1px solid #f2f2f2;
  padding-bottom: 8px;

  // 微调由于胶囊样式带来的高度偏移
  :deep(.custom-category-tabs) {
    .el-tabs__header {
      margin: 0; // 覆盖全局 margin
    }
    .el-tabs__nav-scroll {
      padding: 4px 0;
    }
  }
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center; // 分页居中
}

// 响应式调整
@media screen and (max-width: 768px) {
  .home-main-card {
    :deep(.el-card__body) {
      padding: 15px 10px;
    }
  }
}
</style>