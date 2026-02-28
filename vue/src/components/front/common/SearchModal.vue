<template>
  <div class="search-trigger pc-only" @click="openSearch">
    <el-icon><Search /></el-icon>
    <span class="text">搜索文章...</span>
    <span class="key-badge">{{ shortcutText }}</span>
  </div>

  <el-button text class="icon-btn mobile-only" @click="openSearch">
    <el-icon size="24"><Search /></el-icon>
  </el-button>

  <el-dialog
      v-model="isOpen"
      width="560px"
      :show-close="false"
      class="custom-search-dialog"
      @opened="focusInput"
      append-to-body
      align-center
  >
    <div class="search-box">
      <el-icon class="search-icon" :size="22"><Search /></el-icon>
      <input
          ref="inputRef"
          v-model="keyword"
          class="custom-input"
          placeholder="搜索文章标题、摘要、内容或标签..."
          @input="handleSearch"
      />
      <div class="close-btn" @click="isOpen = false" title="关闭 (ESC)">
        <el-icon :size="16"><Close /></el-icon>
      </div>
    </div>

    <div class="search-results">
      <div v-if="!keyword" class="empty-tip init-state">
        <el-icon class="empty-icon"><Document /></el-icon>
        <p>输入关键词开始全站搜索</p>
      </div>

      <div v-else-if="results.length === 0" class="empty-tip">
        <el-empty description="哎呀，没有找到相关文章" :image-size="80"></el-empty>
      </div>

      <div v-else class="result-list">
        <div
            v-for="item in results"
            :key="item.id"
            class="result-item"
            @click="jumpTo(item.id)"
        >
          <div class="item-title" v-html="item._highlight.title"></div>
          <div class="item-summary" v-html="item._highlight.summary"></div>
          <div class="item-tags" v-if="item._highlight.tagNames && item._highlight.tagNames.length > 0">
            <el-tag
                class="mr-2"
                size="small"
                v-for="(tagHtml, index) in item._highlight.tagNames"
                :key="index"
                v-html="tagHtml"
                effect="light"
                round
            >
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
// 引入 Close 图标
import { Search, Document, Close } from '@element-plus/icons-vue';
import Fuse from 'fuse.js';
import { getSearchIndex } from "@/api/front/article.js"; // 确保路径正确
import { ElMessage } from "element-plus";

// === 状态管理 ===
const articles = ref([]);
const isOpen = ref(false);
const keyword = ref('');
const results = ref([]);
const inputRef = ref(null);
const router = useRouter();

// 判断操作系统以显示对应的快捷键提示
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);
const shortcutText = computed(() => isMac ? '⌘ K' : 'Ctrl K');

let fuse = null;

// === Fuse 配置 (开启 includeMatches 用于高亮) ===
const fuseOptions = {
  keys: ['title', 'summary', 'content', 'tagNames'], // 匹配字段：标题、摘要、内容、标签
  threshold: 0.4,       // 模糊匹配阈值 (0=完全匹配, 1=匹配所有)
  ignoreLocation: true, // 忽略位置约束，只要出现即可匹配
  includeMatches: true, // 开启此项才能获取高亮坐标 indices
};

// === XSS 安全转义函数 ===
const escapeHtml = (unsafe) => {
  return (unsafe || "").toString()
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
};

// === 生成高亮 HTML ===
const generateHighlightedText = (text, indices) => {
  if (!text) return '';
  let result = '';
  let lastIndex = 0;
  // Fuse 的 indices 格式为 [[start1, end1], [start2, end2]]
  indices.forEach(([start, end]) => {
    // 截取高亮之前的部分
    result += escapeHtml(text.substring(lastIndex, start));
    // 截取并包裹被高亮的部分
    result += `<mark class="highlight-text">` + escapeHtml(text.substring(start, end + 1)) + `</mark>`;
    lastIndex = end + 1;
  });
  // 补上剩余的部分
  result += escapeHtml(text.substring(lastIndex));
  return result;
};

// === 方法 ===
const loadArticles = async () => {
  try {
    const res = await getSearchIndex();
    if (res.code === 200) {
      // 预处理数据：将 tags [{id, name}] 转换为 tagNames ['name'] 以便搜索
      articles.value = (res.data || []).map(article => ({
        ...article,
        tagNames: article.tags ? article.tags.map(t => t.name) : []
      }));
      fuse = new Fuse(articles.value, fuseOptions);
    } else {
      ElMessage.error(res.msg || '获取搜索索引失败');
    }
  } catch (error) {
    console.error("加载搜索索引失败:", error);
  }
};

const openSearch = () => {
  isOpen.value = true;
  keyword.value = '';
  results.value = [];
  if (articles.value.length === 0) {
    loadArticles();
  }
};

const focusInput = () => {
  nextTick(() => {
    inputRef.value?.focus();
  });
};

const handleSearch = () => {
  if (!keyword.value) {
    results.value = [];
    return;
  }
  if (!fuse && articles.value.length > 0) {
    fuse = new Fuse(articles.value, fuseOptions);
  }
  if (fuse) {
    const searchResult = fuse.search(keyword.value);

    // 处理搜索结果，注入高亮 HTML
    results.value = searchResult.map(res => {
      const item = { ...res.item };

      // 默认的转义文本（防止未匹配的字段直接渲染出问题）
      item._highlight = {
        title: escapeHtml(item.title),
        summary: escapeHtml(item.summary),
        tagNames: (item.tagNames || []).map(escapeHtml)
      };

      // 遍历所有匹配项进行高亮替换
      res.matches.forEach(match => {
        if (match.key === 'title') {
          item._highlight.title = generateHighlightedText(match.value, match.indices);
        } else if (match.key === 'summary') {
          item._highlight.summary = generateHighlightedText(match.value, match.indices);
        } else if (match.key === 'tagNames' && match.refIndex !== undefined) {
          item._highlight.tagNames[match.refIndex] = generateHighlightedText(match.value, match.indices);
        }
      });
      return item;
    });
  }
};

const jumpTo = (id) => {
  isOpen.value = false;
  router.push(`/post/${id}`);
};

// === 快捷键支持 ===
const onKeydown = (e) => {
  if (e.defaultPrevented) return;
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault();
    openSearch();
  }
  if (e.key === 'Escape') isOpen.value = false;
};

onMounted(() => {
  window.addEventListener('keydown', onKeydown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown);
});
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
  background-color: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
  font-weight: bold;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>