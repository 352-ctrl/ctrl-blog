<template>
  <el-card class="archive-card" shadow="never">
    <div class="archive-header">
      <div class="header-title">
        <el-icon class="header-icon"><Collection /></el-icon>
        文章归档
      </div>
      <div class="header-subtitle">
        共 <span class="count">{{ data.total }}</span> 篇
      </div>
    </div>

    <div class="archive-timeline">
      <div v-for="item in data.archiveData" :key="item.year" class="year-section">

        <div class="year-header" @click="toggleYear(item.year)">
          <span class="year-text">{{ item.year }}</span>
          <span class="year-count">({{ item.articles.length }}篇)</span>
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
                  <el-icon><Clock /></el-icon>
                  {{ formatFullDate(article.createTime) }}
                </div>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>

    <el-empty v-if="data.archiveData.length === 0" description="暂无归档文章" />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { getArticleArchive } from "@/api/front/article.js";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";

const router = useRouter();
const expandedYears = ref({});

const data = reactive({
  archiveData: [],
  total: 0,
});

const navToArticle = (id) => {
  router.push(`/post/${id}`);
};

// 加载数据
const loadArchive = async () => {
  try {
    const res = await getArticleArchive();
    if (res.code === 200) {
      data.archiveData = res.data || [];
      data.total = data.archiveData.reduce((sum, item) => sum + (item.count || 0), 0);
      initExpandedStates();
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    console.error('获取归档失败:', error);
  }
};

// 初始化展开状态 (默认展开所有年份)
const initExpandedStates = () => {
  data.archiveData.forEach(item => {
    expandedYears.value[item.year] = true;
  });
};

// 切换年份折叠
const toggleYear = (year) => {
  expandedYears.value[year] = !expandedYears.value[year];
};

// --- 日期处理工具函数 ---
const getMonth = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).getMonth() + 1 + '月';
};

const getDay = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).getDate();
};

const formatFullDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

onMounted(() => {
  loadArchive();
});
</script>

<style scoped>
.archive-card {
  border-radius: 8px;
}

/* 头部样式 */
.archive-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid #f0f2f5;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.header-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-subtitle {
  font-size: 14px;
  color: #909399;
}

.header-subtitle .count {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  margin: 0 4px;
}

/* 年份区域 */
.year-section {
  margin-bottom: 20px;
}

/* 年份标题 */
.year-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px 0;
  transition: all 0.3s;
}

.year-header:hover .year-text {
  color: #409EFF;
}

.year-text {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  margin-right: 10px;
  /* 类似时间轴的大圆点效果 */
  position: relative;
  padding-left: 15px;
}

.year-text::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 24px;
  background-color: #409EFF;
  border-radius: 4px;
}

.year-count {
  font-size: 14px;
  color: #999;
  margin-right: auto; /* 让箭头靠右 */
}

.arrow-icon {
  font-size: 16px;
  color: #909399;
  transition: transform 0.3s;
}

.arrow-icon.is-active {
  transform: rotate(90deg);
}

/* 文章列表容器 */
.article-list {
  padding-left: 20px; /* 缩进 */
  border-left: 2px dashed #e4e7ed; /* 左侧虚线引导线 */
  margin-left: 18px; /* 配合 year-text 的位置 */
}

/* 单个文章项 */
.article-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background-color: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent; /* 预留边框位置避免抖动 */
}

.article-item:hover {
  transform: translateX(10px); /* 向右轻微移动 */
  background-color: #f7f8fa;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 左侧日期方块 */
.date-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background-color: #ecf5ff;
  border-radius: 8px;
  margin-right: 15px;
  color: #409EFF;
  flex-shrink: 0; /* 防止被压缩 */
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

/* 右侧信息区 */
.info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.article-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
  transition: color 0.3s;
}

.article-item:hover .article-title {
  color: #409EFF;
}

.article-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 响应式调整 */
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