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
              color="var(--el-color-success)"
          />
        </div>
      </div>
    </template>

    <div class="toc-body">
      <div
          class="anchor-wrapper"
          v-if="tocList.length > 0 && scrollContainer"
          @click.capture.stop.prevent="handleAnchorClick"
      >
        <el-anchor
            class="custom-anchor"
            :container="scrollContainer"
            :offset-top="20"
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
  // 注意：确保您的外层滚动容器确实有 .page-scroll-view 这个类名。
  // 如果您的网页是全局滚动，这里请改为 window 并适配对应的监听事件
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
      const id = `toc-heading-${index}`;
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
  if (e.preventDefault) e.preventDefault();
  e.stopPropagation();

  const linkNode = e.target.closest('a');
  if (!linkNode) return;

  const href = linkNode.getAttribute('href');
  if (!href) return;

  const id = href.replace('#', '');
  const target = document.getElementById(id);
  const container = scrollContainer.value;

  if (target && container) {
    const targetRect = target.getBoundingClientRect();
    // 判断容器是普通的 DOM 元素还是 window 对象
    const containerTop = container === window ? 0 : container.getBoundingClientRect().top;
    const currentScrollTop = container === window ? window.scrollY : container.scrollTop;

    const offsetInsideContainer = targetRect.top - containerTop;
    const targetScrollTop = currentScrollTop + offsetInsideContainer - 20; // 减去 20px 顶部安全距离

    container.scrollTo({
      top: targetScrollTop,
      behavior: 'smooth'
    });
  }
};

const handleScroll = () => {
  const el = scrollContainer.value;
  const articleDom = document.querySelector(props.containerSelector);

  // 1. 兜底逻辑：如果找不到文章DOM，退回原有的整个容器滚动计算
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

  // 2. 获取文章主体和可视容器的物理坐标
  const articleRect = articleDom.getBoundingClientRect();
  const containerHeight = el === window ? window.innerHeight : el.getBoundingClientRect().height;
  const containerTop = el === window ? 0 : el.getBoundingClientRect().top;

  // 3. 计算滚动的距离：容器顶部坐标 - 文章顶部坐标
  const scrolledDistance = containerTop - articleRect.top;

  // 4. 计算文章正文部分的真正可滚动总距离：文章总高度 - 视口高度
  const totalScrollable = articleRect.height - containerHeight;

  // 5. 计算并更新进度
  if (totalScrollable <= 0) {
    // 情况 A：文章很短，一屏就能全部看完，直接 100%
    readProgress.value = 100;
  } else {
    // 情况 B：正常长文章，计算百分比
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