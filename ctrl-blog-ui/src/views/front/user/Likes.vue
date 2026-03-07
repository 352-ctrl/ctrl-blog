<template>
  <el-card class="user-page-card" shadow="never">
    <template #header>
      <div class="page-header">
        <el-icon class="header-icon color-danger"><SuccessFilled /></el-icon>
        <span class="header-title">我的点赞</span>
      </div>
    </template>

    <div class="page-content" v-loading="loading">
      <div v-if="articleList.length > 0">
        <SimpleArticleCard
            :articles="articleList"
            @click="navToArticle"
        />

        <FrontPagination
            v-model:current-page="queryParams.pageNum"
            :page-size="queryParams.pageSize"
            :total="total"
            @change="handlePageChange"
        />
      </div>

      <el-empty v-else description="还没有点赞过文章哦" :image-size="100" />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserLikes } from '@/api/front/system/userInfo.js'
import { useTable } from '@/composables/useTable.js'

const router = useRouter()

// 使用 useTable 统一接管分页逻辑
const {
  loading,
  list: articleList,
  total,
  query: queryParams,
  loadData,
  handlePageChange
} = useTable(getUserLikes)

const navToArticle = (id) => {
  router.push(`/post/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-page-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background-color: var(--el-bg-color-overlay);
}

:deep(.user-page-card .el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
}

.header-title {
  font-family: 'SmileySans', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 1px;
}

.color-danger { color: #f56c6c; }

.page-content {
  padding: 10px 0;
}
</style>