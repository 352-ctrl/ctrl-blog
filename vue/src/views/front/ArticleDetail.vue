<template>
  <el-card shadow="never" class="article-container">
    <div class="article-title">
      {{ data.articleData.title }}
    </div>

    <div class="article-meta">
      <div style="margin-right: 10px">
        <el-avatar :size="35" :src="data.articleData.userAvatar" />
      </div>
      <div style="display: flex; flex-direction: column">
        <span class="nickname">{{ data.articleData.userNickname }}</span>
        <div style="display: flex; align-items: center; font-size: 13px; color: #999;">
          {{ data.articleData.createTime }}
          <el-tag size="small" style="margin-left: 10px">
            {{ data.articleData.categoryName }}
          </el-tag>
        </div>
      </div>
      <div style="flex: 1"></div>
      <div style="display: flex; align-items: center; gap: 20px; color: #666;">
        <div>
          <el-icon style="vertical-align: middle"><View /></el-icon>
          {{ data.articleData.viewCount }} 阅读
        </div>
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
          <span style="margin-left: 5px; font-weight: bold">摘要</span>
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

    <div v-if="data.articleData.tags && data.articleData.tags.length > 0" style="margin-top: 20px">
      <el-tag
          v-for="(tag, index) in data.articleData.tags"
          :key="index"
          size="small"
          effect="plain"
          style="margin-right: 5px; margin-bottom: 5px; cursor: pointer"
          @click="handleTagClick(tag.id)"
      >
        # {{ tag.name }}
      </el-tag>
    </div>
  </el-card>

  <div id="comment-section" style="margin-top: 20px;">
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
import {onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {useRoute, useRouter} from "vue-router";
import {View, MagicStick, Collection} from "@element-plus/icons-vue"; // 引入新图标
import 'emoji-picker-element';
import {sanitizeHtml} from "@/utils/filter.js";
import {getArticleById} from "@/api/front/article.js";
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

// 初始化加载
onMounted(() => {
  loadArticle();
});

const handleTagClick = (id) => {
  // 建议使用 router.push 而不是 location.href 以避免整页刷新
  router.push(`/search?tagId=${id}`); // 假设您有搜索页，或者 `/post/${id}`
  // location.href = `/post/${id}`;
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
.article-container {
  padding: 10px;
}

.article-title {
  font-weight: bold;
  font-size: 32px;
  text-align: center;
  padding-bottom: 20px;
  color: #333;
}

.article-meta {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.nickname {
  font-weight: 500;
  font-size: 15px;
}

/* 摘要区域通用样式 */
.summary-wrapper {
  margin-bottom: 25px;
}

/* AI 摘要样式 */
.ai-summary {
  background-color: #ecf5ff; /* Element Plus 浅蓝色 */
  border: 1px solid #d9ecff;
  border-radius: 8px;
  padding: 15px;
  position: relative;
}

.ai-header {
  display: flex;
  align-items: center;
  color: #409eff; /* Primary Blue */
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
}

.ai-icon {
  margin-right: 5px;
  font-size: 18px;
  animation: pulse 2s infinite; /* 简单的呼吸动画 */
}

.ai-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  text-align: justify;
}

/* 普通摘要样式 */
.normal-summary {
  background-color: #f8f8f8;
  border-left: 4px solid #909399; /* 灰色左边框 */
  padding: 15px;
  border-radius: 4px;
}

.normal-header {
  display: flex;
  align-items: center;
  color: #606266;
  margin-bottom: 5px;
}

.normal-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 正文区域样式 */
.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  overflow-wrap: break-word;
}

/* 定义一个简单的动画 */
@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

/* 深度选择器处理 v-html 内部样式 (如果 scoped 阻挡了样式) */
:deep(.article-content img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}
</style>