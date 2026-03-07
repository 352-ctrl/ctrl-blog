<template>
  <div class="app-container">
    <el-card shadow="never" class="redis-card" v-loading="infoLoading">
      <template #header>
        <div class="card-header">
          <span class="header-title"><el-icon><Monitor /></el-icon><span>Redis 监控信息</span></span>
          <el-button type="primary" link icon="Refresh" @click="loadInfo">刷新状态</el-button>
        </div>
      </template>

      <el-descriptions :column="columnCount" border>
        <el-descriptions-item label="Redis版本">{{ info.version }}</el-descriptions-item>
        <el-descriptions-item label="运行模式">{{ info.runMode }}</el-descriptions-item>
        <el-descriptions-item label="端口">{{ info.port }}</el-descriptions-item>
        <el-descriptions-item label="运行时间(天)">{{ info.uptime }}</el-descriptions-item>
        <el-descriptions-item label="连接客户端数">{{ info.clientCount }}</el-descriptions-item>
        <el-descriptions-item label="内存配置">{{ info.memoryConfig }}</el-descriptions-item>
        <el-descriptions-item label="AOF是否开启"><el-tag :type="info.aofEnabled === '是' ? 'success' : 'info'">{{ info.aofEnabled }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="RDB是否成功"><el-tag :type="info.rdbStatus === '成功' ? 'success' : 'danger'">{{ info.rdbStatus }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="Key数量">{{ info.keyCount }}</el-descriptions-item>
        <el-descriptions-item label="网络入口/出口">{{ info.networkInput }} kbps / {{ info.networkOutput }} kbps</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="redis-card keys-card">
      <template #header>
        <div class="card-header responsive-header">
          <span class="header-title"><el-icon><Key /></el-icon><span>键值列表</span></span>
          <el-form :inline="true" class="header-form">
            <el-form-item>
              <el-input v-model="query.keyword" placeholder="请输入键名进行查询" prefix-icon="Search" clearable @clear="handleSearch" @keyup.enter="handleSearch" class="search-input" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查 询</el-button>
              <el-button @click="resetQuery">重 置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <el-table v-loading="tableLoading" :data="keyList" stripe class="keys-table" height="500">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="key" label="Key 键名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="数据类型" width="120" align="center">
          <template #default="scope"><el-tag effect="plain" :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="size" label="大小/数量" width="120" align="center">
          <template #default="scope"><el-tag effect="plain" type="primary">{{ scope.row.size }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="ttl" label="剩余存活时间" width="150" align="center">
          <template #default="scope"><el-tag :type="getTtlTagType(scope.row.ttl)" disable-transitions>{{ scope.row.ttl }}</el-tag></template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="loadKeys" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getRedisInfo, getRedisKeys } from '@/api/admin/monitor/redis.js'
import AdminPagination from '@/components/admin/AdminPagination.vue'
import { useTable } from '@/composables/useTable.js';
import { useRequest } from '@/composables/useRequest.js';

const columnCount = ref(4)
const info = ref({ version: '-', runMode: '-', port: '-', uptime: '-', clientCount: '-', memoryConfig: '-', aofEnabled: '-', rdbStatus: '-', keyCount: '-', networkInput: '0', networkOutput: '0' })

// Hook 1: 基础信息请求
const { loading: infoLoading, execute: fetchRedisInfo } = useRequest(getRedisInfo);
const loadInfo = async () => {
  const data = await fetchRedisInfo();
  if (data) info.value = data;
};

// Hook 2: 键值分页列表
const { loading: tableLoading, list: keyList, total, query, loadData: loadKeys, resetQuery } = useTable(getRedisKeys, { keyword: '' });

const calculateColumn = () => columnCount.value = window.innerWidth < 768 ? 2 : 4;
const getTypeTag = (type) => ({ 'string': 'success', 'hash': 'warning', 'list': 'info', 'set': 'primary', 'zset': 'danger' }[type] || 'info');
const getTtlTagType = (ttl) => {
  if (ttl === '永不过期') return 'info';
  if (ttl === '已过期/不存在') return 'danger';
  const seconds = parseInt(ttl);
  if (isNaN(seconds)) return 'info';
  return seconds < 60 ? 'danger' : seconds < 3600 ? 'warning' : 'success';
};

const handleSearch = () => { query.pageNum = 1; loadKeys(); };

onMounted(() => {
  loadInfo();
  loadKeys();
  calculateColumn();
  window.addEventListener('resize', calculateColumn);
});
onUnmounted(() => window.removeEventListener('resize', calculateColumn));
</script>

<style scoped>
/* ==========================================
 * 卡片基础样式
 * ========================================== */
.redis-card {
  border-radius: 8px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
}

.keys-card {
  margin-top: 15px;
}

/* ==========================================
 * 头部排版
 * ========================================== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-weight: bold;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ==========================================
 * 搜索表单排版
 * ========================================== */
.header-form {
  display: flex;
  align-items: center;
}

/* 抵消 el-form-item 默认的 bottom margin */
.header-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 12px;
}

.header-form :deep(.el-form-item:last-child) {
  margin-right: 0;
}

.search-input {
  width: 250px;
}

/* ==========================================
 * 表格与分页
 * ========================================== */
.keys-table {
  width: 100%;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* ==========================================
 * 移动端响应式适配
 * ========================================== */
@media screen and (max-width: 768px) {
  .responsive-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-form {
    width: 100%;
    flex-wrap: wrap;
    gap: 10px;
  }

  .header-form :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }

  /* 手机端搜索框撑满容器 */
  .search-input {
    width: 100%;
  }
}
</style>