<template>
  <el-card shadow="never" class="article-container" v-loading="loading">
    <template v-if="article">
      <div class="article-title">{{ article.title }}</div>

      <div class="article-meta">
        <div class="avatar-box">
          <UserInfoPopover
              :user-id="article.userId"
              :avatar="article.userAvatar"
              :nickname="article.userNickname"
              :bio="article.userBio"
          >
            <el-avatar :size="35" :src="article.userAvatar" class="meta-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </UserInfoPopover>
        </div>
        <div class="meta-author-info">
          <span class="nickname">{{ article.userNickname }}</span>
          <div class="meta-time-category">
            {{ formatTimeAgo(article.createTime) }}
            <el-tag size="small" class="category-tag">{{ article.categoryName }}</el-tag>
          </div>
        </div>
        <div class="flex-spacer"></div>
        <div class="meta-view-count">
          <el-icon class="view-icon"><View /></el-icon>
          {{ article.viewCount }} 阅读
        </div>
      </div>

      <div v-if="article.summary" class="summary-wrapper">
        <div v-if="article.isAiSummary === 1" class="ai-summary">
          <div class="ai-header">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <span class="ai-label">AI 智能摘要</span>
          </div>
          <div class="ai-content">
            {{ article.summary }}
          </div>
        </div>

        <div v-else class="normal-summary">
          <div class="normal-header">
            <el-icon><Collection /></el-icon>
            <span class="normal-summary-text">摘要</span>
          </div>
          <div class="normal-content">
            {{ article.summary }}
          </div>
        </div>
      </div>

      <div id="article-content-wrapper" class="article-content" v-html="sanitizeHtml(article.contentHtml)"></div>

      <div v-if="article.tags?.length > 0" class="article-tags">
        <el-tag
            v-for="tag in article.tags"
            :key="tag.id"
            class="tag-item"
            @click="handleTagClick(tag.id)"
        >
          # {{ tag.name }}
        </el-tag>
      </div>
    </template>
  </el-card>

  <div id="comment-section" class="comment-container">
    <CommentSection
        :article-id="articleId"
        :initial-total="Number(article?.commentCount || 0)"
        @comment-success="handleCommentSuccess"
    />
  </div>

  <LeftSideBar
      v-if="article?.id"
      :articleId="article.id"
      v-model:isLiked="article.liked"
      v-model:likeCount="article.likeCount"
      v-model:isFavorite="article.favorite"
      v-model:favoriteCount="article.favoriteCount"
      :commentCount="Number(article.commentCount || 0)"
      @scrollToComment="handleScrollToComment"
  />
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { sanitizeHtml } from "@/utils/filter.js";
import { formatTimeAgo } from '@/utils/date.js';
import { getArticleById, incrementArticleView } from "@/api/front/article/article.js";
import { useRequest } from "@/composables/useRequest";
import CommentSection from '@/views/front/article/components/Comment/CommentSection.vue';
import LeftSideBar from '@/views/front/article/components/LeftSidebar/LeftSideBar.vue';

const route = useRoute();
const router = useRouter();
const articleId = route.params.id;

const { loading, data: article, execute: fetchArticle } = useRequest(getArticleById);
const { execute: updateView } = useRequest(incrementArticleView);
let viewTimer = null;

onMounted(async () => {
  try {
    await fetchArticle(articleId);
    if (!article.value) throw new Error();
  } catch {
    await router.replace('/404');
    return;
  }

  viewTimer = setTimeout(async () => {
    try {
      await updateView(articleId);
      if (article.value) article.value.viewCount++;
    } catch (e) {
      console.error('更新阅读量失败', e);
    }
  }, 5000);
});

onUnmounted(() => {
  if (viewTimer) clearTimeout(viewTimer);
});

const handleTagClick = (id) => {
  router.push({ name: 'FrontTags', query: { id } });
}

const handleScrollToComment = () => {
  document.getElementById('comment-section')?.scrollIntoView({ behavior: 'smooth' });
};

const handleCommentSuccess = () => {
  if (article.value) article.value.commentCount++;
}
</script>

<style scoped>
/* ====================================
   全局及布局样式
   ==================================== */
.article-container {
  padding: 10px;
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color-light);
  border-radius: 8px;
}

.article-title {
  font-family: 'SmileySans', sans-serif;
  font-weight: bold;
  font-size: 40px;
  text-align: center;
  padding-bottom: 20px;
  color: var(--el-text-color-primary);
}

.flex-spacer {
  flex: 1;
}

.comment-container {
  margin-top: 20px;
}

/* ====================================
   元数据区域
   ==================================== */
.article-meta {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: 20px;
}

.avatar-box {
  margin-right: 10px;
}

/* 提取的行内样式类 */
.meta-avatar {
  cursor: pointer;
}

.meta-author-info {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 500;
  font-size: 15px;
  color: var(--el-text-color-primary);
}

.meta-time-category {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.category-tag {
  margin-left: 10px;
}

.meta-view-count {
  display: flex;
  align-items: center;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.view-icon {
  vertical-align: middle;
  margin-right: 4px;
}

/* ====================================
   摘要区域
   ==================================== */
.summary-wrapper {
  margin-bottom: 25px;
}

.ai-summary {
  font-family: 'SmileySans', sans-serif;
  background-color: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 8px;
  padding: 15px;
  position: relative;
}

.ai-header {
  display: flex;
  align-items: center;
  color: var(--el-color-primary);
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
}

.ai-icon {
  margin-right: 5px;
  font-size: 18px;
  animation: pulse 2s infinite;
}

.ai-content {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  text-align: justify;
}

.normal-summary {
  background-color: var(--el-fill-color-light);
  border-left: 4px solid var(--el-border-color-dark);
  padding: 15px;
  border-radius: 4px;
}

.normal-header {
  display: flex;
  align-items: center;
  color: var(--el-text-color-primary);
  margin-bottom: 5px;
}

.normal-summary-text {
  margin-left: 5px;
  font-weight: bold;
}

.normal-content {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

/* ====================================
   正文区域
   ==================================== */
.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--el-text-color-primary);
  overflow-wrap: break-word;
}

:deep(.article-content img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

:deep(.article-content h1),
:deep(.article-content h2),
:deep(.article-content h3),
:deep(.article-content h4),
:deep(.article-content h5),
:deep(.article-content h6) {
  color: var(--el-text-color-primary);
}

:deep(.article-content a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

:deep(.article-content a:hover) {
  text-decoration: underline;
}

:deep(.article-content blockquote) {
  color: var(--el-text-color-secondary);
  border-left: 4px solid var(--el-border-color);
  background: var(--el-fill-color-light);
  padding: 10px 15px;
  margin: 15px 0;
  border-radius: 4px;
}

:deep(.article-content code) {
  background-color: #f5f7fa;
  color: #303643;
  padding: 2px 4px;
  border-radius: 4px;
  border: 1px solid #e5e6eb;
  font-family: 'Consolas', 'Monaco', monospace;
}

:deep(.article-content pre) {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e5e6eb;
  overflow-x: auto;
  margin: 15px 0;
}
:deep(.article-content pre code) {
  background-color: transparent;
  padding: 0;
  border: none;
}

/* ====================================
   标签区域
   ==================================== */
.article-tags {
  margin-top: 20px;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

/* ====================================
   动画与响应式
   ==================================== */
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

@media screen and (max-width: 992px) {
  .comment-container {
    padding-bottom: calc(75px + env(safe-area-inset-bottom));
  }
}
</style>