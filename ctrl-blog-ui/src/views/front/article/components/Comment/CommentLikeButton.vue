<template>
  <el-button
      text
      size="small"
      class="like-btn"
      :class="{ 'is-active': isLiked }"
      @click.stop="handleLike"
      :loading="isRequesting"
  >
    <el-icon size="16" class="icon-wrapper">
      <IconThumbUpFill v-if="isLiked" />
      <IconThumbUpLine v-else />
    </el-icon>
    <span class="count-text">{{ displayCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useUserStore } from "@/store/user.js";
import { likeComment, cancelLikeComment } from "@/api/front/interaction/comment.js";
import { ElMessage } from "element-plus";
import IconThumbUpFill from '@/components/common/Icon/IconThumbUpFill.vue';
import IconThumbUpLine from '@/components/common/Icon/IconThumbUpLine.vue';
import { useRequest } from "@/composables/useRequest.js";

const props = defineProps({
  commentId: { type: [String, Number], required: true },
  initialCount: { type: [Number, String], default: 0 },
  initialLiked: { type: Boolean, default: false }
});

const userStore = useUserStore();
const likeCount = ref(Number(props.initialCount) || 0);

const isLiked = ref(Boolean(props.initialLiked));

const displayCount = computed(() => likeCount.value > 0 ? likeCount.value : '点赞');

watch(() => props.initialCount, (val) => likeCount.value = Number(val) || 0);
watch(() => props.initialLiked, (val) => isLiked.value = Boolean(val));

const { loading: likeLoading, execute: execLike } = useRequest(likeComment);
const { loading: cancelLoading, execute: execCancel } = useRequest(cancelLikeComment);
const isRequesting = computed(() => likeLoading.value || cancelLoading.value);

const emit = defineEmits(['update:liked', 'update:count']);

const handleLike = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录后点赞');
  if (isRequesting.value) return;

  // 1. 乐观更新 UI
  const previousLiked = isLiked.value;
  const previousCount = likeCount.value;

  isLiked.value = !previousLiked;
  likeCount.value = previousLiked ? Math.max(0, previousCount - 1) : previousCount + 1;

  // 2. 发起请求
  try {
    // ⚠️ 检查点: 请根据你的 comment.js 方法签名确认这里需不需要传对象
    // 如果后端要求传对象，这里需要改成: await execLike({ commentId: props.commentId })
    if (previousLiked) {
      await execCancel(props.commentId);
    } else {
      await execLike(props.commentId);
    }
    emit('update:liked', isLiked.value);
    emit('update:count', likeCount.value);
  } catch (error) {
    // 3. 失败时回滚乐观更新
    isLiked.value = previousLiked;
    likeCount.value = previousCount;

    // 关键修复: 把之前吞掉的错误吐出来，让你知道到底哪里崩了！
    console.error('点赞请求异常:', error);
    ElMessage.error(error?.message || '操作失败，请重试');
  }
};
</script>

<style scoped>
/* 样式保持你原来的即可 */
.like-btn {
  padding: 0 4px;
  height: auto;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  border: none;
  background: transparent;
  transition: all 0.2s;
}

.like-btn:hover {
  background: transparent;
  color: var(--el-text-color-primary);
}

.count-text {
  margin-left: 4px;
  font-weight: 400;
}

.icon-wrapper {
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); /* 改了个更 Q 弹的动画曲线 */
}

.like-btn.is-active {
  color: var(--el-color-primary);
}

.like-btn.is-active:hover {
  color: var(--el-color-primary);
}

.like-btn.is-active .icon-wrapper {
  animation: scale-up 0.3s ease-in-out;
}

@keyframes scale-up {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}
</style>