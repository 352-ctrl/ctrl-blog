<template>
  <div :id="`comment-node-${data.id}`" class="comment-item" :class="{ 'is-sub-item': isSub }">
    <div class="comment-item-inner">
      <UserInfoPopover
          :user-id="data.userId"
          :avatar="data.userAvatar"
          :nickname="data.userNickname"
          :bio="data.userBio"
      >
        <el-avatar
            :size="isSub ? 24 : 34"
            :src="data.userAvatar"
            class="user-avatar"
        >
          <el-icon><User /></el-icon>
        </el-avatar>
      </UserInfoPopover>

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
          <span class="time">{{ formatTimeAgo(data.createTime) }}</span>

          <CommentLikeButton
              :comment-id="data.id"
              :initial-count="data.likeCount"
              :initial-liked="data.liked"
              @update:liked="val => data.liked = val"
              @update:count="val => data.likeCount = val"
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

          <el-dropdown trigger="hover" placement="bottom" @command="handleCommand" class="more-dropdown">
            <el-button text size="small" class="more-btn">
              <el-icon size="14"><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="report">
                  <div class="dropdown-item-wrapper">
                    <el-icon><Warning /></el-icon>
                    <span>举报</span>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item
                    v-if="userStore.userId === data.userId"
                    command="delete"
                    divided
                >
                  <div class="dropdown-item-wrapper delete-action">
                    <el-icon><Delete /></el-icon>
                    <span>删除</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
              :reply-to-user="
                (!child.replyUserNickname || child.replyUserNickname === data.userNickname || child.replyUserNickname === child.userNickname)
                  ? ''
                  : child.replyUserNickname
              "
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
import { ref, computed, inject, watch } from 'vue';
import { formatTimeAgo } from '@/utils/date.js';
import CommentBox from './CommentBox.vue';
import CommentLikeButton from './CommentLikeButton.vue';
import { addComment, deleteComment } from "@/api/front/interaction/comment.js";
import { useUserStore } from "@/store/user.js";
import { useDialogStore } from "@/store/dialog.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { useRequest } from "@/composables/useRequest.js"; // 引入 Hook

defineOptions({ name: 'CommentItem' });

const props = defineProps({
  data: { type: Object, required: true },
  articleId: { type: [String, Number], required: true },
  replyToUser: { type: String, default: '' },
  isSub: { type: Boolean, default: false }
});

const emit = defineEmits(['reply-success']);
const userStore = useUserStore();
const dialogStore = useDialogStore();

const { activeReplyId, setActiveReplyId, targetCommentIdRef } = inject('commentState');
const isReplying = computed(() => activeReplyId.value === props.data.id);

const FOLD_THRESHOLD = 3;
const isExpanded = ref(false);

watch(() => targetCommentIdRef?.value, (newTargetId) => {
  if (newTargetId && props.data.children && props.data.children.length > 0) {
    if (props.data.children.some(child => String(child.id) === String(newTargetId))) {
      isExpanded.value = true;
    }
  }
}, { immediate: true });

const displayedChildren = computed(() => {
  const children = props.data.children || [];
  return (isExpanded.value || children.length <= FOLD_THRESHOLD) ? children : children.slice(0, FOLD_THRESHOLD);
});

const toggleReplyBox = () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录后回复');
  setActiveReplyId(props.data.id);
};

const closeReplyBox = () => setActiveReplyId(null);

// ======= Hooks 接入 =======
const { execute: execReply } = useRequest(addComment, { successMsg: '回复成功' });
const { execute: execDelete } = useRequest(deleteComment, { successMsg: '删除成功' });

const handleSubmitReply = async (content) => {
  try {
    await execReply({ content, parentId: props.data.id, userId: userStore.userId, articleId: props.articleId });
    setActiveReplyId(null);
    if (props.data.replyCount >= FOLD_THRESHOLD) isExpanded.value = true;
    emit('reply-success');
  } catch (e) {} // 错误已由 Hook 提示
};

const handleCommand = async (command) => {
  if (command === 'report') {
    if (!userStore.isLoggedIn) return ElMessage.warning('请先登录后操作');
    dialogStore.openReportDialog(props.data.id, 'COMMENT');
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这条评论吗？删除后不可恢复。', '提示', { type: 'warning' });
      await execDelete(props.data.id);
      emit('reply-success'); // 通知重新加载
    } catch (e) {}
  }
};
</script>

<style scoped>
.comment-item {
  transition: background-color 0.2s;
  border-radius: 6px; /* 增加圆角让高亮更美观 */
}
.comment-item-inner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0 10px 10px;
}
.user-avatar { flex-shrink: 0; }
.comment-main-content {
  flex: 1;
  min-width: 0;
  width: 100%;
  padding-right: 10px;
}
.is-sub-item .comment-main-content {
  padding-right: 0;
}
.reply-icon { margin-right: 2px; }
.expand-text { display: flex; align-items: center; gap: 4px; }
.is-sub-item .comment-item-inner { padding-top: 5px; padding-bottom: 5px; }
.info-row { display: flex; align-items: center; margin-bottom: 2px; }
.nickname { font-size: 14px; font-weight: bold; color: var(--el-text-color-secondary); }
.sub-nickname { font-size: 13px; color: var(--el-text-color-secondary); }
.comment-content { color: var(--el-text-color-primary); font-size: 15px; line-height: 1.6; margin-bottom: 4px; word-break: break-all; }
.sub-content { font-size: 14px; line-height: 1.5; }
.reply-target { color: var(--el-color-primary); margin-right: 4px; font-weight: 500; font-size: 14px; }
.meta-row {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  width: 100%;
  margin-top: 4px;
}
.reply-btn { padding: 0; height: auto; font-size: 12px; color: var(--el-text-color-placeholder); display: flex; align-items: center; }
.reply-btn:hover { color: var(--el-color-primary); background: transparent; }
.reply-box-wrapper { margin-top: 10px; }
.sub-comment-wrapper { margin-top: 10px; border-radius: 4px; padding: 10px 0; }
.expand-control { margin-top: 8px; font-size: 12px; color: var(--el-text-color-placeholder); }
.expand-btn { cursor: pointer; display: inline-block; padding: 2px 0; transition: all 0.2s; }
.expand-btn:hover { color: var(--el-color-primary); }
.more-dropdown {
  margin-left: auto;
  margin-right: -4px;
}
.more-btn {
  padding: 0 4px;
  height: auto;
  color: var(--el-text-color-placeholder);
  display: flex;
  align-items: center;
}
.more-btn:hover {
  color: var(--el-text-color-primary);
  background: transparent;
}
.dropdown-item-wrapper {
  display: flex;
  align-items: center;
  gap: 6px; /* 控制图标和文字的间距 */
  font-size: 13px; /* 字体大小适配 */
  padding: 2px 4px; /* 稍微增加一点点击区域 */
}

/* 删除按钮的专属高亮警告色 */
.delete-action {
  color: var(--el-color-danger);
  transition: opacity 0.2s;
}

.delete-action:hover {
  opacity: 0.8; /* 悬浮时增加轻微的透明度反馈 */
}

/* 确保图标颜色与文字保持一致 */
.delete-action .el-icon {
  color: var(--el-color-danger);
}
</style>

<style>
/* ==========================================
 * 全局样式：高亮动画 (由于作用于 JS 动态添加的类，不能放在 scoped 里面)
 * ========================================== */
.highlight-flash {
  animation: comment-flash 3s ease-out forwards;
}

@keyframes comment-flash {
  0% { background-color: var(--el-color-primary-light-8); }
  20% { background-color: var(--el-color-primary-light-8); }
  100% { background-color: transparent; }
}

html.dark .highlight-flash {
  animation: comment-flash-dark 3s ease-out forwards;
}

@keyframes comment-flash-dark {
  0% { background-color: rgba(64, 158, 255, 0.2); }
  20% { background-color: rgba(64, 158, 255, 0.15); }
  100% { background-color: transparent; }
}
</style>