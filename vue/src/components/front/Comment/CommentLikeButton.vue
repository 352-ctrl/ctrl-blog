<template>
  <el-button
      text
      size="small"
      class="like-btn"
      :class="{ 'is-active': isLiked }"
      @click="handleLike"
      :loading="loading"
  >
    <el-icon size="16" class="icon-wrapper">
      <IconThumbUpFill v-if="isLiked" />
      <IconThumbUpLine v-else />
    </el-icon>

    <span class="count-text">
      {{ displayCount }}
    </span>
  </el-button>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useUserStore } from "@/store/user.js";
import { likeComment, cancelLikeComment } from "@/api/front/comment.js";
import { ElMessage } from "element-plus";
import IconThumbUpFill from '@/components/common/Icon/IconThumbUpFill.vue';
import IconThumbUpLine from '@/components/common/Icon/IconThumbUpLine.vue';

const props = defineProps({
  commentId: { type: [String, Number], required: true },
  initialCount: { type: [Number, String], default: 0 },
  initialLiked: { type: Boolean, default: false }
});

const userStore = useUserStore();
const loading = ref(false);

const likeCount = ref(Number(props.initialCount) || 0);
const isLiked = ref(props.initialLiked);

const displayCount = computed(() => {
  return likeCount.value > 0 ? likeCount.value : '点赞';
});

watch(() => props.initialCount, (val) => likeCount.value = val);
watch(() => props.initialLiked, (val) => {
  isLiked.value = Number(val) || 0;
});

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后点赞');
    return;
  }

  if (loading.value) return;

  // 1. 乐观更新
  const previousLiked = isLiked.value;
  const previousCount = likeCount.value;

  if (previousLiked) {
    isLiked.value = false;
    likeCount.value = Math.max(0, previousCount - 1);
  } else {
    isLiked.value = true;
    likeCount.value = previousCount + 1;
  }

  loading.value = true;

  try {
    let res;
    if (previousLiked) {
      res = await cancelLikeComment(props.commentId);
    } else {
      res = await likeComment(props.commentId);
    }

    if (res.code !== 200) {
      throw new Error(res.msg);
    }
  } catch (error) {
    console.error(error);
    isLiked.value = previousLiked;
    likeCount.value = previousCount;
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.like-btn {
  padding: 0 4px;
  height: auto;
  font-size: 13px;
  color: var(--el-text-color-secondary); /* 替换 #8a919f */
  display: flex;
  align-items: center;
  border: none;
  background: transparent;
  transition: all 0.2s;
}

.like-btn:hover {
  background: transparent;
  color: var(--el-text-color-primary); /* 替换 #515767 */
}

/* 计数文字 */
.count-text {
  margin-left: 4px;
  font-weight: 400;
}

/* 图标容器 */
.icon-wrapper {
  transition: transform 0.2s;
}

/* 激活状态 (点赞后) */
.like-btn.is-active {
  color: var(--el-color-primary); /* 替换 #1e80ff */
}

.like-btn.is-active:hover {
  color: var(--el-color-primary); /* 替换 #1e80ff */
}

/* 点赞时的微动画 */
.like-btn.is-active .icon-wrapper {
  animation: scale-up 0.3s ease-in-out;
}

@keyframes scale-up {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}
</style>