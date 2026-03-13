<template>
  <div class="right-sidebar-wrapper hide-md-down">
    <div class="sidebar-sticky-content">

      <template v-if="route.name === 'FrontArticleDetail'">
        <WebmasterCard />
        <ArticleTocCard container-selector="#article-content-wrapper" />
      </template>

      <template v-else>
        <WebmasterCard />
        <NoticeCard />
        <HotArticleCard />
        <TagCloudCard />
      </template>

    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
// 引入卡片组件
import WebmasterCard from '@/layouts/FrontLayout/components/RightSidebar/cards/WebmasterCard.vue';
import NoticeCard from '@/layouts/FrontLayout/components/RightSidebar/cards/NoticeCard.vue';
import TagCloudCard from '@/layouts/FrontLayout/components/RightSidebar/cards/TagCloudCard.vue';
import ArticleTocCard from '@/layouts/FrontLayout/components/RightSidebar/cards/ArticleTocCard.vue';
import HotArticleCard from '@/layouts/FrontLayout/components/RightSidebar/cards/HotArticleCard.vue';

const route = useRoute();
</script>

<style scoped>
/* 侧边栏整体容器 */
.right-sidebar-wrapper {
  width: 300px;
  margin-left: 20px;
  flex-shrink: 0; /* 防止被挤压 */
}

/* 核心：粘性定位容器 */
.sidebar-sticky-content {
  position: sticky;
  top: 20px;
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  max-height: calc(100vh - 80px);

  overflow-y: scroll;
  overflow-x: hidden;
  scrollbar-gutter: stable;

  /* Firefox 浏览器滚动条处理：默认透明 */
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  transition: scrollbar-color 0.3s ease;
}

/* Firefox：鼠标悬浮时显示减淡的颜色 */
.sidebar-sticky-content:hover {
  scrollbar-color: var(--el-border-color-darker) transparent;
}

/* 防止内部卡片被压缩 */
.sidebar-sticky-content > * {
  flex-shrink: 0;
}

/* ==========================================
 * Webkit (Chrome/Edge/Safari) 滚动条定制
 * ========================================== */

/* 定义滚动条整体宽度和背景 */
.sidebar-sticky-content::-webkit-scrollbar {
  width: 4px;
  background-color: transparent; /* 轨道背景透明 */
}

/* 默认状态：滑块背景完全透明（隐藏） */
.sidebar-sticky-content::-webkit-scrollbar-thumb {
  background-color: transparent;
  border-radius: 4px;
}

/* 鼠标悬浮到整个侧边栏区域时：显示减淡的滑块 */
.sidebar-sticky-content:hover::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-darker);
}

/* 鼠标放在滚动条上时稍微加深 */
.sidebar-sticky-content::-webkit-scrollbar-thumb:hover {
  background-color: var(--el-text-color-secondary);
}

/* 响应式：中屏以下隐藏 */
@media screen and (max-width: 992px) {
  .hide-md-down {
    display: none;
  }
}
</style>