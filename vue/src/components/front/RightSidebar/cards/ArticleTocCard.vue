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
import { onMounted, onUnmounted, ref, shallowRef, nextTick } from "vue";
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
  const container = document.querySelector('.page-scroll-view');
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
  if (headings.length === 0) return;

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
  // 1. 阻止默认行为和冒泡，彻底切断 Element Plus 原生锚点逻辑的干扰
  if (e.preventDefault) e.preventDefault();
  e.stopPropagation();

  // 2. 获取点击的链接
  const linkNode = e.target.closest('a');
  if (!linkNode) return;

  const href = linkNode.getAttribute('href');
  if (!href) return;

  // 3. 解析目标元素
  const id = href.replace('#', '');
  const target = document.getElementById(id);
  const container = scrollContainer.value;

  if (target && container) {
    // 4. 【核心修改】使用 getBoundingClientRect 计算相对位置
    // 这种方式不受父级 position: relative 等样式影响，计算结果是物理像素级的精准
    const targetRect = target.getBoundingClientRect();
    const containerRect = container.getBoundingClientRect();

    // 逻辑：目标距离视口顶部的距离 - 容器距离视口顶部的距离 = 目标在容器内的相对偏移量
    const offsetInsideContainer = targetRect.top - containerRect.top;

    // 5. 计算最终滚动位置
    // 当前滚动位置 + 相对偏移量
    const targetScrollTop = container.scrollTop + offsetInsideContainer;

    // 6. 执行滚动
    container.scrollTo({
      top: targetScrollTop,
      behavior: 'smooth'
    });
  }
};

const handleScroll = () => {
  const el = scrollContainer.value;
  if (!el) return;

  const scrollTop = el.scrollTop;
  const scrollHeight = el.scrollHeight;
  const clientHeight = el.clientHeight;

  const maxScroll = scrollHeight - clientHeight;

  if (maxScroll <= 0) {
    readProgress.value = 100;
  } else {
    const progress = (scrollTop / maxScroll) * 100;
    readProgress.value = Math.min(100, Math.max(0, Math.round(progress)));
  }
};
</script>

<style scoped lang="scss">
.toc-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  margin-bottom: 20px;

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
        color: #67C23A;
        font-size: 18px;
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
    max-height: 400px;
    overflow-y: auto;
    padding-right: 5px;

    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: #e0e0e0;
      border-radius: 4px;
    }
  }

  .empty-toc {
    padding: 20px;
    text-align: center;
    color: #999;
    font-size: 13px;
  }
}

/* 样式优化：修复 Element Plus Anchor 的默认样式 */
:deep(.el-anchor__link) {
  font-size: 14px;
  line-height: 1.8;
  padding: 6px 0 6px 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #606266;
  text-decoration: none;

  &:hover {
    color: #67C23A;
    background-color: transparent;
  }

  &.is-active {
    color: #67C23A;
    font-weight: bold;
    background-color: #f0f9eb;
    border-radius: 0 4px 4px 0;
    border-left: 2px solid #67C23A;
    padding-left: 14px;
  }
}

:deep(.el-anchor__marker) {
  display: none;
}

:deep(.toc-item-level-2 .el-anchor__link) {
  padding-left: 30px !important;
  font-size: 13px;
}
:deep(.toc-item-level-3 .el-anchor__link) {
  padding-left: 45px !important;
  font-size: 12px;
  color: #909399;
}
</style>