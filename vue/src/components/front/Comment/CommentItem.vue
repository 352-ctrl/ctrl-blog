<template>
  <div :id="`comment-node-${data.id}`" class="comment-item" :class="{ 'is-sub-item': isSub }">
    <div class="comment-item-inner">
      <UserInfoPopover
          :user-id="data.userId"
          :avatar="data.userAvatar"
          :nickname="data.userNickname"
          :bio="data.userBio"
          @report-user="handleReportUser"
      >
        <el-avatar
            :size="isSub ? 24 : 34"
            :src="data.userAvatar"
            class="user-avatar"
        />
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

          <el-dropdown trigger="hover" placement="bottom" @command="handleCommand" style="margin-left: auto;">
            <el-button text size="small" class="more-btn">
              <el-icon size="14"><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="report">
                  <el-icon><Warning /></el-icon>
                  举报
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
import { ref, computed, inject, watch } from 'vue';
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

// 接收祖先组件传递下来的定位状态
const { activeReplyId, setActiveReplyId, targetCommentIdRef, openReportDialog } = inject('commentState');

const isReplying = computed(() => activeReplyId.value === props.data.id);

const FOLD_THRESHOLD = 3;
const isExpanded = ref(false);

// 核心：智能展开。如果跳转的目标 ID 藏在这个父评论的子评论列表里，强制展开折叠面板
watch(() => targetCommentIdRef?.value, (newTargetId) => {
  if (newTargetId && props.data.children && props.data.children.length > 0) {
    const hasTarget = props.data.children.some(child => String(child.id) === String(newTargetId));
    if (hasTarget) {
      isExpanded.value = true;
    }
  }
}, { immediate: true });

// 处理点击举报用户头像的事件
const handleReportUser = (userId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后操作');
    return;
  }

  openReportDialog(userId, 'USER');
};

const displayedChildren = computed(() => {
  const children = props.data.children || [];
  if (isExpanded.value || children.length <= FOLD_THRESHOLD) {
    return children;
  }
  return children.slice(0, FOLD_THRESHOLD);
});

const toggleReplyBox = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后回复');
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
      if (props.data.replyCount >= FOLD_THRESHOLD) {
        isExpanded.value = true;
      }
      emit('reply-success');
    } else {
      ElMessage.error(res.msg);
    }
  } catch (e) {
    console.error(e);
  }
};

// 处理下拉菜单的事件
const handleCommand = (command) => {
  if (command === 'report') {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后操作');
      return;
    }
    // 唤起父组件提供的举报弹窗方法，传入这条评论的 ID 以及类型 'COMMENT'
    openReportDialog(props.data.id, 'COMMENT');
  }
};
</script>

<style scoped>
/* 保持原有 scoped 样式不变... */
.comment-item {
  transition: background-color 0.2s;
  border-radius: 6px; /* 增加圆角让高亮更美观 */
}
.comment-item-inner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 10px; /* 两侧增加一点 padding，防止高亮背景贴边 */
}
.user-avatar { flex-shrink: 0; }
.comment-main-content { flex: 1; min-width: 0; }
.reply-icon { margin-right: 2px; }
.expand-text { display: flex; align-items: center; gap: 4px; }
.is-sub-item .comment-item-inner { padding-top: 5px; padding-bottom: 5px; }
.info-row { display: flex; align-items: center; margin-bottom: 2px; }
.nickname { font-size: 14px; font-weight: bold; color: var(--el-text-color-secondary); }
.sub-nickname { font-size: 13px; color: var(--el-text-color-secondary); }
.comment-content { color: var(--el-text-color-primary); font-size: 15px; line-height: 1.6; margin-bottom: 4px; word-break: break-all; }
.sub-content { font-size: 14px; line-height: 1.5; }
.reply-target { color: var(--el-color-primary); margin-right: 4px; font-weight: 500; font-size: 14px; }
.meta-row { display: flex; align-items: center; gap: 10px; font-size: 12px; color: var(--el-text-color-placeholder); }
.reply-btn { padding: 0; height: auto; font-size: 12px; color: var(--el-text-color-placeholder); display: flex; align-items: center; }
.reply-btn:hover { color: var(--el-color-primary); background: transparent; }
.reply-box-wrapper { margin-top: 10px; }
.sub-comment-wrapper { margin-top: 10px; border-radius: 4px; padding: 10px 0; }
.expand-control { margin-top: 8px; font-size: 12px; color: var(--el-text-color-placeholder); }
.expand-btn { cursor: pointer; display: inline-block; padding: 2px 0; transition: all 0.2s; }
.expand-btn:hover { color: var(--el-color-primary); }
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