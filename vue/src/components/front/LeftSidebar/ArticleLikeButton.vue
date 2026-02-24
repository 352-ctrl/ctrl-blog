<template>
  <el-button
      circle
      class="panel-btn like-btn"
      :class="{ 'is-active': localLiked }"
      @click="handleLike"
      :loading="loading"
  >
    <el-icon size="20" class="icon-wrapper">
      <IconThumbUpFill v-if="localLiked" />
      <IconThumbUpLine v-else />
    </el-icon>

    <span class="badge" v-if="localLikeCount > 0">{{ localLikeCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useUserStore } from "@/store/user.js"; // 引入用户状态
import { likeArticle, cancelLikeArticle } from "@/api/front/articleLike.js";
import { ElMessage } from "element-plus";
import IconThumbUpFill from "@/components/common/Icon/IconThumbUpFill.vue";
import IconThumbUpLine from "@/components/common/Icon/IconThumbUpLine.vue";

// 接收参数
const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  },
  liked: {
    type: Boolean,
    default: false
  },
  count: {
    type: [Number, String],
    default: 0
  }
});

// 定义事件
const emit = defineEmits(['update:liked', 'update:count', 'change']);

const userStore = useUserStore();
const loading = ref(false);

// 本地状态（用于乐观更新）
const localLiked = ref(props.liked);
const localLikeCount = ref(Number(props.count) || 0);

// 监听 props 变化（处理父组件数据异步加载的情况）
watch(() => props.liked, (val) => localLiked.value = val);
watch(() => props.count, (val) => {
  localLikeCount.value = Number(val) || 0;
});

const handleLike = async () => {
  // 1. 登录校验
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后点赞');
    return;
  }

  // 2. 防抖
  if (loading.value) return;

  // 3. 记录旧状态（用于失败回滚）
  const previousLiked = localLiked.value;
  const previousCount = localLikeCount.value;

  // 4. 乐观更新 UI (先变色，让用户感觉很快)
  if (previousLiked) {
    localLiked.value = false;
    localLikeCount.value = Math.max(0, previousCount - 1); // 防止减成负数
  } else {
    localLiked.value = true;
    localLikeCount.value = previousCount + 1;
  }

  // 通知父组件 UI 变化
  emit('change', { liked: localLiked.value, count: localLikeCount.value });

  loading.value = true;

  try {
    let res;

    if (localLiked.value) {
      res = await likeArticle(props.articleId);
    } else {
      res = await cancelLikeArticle(props.articleId);
    }

    // 6. 成功：使用后端返回的最新精准数量覆盖本地
    if (res.code === 200) {
      const finalCount = (res.data !== null && res.data !== undefined)
          ? Number(res.data)
          : localLikeCount.value;

      emit('update:count', res.data);
      emit('update:liked', localLiked.value);

      // 修正本地显示（以防万一）
      localLikeCount.value = finalCount;
    } else {
      throw new Error(res.msg || '操作异常');
    }

  } catch (error) {
    console.error(error);
    // 7. 失败：回滚到旧状态
    localLiked.value = previousLiked;
    localLikeCount.value = previousCount;
    // 回滚父组件数据
    emit('update:count', previousCount);
    emit('update:liked', previousLiked);
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
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