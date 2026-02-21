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
      <div v-if="commentList.length === 0" style="text-align: center; color: #999; padding: 40px 0">
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
import { getCommentPage, addComment } from "@/api/front/comment.js";
import { useUserStore } from "@/store/user.js";
import { ElMessage } from "element-plus";
import CommentBox from './CommentBox.vue';
import CommentItem from './CommentItem.vue';

const emit = defineEmits(['comment-success']);

const props = defineProps({
  articleId: { type: [String, Number], required: true },
  initialTotal: { type: Number, default: 0 }
});

const userStore = useUserStore();
const commentList = ref([]);
const displayTotal = ref(props.initialTotal);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const mainBoxRef = ref(null);

// 修改点 2：新增排序状态 (1: 最新, 2: 最热)
const sortType = ref(1);

// 全局控制回复框状态
const activeReplyId = ref(null); // 当前正在回复的评论ID

// 提供给子组件调用的方法：设置当前回复ID
const setActiveReplyId = (id) => {
  if (activeReplyId.value === id) {
    // 如果点击的是当前已展开的，则关闭
    activeReplyId.value = null;
  } else {
    // 否则展开新的（会自动关闭旧的）
    activeReplyId.value = id;
  }
};

// 注入给所有后代组件 (CommentItem)
provide('commentState', {
  activeReplyId,
  setActiveReplyId
});

// 修改点 3：切换排序方法
const handleSortChange = (type) => {
  if (sortType.value === type) return; // 如果点击的是当前已选的排序，不进行任何操作
  sortType.value = type;
  pageNum.value = 1; // 切换排序后回到第一页
  loadComment();
};

// 加载数据
const loadComment = async () => {
  loading.value = true;
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    articleId: props.articleId,
    sortType: sortType.value // 修改点 4：把排序参数传给后端
  };
  try {
    const res = await getCommentPage(params);
    if (res.code === 200) {
      commentList.value = res.data.records;
      total.value = res.data.total;
      // 翻页或刷新时，关闭所有回复框
      activeReplyId.value = null;
    }
  } finally {
    loading.value = false;
  }
};

// 提交主评论
const submitMainComment = async (content) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }

  const postData = {
    content: content,
    parentId: null, // 主评论
    userId: userStore.userId,
    articleId: props.articleId
  };

  try {
    const res = await addComment(postData);
    if (res.code === 200) {
      ElMessage.success('评论成功');
      mainBoxRef.value?.clear(); // 清空输入框

      // 发表评论后，为了让用户立刻看到自己的评论，强制切回“最新”排序，并回到第一页
      sortType.value = 1;
      pageNum.value = 1;

      displayTotal.value++;
      await loadComment(); // 刷新列表
      emit('comment-success');
    } else {
      ElMessage.error(res.msg);
    }
  } catch (e) {
    console.error(e);
  }
};

// 回复子评论成功后触发
const handleReplySuccess = async () => {
  await loadComment();
  emit('comment-success');
};

// 监听文章ID变化
watch(() => props.articleId, () => {
  pageNum.value = 1;
  sortType.value = 1; // 切换文章时重置为最新排序
  loadComment();
});

watch(() => props.initialTotal, (val) => {
  displayTotal.value = val;
});

onMounted(() => {
  loadComment();
});
</script>

<style scoped>
.comment-container {
  margin-top: 20px;
  border-radius: 8px;
}

/* 专门定位主评论区域内的提交按钮 */
.main-input-wrapper :deep(.el-button--primary) {
  width: 80px;  /* 自定义宽度 */
  height: 30px;  /* 自定义高度 */
}

/* === 新增：头部与排序切换样式 === */
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.comment-header .title {
  font-weight: bold;
  font-size: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
  line-height: 1; /* 保持对齐 */
}

.comment-header .title .count {
  font-size: 14px;
  color: #999;
  font-weight: normal;
  margin-left: 5px;
}

.sort-tabs {
  font-size: 14px;
  color: #9499a0;
  display: flex;
  align-items: center;
}

.sort-tabs span {
  cursor: pointer;
  transition: color 0.2s;
}

.sort-tabs span:hover {
  color: #409EFF;
}

.sort-tabs span.active {
  color: #222;
  font-weight: bold;
}
</style>