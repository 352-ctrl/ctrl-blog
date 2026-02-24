<template>
  <el-button
      circle
      class="panel-btn favorite-btn"
      :class="{ 'is-active': localIsFavorite }"
      @click="handleFavorite"
      :loading="loading"
  >
    <el-icon size="20" class="icon-wrapper">
      <StarFilled v-if="localIsFavorite" />
      <Star v-else />
    </el-icon>

    <span class="badge" v-if="localFavoriteCount > 0">{{ localFavoriteCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useUserStore } from "@/store/user.js";
import { favoriteArticle, cancelFavoriteArticle } from "@/api/front/articleFavorite.js";
import { ElMessage } from "element-plus";

// 接收参数
const props = defineProps({
  articleId: {
    type: [String, Number],
    required: true
  },
  isFavorite: {
    type: Boolean,
    default: false
  },
  count: {
    type: [Number, String],
    default: 0
  }
});

// 定义事件
const emit = defineEmits(['update:isFavorite', 'update:count', 'change']);

const userStore = useUserStore();
const loading = ref(false);

// 本地状态（用于乐观更新）
const localIsFavorite = ref(props.isFavorite);
const localFavoriteCount = ref(Number(props.count) || 0);

// 监听 props 变化（处理父组件数据异步加载的情况）
watch(() => props.isFavorite, (val) => localIsFavorite.value = val);
watch(() => props.count, (val) => {
  localFavoriteCount.value = Number(val) || 0;
});

const handleFavorite = async () => {
  // 1. 登录校验
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后收藏');
    return;
  }

  // 2. 防抖
  if (loading.value) return;

  // 3. 记录旧状态（用于失败回滚）
  const previousIsFavorite = localIsFavorite.value;
  const previousFavoriteCount = localFavoriteCount.value;

  // 4. 乐观更新 UI
  if (previousIsFavorite) {
    // 取消收藏：状态变 false，数量 -1
    localIsFavorite.value = false;
    localFavoriteCount.value = Math.max(0, previousFavoriteCount - 1);
  } else {
    // 添加收藏：状态变 true，数量 +1
    localIsFavorite.value = true;
    localFavoriteCount.value = previousFavoriteCount + 1;
  }

  // 通知父组件 UI 变化
  emit('change', { isFavorite: localIsFavorite.value, count: localFavoriteCount.value });

  loading.value = true;

  try {
    let res;

    if (localIsFavorite.value) {
      res = await favoriteArticle(props.articleId);
    } else {
      res = await cancelFavoriteArticle(props.articleId);
    }

    // 6. 成功处理
    // 假设后端返回的数据结构与点赞接口一致：res.code === 200，res.data 返回最新数量
    // 如果后端收藏接口没有返回最新数量，则保留前端计算的值
    if (res.code === 200) {
      // 如果后端返回了最新的 count，则使用后端的；否则使用本地计算的
      const finalCount = (res.data !== null && res.data !== undefined)
          ? Number(res.data)
          : localFavoriteCount.value;

      // 同步最终精准状态给父组件
      emit('update:count', res.data);
      emit('update:isFavorite', localIsFavorite.value);

      // 修正本地显示（以防万一）
      localFavoriteCount.value = finalCount;
    } else {
      throw new Error(res.msg || '操作异常');
    }

  } catch (error) {
    console.error(error);
    // 7. 失败：回滚到旧状态
    localIsFavorite.value = previousIsFavorite;
    localFavoriteCount.value = previousFavoriteCount;

    // 回滚父组件数据
    emit('update:count', previousFavoriteCount);
    emit('update:isFavorite', previousIsFavorite);

    ElMessage.error(error.message || '收藏失败，请稍后重试');
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

  /* 收藏状态颜色：使用 Element 警告色（金黄色） */
  .is-active & {
    color: var(--el-color-warning);
  }
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
  background-color: var(--el-color-warning);
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