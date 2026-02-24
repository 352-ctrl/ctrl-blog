<template>
  <div class="app-container">
    <el-card shadow="never" class="redis-card" v-loading="infoLoading">
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><Monitor /></el-icon>
            <span>Redis 监控信息</span>
          </span>
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
        <el-descriptions-item label="AOF是否开启">
          <el-tag :type="info.aofEnabled === '是' ? 'success' : 'info'">{{ info.aofEnabled }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="RDB是否成功">
          <el-tag :type="info.rdbStatus === '成功' ? 'success' : 'danger'">{{ info.rdbStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Key数量">{{ info.keyCount }}</el-descriptions-item>
        <el-descriptions-item label="网络入口/出口">
          {{ info.networkInput }} kbps / {{ info.networkOutput }} kbps
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="redis-card keys-card">
      <template #header>
        <div class="card-header responsive-header">
          <span class="header-title">
            <el-icon><Key /></el-icon>
            <span>键值列表</span>
          </span>

          <el-form :inline="true" class="header-form">
            <el-form-item>
              <el-input
                  v-model="queryParams.keyword"
                  placeholder="请输入键名进行查询"
                  prefix-icon="Search"
                  clearable
                  @clear="handleSearch"
                  @keyup.enter="handleSearch"
                  class="search-input"
              />
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
          <template #default="scope">
            <el-tag effect="plain" :type="getTypeTag(scope.row.type)">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="size" label="大小/数量" width="120" align="center">
          <template #default="scope">
            <el-tag effect="plain" type="primary">{{ scope.row.size }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="ttl" label="剩余存活时间" width="150" align="center">
          <template #default="scope">
            <el-tag :type="getTtlTagType(scope.row.ttl)" disable-transitions>
              {{ scope.row.ttl }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <AdminPagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            @change="loadKeys"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRedisInfo, getRedisKeys } from '@/api/admin/redis.js'
import AdminPagination from '@/components/admin/AdminPagination/AdminPagination.vue'

// --- 状态定义 ---
const infoLoading = ref(false)
const tableLoading = ref(false)
const columnCount = ref(4)

// 基础信息
const info = ref({
  version: '-', runMode: '-', port: '-', uptime: '-',
  clientCount: '-', memoryConfig: '-', aofEnabled: '-',
  rdbStatus: '-', keyCount: '-', networkInput: '0', networkOutput: '0'
})

// 分页列表相关
const keyList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

// --- 辅助计算方法 ---

// 响应式布局：根据屏幕宽度计算列数
const calculateColumn = () => {
  columnCount.value = window.innerWidth < 768 ? 2 : 4
}

// 标签颜色优化 1：数据类型
const getTypeTag = (type) => {
  const map = { 'string': 'success', 'hash': 'warning', 'list': 'info', 'set': 'primary', 'zset': 'danger' }
  return map[type] || 'info'
}

// 标签颜色优化 2：剩余存活时间 (TTL)
const getTtlTagType = (ttl) => {
  if (ttl === '永不过期') return 'info'
  if (ttl === '已过期/不存在') return 'danger'

  // 提取数字部分 (例如 "300 s" -> 300)
  const seconds = parseInt(ttl)
  if (isNaN(seconds)) return 'info'
  if (seconds < 60) return 'danger'      // 小于1分钟：红色警告
  if (seconds < 3600) return 'warning'   // 小于1小时：橙色提示
  return 'success'                       // 超过1小时：绿色安全
}

// --- 数据加载方法 ---

// 加载基础统计信息
const loadInfo = () => {
  infoLoading.value = true
  getRedisInfo().then(res => {
    if (res.code === 200) info.value = res.data
    else ElMessage.error(res.msg || '获取Redis状态失败')
  }).finally(() => infoLoading.value = false)
}

// 分页加载键值列表
const loadKeys = () => {
  tableLoading.value = true
  getRedisKeys(queryParams).then(res => {
    if (res.code === 200) {
      keyList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '获取键值列表失败')
    }
  }).finally(() => tableLoading.value = false)
}

// 搜索事件
const handleSearch = () => {
  queryParams.pageNum = 1 // 搜索时重置为第一页
  loadKeys()
}

// 重置搜索
const resetQuery = () => {
  queryParams.keyword = ''
  handleSearch()
}

// --- 生命周期 ---
onMounted(() => {
  loadInfo()
  loadKeys()
  calculateColumn()
  window.addEventListener('resize', calculateColumn)
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateColumn)
})
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