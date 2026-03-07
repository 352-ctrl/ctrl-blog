<template>
  <el-card class="archive-card" shadow="never" v-loading="loading">
    <div class="archive-header">
      <div class="header-title">
        <el-icon class="header-icon"><Collection /></el-icon>
        文章归档
      </div>
      <div class="header-subtitle">
        共 <span class="count">{{ totalCount }}</span> 篇
      </div>
    </div>

    <div class="archive-timeline">
      <div v-for="item in archiveData" :key="item.year" class="year-section">
        <div class="year-header" @click="toggleYear(item.year)">
          <span class="year-text">{{ item.year }}</span>
          <span class="year-count">({{ item.articles?.length || 0 }}篇)</span>
          <el-icon class="arrow-icon" :class="{ 'is-active': expandedYears[item.year] }">
            <ArrowRight />
          </el-icon>
        </div>

        <el-collapse-transition>
          <div v-show="expandedYears[item.year]" class="article-list">
            <div
                v-for="article in item.articles"
                :key="article.id"
                class="article-item"
                @click="navToArticle(article.id)"
            >
              <div class="date-box">
                <span class="day">{{ getDay(article.createTime) }}</span>
                <span class="month">{{ getMonth(article.createTime) }}</span>
              </div>

              <div class="info-box">
                <div class="article-title">{{ article.title }}</div>
                <div class="article-meta">
                  <el-icon class="meta-icon"><Clock /></el-icon>
                  {{ formatFullDate(article.createTime) }}
                </div>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>

    <el-empty v-if="!loading && (!archiveData || archiveData.length === 0)" description="暂无归档文章" />
  </el-card>
</template>

<script setup>
import { onMounted, ref, computed } from "vue";
import { getArticleArchive } from "@/api/front/article/article.js";
import { useRouter } from "vue-router";
import { useRequest } from "@/composables/useRequest";
import { useFormat } from "@/composables/useFormat";

const router = useRouter();
const expandedYears = ref({});

// 1. 使用格式化 Hook
const { getMonth, getDay, formatFullDate } = useFormat();

// 2. 使用请求 Hook
const { loading, data: archiveData, execute: fetchArchive } = useRequest(getArticleArchive);

// 3. 计算属性：自动计算总数
const totalCount = computed(() => {
  if (!archiveData.value) return 0;
  return archiveData.value.reduce((sum, item) => sum + (item.articles?.length || 0), 0);
});

const navToArticle = (id) => {
  router.push(`/post/${id}`);
};

const toggleYear = (year) => {
  expandedYears.value[year] = !expandedYears.value[year];
};

onMounted(async () => {
  const res = await fetchArchive(); // 显式 await 避免 ignored promise
  if (res) {
    // 默认展开所有年份
    res.forEach(item => {
      expandedYears.value[item.year] = true;
    });
  }
});
</script>

<style scoped>
/* ====================================
   外层卡片
   ==================================== */
.archive-card {
  border-radius: 8px;
  /* 核心优化：使用 overlay 让卡片在暗黑模式下与底层 page 背景区分开，更具立体感 */
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color-light);
}

/* ====================================
   头部区域
   ==================================== */
.archive-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid var(--el-border-color-lighter);
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.header-title {
  font-family: 'SmileySans', sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.header-subtitle .count {
  font-size: 20px;
  font-weight: bold;
  color: var(--el-color-primary);
  margin: 0 4px;
}

/* ====================================
   年份时间轴区域
   ==================================== */
.year-section {
  margin-bottom: 20px;
}

.year-header {
  font-family: 'SmileySans', sans-serif;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px 0;
  transition: all 0.3s;
}

.year-header:hover .year-text {
  color: var(--el-color-primary);
}

.year-text {
  font-size: 26px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  margin-right: 10px;
  position: relative;
  padding-left: 15px;
  transition: color 0.3s;
}

/* 时间轴的大圆点/竖线效果 */
.year-text::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 24px;
  background-color: var(--el-color-primary);
  border-radius: 4px;
}

.year-count {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-right: auto;
}

.arrow-icon {
  font-size: 16px;
  color: var(--el-text-color-secondary);
  transition: transform 0.3s;
}

.arrow-icon.is-active {
  transform: rotate(90deg);
}

/* ====================================
   文章列表与项
   ==================================== */
.article-list {
  padding-left: 20px;
  border-left: 2px dashed var(--el-border-color);
  margin-left: 18px;
}

.article-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background-color: transparent; /* 透明继承卡片底色，使页面更干净 */
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}

.article-item:hover {
  transform: translateX(10px);
  background-color: var(--el-fill-color-light); /* 悬浮时填充规范的浅色 */
  box-shadow: var(--el-box-shadow-light);
}

/* ====================================
   左侧日期方块
   ==================================== */
.date-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background-color: var(--el-color-primary-light-9);
  border-radius: 8px;
  margin-right: 15px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.date-box .day {
  font-size: 20px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 2px;
}

.date-box .month {
  font-size: 12px;
  line-height: 1;
}

/* ====================================
   右侧文章信息
   ==================================== */
.info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.article-title {
  font-size: 16px;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
  font-weight: 500;
  transition: color 0.3s;
}

.article-item:hover .article-title {
  color: var(--el-color-primary);
}

.article-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  gap: 5px;
}

.meta-icon {
  font-size: 14px; /* 让时间图标和文字比例更协调 */
}

/* ====================================
   响应式调整
   ==================================== */
@media screen and (max-width: 768px) {
  .year-text {
    font-size: 20px;
  }
  .article-list {
    padding-left: 10px;
    margin-left: 10px;
  }
  .date-box {
    width: 45px;
    height: 45px;
  }
  .article-title {
    font-size: 15px;
  }
}
</style>