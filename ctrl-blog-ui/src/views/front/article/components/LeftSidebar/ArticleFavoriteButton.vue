<template>
  <el-button
      circle
      class="panel-btn favorite-btn"
      :class="{ 'is-active': localIsFavorite }"
      @click="handleFavorite"
      :loading="isRequesting"
  >
    <el-icon size="20" class="icon-wrapper">
      <StarFilled v-if="localIsFavorite" />
      <Star v-else />
    </el-icon>

    <span class="badge" v-if="localFavoriteCount > 0">{{ localFavoriteCount }}</span>
  </el-button>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useUserStore } from "@/store/user.js";
import { favoriteArticle, cancelFavoriteArticle } from "@/api/front/article/articleFavorite.js";
import { ElMessage } from "element-plus";
import { useRequest } from "@/composables/useRequest.js"; // 引入 Hook

const props = defineProps({
  articleId: { type: [String, Number], required: true },
  isFavorite: { type: Boolean, default: false },
  count: { type: [Number, String], default: 0 }
});

const emit = defineEmits(['update:isFavorite', 'update:count', 'change']);
const userStore = useUserStore();

// 本地状态（用于乐观更新）
const localIsFavorite = ref(props.isFavorite);
const localFavoriteCount = ref(Number(props.count) || 0);

watch(() => props.isFavorite, (val) => localIsFavorite.value = val);
watch(() => props.count, (val) => localFavoriteCount.value = Number(val) || 0);

// ======= Hooks 接入 =======
const { loading: favLoading, execute: execFav } = useRequest(favoriteArticle);
const { loading: cancelLoading, execute: execCancel } = useRequest(cancelFavoriteArticle);
const isRequesting = computed(() => favLoading.value || cancelLoading.value);

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录后收藏');
  if (isRequesting.value) return;

  const previousIsFavorite = localIsFavorite.value;
  const previousFavoriteCount = localFavoriteCount.value;

  // 1. 乐观更新 UI
  if (previousIsFavorite) {
    localIsFavorite.value = false;
    localFavoriteCount.value = Math.max(0, previousFavoriteCount - 1);
  } else {
    localIsFavorite.value = true;
    localFavoriteCount.value = previousFavoriteCount + 1;
  }
  emit('change', { isFavorite: localIsFavorite.value, count: localFavoriteCount.value });

  // 2. 发起真实请求
  try {
    let resultData; // useRequest 成功时直接返回 res.data
    if (localIsFavorite.value) {
      resultData = await execFav(props.articleId);
    } else {
      resultData = await execCancel(props.articleId);
    }

    const finalCount = (resultData !== null && resultData !== undefined) ? Number(resultData) : localFavoriteCount.value;
    emit('update:count', finalCount);
    emit('update:isFavorite', localIsFavorite.value);
    localFavoriteCount.value = finalCount;

  } catch (error) {
    // 3. 请求失败：回滚到旧状态，Hook 已自动提示错误信息
    localIsFavorite.value = previousIsFavorite;
    localFavoriteCount.value = previousFavoriteCount;
    emit('update:count', previousFavoriteCount);
    emit('update:isFavorite', previousIsFavorite);
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