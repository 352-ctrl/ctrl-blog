<template>
  <div class="mini-article-list">
    <div
        v-for="(item, index) in articles"
        :key="item.id"
        class="mini-item animate__animated animate__fadeIn"
        :class="itemClass(item, index)"
        @click="handleCardClick(item.id)"
    >
      <slot name="prefix" :item="item" :index="index"></slot>

      <el-image
          v-if="item.cover"
          class="article-cover"
          :src="item.cover"
          fit="cover"
          lazy
      >
        <template #error>
          <div class="image-slot">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <div v-else class="article-cover empty-cover">
        <el-icon><Picture /></el-icon>
      </div>

      <div class="article-info">
        <div class="title" :title="item.title">{{ item.title }}</div>
        <div class="meta-row">
          <span class="date">{{ item.createTime?.substring(0, 10) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

const props = defineProps({
  articles: {
    type: Array,
    default: () => []
  },
  // 动态类名回调：允许父组件根据 item 状态动态注入 class
  itemClass: {
    type: Function,
    default: () => ''
  }
})

const emit = defineEmits(['click'])

const handleCardClick = (id) => {
  emit('click', id)
}
</script>

<style scoped lang="scss">
.mini-article-list {
  display: flex;
  flex-direction: column;
}

.mini-item {
  display: flex;
  align-items: center;
  padding: 12px 10px;
  margin: 0 -10px;
  border-radius: 8px;
  border-bottom: 1px dashed transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative; /* 为父组件注入左侧指示条留身位 */

  /* 底部分割线 */
  &:not(:last-child) {
    border-bottom-color: var(--el-border-color-lighter);
  }

  /* ====== 鼠标悬浮特效 ====== */
  &:hover {
    background-color: var(--el-fill-color-light);
    border-bottom-color: transparent;
    transform: translateX(4px); /* 悬浮右移，交互感拉满 */

    .article-info .title {
      color: var(--el-color-primary);
    }
    .article-cover {
      transform: scale(1.05);
    }
  }

  /* 封面图样式 */
  .article-cover {
    width: 65px;
    height: 48px;
    border-radius: 6px;
    margin-right: 14px;
    flex-shrink: 0;
    transition: transform 0.3s ease;

    &.empty-cover, .image-slot {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      background: var(--el-fill-color-light);
      color: var(--el-text-color-secondary);
      font-size: 18px;
      border-radius: 6px;
    }
  }

  /* 文章文本信息 */
  .article-info {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 6px;

    .title {
      font-size: 14px;
      color: var(--el-text-color-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      transition: color 0.3s;
      font-weight: 500;
    }

    .meta-row {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        transition: color 0.3s;
      }

      :deep(.el-tag) {
        border-color: transparent;
        background-color: var(--el-fill-color);
        height: 20px;
        padding: 0 6px;
      }
    }
  }
}
</style>