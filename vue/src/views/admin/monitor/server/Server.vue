<template>
  <div class="monitor-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="12" :xs="24" class="mb-col">
        <el-card class="monitor-card" shadow="always">
          <template #header>
            <div class="card-header"><el-icon><Cpu /></el-icon> <span>CPU使用率</span></div>
          </template>
          <div class="dashboard-wrapper">
            <div class="chart-box">
              <el-progress type="dashboard" :percentage="parseP(info.cpu.sysUsage)" :color="colors" :width="160" />
            </div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">核心数</span>
                <span class="value">{{ info.cpu.cpuNum }}</span>
              </div>
              <div class="detail-item">
                <span class="label">用户使用率</span>
                <span class="value">{{ info.cpu.userUsage }}</span>
              </div>
              <div class="detail-item">
                <span class="label">系统使用率</span>
                <span class="value">{{ info.cpu.sysUsage }}</span>
              </div>
              <div class="detail-item">
                <span class="label">当前空闲率</span>
                <span class="value">{{ info.cpu.freeUsage }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12" :xs="24" class="mb-col">
        <el-card class="monitor-card" shadow="always">
          <template #header>
            <div class="card-header"><el-icon><Memo /></el-icon> <span>内存使用率</span></div>
          </template>
          <div class="dashboard-wrapper">
            <div class="chart-box">
              <el-progress type="dashboard" :percentage="parseP(info.memory.usage)" :color="colors" :width="160" />
            </div>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">总内存</span>
                <span class="value">{{ info.memory.total }}</span>
              </div>
              <div class="detail-item">
                <span class="label">已用内存</span>
                <span class="value">{{ info.memory.used }}</span>
              </div>
              <div class="detail-item">
                <span class="label">剩余内存</span>
                <span class="value">{{ info.memory.free }}</span>
              </div>
              <div class="detail-item">
                <span class="label">使用率</span>
                <span class="value">{{ info.memory.usage }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card class="monitor-card" shadow="always">
          <template #header>
            <div class="card-header"><el-icon><Platform /></el-icon> <span>JVM & 服务器信息</span></div>
          </template>
          <div class="info-grid">
            <div class="info-item"><span class="label">操作系统</span><span class="value">{{ info.sys.osName }}</span></div>
            <div class="info-item"><span class="label">系统架构</span><span class="value">{{ info.sys.osArch }}</span></div>
            <div class="info-item"><span class="label">Java版本</span><span class="value">{{ info.jvm.version }}</span></div>
            <div class="info-item"><span class="label">运行时长</span><span class="value">{{ info.jvm.uptime }}</span></div>
            <div class="info-item" style="grid-column: span 2;">
              <span class="label">安装路径</span><span class="value">{{ info.jvm.home }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="24">
        <el-card class="monitor-card" shadow="always">
          <template #header>
            <div class="card-header"><el-icon><Files /></el-icon> <span>磁盘状态</span></div>
          </template>
          <el-table :data="[info.disk]" border style="width: 100%">
            <el-table-column label="盘符路径" align="center"><template #default>/</template></el-table-column>
            <el-table-column prop="total" label="总大小" align="center" />
            <el-table-column prop="used" label="已用大小" align="center" />
            <el-table-column prop="free" label="可用大小" align="center" />
            <el-table-column label="使用率" align="center">
              <template #default="scope">
                <el-progress :percentage="parseP(scope.row.usage)" :color="colors" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { getServerInfo } from "@/api/admin/server.js";
import { Cpu, Memo, Platform, Files } from '@element-plus/icons-vue';

const loading = ref(true);
const info = ref({ cpu:{}, memory:{}, jvm:{}, sys:{}, disk:{} });
const colors = [{ color: '#67C23A', percentage: 70 }, { color: '#E6A23C', percentage: 90 }, { color: '#F56C6C', percentage: 100 }];

const parseP = (s) => s ? parseFloat(s.replace('%','')) : 0;

const loadData = async () => {
  const res = await getServerInfo();
  if (res.code === 200) info.value = res.data;
  loading.value = false;
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

/* 统一间距控制 */
.mt-4 { margin-top: 10px; }

/* 1. 解决移动端垂直堆叠时的间距问题 */
.mb-col {
  margin-bottom: 0; /* 默认无底部间距 (PC端) */
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  /* 在移动端，让第一列有底部间距，从而与第二列产生间隔 */
  .mb-col {
    margin-bottom: 10px;
  }

  /* 确保最后一列（如果是堆叠的最后一个）不需要margin，或者保持一致 */
  .mb-col:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

/* 2. 优化仪表盘布局：Flex 左右布局 */
.dashboard-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  gap: 20px; /* 进度条和文字区之间的间距 */
  flex-wrap: wrap; /* 允许换行，适应极小屏幕 */
}

.chart-box {
  flex: 0 0 auto; /* 保持进度条区域不被压缩 */
  display: flex;
  justify-content: center;
}

/* 3. 优化文字容器样式：Grid 2x2 布局 */
.detail-grid {
  flex: 1; /* 占据剩余空间 */
  display: grid;
  grid-template-columns: 1fr 1fr; /* 两列 */
  gap: 15px;
  min-width: 200px; /* 防止过窄 */
}

/* 单个文字项样式优化 */
.detail-item {
  display: flex;
  flex-direction: column; /* 上下排列：标签在上，数值在下 */
  justify-content: center;
  align-items: center;
  padding: 12px 5px;
  background-color: #f8f9fa; /* 轻微背景色 */
  border-radius: 6px; /* 圆角 */
  transition: all 0.3s;

  &:hover {
    background-color: #f0f2f5;
  }

  .label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }

  .value {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

/* JVM 信息网格 (保持原样或微调) */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  padding: 10px;
}

.info-item {
  display: flex;
  justify-content: space-between; /* 左右分布 */
  align-items: center;
  background: #f8f9fa;
  padding: 12px 15px;
  border-radius: 4px;

  .label {
    color: #909399;
    font-size: 13px;
  }

  .value {
    font-weight: 500;
    color: #303133;
    font-size: 14px;
    word-break: break-all; /* 防止长路径溢出 */
  }
}

/* 移动端 JVM 信息单列显示 */
@media screen and (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  .info-item[style*="span 2"] {
    grid-column: span 1 !important;
  }
}
</style>