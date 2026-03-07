<template>
  <el-card class="comment-container" shadow="never">
    <div class="main-input-wrapper">
      <div class="comment-header">
        <div class="title">
          评论 <span class="count">({{ displayTotal }})</span>
        </div>
        <div class="sort-tabs" v-if="displayTotal > 0">
          <span :class="{ active: sortType === 2 }" @click="handleSortChange(2)">最热</span>
          <el-divider direction="vertical" />
          <span :class="{ active: sortType === 1 }" @click="handleSortChange(1)">最新</span>
        </div>
      </div>

      <CommentBox
          ref="mainBoxRef"
          placeholder="发表你的看法..."
          submit-text="发布"
          :rows="4"
          @submit="submitMainComment"
      />
    </div>

    <el-divider />

    <div class="comment-list-wrapper" v-loading="loading">
      <div v-if="commentList.length === 0" class="empty-comment">
        暂无评论，快来抢沙发吧~
      </div>

      <div v-else>
        <CommentItem
            v-for="item in commentList"
            :key="item.id"
            :data="item"
            :article-id="articleId"
            @reply-success="handleReplySuccess"
        />
      </div>
    </div>

    <ReportDialog ref="reportDialogRef" />

    <FrontPagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @change="loadComment"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, provide } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getCommentPage, addComment, getCommentLocatorPage } from "@/api/front/interaction/comment.js";
import { useUserStore } from "@/store/user.js";
import { ElMessage } from "element-plus";
import CommentBox from './CommentBox.vue';
import CommentItem from './CommentItem.vue';
import FrontPagination from '@/components/front/FrontPagination.vue';
import { useRequest } from "@/composables/useRequest.js";

const emit = defineEmits(['comment-success']);
const props = defineProps({ articleId: { type: [String, Number], required: true }, initialTotal: { type: Number, default: 0 } });

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const commentList = ref([]);
const displayTotal = ref(props.initialTotal);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const mainBoxRef = ref(null);
const reportDialogRef = ref(null);
const sortType = ref(1);

const openReportDialog = (targetId, targetType = 'COMMENT') => reportDialogRef.value?.open(targetType, targetId);
const activeReplyId = ref(null);
const targetCommentIdRef = ref(null);

const setActiveReplyId = (id) => activeReplyId.value = activeReplyId.value === id ? null : id;

provide('commentState', { activeReplyId, setActiveReplyId, targetCommentIdRef, openReportDialog });

// ======= Hooks 接入 =======
const { loading, execute: fetchComments } = useRequest(getCommentPage);
const { execute: submitCommentApi } = useRequest(addComment, { successMsg: '评论成功' });
const { execute: fetchLocator } = useRequest(getCommentLocatorPage);

const handleSortChange = (type) => {
  if (sortType.value === type) return;
  sortType.value = type;
  pageNum.value = 1;
  loadComment();
};

const initAndCheckTargetComment = async () => {
  const targetCommentId = route.query.commentId;
  if (targetCommentId) {
    targetCommentIdRef.value = targetCommentId;
    sortType.value = 1;
    try {
      const resData = await fetchLocator(targetCommentId, pageSize.value);
      if (resData) pageNum.value = resData; // resData 就是页码
    } catch (error) {}
  }
  await loadComment();
  if (targetCommentId) executeHighlightAndScroll(targetCommentId);
};

const executeHighlightAndScroll = (commentId) => {
  let attempts = 0;
  const timer = setInterval(() => {
    attempts++;
    const targetElement = document.getElementById(`comment-node-${commentId}`);
    if (targetElement) {
      clearInterval(timer);
      targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      targetElement.classList.add('highlight-flash');

      setTimeout(() => {
        targetElement.classList.remove('highlight-flash');
        targetCommentIdRef.value = null;
        const newQuery = { ...route.query };
        delete newQuery.commentId;
        const queryString = Object.keys(newQuery).map(key => `${key}=${newQuery[key]}`).join('&');
        const newPath = queryString ? `${route.path}?${queryString}` : route.path;
        window.history.replaceState(history.state, '', newPath);
      }, 3000);
    } else if (attempts >= 15) {
      clearInterval(timer);
    }
  }, 200);
};

const loadComment = async () => {
  const resData = await fetchComments({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    articleId: props.articleId,
    sortType: sortType.value
  });

  if (resData) {
    commentList.value = resData.records;
    total.value = resData.total;
    activeReplyId.value = null;
  }
};

const submitMainComment = async (content) => {
  if (!content || !content.trim()) return ElMessage.warning('评论内容不能为空');
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录后评论');

  try {
    await submitCommentApi({ content, parentId: null, userId: userStore.userId, articleId: props.articleId });
    mainBoxRef.value?.clear();
    sortType.value = 1;
    pageNum.value = 1;
    displayTotal.value++;
    await loadComment();
    emit('comment-success');
  } catch (e) {} // 异常由 Hook 处理
};

const handleReplySuccess = async () => {
  await loadComment();
  emit('comment-success');
};

watch(() => props.articleId, () => {
  if (route.query.commentId) initAndCheckTargetComment();
  else { pageNum.value = 1; sortType.value = 1; loadComment(); }
});

watch(() => route.query.commentId, (newVal) => {
  if (newVal && props.articleId) initAndCheckTargetComment();
});

watch(() => props.initialTotal, (val) => displayTotal.value = val);

onMounted(() => initAndCheckTargetComment());
</script>

<style scoped>
.comment-container {
  margin-top: 20px;
  border-radius: 8px;
}
.main-input-wrapper :deep(.el-button--primary) {
  width: 80px;
  height: 30px;
}
.empty-comment {
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 40px 0;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}
.comment-header .title {
  color: var(--el-text-color-primary);
  font-weight: bold;
  font-size: 20px;
  border-left: 4px solid var(--el-color-primary);
  padding-left: 10px;
  line-height: 1;
}
.comment-header .title .count {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
  margin-left: 5px;
}
.sort-tabs {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.sort-tabs :deep(.el-divider) {
  display: none;
}
.sort-tabs span {
  cursor: pointer;
  padding: 6px 16px;
  border-radius:5px;
  color: var(--el-text-color-regular);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.sort-tabs span:hover {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}
.sort-tabs span.active {
  color: var(--el-color-primary);
  font-weight: 600;
  background-color: var(--el-color-primary-light-9);
}
</style>