<template>
  <div class="comment-item" :class="{ 'is-sub-item': isSub }">
    <div class="comment-item-inner">
      <el-avatar
          :size="isSub ? 24 : 34"
          :src="data.userAvatar"
          class="user-avatar"
      />

      <div class="comment-main-content">
        <div class="info-row">
          <span class="nickname" :class="{ 'sub-nickname': isSub }">
            {{ data.userNickname }}
          </span>
        </div>

        <div class="comment-content" :class="{ 'sub-content': isSub }">
          <span v-if="replyToUser" class="reply-target">回复 @{{ replyToUser }} :</span>
          {{ data.content }}
        </div>

        <div class="meta-row">
          <span class="time">{{ data.createTime }}</span>

          <CommentLikeButton
              :comment-id="data.id"
              :initial-count="data.likeCount"
              :initial-liked="data.liked"
          />

          <el-button
              text
              size="small"
              class="reply-btn"
              @click="toggleReplyBox"
          >
            <el-icon size="16" class="reply-icon"><ChatDotRound /></el-icon>

            <span>
              {{ (isSub || !data.replyCount) ? '回复' : data.replyCount }}
            </span>
          </el-button>
        </div>

        <div v-if="isReplying" class="reply-box-wrapper">
          <CommentBox
              :placeholder="`回复 @${data.userNickname}`"
              submit-text="回复"
              :show-cancel="true"
              @cancel="closeReplyBox"
              @submit="handleSubmitReply"
          />
        </div>

        <div v-if="data.children && data.children.length > 0" class="sub-comment-wrapper">

          <CommentItem
              v-for="child in displayedChildren"
              :key="child.id"
              :data="child"
              :article-id="articleId"
              :reply-to-user="child.replyUserNickname || data.userNickname"
              :is-sub="true"
              @reply-success="$emit('reply-success')"
          />

          <div v-if="data.replyCount > FOLD_THRESHOLD" class="expand-control">
            <div v-if="!isExpanded" @click="isExpanded = true" class="expand-btn">
              <span class="expand-text">
                查看全部 {{ data.replyCount }} 条回复
                <el-icon><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-else @click="isExpanded = false" class="expand-btn">
              <span class="expand-text">
                收起 <el-icon><ArrowUp /></el-icon>
              </span>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue';
import CommentBox from './CommentBox.vue';
import CommentLikeButton from './CommentLikeButton.vue';
import { addComment } from "@/api/front/comment.js";
import { useUserStore } from "@/store/user.js";
import { ElMessage } from "element-plus";

defineOptions({ name: 'CommentItem' });

const props = defineProps({
  data: { type: Object, required: true },
  articleId: { type: [String, Number], required: true },
  replyToUser: { type: String, default: '' },
  isSub: { type: Boolean, default: false }
});

const emit = defineEmits(['reply-success']);
const userStore = useUserStore();

const { activeReplyId, setActiveReplyId } = inject('commentState');

const isReplying = computed(() => activeReplyId.value === props.data.id);

const FOLD_THRESHOLD = 3;
const isExpanded = ref(false);

const displayedChildren = computed(() => {
  const children = props.data.children || [];
  if (isExpanded.value || children.length <= FOLD_THRESHOLD) {
    return children;
  }
  return children.slice(0, FOLD_THRESHOLD);
});

const toggleReplyBox = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  setActiveReplyId(props.data.id);
};

const closeReplyBox = () => {
  setActiveReplyId(null);
};

const handleSubmitReply = async (content) => {
  const postData = {
    content: content,
    parentId: props.data.id,
    userId: userStore.userId,
    articleId: props.articleId
  };
  try {
    const res = await addComment(postData);
    if (res.code === 200) {
      ElMessage.success('回复成功');
      setActiveReplyId(null);
      emit('reply-success');
    } else {
      ElMessage.error(res.msg);
    }
  } catch (e) {
    console.error(e);
  }
};
</script>

<style scoped>
/* ====== 布局提取 ====== */
.comment-item {
  transition: background-color 0.2s;
}

.comment-item-inner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
}

.user-avatar {
  flex-shrink: 0;
}

.comment-main-content {
  flex: 1;
  min-width: 0;
}

.reply-icon {
  margin-right: 2px;
}

.expand-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 子评论项的样式调整 */
.is-sub-item .comment-item-inner {
  padding-top: 5px;
  padding-bottom: 5px;
}

/* === 1. 昵称行 === */
.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
}
.nickname {
  font-size: 14px;
  font-weight: bold;
  color: var(--el-text-color-secondary); /* 替换 #61666d */
}
.sub-nickname {
  font-size: 13px;
  color: var(--el-text-color-secondary); /* 替换 #61666d */
}

/* === 2. 内容行 === */
.comment-content {
  color: var(--el-text-color-primary); /* 替换 #18191c */
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 4px;
  word-break: break-all;
}
.sub-content {
  font-size: 14px;
  line-height: 1.5;
}

.reply-target {
  color: var(--el-color-primary); /* 替换 #409EFF */
  margin-right: 4px;
  font-weight: 500;
  font-size: 14px;
}

/* === 3. 底部信息行 (时间+操作) === */
.meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--el-text-color-placeholder); /* 替换 #9499a0 */
}
.reply-btn {
  padding: 0;
  height: auto;
  font-size: 12px;
  color: var(--el-text-color-placeholder); /* 替换 #9499a0 */
  display: flex;
  align-items: center;
}
.reply-btn:hover {
  color: var(--el-color-primary); /* 替换 #409EFF */
  background: transparent;
}

/* 回复框容器 */
.reply-box-wrapper {
  margin-top: 10px;
}

/* === 子评论容器 === */
.sub-comment-wrapper {
  margin-top: 10px;
  border-radius: 4px;
  padding: 10px 0;
}

/* 展开控制条样式 */
.expand-control {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder); /* 替换 #9499a0 */
}

.expand-btn {
  cursor: pointer;
  display: inline-block;
  padding: 2px 0;
  transition: all 0.2s;
}

.expand-btn:hover {
  color: var(--el-color-primary); /* 替换 #409EFF */
}
</style>