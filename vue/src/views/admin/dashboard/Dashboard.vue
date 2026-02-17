<template>
  <div>
    <el-row :gutter="20">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card" style="display: flex; align-items: center">
          <el-icon size="35" color="#409EFF"><Document /></el-icon>
          <div style="flex: 1"></div>
          <div>
            <span>文章数量</span><br>
            <span style="font-weight: bolder; font-size: 20px">{{ data.articleCount }}</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card" style="display: flex; align-items: center">
          <el-icon size="35" color="#67C23A"><User /></el-icon>
          <div style="flex: 1"></div>
          <div>
            <span>用户数量</span><br>
            <span style="font-weight: bolder; font-size: 20px">{{ data.userCount }}</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card" style="display: flex; align-items: center">
          <el-icon size="35" color="#E6A23C"><ChatDotRound /></el-icon>
          <div style="flex: 1"></div>
          <div>
            <span>评论数量</span><br>
            <span style="font-weight: bolder; font-size: 20px">{{ data.commentCount }}</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="card" style="display: flex; align-items: center">
          <el-icon size="35" color="#909399"><View /></el-icon>
          <div style="flex: 1"></div>
          <div>
            <span>总访问量</span><br>
            <span style="font-weight: bolder; font-size: 20px">{{ data.visitCount }}</span>
          </div>
        </div>
      </el-col>

      <el-col :sm="24" :md="16">
        <div class="card" id="line" style="height: 400px; overflow: hidden;"></div>
      </el-col>
      <el-col :sm="24" :md="8">
        <div class="card" id="pie" style="height: 400px; overflow: hidden;"></div>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import {nextTick, onMounted, onUnmounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import * as echarts from 'echarts';
// 引入 API 模块
import {getDashboardData} from "@/api/admin/dashboard.js";

const data = reactive({
  articleCount:0,
  userCount:0,
  commentCount:0,
  visitCount:0
});

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

const initDashboard = async () => {
  try {
    const res = await getDashboardData();
    if (res.code === 200) {

      // (A) 填充顶部卡片数据
      data.articleCount = res.data.articleCount;
      data.userCount = res.data.userCount;
      data.commentCount = res.data.commentCount;
      data.visitCount = res.data.visitCount;

      // (B) 渲染图表 (使用 nextTick 确保 DOM 已更新)
      await nextTick(() => {
        renderLineChart(res.data.visitTrend);
        renderPieChart(res.data.categoryPie);
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

  // 如果图表实例已存在，先销毁
  if (lineChart) {
    lineChart.dispose();
  }

  lineChart = echarts.init(chartDom);
  lineOption.xAxis.data = trendData.dates || [];
  lineOption.series[0].data = trendData.pvCounts || [];
  lineChart.setOption(lineOption);

  // 确保图表初始大小正确
  setTimeout(() => {
    lineChart.resize();
  }, 100);
}

const renderPieChart = (categoryData) => {
  let chartDom = document.getElementById('pie');

  // 如果图表实例已存在，先销毁
  if (pieChart) {
    pieChart.dispose();
  }

  pieChart = echarts.init(chartDom);
  pieOption.series[0].data = categoryData || [];
  pieChart.setOption(pieOption);

  // 确保图表初始大小正确
  setTimeout(() => {
    pieChart.resize();
  }, 100);
}

// 窗口大小调整
const handleResize = () => {
  // 清除上一次的计时器
  if (resizeTimer) clearTimeout(resizeTimer);

  // 延迟 100-200ms 执行，等待 DOM 布局稳定
  resizeTimer = setTimeout(() => {
    if (lineChart) {
      lineChart.resize({
        animation: { duration: 0 } // 缩放时禁用动画，防止卡顿
      });
    }
    if (pieChart) {
      pieChart.resize({
        animation: { duration: 0 }
      });
    }
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

// 折线图
let lineOption = {
  title: {
    text: '近7天访问量',
    left: 'left',
  },
  grid: {
    left: '8%',    // 左侧留白
    right: '8%',   // 右侧留白
    bottom: '8%',  // 底部留白
    top: '15%',    // 顶部留白
    containLabel: true  // 确保坐标轴标签在grid内
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
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 40,
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