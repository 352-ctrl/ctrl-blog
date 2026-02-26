<template>
  <el-card shadow="never" class="article-container">
    <div class="article-title">
      {{ data.articleData.title }}
    </div>

    <div class="article-meta">
      <div class="avatar-box">
        <el-avatar :size="35" :src="data.articleData.userAvatar" />
      </div>
      <div class="meta-author-info">
        <span class="nickname">{{ data.articleData.userNickname }}</span>
        <div class="meta-time-category">
          {{ data.articleData.createTime }}
          <el-tag size="small" class="category-tag">
            {{ data.articleData.categoryName }}
          </el-tag>
        </div>
      </div>
      <div class="flex-spacer"></div>
      <div class="meta-view-count">
        <el-icon class="view-icon"><View /></el-icon>
        {{ data.articleData.viewCount }} 阅读
      </div>
    </div>

    <div v-if="data.articleData.summary" class="summary-wrapper">
      <div v-if="data.articleData.isAiSummary === 1" class="ai-summary">
        <div class="ai-header">
          <el-icon class="ai-icon"><MagicStick /></el-icon>
          <span class="ai-label">AI 智能摘要</span>
        </div>
        <div class="ai-content">
          {{ data.articleData.summary }}
        </div>
      </div>

      <div v-else class="normal-summary">
        <div class="normal-header">
          <el-icon><Collection /></el-icon>
          <span class="normal-summary-text">摘要</span>
        </div>
        <div class="normal-content">
          {{ data.articleData.summary }}
        </div>
      </div>
    </div>

    <div
        ref="contentRef"
        id="article-content-wrapper"
        class="article-content"
        v-html="sanitizeHtml(data.articleData.contentHtml)"
    ></div>

    <div v-if="data.articleData.tags && data.articleData.tags.length > 0" class="article-tags">
      <el-tag
          v-for="(tag, index) in data.articleData.tags"
          :key="index"
          size="small"
          effect="plain"
          class="tag-item"
          @click="handleTagClick(tag.id)"
      >
        # {{ tag.name }}
      </el-tag>
    </div>
  </el-card>

  <div id="comment-section" class="comment-container">
    <CommentSection
        :article-id="data.articleId"
        :initial-total="data.articleData.commentCount"
        @comment-success="handleCommentSuccess"
    />
  </div>

  <LeftSideBar
      v-if="data.articleData.id"
      :articleId="data.articleData.id"

      v-model:isLiked="data.articleData.liked"
      v-model:likeCount="data.articleData.likeCount"

      v-model:isFavorite="data.articleData.favorite"
      v-model:favoriteCount="data.articleData.favoriteCount"

      :commentCount="data.articleData.commentCount"

      @scrollToComment="handleScrollToComment"
  />

</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import 'emoji-picker-element';
import { sanitizeHtml } from "@/utils/filter.js";
import { getArticleById, incrementArticleView } from "@/api/front/article.js";
import CommentSection from '@/components/front/Comment/CommentSection.vue';

const route = useRoute();
const router = useRouter();
const contentRef = ref(null);

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  articleId: route.params.id,
  articleData: {}, // 初始化为空对象
  commentList:[],
})

let viewTimer = null;

// 初始化加载
onMounted(() => {
  loadArticle();

  // 延迟 5 秒发送增加阅读量请求 (防爬虫、防秒退)
  viewTimer = setTimeout(() => {
    if (data.articleId) {
      incrementArticleView(data.articleId).then(res => {
        if (res.code === 200) {
          data.articleData.viewCount = (parseInt(data.articleData.viewCount) || 0) + 1;
        }
      }).catch(err => {
        console.error('记录阅读量异常:', err);
      });
    }
  }, 5000);
});

// 组件卸载时清理定时器
onUnmounted(() => {
  if (viewTimer) {
    clearTimeout(viewTimer);
    viewTimer = null;
  }
});

const handleTagClick = (id) => {
  router.push({
    name: 'FrontTags',
    query: { id: id }
  });
}

// 加载数据
const loadArticle = () => {
  getArticleById(data.articleId).then(res => {
    if (res.code === 200) {
      data.articleData = res.data;
      // 可以在这里处理 tags 为 null 的情况
      if (!data.articleData.tags) {
        data.articleData.tags = [];
      }
    } else {
      router.push('/404')
      ElMessage.error(res.msg);
    }
  });
};

const handleScrollToComment = () => {
  const el = document.getElementById('comment-section');
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' });
  }
};

const handleCommentSuccess = () => {
  if (data.articleData.commentCount !== undefined) {
    data.articleData.commentCount = parseInt(data.articleData.commentCount) + 1;
  } else {
    data.articleData.commentCount = 1;
  }
}
</script>

<style scoped>
/* ====================================
   全局及布局样式
   ==================================== */
.article-container {
  padding: 10px;
  /* 核心优化：使用 overlay 以与 page 背景形成高级感的层级落差 */
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
</style>