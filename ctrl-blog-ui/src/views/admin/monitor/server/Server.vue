<template>
  <div class="monitor-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="12" :xs="24" class="mb-col">
        <el-card class="monitor-card card" shadow="never">
          <template #header><div class="card-header"><el-icon><Cpu /></el-icon> <span>CPU使用率</span></div></template>
          <div class="dashboard-wrapper">
            <div class="chart-box"><el-progress type="dashboard" :percentage="parseP(info.cpu.sysUsage)" :color="colors" :width="160" /></div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">核心数</span><span class="value">{{ info.cpu.cpuNum }}</span></div>
              <div class="detail-item"><span class="label">用户使用率</span><span class="value">{{ info.cpu.userUsage }}</span></div>
              <div class="detail-item"><span class="label">系统使用率</span><span class="value">{{ info.cpu.sysUsage }}</span></div>
              <div class="detail-item"><span class="label">当前空闲率</span><span class="value">{{ info.cpu.freeUsage }}</span></div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" :xs="24" class="mb-col">
        <el-card class="monitor-card card" shadow="never">
          <template #header><div class="card-header"><el-icon><Memo /></el-icon> <span>内存使用率</span></div></template>
          <div class="dashboard-wrapper">
            <div class="chart-box"><el-progress type="dashboard" :percentage="parseP(info.memory.usage)" :color="colors" :width="160" /></div>
            <div class="detail-grid">
              <div class="detail-item"><span class="label">总内存</span><span class="value">{{ info.memory.total }}</span></div>
              <div class="detail-item"><span class="label">已用内存</span><span class="value">{{ info.memory.used }}</span></div>
              <div class="detail-item"><span class="label">剩余内存</span><span class="value">{{ info.memory.free }}</span></div>
              <div class="detail-item"><span class="label">使用率</span><span class="value">{{ info.memory.usage }}</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card class="monitor-card card" shadow="never">
          <template #header><div class="card-header"><el-icon><Platform /></el-icon> <span>JVM & 服务器信息</span></div></template>
          <div class="info-grid">
            <div class="info-item"><span class="label">操作系统</span><span class="value">{{ info.sys.osName }}</span></div>
            <div class="info-item"><span class="label">系统架构</span><span class="value">{{ info.sys.osArch }}</span></div>
            <div class="info-item"><span class="label">Java版本</span><span class="value">{{ info.jvm.version }}</span></div>
            <div class="info-item"><span class="label">运行时长</span><span class="value">{{ info.jvm.uptime }}</span></div>
            <div class="info-item is-full"><span class="label">安装路径</span><span class="value">{{ info.jvm.home }}</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card class="monitor-card card" shadow="never">
          <template #header><div class="card-header"><el-icon><Files /></el-icon> <span>磁盘状态</span></div></template>
          <el-table :data="[info.disk]" border class="disk-table">
            <el-table-column label="盘符路径" align="center"><template #default>/</template></el-table-column>
            <el-table-column prop="total" label="总大小" align="center" />
            <el-table-column prop="used" label="已用大小" align="center" />
            <el-table-column prop="free" label="可用大小" align="center" />
            <el-table-column label="使用率" align="center">
              <template #default="scope"><el-progress :percentage="parseP(scope.row.usage)" :color="colors" /></template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { getServerInfo } from "@/api/admin/monitor/server.js";
import { Cpu, Memo, Platform, Files } from '@element-plus/icons-vue';
// 引入基础请求 Hook
import { useRequest } from '@/composables/useRequest.js';

const info = ref({ cpu:{}, memory:{}, jvm:{}, sys:{}, disk:{} });
const colors = [ { color: '#67C23A', percentage: 70 }, { color: '#E6A23C', percentage: 90 }, { color: '#F56C6C', percentage: 100 } ];

const parseP = (s) => s ? parseFloat(s.replace('%','')) : 0;

// 使用 useRequest 接管底层 loading 和错误处理
const { loading, execute: fetchServerInfo } = useRequest(getServerInfo);

const loadData = async () => {
  try {
    const data = await fetchServerInfo();
    if (data) info.value = data;
  } catch (error) {
    // 错误已由 Hook 提示，这里静默拦截防止定时器崩溃
  }
};

let timer = null;
onMounted(() => {
  loadData();
  timer = setInterval(loadData, 5000);
});
onUnmounted(() => clearInterval(timer));
</script>

<style scoped lang="scss">
.monitor-container {
  padding: 15px;
}

.mt-4 { margin-top: 20px; }

/* 响应式间距控制 */
.mb-col {
  margin-bottom: 0;
}

@media screen and (max-width: 768px) {
  .mb-col {
    margin-bottom: 20px;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

/* 布局：Flex 左右布局 */
.dashboard-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  gap: 30px;
  flex-wrap: wrap;
}

.chart-box {
  flex: 0 0 auto;
}

/* 文字区域网格 */
.detail-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  min-width: 200px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 15px 10px;
  /* 使用 Element Plus 变量适配暗黑模式 */
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
  transition: all 0.3s ease;

  &:hover {
    background-color: var(--el-fill-color);
  }

  .label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-bottom: 6px;
  }

  .value {
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

/* JVM & 服务器信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  padding: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--el-fill-color-light);
  padding: 12px 18px;
  border-radius: 6px;

  &.is-full {
    grid-column: span 2;
  }

  .label {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .value {
    font-weight: 500;
    color: var(--el-text-color-primary);
    font-size: 14px;
    word-break: break-all;
  }
}

/* 磁盘状态表格宽度 */
.disk-table {
  width: 100%;
}

/* 适配移动端表格及网格 */
@media screen and (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  .info-item.is-full {
    grid-column: span 1;
  }

  .dashboard-wrapper {
    gap: 15px;
  }
}

/* 覆盖表格在暗黑模式下的基础外观 */
:deep(.el-table) {
  background-color: transparent !important;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
}
</style>