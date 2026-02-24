<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper primary">
            <el-icon size="32"><Document /></el-icon>
          </div>
          <div class="flex-spacer"></div>
          <el-statistic title="文章数量" :value="data.articleCount" class="custom-statistic" />
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper success">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="flex-spacer"></div>
          <el-statistic title="用户数量" :value="data.userCount" class="custom-statistic" />
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper warning">
            <el-icon size="32"><ChatDotRound /></el-icon>
          </div>
          <div class="flex-spacer"></div>
          <el-statistic title="评论数量" :value="data.commentCount" class="custom-statistic" />
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card stat-card">
          <div class="icon-wrapper info">
            <el-icon size="32"><View /></el-icon>
          </div>
          <div class="flex-spacer"></div>
          <el-statistic title="总访问量" :value="data.visitCount" class="custom-statistic" />
        </div>
      </el-col>

      <el-col :sm="24" :md="16">
        <div class="card chart-card" id="line"></div>
      </el-col>
      <el-col :sm="24" :md="8">
        <div class="card chart-card" id="pie"></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from 'echarts';
import { useDark } from '@vueuse/core';
// 引入 API 模块
import { getDashboardData } from "@/api/admin/dashboard.js";

const isDark = useDark();

const data = reactive({
  articleCount: 0,
  userCount: 0,
  commentCount: 0,
  visitCount: 0
});

// 缓存数据，以便在主题切换时直接重绘图表，而不需要重新发请求
let currentTrendData = null;
let currentCategoryData = null;

// 存储图表实例
let lineChart = null;
let pieChart = null;
let resizeTimer = null;

// 初始化加载
onMounted(() => {
  initDashboard();
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  // 清理事件监听
  window.removeEventListener('resize', handleResize);
  disposeCharts();
});

// 监听暗黑模式切换，无缝重绘图表
watch(isDark, () => {
  if (currentTrendData && currentCategoryData) {
    renderLineChart(currentTrendData);
    renderPieChart(currentCategoryData);
  }
});

const initDashboard = async () => {
  try {
    const res = await getDashboardData();
    if (res.code === 200) {
      // (A) 填充顶部卡片数据
      data.articleCount = res.data.articleCount;
      data.userCount = res.data.userCount;
      data.commentCount = res.data.commentCount;
      data.visitCount = res.data.visitCount;

      // 缓存图表数据
      currentTrendData = res.data.visitTrend;
      currentCategoryData = res.data.categoryPie;

      // (B) 渲染图表 (使用 nextTick 确保 DOM 已更新)
      await nextTick(() => {
        renderLineChart(currentTrendData);
        renderPieChart(currentCategoryData);
      });
    } else {
      ElMessage.error(res.msg || '获取仪表盘数据失败');
    }
  } catch (error) {
    console.error(error);
    ElMessage.error('网络请求异常');
  }
};

const renderLineChart = (trendData) => {
  let chartDom = document.getElementById('line');
  if (lineChart) {
    lineChart.dispose();
  }

  // 核心：根据 isDark 动态注入 echarts 的 'dark' 主题
  lineChart = echarts.init(chartDom, isDark.value ? 'dark' : null);

  lineOption.backgroundColor = 'transparent'; // 强制背景透明，跟随卡片背景
  lineOption.xAxis.data = trendData.dates || [];
  lineOption.series[0].data = trendData.pvCounts || [];
  lineChart.setOption(lineOption);

  setTimeout(() => {
    lineChart.resize();
  }, 100);
}

const renderPieChart = (categoryData) => {
  let chartDom = document.getElementById('pie');
  if (pieChart) {
    pieChart.dispose();
  }

  pieChart = echarts.init(chartDom, isDark.value ? 'dark' : null);

  pieOption.backgroundColor = 'transparent'; // 强制背景透明，跟随卡片背景

  pieOption.series[0].itemStyle.borderColor = isDark.value ? '#1d1e1f' : '#ffffff';

  pieOption.series[0].data = categoryData || [];
  pieChart.setOption(pieOption);

  setTimeout(() => {
    pieChart.resize();
  }, 100);
}

// 窗口大小调整
const handleResize = () => {
  if (resizeTimer) clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => {
    if (lineChart) lineChart.resize({ animation: { duration: 0 } });
    if (pieChart) pieChart.resize({ animation: { duration: 0 } });
  }, 200);
};

// 销毁图表
const disposeCharts = () => {
  if (lineChart) {
    lineChart.dispose();
    lineChart = null;
  }
  if (pieChart) {
    pieChart.dispose();
    pieChart = null;
  }
};

// ================= ECharts 配置 =================
// 折线图
let lineOption = {
  title: {
    text: '近7天访问量',
    left: 'left',
  },
  grid: {
    left: '8%',
    right: '8%',
    bottom: '8%',
    top: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: []
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b} <br/>访客 : {c}'
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

// 饼图
let pieOption = {
  title: {
    text: '分类统计',
    left: 'left'
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: '10%',
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '分类统计',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '55%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 30,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: []
    }
  ]
};
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