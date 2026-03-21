<template>
  <el-card class="toc-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="icon-reading icon-swing"><Reading /></el-icon>
          文章目录
        </span>
        <div class="header-progress">
          <span class="progress-num">{{ readProgress }}%</span>
          <el-progress
              :percentage="readProgress"
              :width="20"
              :stroke-width="3"
              :show-text="false"
              color="var(--el-color-success)"
          />
        </div>
      </div>
    </template>

    <div class="toc-body">
      <div class="anchor-wrapper" v-if="tocList.length > 0 && scrollContainer" @click.capture.prevent="handleAnchorClick">
        <el-anchor
            class="custom-anchor"
            :container="scrollContainer"
            :offset-top="80"
            type="underline"
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

      <div v-else class="empty-toc">
        <span v-if="loading">正在解析目录...</span>
        <span v-else>暂无目录</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, onUnmounted, ref, shallowRef } from "vue";
import { Reading } from '@element-plus/icons-vue';

const props = defineProps({
  containerSelector: {
    type: String,
    default: '#article-content-wrapper'
  }
});

const tocList = ref([]);
const readProgress = ref(0);
const loading = ref(true);

const scrollContainer = shallowRef(null);
let contentObserver = null;

onMounted(() => {
  // 绑定全局滚动容器，用于传递给 el-anchor 以及计算阅读进度
  const container = document.querySelector('.page-scroll-view') || window;
  if (container) {
    scrollContainer.value = container;
    container.addEventListener('scroll', handleScroll);
  }
  initObserver();
});

onUnmounted(() => {
  if (scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll);
  }
  if (contentObserver) {
    contentObserver.disconnect();
  }
});

const initObserver = () => {
  const articleDom = document.querySelector(props.containerSelector);
  if (!articleDom) {
    setTimeout(initObserver, 200);
    return;
  }

  contentObserver = new MutationObserver((mutations) => {
    let shouldUpdate = false;
    for (const mutation of mutations) {
      if (mutation.type === 'childList') {
        shouldUpdate = true;
        break;
      }
    }
    if (shouldUpdate) {
      generateToc();
    }
  });
  contentObserver.observe(articleDom, { childList: true, subtree: true });
  generateToc();
};

const generateToc = () => {
  const articleDom = document.querySelector(props.containerSelector);
  if (!articleDom) return;

  const headings = articleDom.querySelectorAll('h1, h2, h3');
  if (headings.length === 0) {
    loading.value = false;
    return;
  }

  const list = [];
  headings.forEach((heading, index) => {
    const text = heading.textContent.trim();
    if (text) {
      // 这里的 id 会被 md-editor 自动生成，如果没生成我们再兜底
      // 最好是使用 heading 现有的 id，配合 sanitizeHtml 中白名单放行的 id 属性
      const id = heading.id || `toc-heading-${index}`;
      heading.id = id;

      list.push({
        id,
        text,
        level: parseInt(heading.tagName.replace('H', ''))
      });
    }
  });

  tocList.value = list;
  loading.value = false;
  handleScroll();
};

const handleAnchorClick = (e) => {
  // 拦截默认跳转
  e.preventDefault();

  // 找到真正被点击的 a 标签
  const linkNode = e.target.closest('a');
  if (!linkNode) return;

  const href = linkNode.getAttribute('href');
  if (!href) return;

  const id = href.replace('#', '');
  const target = document.getElementById(id);

  if (target) {
    // 原生 DOM 接口：平滑滚动，并将目标元素的顶部对齐视口顶部
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

const handleScroll = () => {
  const el = scrollContainer.value;
  const articleDom = document.querySelector(props.containerSelector);

  if (!el || !articleDom) {
    if (!el) return;
    const maxScroll = el === window
        ? document.documentElement.scrollHeight - window.innerHeight
        : el.scrollHeight - el.clientHeight;

    const currentScroll = el === window ? window.scrollY : el.scrollTop;
    const progress = maxScroll <= 0 ? 100 : (currentScroll / maxScroll) * 100;
    readProgress.value = Math.min(100, Math.max(0, Math.round(progress)));
    return;
  }

  const articleRect = articleDom.getBoundingClientRect();
  const containerHeight = el === window ? window.innerHeight : el.getBoundingClientRect().height;
  const containerTop = el === window ? 0 : el.getBoundingClientRect().top;

  const scrolledDistance = containerTop - articleRect.top;
  const totalScrollable = articleRect.height - containerHeight;

  if (totalScrollable <= 0) {
    readProgress.value = 100;
  } else {
    const progress = (scrolledDistance / totalScrollable) * 100;
    readProgress.value = Math.min(100, Math.max(0, Math.round(progress)));
  }
};
</script>

<style scoped lang="scss">
.toc-card {
  border: 1px solid var(--el-border-color-light);
  margin: 0;
  border-radius: 8px;
  background: var(--el-bg-color-overlay);

  :deep(.el-card__header) {
    padding: 15px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-family: 'SmileySans', sans-serif;
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .icon-reading {
        margin-right: 10px;
        color: var(--el-color-success);
        font-size: 18px;
        transform-origin: bottom center;
      }
    }

    .header-progress {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: var(--el-text-color-secondary);

      .progress-num {
        margin-right: 8px;
        font-weight: bold;
        color: var(--el-color-success);
      }
    }
  }

  .toc-body {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 5px;

    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color-darker);
      border-radius: 4px;
    }
  }

  .empty-toc {
    padding: 20px;
    text-align: center;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
}

:deep(.el-anchor) {
  background-color: transparent;
}

/* ====================================
   底线与绿色激活滑块
   ==================================== */
:deep(.el-anchor::before) {
  background-color: var(--el-border-color) !important;
  width: 2px !important;
}

html.dark :deep(.el-anchor::before) {
  background-color: var(--el-border-color-light) !important;
}

:deep(.el-anchor__marker) {
  display: block;
  background-color: var(--el-color-success);
  width: 2px;
  height: 24px !important;
  border-radius: 2px;
}

/* ====================================
   目录链接的样式
   ==================================== */
:deep(.el-anchor__link) {
  font-size: 14px;
  line-height: 1.8;
  padding: 6px 0 6px 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--el-text-color-regular);
  text-decoration: none;

  &:hover {
    color: var(--el-color-success);
    background-color: transparent;
  }

  &.is-active {
    color: var(--el-color-success);
    font-weight: bold;
    background-color: var(--el-color-success-light-9);
    border-radius: 0 4px 4px 0;
  }
}

/* 层级缩进 */
:deep(.toc-item-level-2 .el-anchor__link) {
  padding-left: 30px !important;
  font-size: 13px;
}
:deep(.toc-item-level-3 .el-anchor__link) {
  padding-left: 45px !important;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>