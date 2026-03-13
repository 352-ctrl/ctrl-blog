<template>
  <div>
    <AdminSearchBar
        :on-search="loadData"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteApi"
        :selected-ids="selectedIds"
        batchDeleteTip="确定批量删除选中的操作日志吗？"
        @batch-delete-success="loadData"
    >
      <template #search-items>
        <el-input v-model="query.module" placeholder="操作模块" prefix-icon="Search" clearable @keyup.enter="loadData" />
        <el-input v-model="query.nickname" placeholder="操作人昵称" prefix-icon="Search" clearable @keyup.enter="loadData" />
        <el-select v-model="query.status" placeholder="操作状态" clearable>
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
            v-model="query.dateRange"
            type="datetimerange"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            clearable
            @change="loadData"
        />
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable
          :table-data="tableData"
          :columns="columns"
          :expandable="true"
          v-model:selectedIds="selectedIds"
          :editable="false"
          :delete-api="deleteApi"
          delete-tip="确定删除该操作日志吗？"
          @delete-success="loadData"
      >
        <template #expand="{ row }">
          <el-row><el-form-item label="执行方法："><span class="expand-value-box">{{ row.method }}</span></el-form-item></el-row>
          <el-row><el-form-item label="请求参数："><span class="expand-value-box">{{ row.params || '无' }}</span></el-form-item></el-row>
          <el-row><el-form-item label="返回结果："><span class="expand-value-box">{{ row.result || '无' }}</span></el-form-item></el-row>
          <el-row v-if="row.status === 0 && row.errorMsg">
            <el-form-item label="异常信息：">
              <span class="expand-value-box error-msg-box">
                {{ row.errorMsg }}
              </span>
            </el-form-item>
          </el-row>
          <el-row><el-form-item label="用户代理："><span class="expand-value-box">{{ row.userAgent }}</span></el-form-item></el-row>
        </template>
      </AdminTable>

      <AdminPagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { getOperLogPage, deleteOperLog, deleteOperLogs } from "@/api/admin/system/sysOperLog.js";
import AdminSearchBar from "@/components/admin/AdminSearchBar.vue";
import AdminTable from "@/components/admin/AdminTable.vue";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";

const selectedIds = ref([]);

const fetchApi = (params) => {
  const { dateRange, ...rest } = params;
  return getOperLogPage({
    ...rest,
    startTime: dateRange && dateRange.length === 2 ? dateRange[0] : null,
    endTime: dateRange && dateRange.length === 2 ? dateRange[1] : null,
  });
};

const {
  loading,
  list: tableData,
  total,
  query,
  loadData,
  handlePageChange,
  resetQuery
} = useTable(fetchApi, { module: '', nickname: '', status: null, dateRange: [] });

onMounted(() => loadData());

const resetSearch = () => {
  resetQuery();
  selectedIds.value = [];
};

const deleteApi = async (id) => deleteOperLog(id);
const batchDeleteApi = async (ids) => deleteOperLogs(ids);

const columns = reactive([
  { prop: 'nickname', label: '操作人', minWidth: '100px' },
  { prop: 'module', label: '操作模块', minWidth: '100px' },
  { prop: 'type', label: '操作类型', minWidth: '90px' },
  { prop: 'description', label: '操作描述', minWidth: '160px', align: 'left', showOverflowTooltip: true },
  {
    type: 'status', prop: 'requestMethod', label: '请求方式', minWidth: '100px',
    statusMap: {
      'GET': { text: 'GET', type: '' }, 'POST': { text: 'POST', type: 'success' },
      'PUT': { text: 'PUT', type: 'warning' }, 'DELETE': { text: 'DELETE', type: 'danger' },
      'PATCH': { text: 'PATCH', type: 'warning' }, 'OPTIONS': { text: 'OPTIONS', type: 'info' },
      'HEAD': { text: 'HEAD', type: 'info' }
    }
  },
  { prop: 'ip', label: '操作IP', minWidth: '120px' },
  {
    type: 'status', prop: 'status', label: '操作状态', minWidth: '90px',
    statusMap: { 1: { text: '成功', type: 'success' }, 0: { text: '失败', type: 'danger' } }
  },
  { prop: 'costTime', label: '耗时(ms)', minWidth: '90px' },
  { prop: 'createTime', label: '操作时间', minWidth: '160px' }
]);
</script>

<style scoped>
/* 异常信息的高亮警告样式 */
.error-msg-box {
  color: var(--el-color-danger);
  border-color: var(--el-color-danger-light-7);
  background-color: var(--el-color-danger-light-9);
}
</style>