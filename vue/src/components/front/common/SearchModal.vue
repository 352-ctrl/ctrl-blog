<template>
  <div class="search-trigger pc-only" @click="openSearch">
    <el-icon><Search /></el-icon>
    <span class="text">搜索文章...</span>
    <span class="key-badge">Ctrl K</span>
  </div>

  <el-button text class="icon-btn mobile-only" @click="openSearch">
    <el-icon size="24"><Search /></el-icon>
  </el-button>

  <el-dialog
      v-model="isOpen"
      width="450px"
      :show-close="false"
      class="custom-search-dialog"
      @opened="focusInput"
      append-to-body
  >
    <div class="search-box">
      <el-icon class="search-icon" :size="20"><Search /></el-icon>
      <input
          ref="inputRef"
          v-model="keyword"
          class="custom-input"
          placeholder="请输入标题、摘要或标签"
          @input="handleSearch"
      />
      <div class="close-btn" @click="isOpen = false">ESC 关闭</div>
    </div>

    <div class="search-results">
      <div v-if="!keyword" class="empty-tip">
        输入关键词开始搜索
      </div>

      <div v-else-if="results.length === 0" class="empty-tip">
        未找到相关文章
      </div>

      <div v-else class="result-list">
        <div
            v-for="item in results"
            :key="item.id"
            class="result-item"
            @click="jumpTo(item.id)"
        >
          <div class="item-title">{{ item.title }}</div>
          <div class="item-summary">{{ item.summary }}</div>
          <div class="item-tags">
            <el-tag
                class="mr-1"
                size="small"
                v-for="tagName in item.tagNames"
                :key="tagName"
            >
              {{ tagName }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import Fuse from 'fuse.js';
import { getSearchIndex } from "@/api/front/article.js";
import { ElMessage } from "element-plus";

// === 状态管理 ===
const articles = ref([]);
const isOpen = ref(false);
const keyword = ref('');
const results = ref([]);
const inputRef = ref(null);
const router = useRouter();

// 定义 fuse 实例变量
let fuse = null;

// === Fuse 配置 ===
const fuseOptions = {
  keys: ['title', 'tagNames', 'categoryName', 'summary'],
  threshold: 0.3,
};

// === 方法 ===
const loadArticles = async () => {
  try {
    const res = await getSearchIndex();
    if (res.code === 200) {
      articles.value = res.data || [];
      // 初始化 Fuse
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
  keyword.value = ''; // 打开时清空输入
  results.value = [];
  // 懒加载：如果没有数据，尝试加载
  if (articles.value.length === 0) {
    loadArticles();
  }
};

const focusInput = () => {
  // 使用 nextTick 确保 DOM 已更新
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
    results.value = searchResult.map(res => res.item);
  }
};

const jumpTo = (id) => {
  isOpen.value = false;
  router.push(`/post/${id}`);
};

// === 生命周期与监听 ===
const onKeydown = (e) => {
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
/* === 通用样式 === */
.icon-btn {
  padding: 0;
  height: auto; /* 让高度自适应或由外部控制 */
}

/* === PC 端样式 (默认) === */
.search-trigger {
  display: flex;
  align-items: center;
  background: #f0f2f5;
  border-radius: 40px;
  padding: 8px 15px;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
  height: 35px;
  box-sizing: border-box;
  white-space: nowrap;
}
.search-trigger:hover {
  background: #e4e7ed;
  color: #606266;
}
.search-trigger .text {
  margin: 0 8px;
  font-size: 14px;
}
.key-badge {
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
  color: #909399;
}

/* === 控制显示隐藏的辅助类 === */
.mobile-only {
  display: none; /* 默认隐藏移动端按钮 */
}
.pc-only {
  display: flex; /* 默认显示PC端按钮 */
}

/* === 弹窗内部样式 === */
.search-box {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #dcdfe6;
  padding-bottom: 15px;
  margin-bottom: 10px;
}
.custom-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  margin-left: 10px;
  color: #303133;
}
.close-btn {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

/* === 结果列表样式 === */
.search-results {
  max-height: 400px;
  overflow-y: auto;
}
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
.result-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 5px;
}
.result-item:hover {
  background-color: #f5f7fa;
}
.item-title {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}
.item-summary {
  color: #909399;
  font-size: 13px;
  margin: 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* === 响应式调整 (断点设为 992px，可根据需要调整) === */
@media screen and (max-width: 992px) {
  /* 隐藏 PC 端胶囊条 */
  .pc-only {
    display: none !important;
  }

  /* 显示移动端图标按钮 */
  .mobile-only {
    display: inline-flex !important; /* el-button 默认是 inline-flex */
    color: #303133; /* 保持图标颜色清晰 */
  }

  /* 移动端图标按钮的具体样式微调，使其更像 IconHamburger */
  .mobile-only .el-icon {
    /* 如果需要加粗或调整大小可以在这里覆盖 */
    font-size: 24px;
  }

  /* 弹窗适配移动端 */
  :deep(.custom-search-dialog) {
    width: 90% !important;
    margin-top: 10vh !important; /* 距离顶部一点距离 */
  }
}
</style>

<style>
.custom-search-dialog .el-dialog__header {
  display: none;
}
.custom-search-dialog .el-dialog__body {
  padding: 20px;
}
.custom-search-dialog {
  border-radius: 12px;
}
</style>