<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper primary"><el-icon size="32"><Document /></el-icon></div>
          <div class="flex-spacer"></div>
          <el-statistic title="文章数量" :value="data.articleCount" class="custom-statistic" />
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper success"><el-icon size="32"><User /></el-icon></div>
          <div class="flex-spacer"></div>
          <el-statistic title="用户数量" :value="data.userCount" class="custom-statistic" />
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper warning"><el-icon size="32"><ChatDotRound /></el-icon></div>
          <div class="flex-spacer"></div>
          <el-statistic title="评论数量" :value="data.commentCount" class="custom-statistic" />
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper info"><el-icon size="32"><View /></el-icon></div>
          <div class="flex-spacer"></div>
          <el-statistic title="总访问量" :value="data.visitCount" class="custom-statistic" />
        </div>
      </el-col>

      <el-col :sm="24" :md="16"><div class="card chart-card" id="line"></div></el-col>
      <el-col :sm="24" :md="8"><div class="card chart-card" id="pie"></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, reactive, watch } from "vue";
import * as echarts from 'echarts';
import { useDark } from '@vueuse/core';
import { getDashboardData } from "@/api/admin/dashboard.js";
import { useRequest } from "@/composables/useRequest.js"; // 引入 Hook

const isDark = useDark();

const data = reactive({ articleCount: 0, userCount: 0, commentCount: 0, visitCount: 0 });

let currentTrendData = null;
let currentCategoryData = null;
let lineChart = null;
let pieChart = null;
let resizeTimer = null;

// 使用 useRequest 接管数据请求
const { execute: fetchDashboard } = useRequest(getDashboardData);

onMounted(() => {
  initDashboard();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  disposeCharts();
});

watch(isDark, () => {
  if (currentTrendData && currentCategoryData) {
    renderLineChart(currentTrendData);
    renderPieChart(currentCategoryData);
  }
});

const initDashboard = async () => {
  // useRequest 成功时直接返回 res.data
  const resData = await fetchDashboard();
  if (resData) {
    data.articleCount = resData.articleCount;
    data.userCount = resData.userCount;
    data.commentCount = resData.commentCount;
    data.visitCount = resData.visitCount;

    currentTrendData = resData.visitTrend;
    currentCategoryData = resData.categoryPie;

    await nextTick(() => {
      renderLineChart(currentTrendData);
      renderPieChart(currentCategoryData);
    });
  }
};

const renderLineChart = (trendData) => {
  let chartDom = document.getElementById('line');
  if (lineChart) lineChart.dispose();
  lineChart = echarts.init(chartDom, isDark.value ? 'dark' : null);
  lineOption.backgroundColor = 'transparent';
  lineOption.xAxis.data = trendData.dates || [];
  lineOption.series[0].data = trendData.pvCounts || [];
  lineChart.setOption(lineOption);
  setTimeout(() => lineChart.resize(), 100);
}

const renderPieChart = (categoryData) => {
  let chartDom = document.getElementById('pie');
  if (pieChart) pieChart.dispose();
  pieChart = echarts.init(chartDom, isDark.value ? 'dark' : null);
  pieOption.backgroundColor = 'transparent';
  pieOption.series[0].itemStyle.borderColor = isDark.value ? '#1d1e1f' : '#ffffff';
  pieOption.series[0].data = categoryData || [];
  pieChart.setOption(pieOption);
  setTimeout(() => pieChart.resize(), 100);
}

const handleResize = () => {
  if (resizeTimer) clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => {
    if (lineChart) lineChart.resize({ animation: { duration: 0 } });
    if (pieChart) pieChart.resize({ animation: { duration: 0 } });
  }, 200);
};

const disposeCharts = () => {
  if (lineChart) { lineChart.dispose(); lineChart = null; }
  if (pieChart) { pieChart.dispose(); pieChart = null; }
};

// ================= ECharts 配置 =================
// (图表配置对象 lineOption 和 pieOption 保持你原有的配置不变，这里省略以节省空间)
let lineOption = { title: { text: '近7天访问量', left: 'left', }, grid: { left: '8%', right: '8%', bottom: '8%', top: '15%', containLabel: true }, xAxis: { type: 'category', data: [] }, tooltip: { trigger: 'axis', formatter: '{b} <br/>访客 : {c}' }, yAxis: { type: 'value' }, series: [ { data: [], type: 'line', smooth: true } ] };
let pieOption = { title: { text: '分类统计', left: 'left' }, tooltip: { trigger: 'item' }, legend: { top: '10%', orient: 'vertical', left: 'left' }, series: [ { name: '分类统计', type: 'pie', radius: ['40%', '70%'], center: ['60%', '55%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 10, borderWidth: 2 }, label: { show: false, position: 'center' }, emphasis: { label: { show: true, fontSize: 30, fontWeight: 'bold' } }, labelLine: { show: false }, data: [] } ] };
</script>

<style scoped>
.dashboard-container {
  width: 100%;
}

/* ==========================================
   顶部数据卡片布局
   ========================================== */
.stat-card {
  display: flex;
  align-items: center;
  /* 卡片的其余样式(背景色、阴影)由全局 .card 接管 */
}

/* 弹簧占位符，把图标和文字推向两侧 */
.flex-spacer {
  flex: 1;
}

/* 图标的高级感微透明背景 */
.icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-wrapper.primary { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
.icon-wrapper.success { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
.icon-wrapper.warning { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
.icon-wrapper.info    { background: rgba(144, 147, 153, 0.1); color: #909399; }

/* ==========================================
   Statistic 统计组件自定义样式
   ========================================== */
.custom-statistic {
  text-align: right; /* 确保标题和数字靠右对齐 */
}

:deep(.custom-statistic .el-statistic__head) {
  justify-content: flex-end;
  margin-bottom: 8px;
}

:deep(.custom-statistic .el-statistic__content) {
  justify-content: flex-end;
  font-weight: bolder;
  font-size: 26px;
}

/* ==========================================
   图表卡片样式
   ========================================== */
.chart-card {
  height: 400px;
  overflow: hidden;
  padding: 15px;
}
</style>