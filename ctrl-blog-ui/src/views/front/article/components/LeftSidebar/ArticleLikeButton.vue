<template>
  <el-button
      circle
      class="panel-btn like-btn"
      :class="{ 'is-active': localLiked }"
      @click="handleLike"
      :loading="isRequesting"
  >
    <el-icon size="20" class="icon-wrapper">
      <IconThumbUpFill v-if="localLiked" />
      <IconThumbUpLine v-else />
    </el-icon>

    <span class="badge" v-if="localLikeCount > 0">{{ localLikeCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useUserStore } from "@/store/user.js";
import { likeArticle, cancelLikeArticle } from "@/api/front/article/articleLike.js";
import { ElMessage } from "element-plus";
import IconThumbUpFill from "@/components/common/Icon/IconThumbUpFill.vue";
import IconThumbUpLine from "@/components/common/Icon/IconThumbUpLine.vue";
import { useRequest } from "@/composables/useRequest.js"; // 引入 Hook

const props = defineProps({
  articleId: { type: [String, Number], required: true },
  liked: { type: Boolean, default: false },
  count: { type: [Number, String], default: 0 }
});

const emit = defineEmits(['update:liked', 'update:count', 'change']);
const userStore = useUserStore();

// 本地状态（用于乐观更新）
const localLiked = ref(props.liked);
const localLikeCount = ref(Number(props.count) || 0);

watch(() => props.liked, (val) => localLiked.value = val);
watch(() => props.count, (val) => localLikeCount.value = Number(val) || 0);

// ======= Hooks 接入 =======
const { loading: likeLoading, execute: execLike } = useRequest(likeArticle);
const { loading: cancelLoading, execute: execCancel } = useRequest(cancelLikeArticle);
const isRequesting = computed(() => likeLoading.value || cancelLoading.value);

const handleLike = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录后点赞');
  if (isRequesting.value) return;

  const previousLiked = localLiked.value;
  const previousCount = localLikeCount.value;

  // 1. 乐观更新 UI
  if (previousLiked) {
    localLiked.value = false;
    localLikeCount.value = Math.max(0, previousCount - 1);
  } else {
    localLiked.value = true;
    localLikeCount.value = previousCount + 1;
  }
  emit('change', { liked: localLiked.value, count: localLikeCount.value });

  // 2. 发起真实请求
  try {
    let resultData; // useRequest 成功时直接返回 res.data
    if (previousLiked) {
      resultData = await execCancel(props.articleId);
    } else {
      resultData = await execLike(props.articleId);
    }

    const finalCount = (resultData !== null && resultData !== undefined) ? Number(resultData) : localLikeCount.value;
    emit('update:count', finalCount);
    emit('update:liked', localLiked.value);
    localLikeCount.value = finalCount;

  } catch (error) {
    // 3. 请求失败：回滚到旧状态，Hook 已自动提示错误信息
    localLiked.value = previousLiked;
    localLikeCount.value = previousCount;
    emit('update:count', previousCount);
    emit('update:liked', previousLiked);
  }
};
</script>

<style scoped lang="scss">
.panel-btn {
  /* 强制重置 el-button 样式 */
  &.el-button {
    border: none !important;
    margin-left: 0 !important;
    font-weight: normal;
    padding: 0;
  }

  position: relative;
  width: 50px !important;
  height: 50px !important;
  background-color: var(--el-bg-color-overlay) !important;
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
    background-color: var(--el-bg-color-overlay) !important;
  }

}

.icon-wrapper {
  font-size: 20px;
  width: 20px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  .is-active & { color: var(--el-color-primary); }
}

/* 徽标样式 */
.badge {
  position: absolute;
  top: 0;
  left: 75%;
  height: 17px;
  line-height: 17px;
  padding: 0 5px;
  border-radius: 9px;
  font-size: 11px;
  text-align: center;
  white-space: nowrap;
  background-color: var(--el-text-color-disabled);
  color: #fff;
  transform: translateY(-20%);
  z-index: 10;
  transition: background-color 0.3s;
}

/* 激活时徽标背景变色 */
.panel-btn.is-active .badge {
  background-color: var(--el-color-primary);
}

/* 动画 */
.panel-btn.is-active .icon-wrapper {
  animation: scale-up 0.3s ease-in-out;
}

@keyframes scale-up {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}
</style>