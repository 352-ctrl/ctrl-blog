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
            <el-avatar :size="35" :src="article.userAvatar" style="cursor: pointer;" />
          </UserInfoPopover>
        </div>
        <div class="meta-author-info">
          <span class="nickname">{{ article.userNickname }}</span>
          <div class="meta-time-category">
            {{ article.createTime }}
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
      </div>

      <div class="article-content" v-html="sanitizeHtml(article.contentHtml)"></div>

      <div v-if="article.tags?.length > 0" class="article-tags">
        <el-tag v-for="tag in article.tags" :key="tag.id" @click="handleTagClick(tag.id)"># {{ tag.name }}</el-tag>
      </div>
    </template>
  </el-card>

  <div id="comment-section" class="comment-container">
    <CommentSection
        :article-id="articleId"
        :initial-total="article?.commentCount || 0"
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
      :commentCount="article.commentCount"
      @scrollToComment="handleScrollToComment"
  />
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { sanitizeHtml } from "@/utils/filter.js";
import { getArticleById, incrementArticleView } from "@/api/front/article/article.js";
import { useRequest } from "@/composables/useRequest";
import CommentSection from '@/views/front/article/components/Comment/CommentSection.vue';
import LeftSideBar from '@/views/front/article/components/LeftSidebar/LeftSideBar.vue';

const route = useRoute();
const router = useRouter();
const articleId = route.params.id;

// 1. 获取详情
const { loading, data: article, execute: fetchArticle } = useRequest(getArticleById);

// 2. 阅读量递增逻辑
const { execute: updateView } = useRequest(incrementArticleView);
let viewTimer = null;

onMounted(async () => {
  // 获取文章详情，失败则跳 404
  try {
    await fetchArticle(articleId);
    if (!article.value) throw new Error();
  } catch {
    await router.push('/404');
    return;
  }

  // 延迟 5 秒发送增加阅读量请求
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
  color: var(--el-text-color-primary); /* 适配暗黑标题 */
}

.flex-spacer {
  flex: 1;
}

.comment-container {
  margin-top: 20px;
}

/* ====================================
   元数据区域 (作者、时间、阅读量)
   ==================================== */
.article-meta {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter); /* 适配分割线 */
  margin-bottom: 20px;
}

.avatar-box {
  margin-right: 10px;
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
  color: var(--el-text-color-secondary); /* 适配暗黑次级文本 */
  margin-top: 2px;
}

.category-tag {
  margin-left: 10px;
}

.meta-view-count {
  display: flex;
  align-items: center;
  color: var(--el-text-color-regular); /* 适配暗黑常规文本 */
  font-size: 14px;
}

.view-icon {
  vertical-align: middle;
  margin-right: 4px;
}

/* ====================================
   摘要区域 (AI 摘要 & 普通摘要)
   ==================================== */
.summary-wrapper {
  margin-bottom: 25px;
}

/* AI 摘要样式 */
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
  color: var(--el-color-primary); /* 品牌主色 */
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

/* 普通摘要样式 */
.normal-summary {
  /* fill-color-light 在明亮是浅灰，暗黑是深灰 */
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
  color: var(--el-text-color-primary); /* 适配暗黑模式正文内容 */
  overflow-wrap: break-word;
}

/* 深度选择器处理 v-html 内部样式 */
:deep(.article-content img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

/* 对于 v-html 中的其他原生标签做一下基础暗黑适配 */
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
  background-color: var(--el-fill-color);
  padding: 2px 4px;
  border-radius: 4px;
  color: var(--el-color-danger); /* 类似 GitHub 的代码高亮色 */
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
   动画
   ==================================== */
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

@media screen and (max-width: 992px) {
  .comment-container {
    /* 60px (底部栏高度) + 15px (额外间距) + iOS安全区 */
    padding-bottom: calc(75px + env(safe-area-inset-bottom));
  }
}
</style>