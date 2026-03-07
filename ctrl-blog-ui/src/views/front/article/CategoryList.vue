<template>
  <el-card class="home-main-card" shadow="never" v-loading="articleLoading">
    <div class="tabs-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-category-tabs">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane
            v-for="item in (categoryList || [])"
            :key="item.id"
            :label="item.name"
            :name="item.id.toString()"
        />
      </el-tabs>
    </div>

    <div class="article-list-wrapper">
      <ArticleListCard :articles="articleList" @click="navToArticle" />
      <el-empty v-if="!articleLoading && articleList.length === 0" description="该分类下暂无文章" />
    </div>

    <div class="pagination-container" v-if="total > 0">
      <FrontPagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          @change="handlePageChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getArticlePage } from "@/api/front/article/article.js";
import { getCategoryList } from '@/api/front/article/category.js';
import { useTable } from "@/composables/useTable";
import { useRequest } from "@/composables/useRequest";

const route = useRoute();
const router = useRouter();
const activeTab = ref('all');

// 1. 分页表格管理
const {
  loading: articleLoading,
  list: articleList,
  total,
  query,
  loadData,
  handlePageChange
} = useTable(getArticlePage, { categoryId: null });

// 2. 分类字典管理
const { data: categoryList, execute: fetchCategories } = useRequest(getCategoryList);

const handleTabChange = async (tabName) => {
  query.pageNum = 1;
  query.categoryId = tabName === 'all' ? null : tabName;
  await loadData(); // 显式 await
};

const navToArticle = (id) => {
  router.push({ path: `/post/${id}` });
};

onMounted(async () => {
  // 并行执行分类获取和路由参数解析
  await fetchCategories();

  const queryId = route.query.id;
  if (queryId) {
    activeTab.value = queryId.toString();
    query.categoryId = queryId;
  }

  await loadData();
});
</script>