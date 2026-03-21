<template>
  <div class="search-trigger pc-only" @click="openSearch">
    <el-icon><Search /></el-icon>
    <span class="text">搜索文章...</span>
    <span class="key-badge">{{ shortcutText }}</span>
  </div>

  <el-button text class="icon-btn mobile-only" @click="openSearch">
    <el-icon size="24"><Search /></el-icon>
  </el-button>

  <el-dialog v-model="isOpen" width="560px" :show-close="false" class="custom-search-dialog" @opened="focusInput" append-to-body align-center>
    <div class="search-box">
      <el-icon class="search-icon" :size="22"><Search /></el-icon>
      <input ref="inputRef" v-model="keyword" class="custom-input" placeholder="搜索文章标题、摘要、内容或标签..." />
      <div class="close-btn" @click="isOpen = false" title="关闭 (ESC)"><el-icon :size="16"><Close /></el-icon></div>
    </div>

    <div class="search-results">
      <div v-if="!keyword" class="empty-tip init-state">
        <el-icon class="empty-icon"><Document /></el-icon><p>输入关键词开始全站搜索</p>
      </div>

      <div v-else-if="results.length === 0" class="empty-tip">
        <el-empty description="哎呀，没有找到相关文章" :image-size="80"></el-empty>
      </div>

      <div v-else class="result-list">
        <div v-for="item in results" :key="item.id" class="result-item" @click="jumpTo(item.id)">
          <div class="item-title" v-html="item._highlight.title"></div>
          <div class="item-summary" v-html="item._highlight.summary"></div>
          <div class="item-tags" v-if="item._highlight.tagNames && item._highlight.tagNames.length > 0">
            <el-tag class="mr-2" size="small" v-for="(tagHtml, index) in item._highlight.tagNames" :key="index" v-html="tagHtml" effect="light" round></el-tag>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Document, Close } from '@element-plus/icons-vue';
import Fuse from 'fuse.js';
import { getSearchIndex } from "@/api/front/article/article.js";
// 引入 Hook
import { useRequest } from "@/composables/useRequest.js";

const articles = ref([]);
const isOpen = ref(false);
const keyword = ref('');
const results = ref([]);
const inputRef = ref(null);
const router = useRouter();

const isMac = /macintosh|mac os x/i.test(navigator.userAgent);
const shortcutText = computed(() => isMac ? '⌘ K' : 'Ctrl K');

let fuse = null;

const fuseOptions = {
  keys: ['title', 'summary', 'content', 'tagNames'],
  threshold: 0.4,
  ignoreLocation: true,
  includeMatches: true,
};

watch(keyword, () => {
  handleSearch();
});

// 监听文章数据，一旦加载完成，如果用户已经输入了关键词，就自动执行一次搜索补偿
watch(articles, (newVal) => {
  if (newVal.length > 0 && keyword.value) {
    handleSearch();
  }
});

const escapeHtml = (unsafe) => {
  return (unsafe || "").toString().replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
};

const generateHighlightedText = (text, indices) => {
  if (!text) return '';
  let result = '';
  let lastIndex = 0;
  indices.forEach(([start, end]) => {
    result += escapeHtml(text.substring(lastIndex, start));
    result += `<mark class="highlight-text">` + escapeHtml(text.substring(start, end + 1)) + `</mark>`;
    lastIndex = end + 1;
  });
  result += escapeHtml(text.substring(lastIndex));
  return result;
};

// ======= Hooks 接入 =======
// 接管获取索引数据的操作
const { execute: fetchSearchIndex } = useRequest(getSearchIndex, { errorMsg: '获取搜索索引失败' });

const loadArticles = async () => {
  try {
    // 成功直接返回 res.data 的数组内容
    const resData = await fetchSearchIndex();
    if (resData) {
      articles.value = resData.map(article => ({
        ...article,
        tagNames: article.tags ? article.tags.map(t => t.name) : []
      }));
      fuse = new Fuse(articles.value, fuseOptions);
    }
  } catch (error) {} // 捕获异常即可，错误提示由 Hook 处理
};

const openSearch = () => {
  isOpen.value = true;
  keyword.value = '';
  results.value = [];
  if (articles.value.length === 0) loadArticles();
};

const focusInput = () => nextTick(() => inputRef.value?.focus());

const handleSearch = () => {
  if (!keyword.value) { results.value = []; return; }
  if (!fuse && articles.value.length > 0) fuse = new Fuse(articles.value, fuseOptions);

  if (fuse) {
    const searchResult = fuse.search(keyword.value);
    results.value = searchResult.map(res => {
      const item = { ...res.item };
      item._highlight = {
        title: escapeHtml(item.title),
        summary: escapeHtml(item.summary),
        tagNames: (item.tagNames || []).map(escapeHtml)
      };

      res.matches.forEach(match => {
        if (match.key === 'title') item._highlight.title = generateHighlightedText(match.value, match.indices);
        else if (match.key === 'summary') item._highlight.summary = generateHighlightedText(match.value, match.indices);
        else if (match.key === 'tagNames' && match.refIndex !== undefined) item._highlight.tagNames[match.refIndex] = generateHighlightedText(match.value, match.indices);
      });
      return item;
    });
  }
};

const jumpTo = (id) => { isOpen.value = false; router.push(`/post/${id}`); };

const onKeydown = (e) => {
  if (e.defaultPrevented) return;
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') { e.preventDefault(); openSearch(); }
  if (e.key === 'Escape') isOpen.value = false;
};

onMounted(() => window.addEventListener('keydown', onKeydown));
onUnmounted(() => window.removeEventListener('keydown', onKeydown));
</script>

<style scoped>
/* === 通用按钮样式 === */
.icon-btn {
  padding: 0;
  height: auto;
}

/* === PC 端胶囊搜索框 === */
.search-trigger {
  display: flex;
  align-items: center;
  background: var(--el-fill-color-light);
  border: 1px solid transparent;
  border-radius: 20px;
  padding: 6px 12px 6px 16px;
  cursor: pointer;
  color: var(--el-text-color-regular);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  height: 36px;
  box-sizing: border-box;
}

.search-trigger:hover {
  background: var(--el-bg-color-overlay);
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 8px var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}

.search-trigger .text {
  margin: 0 12px 0 8px;
  font-size: 14px;
}

.key-badge {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-family: monospace;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.mobile-only { display: none; }
.pc-only { display: flex; }

/* === 弹窗内部：搜索框头 === */
.search-box {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 0 10px 18px 10px;
  margin-bottom: 15px;
}

.search-icon {
  color: var(--el-color-primary);
}

.custom-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 18px;
  margin-left: 12px;
  color: var(--el-text-color-primary);
  background: transparent;
}
.custom-input::placeholder {
  color: var(--el-text-color-placeholder);
  font-size: 16px;
}

/* 优化的圆形叉号按钮 */
.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
}
.close-btn:hover {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
  transform: rotate(90deg);
}

/* === 搜索结果列表 === */
.search-results {
  max-height: 50vh;
  overflow-y: auto;
  padding-right: 5px; /* 留出滚动条空间 */
}

/* 定制滚动条 */
.search-results::-webkit-scrollbar {
  width: 6px;
}
.search-results::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 4px;
}
.search-results::-webkit-scrollbar-thumb:hover {
  background: var(--el-text-color-secondary);
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);
  padding: 50px 0;
}

/* 替换行内样式的空图标 */
.empty-icon {
  font-size: 40px;
  color: var(--el-text-color-disabled);
}

.init-state p {
  margin-top: 15px;
  font-size: 14px;
  letter-spacing: 1px;
}

.result-item {
  padding: 16px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 10px;
  background: var(--el-bg-color-overlay);
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.result-item:hover {
  background-color: var(--el-fill-color-light);
  border-color: var(--el-color-primary-light-8);
  box-shadow: var(--el-box-shadow-light);
  transform: translateY(-1px);
}

.item-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 16px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.item-summary {
  color: var(--el-text-color-regular);
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 限制摘要最多显示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
}
.mr-2 {
  margin-right: 8px;
}

/* === 响应式设计 === */
@media screen and (max-width: 992px) {
  .pc-only { display: none !important; }
  .mobile-only { display: inline-flex !important; color: var(--el-text-color-primary); } /* 替换 #303133 */

  :deep(.custom-search-dialog) {
    width: 95% !important;
  }
}
</style>

<style>
/* === 全局组件级别样式重置 === */

/* 隐藏 el-dialog 自带的头部，使用自定义搜索框 */
.custom-search-dialog .el-dialog__header {
  display: none;
}

.custom-search-dialog .el-dialog__body {
  padding: 25px !important;
}

.custom-search-dialog {
  border-radius: 16px !important;
  background: var(--el-bg-color-overlay) !important;
  box-shadow: 0 20px 40px -10px rgba(0,0,0,0.15) !important;
}

.highlight-text {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-warning);
  font-weight: bold;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>