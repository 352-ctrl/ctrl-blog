<template>
  <div class="right-sidebar-wrapper hide-md-down">
    <div class="sidebar-sticky-content">

      <template v-if="route.name === 'FrontArticleDetail'">
        <ArticleTocCard container-selector="#article-content-wrapper" />
      </template>

      <template v-else>
        <NoticeCard />
        <TagCloudCard />

      </template>

    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
// 引入卡片组件
import NoticeCard from '@/components/front/RightSidebar/cards/NoticeCard.vue';
import TagCloudCard from '@/components/front/RightSidebar/cards/TagCloudCard.vue';
import ArticleTocCard from '@/components/front/RightSidebar/cards/ArticleTocCard.vue';

const route = useRoute();
</script>

<style scoped>
/* * 侧边栏容器样式
 * 原 SidebarContainer 的逻辑移到这里
 */
.right-sidebar-wrapper {
  width: 300px;
  margin-left: 20px;
  flex-shrink: 0; /* 防止被挤压 */
}

.sidebar-sticky-content {
  position: sticky; /* 推荐用 sticky 代替 fixed，布局更简单 */
  top: 0;        /* 距离顶部 header 的距离 */
  width: 300px;

  display: flex;           /* 开启 Flex 布局 */
  flex-direction: column;  /* 垂直排列 */
  gap: 5px;

  /* 如果内容超长，允许内部滚动 */
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  overflow-x: hidden;
}

/* 隐藏滚动条但保留功能 (可选) */
.sidebar-sticky-content::-webkit-scrollbar {
  width: 4px;
}
.sidebar-sticky-content::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 4px;
}

/* 响应式：中屏以下隐藏 */
@media screen and (max-width: 992px) {
  .hide-md-down {
    display: none;
  }
}
</style>