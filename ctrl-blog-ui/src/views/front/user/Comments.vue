<template>
  <el-card class="user-page-card" shadow="never">
    <template #header>
      <div class="page-header">
        <el-icon class="header-icon color-primary"><ChatDotRound /></el-icon>
        <span class="header-title">我的评论</span>
      </div>
    </template>

    <div class="page-content" v-loading="loading">
      <div v-if="commentList.length > 0" class="comment-list">
        <div
            v-for="item in commentList"
            :key="item.id"
            class="comment-item animate__animated animate__fadeIn"
            @click="navToArticle(item.articleId, item.id)"
        >
          <div class="comment-text">{{ item.content }}</div>

          <div class="comment-meta">
            <span class="time">{{ item.createTime }}</span>
            <div class="article-ref">
              <span class="ref-label">评于：</span>
              <span class="ref-title">{{ item.articleTitle }}</span>
            </div>
          </div>
        </div>

        <FrontPagination
            v-model:current-page="queryParams.pageNum"
            :page-size="queryParams.pageSize"
            :total="total"
            @change="handlePageChange"
        />
      </div>

      <el-empty v-else description="暂无评论，快去留下你的足迹吧" :image-size="100" />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserComments } from '@/api/front/system/userInfo.js'
import { useTable } from '@/composables/useTable.js'

const router = useRouter()

// 使用 useTable 统一接管分页逻辑
const {
  loading,
  list: commentList,
  total,
  query: queryParams,
  loadData,
  handlePageChange
} = useTable(getUserComments)

const navToArticle = (articleId, commentId) => {
  router.push({
    path: `/post/${articleId}`,
    query: { commentId: commentId }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 基础卡片样式复用 */
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

.header-icon { font-size: 20px; }
.color-primary { color: var(--el-color-primary); }

.header-title {
  font-family: 'SmileySans', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 1px;
}

/* ==================================
   专属：评论列表样式 (参考卡片交互)
   ================================== */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px; /* 卡片之间的间距 */
}

.comment-item {
  cursor: pointer;
  padding: 16px 20px;
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

/* 鼠标悬浮：上浮 + 阴影 */
.comment-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--el-box-shadow-light);
}

/* 鼠标按下：轻微缩小回弹 */
.comment-item:active {
  transform: scale(0.99);
  transition: transform 0.1s ease;
}

.comment-text {
  font-size: 15px;
  color: var(--el-text-color-primary);
  line-height: 1.6;
  margin-bottom: 12px;
  word-break: break-word;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  border-top: 1px dashed var(--el-border-color-lighter);
  padding-top: 10px;
}

/* 彻底去除了 hover 和 cursor:pointer，作为纯展示元素 */
.article-ref {
  display: flex;
  align-items: center;
  background-color: var(--el-fill-color-light);
  padding: 4px 10px;
  border-radius: 4px;
  max-width: 60%;
}

.ref-label {
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.ref-title {
  color: var(--el-text-color-secondary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media screen and (max-width: 768px) {
  .comment-item {
    padding: 12px 15px;
  }
  .comment-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .article-ref {
    max-width: 100%;
  }
}
</style>