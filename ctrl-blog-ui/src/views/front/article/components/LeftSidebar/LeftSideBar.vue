<template>
  <div class="suspended-panel">
    <div class="panel-inner">
      <ArticleLikeButton
          v-if="articleId"
          :articleId="articleId"
          :liked="isLiked"
          :count="likeCount"
          @update:liked="(val) => $emit('update:isLiked', val)"
          @update:count="(val) => $emit('update:likeCount', val)"
          class="sidebar-btn"
      />

      <ArticleCommentButton
          :count="commentCount"
          @click="scrollToComment"
          class="sidebar-btn"
      />

      <ArticleFavoriteButton
          v-if="articleId"
          :articleId="articleId"
          :isFavorite="isFavorite"
          :count="favoriteCount"
          @update:isFavorite="(val) => $emit('update:isFavorite', val)"
          @update:count="(val) => $emit('update:favoriteCount', val)"
          class="sidebar-btn"
      />

      <div class="panel-btn report-btn sidebar-btn" @click="handleReportArticle" title="举报文章">
        <el-icon size="20"><Warning /></el-icon>
      </div>
    </div>

    <ReportDialog />
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { ElMessage } from "element-plus";
import ArticleLikeButton from './ArticleLikeButton.vue';
import ArticleFavoriteButton from './ArticleFavoriteButton.vue';
import ArticleCommentButton from './ArticleCommentButton.vue';
import { useDialogStore } from '@/store/dialog.js';
import { useUserStore } from "@/store/user.js";

const dialogStore = useDialogStore();

const props = defineProps({
  articleId: { type: [String, Number], default: '' },
  isLiked: { type: Boolean, default: false },
  likeCount: { type: Number, default: 0 },
  commentCount: { type: Number, default: 0 },
  isFavorite: { type: Boolean, default: false },
  favoriteCount: { type: Number, default: 0 }
});

const emit = defineEmits([
  'update:isLiked',
  'update:likeCount',
  'update:isFavorite',
  'update:favoriteCount',
  'scrollToComment'
]);

const userStore = useUserStore();

const scrollToComment = () => {
  emit('scrollToComment');
};

const handleReportArticle = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后举报');
    return;
  }

  // 直接通过 store 打开弹窗并传参
  if (typeof dialogStore.openReportDialog === 'function') {
    dialogStore.openReportDialog(props.articleId, 'ARTICLE');
  } else {
    dialogStore.reportTargetType = 'ARTICLE';
    dialogStore.reportTargetId = props.articleId;
    dialogStore.reportVisible = true;
  }
};
</script>

<style scoped lang="scss">
/* ====================================
   1. 桌面端样式 (> 992px)
   ==================================== */
.suspended-panel {
  position: fixed;
  top: 180px;
  left: max(15px, calc(50% - 680px));
  z-index: 100;
  transition: top 0.3s, background-color 0.3s; /* 去除 left 的过渡，防止计算时产生拖影 */
}

.panel-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 按钮通用样式 */
:deep(.panel-btn), .panel-btn {
  width: 48px;
  height: 48px;
  background-color: var(--el-bg-color-overlay);
  border-radius: 50%;
  box-shadow: var(--el-box-shadow-light);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--el-text-color-secondary);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid var(--el-border-color-lighter);

  &:hover {
    transform: scale(1.1);
    box-shadow: var(--el-box-shadow);
    color: var(--el-color-primary);
  }
}

.report-btn:hover {
  color: var(--el-color-danger) !important;
}

/* ====================================
   2. 移动端/窄屏样式 (< 992px)
   转换为底部固定的工具栏
   ==================================== */
@media screen and (max-width: 992px) {
  .suspended-panel {
    top: auto;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    background-color: var(--el-bg-color-overlay);
    border-top: 1px solid var(--el-border-color-lighter);
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
    padding: 8px 0 calc(8px + env(safe-area-inset-bottom));
    backdrop-filter: blur(10px);
  }

  .panel-inner {
    flex-direction: row; /* 横向排列 */
    justify-content: space-around;
    max-width: 600px;
    margin: 0 auto;
  }

  :deep(.panel-btn), .panel-btn {
    margin-bottom: 0; /* 移除底部间距 */
    background-color: transparent;
    box-shadow: none;
    border: none;
    width: auto;
    height: auto;
    flex-direction: column; /* 手机端图标和文字上下排列（可选） */
    gap: 4px;

    &:hover {
      transform: none;
    }
  }

  /* 隐藏桌面端的阴影等效果，让底部栏看起来更清爽 */
  .sidebar-btn {
    box-shadow: none !important;
  }
}

/* 如果屏幕极窄，隐藏举报按钮以腾出空间 */
@media screen and (max-width: 350px) {
  .report-btn {
    display: none;
  }
}
</style>