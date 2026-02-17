<template>
  <el-card class="toc-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="icon-reading"><Reading /></el-icon>
          文章目录
        </span>

        <div class="header-progress">
          <span class="progress-num">{{ readProgress }}%</span>
          <el-progress
              :percentage="readProgress"
              :width="20"
              :stroke-width="3"
              :show-text="false"
              color="#67C23A"
          />
        </div>
      </div>
    </template>

    <div class="toc-body">
      <el-anchor
          class="custom-anchor"
          type="underline"
          :offset-top="80"
          :container="scrollContainer"
          v-model:active-key="activeAnchor"
      >
        <el-anchor-link
            v-for="item in tocList"
            :key="item.id"
            :href="`#${item.id}`"
            :title="item.text"
            :class="['toc-link', 'toc-item-level-' + item.level]"
        />
      </el-anchor>
    </div>
  </el-card>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from 'vue-router';
import { Reading } from '@element-plus/icons-vue'; // 引入图标

// 接收选择器字符串
const props = defineProps({
  containerSelector: {
    type: String,
    default: '#article-content-wrapper'
  }
});

const route = useRoute();
const tocList = ref([]);
const activeAnchor = ref('');
const readProgress = ref(0);
// 注意：scrollContainer 在 Vue 3 中最好不要定义为 ref 如果它是原生 DOM 元素且不需要响应式
// 这里保持普通变量即可，或者用 shallowRef
let scrollContainer = null;
let contentDom = null;

// 初始化逻辑
const initToc = () => {
  // 1. 找到滚动容器
  scrollContainer = document.querySelector('.page-scroll-view') || window;

  // 2. 找到文章内容 DOM
  contentDom = document.querySelector(props.containerSelector);

  if (contentDom) {
    generateToc();
    // 绑定滚动监听
    if (scrollContainer) {
      scrollContainer.addEventListener('scroll', handleScroll);
      // 立即计算一次
      handleScroll();
    }
  }
};

// 生成目录
const generateToc = () => {
  if (!contentDom) return;
  tocList.value = [];
  // 查找所有 H1-H3 标签
  const headings = contentDom.querySelectorAll('h1, h2, h3');

  headings.forEach((heading, index) => {
    // 确保 ID 唯一且有效
    const id = `heading-${index}-${heading.tagName.toLowerCase()}`;
    heading.id = id;
    tocList.value.push({
      id,
      text: heading.textContent.trim(),
      // 解析层级：H1->1, H2->2...
      level: parseInt(heading.tagName.replace('H', ''))
    });
  });
};

// 滚动处理
const handleScroll = () => {
  if (!contentDom || !scrollContainer) return;

  const clientHeight = scrollContainer === window ? window.innerHeight : scrollContainer.clientHeight;
  const scrollTop = scrollContainer === window ? window.scrollY : scrollContainer.scrollTop;

  // 计算阅读进度
  const rect = contentDom.getBoundingClientRect();
  // 当文章底部出现在视口内时，视为读完
  const isArticleFinished = rect.bottom <= clientHeight;

  if (isArticleFinished) {
    readProgress.value = 100;
  } else {
    // 剩余未读高度 = 底部距离 - 视口高度
    const remaining = rect.bottom - clientHeight;
    // 总可滚动高度 = 文章总高 - 视口高度
    const totalScrollable = rect.height - clientHeight;

    // 避免除以0
    if (totalScrollable <= 0) {
      readProgress.value = 100;
    } else {
      let progress = 1 - (remaining / totalScrollable);
      readProgress.value = Math.max(0, Math.min(100, Math.round(progress * 100)));
    }
  }

  // 锚点高亮逻辑 (简单版：高亮距离顶部最近的标题)
  // 注意：Element Plus Anchor 组件自带监听，但在某些复杂滚动容器下可能需要手动辅助
  // 这里仅更新 activeAnchor，Element Plus 会处理样式
  // 如果 Element Anchor 的自动监听好用，这一步其实可以省略，或者用来同步 activeAnchor 状态
};

onMounted(() => {
  setTimeout(() => {
    initToc();
  }, 500);
});

onUnmounted(() => {
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleScroll);
  }
});

watch(() => route.path, () => {
  setTimeout(initToc, 500);
});
</script>

<style scoped lang="scss">
/* 1. 复用卡片整体样式 */
.toc-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  margin-bottom: 20px;
  position: sticky; /* 目录通常需要吸顶，根据你的布局需求调整 */
  top: 20px;

  /* 2. 头部样式复用 */
  :deep(.el-card__header) {
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #333;

      .icon-reading {
        margin-right: 10px;
        color: #67C23A; /* 绿色代表阅读/健康 */
        font-size: 18px;
        animation: swing 2s infinite linear;
        transform-origin: top center;
      }
    }

    .header-progress {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #909399;

      .progress-num {
        margin-right: 8px;
        font-weight: bold;
        color: #67C23A;
      }
    }
  }

  .toc-body {
    max-height: 400px; /* 限制目录最大高度 */
    overflow-y: auto;  /* 超出滚动 */
    padding-right: 5px;

    /* 滚动条美化 */
    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: #e0e0e0;
      border-radius: 4px;
    }
  }
}

/* 3. 复用动画 */
@keyframes swing {
  0% { transform: rotate(0deg); }
  10% { transform: rotate(15deg); }
  20% { transform: rotate(-10deg); }
  30% { transform: rotate(5deg); }
  40% { transform: rotate(-5deg); }
  50% { transform: rotate(0deg); }
  100% { transform: rotate(0deg); }
}

/* 4. Element Anchor 样式穿透与定制 */
:deep(.el-anchor) {
  background: transparent;
}

:deep(.el-anchor__marker) {
  background-color: #67C23A; /* 绿色游标 */
}

:deep(.el-anchor__link) {
  font-size: 14px;
  line-height: 1.8; /* 稍微增加行高 */
  padding: 6px 0 6px 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #606266;

  &:hover {
    color: #67C23A;
  }

  &.is-active {
    color: #67C23A;
    font-weight: bold;
    background-color: #f0f9eb; /* 选中时的淡绿色背景 */
    border-radius: 0 4px 4px 0;
    padding-left: 12px; /* 视觉微调 */
  }
}

/* 目录层级缩进 */
:deep(.toc-item-level-2 .el-anchor__link) {
  padding-left: 32px !important;
  font-size: 13px;
}
:deep(.toc-item-level-3 .el-anchor__link) {
  padding-left: 48px !important;
  font-size: 12px;
  color: #909399;
}
</style>