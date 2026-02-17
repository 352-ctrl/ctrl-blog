<template>
  <div class="comment-item" :class="{ 'is-sub-item': isSub }">
    <div style="display: flex; align-items: flex-start; gap: 10px; padding: 10px 0">
      <el-avatar
          v-if="data.userAvatar"
          :size="isSub ? 24 : 34"
          :src="data.userAvatar"
          style="flex-shrink: 0"
      />
      <el-avatar
          v-else
          :size="isSub ? 24 : 34"
          :src="defaultAvatar"
          style="flex-shrink: 0"
      />

      <div style="flex: 1; min-width: 0;">
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
            <el-icon size="16" style="margin-right: 2px"><ChatDotRound /></el-icon>

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
              <span style="display: flex; align-items: center; gap: 4px;">
                查看全部 {{ data.replyCount }} 条回复
                <el-icon><ArrowDown /></el-icon>
              </span>
            </div>
            <div v-else @click="isExpanded = false" class="expand-btn">
              <span style="display: flex; align-items: center; gap: 4px;">
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
import defaultAvatar from '@/assets/images/default-avatar.png';
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
/* 容器样式微调 */
.comment-item {
  transition: background-color 0.2s;
}

/* 子评论项的样式调整 */
.is-sub-item {
  padding-top: 5px !important;
  padding-bottom: 5px !important;
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
  color: #61666d; /* B站风格昵称色 */
}
.sub-nickname {
  font-size: 13px;
  color: #61666d;
}

/* === 2. 内容行 === */
.comment-content {
  color: #18191c;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 4px;
  word-break: break-all; /* 防止长英文不换行 */
}
.sub-content {
  font-size: 14px;
  line-height: 1.5;
}

.reply-target {
  color: #409EFF;
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
  color: #9499a0; /* 弱化灰色 */
}
.reply-btn {
  padding: 0;
  height: auto;
  font-size: 12px;
  color: #9499a0; /* 默认灰色 */
  display: flex; /* 让图标和文字对齐 */
  align-items: center;
}
.reply-btn:hover {
  color: #409EFF; /* 悬停变色 */
  background: transparent;
}

/* 回复框容器 */
.reply-box-wrapper {
  margin-top: 10px;
}

/* === 子评论容器 (对齐调整) === */
.sub-comment-wrapper {
  margin-top: 10px;
  border-radius: 4px;
  padding: 10px 0; /* 增加内边距 */
}

/* 展开控制条样式 */
.expand-control {
  margin-top: 8px;
  font-size: 12px;
  color: #9499a0;
  /* 如果 sub-comment-wrapper 有 padding，这里就不需要 padding-left 了 */
}

.expand-btn {
  cursor: pointer;
  display: inline-block;
  padding: 2px 0;
  transition: all 0.2s;
}

.expand-btn:hover {
  color: #409EFF;
}
</style>