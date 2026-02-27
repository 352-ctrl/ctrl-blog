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
import { getCommentPage, addComment, getCommentLocatorPage } from "@/api/front/comment.js";
import { useUserStore } from "@/store/user.js";
import { ElMessage } from "element-plus";
import CommentBox from './CommentBox.vue';
import CommentItem from './CommentItem.vue';
import FrontPagination from '@/components/front/FrontPagination/FrontPagination.vue';

const emit = defineEmits(['comment-success']);

const props = defineProps({
  articleId: { type: [String, Number], required: true },
  initialTotal: { type: Number, default: 0 }
});

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const commentList = ref([]);
const displayTotal = ref(props.initialTotal);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const mainBoxRef = ref(null);

const sortType = ref(1);

// 全局控制回复框状态和目标高亮 ID
const activeReplyId = ref(null);
const targetCommentIdRef = ref(null);

const setActiveReplyId = (id) => {
  activeReplyId.value = activeReplyId.value === id ? null : id;
};

// 注入给子组件，让子组件能够自动展开折叠
provide('commentState', {
  activeReplyId,
  setActiveReplyId,
  targetCommentIdRef
});

const handleSortChange = (type) => {
  if (sortType.value === type) return;
  sortType.value = type;
  pageNum.value = 1;
  loadComment();
};

// ==========================================
// 核心：强化版定位高亮逻辑 (轮询寻找 DOM)
// ==========================================
const initAndCheckTargetComment = async () => {
  const targetCommentId = route.query.commentId;

  if (targetCommentId) {
    targetCommentIdRef.value = targetCommentId; // 记录需要高亮的ID
    sortType.value = 1; // 后端分页基准是最新，必须切回最新
    try {
      const res = await getCommentLocatorPage(targetCommentId, pageSize.value);
      if (res.code === 200) {
        pageNum.value = res.data;
      }
    } catch (error) {
      console.error('获取评论页码失败:', error);
    }
  }

  await loadComment();

  if (targetCommentId) {
    executeHighlightAndScroll(targetCommentId);
  }
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

        // ==============================================================
        // 🚀 核心修复：使用原生 HTML5 History API 清理 URL，不触发 Router 滚动
        // ==============================================================
        const newQuery = { ...route.query };
        delete newQuery.commentId;

        // 将剩余的 query 参数重新拼装成字符串
        const queryString = Object.keys(newQuery)
            .map(key => `${key}=${newQuery[key]}`)
            .join('&');

        // 构建新的完整路径
        const newPath = queryString ? `${route.path}?${queryString}` : route.path;

        // 绕过 Vue Router，直接操作浏览器历史记录，这样绝对不会触发滚动回顶！
        window.history.replaceState(history.state, '', newPath);

      }, 3000);

    } else if (attempts >= 15) {
      clearInterval(timer);
      console.warn(`轮询结束，未能在页面找到 ID 为 comment-node-${commentId} 的元素`);
    }
  }, 200);
};

// ==========================================
// 基础逻辑
// ==========================================
const loadComment = async () => {
  loading.value = true;
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    articleId: props.articleId,
    sortType: sortType.value
  };
  try {
    const res = await getCommentPage(params);
    if (res.code === 200) {
      commentList.value = res.data.records;
      total.value = res.data.total;
      activeReplyId.value = null;
    }
  } finally {
    loading.value = false;
  }
};

const submitMainComment = async (content) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  const postData = {
    content: content,
    parentId: null,
    userId: userStore.userId,
    articleId: props.articleId
  };
  try {
    const res = await addComment(postData);
    if (res.code === 200) {
      ElMessage.success('评论成功');
      mainBoxRef.value?.clear();
      sortType.value = 1;
      pageNum.value = 1;
      displayTotal.value++;
      await loadComment();
      emit('comment-success');
    } else {
      ElMessage.error(res.msg);
    }
  } catch (e) {
    console.error(e);
  }
};

const handleReplySuccess = async () => {
  await loadComment();
  emit('comment-success');
};

// 监听文章变化
watch(() => props.articleId, () => {
  if (route.query.commentId) {
    initAndCheckTargetComment();
  } else {
    pageNum.value = 1;
    sortType.value = 1;
    loadComment();
  }
});

// 如果用户在当前文章页，打开了消息侧边栏并点击了同一篇文章的另一条消息
watch(() => route.query.commentId, (newVal) => {
  if (newVal && props.articleId) {
    initAndCheckTargetComment();
  }
});

watch(() => props.initialTotal, (val) => {
  displayTotal.value = val;
});

onMounted(() => {
  initAndCheckTargetComment();
});
</script>

<style scoped>
/* 保持原有样式不变... */
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