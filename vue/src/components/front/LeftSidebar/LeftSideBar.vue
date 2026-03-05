<template>
  <div class="suspended-panel">

    <ArticleLikeButton
        v-if="articleId"
        :articleId="articleId"
        :liked="isLiked"
        :count="likeCount"
        @update:liked="(val) => $emit('update:isLiked', val)"
        @update:count="(val) => $emit('update:likeCount', val)"
    />

    <ArticleCommentButton
        :count="commentCount"
        @click="scrollToComment"
    />

    <ArticleFavoriteButton
        v-if="articleId"
        :articleId="articleId"
        :isFavorite="isFavorite"
        :count="favoriteCount"
        @update:isFavorite="(val) => $emit('update:isFavorite', val)"
        @update:count="(val) => $emit('update:favoriteCount', val)"
    />

    <div class="panel-btn report-btn" @click="handleReportArticle" title="举报文章">
      <el-icon size="20" class="icon-wrapper"><Warning /></el-icon>
    </div>

    <ReportDialog ref="reportDialogRef" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { defineProps, defineEmits } from 'vue';
import { ElMessage } from "element-plus";
import ArticleLikeButton from './ArticleLikeButton.vue';
import ArticleFavoriteButton from './ArticleFavoriteButton.vue';
import ArticleCommentButton from './ArticleCommentButton.vue';
import {useUserStore} from "@/store/user.js";


// 接收外部传来的数据
const props = defineProps({
  articleId: {
    type: [String, Number],
    default: ''
  },
  isLiked: {
    type: Boolean,
    default: false
  },
  likeCount: {
    type: Number,
    default: 0
  },
  commentCount: {
    type: Number,
    default: 0
  },
  isFavorite: {
    type: Boolean,
    default: false
  },
  favoriteCount: {
    type: Number,
    default: 0
  }
});

// 定义向外抛出的事件
const emit = defineEmits([
  'update:isLiked',
  'update:likeCount',
  'update:isFavorite',
  'update:favoriteCount',
  'scrollToComment'
]);

const scrollToComment = () => {
  emit('scrollToComment');
};


const reportDialogRef = ref(null);
const userStore = useUserStore();

// 处理文章举报
const handleReportArticle = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后举报');
    return;
  }
  // 打开弹窗，传入类型 'ARTICLE' 和 文章ID
  reportDialogRef.value.open('ARTICLE', props.articleId);
};
</script>

<style scoped lang="scss">
/* 核心容器样式 */
.suspended-panel {
  position: fixed;
  top: 180px;
  margin-left: -5rem;
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 按钮通用样式 (备用防御性样式，虽本页面未直接用但已修改适配暗黑) */
.panel-btn {
  position: relative;
  width: 50px;
  height: 50px;
  background-color: var(--el-bg-color-overlay);
  background-position: 50%;
  background-repeat: no-repeat;
  border-radius: 50%;
  box-shadow: var(--el-box-shadow-light);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--el-text-color-secondary);
  transition: all 0.3s;

  &:hover {
    box-shadow: var(--el-box-shadow);
    color: var(--el-text-color-primary);
  }
}

.sprite-icon, .icon-wrapper {
  width: 20px;
  height: 20px;
  font-size: 20px;
}

/* 分享弹窗 (备用防御性样式) */
.share-popup {
  display: none;
  position: absolute;
  margin-left: 10px;
  padding: 10px;
  background: var(--el-bg-color-overlay);
  border-radius: 4px;
  box-shadow: var(--el-box-shadow-light);
  white-space: nowrap;
  z-index: 101;
  left: 100%;
  top: 0;
}

.share-btn-wrapper:hover .share-popup {
  display: block;
}

.share-item {
  padding: 8px 12px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  &:hover {
    background-color: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}

.panel-btn.report-btn:hover {
  color: var(--el-color-danger); /* 鼠标悬浮变红色，提示操作警告 */
}
</style>