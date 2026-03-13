<template>
  <div>
    <AdminSearchBar
        :on-search="loadData"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteApi"
        :selected-ids="selectedIds"
        batchDeleteTip="确定批量删除选中的登录日志吗？"
        @batch-delete-success="loadData"
    >
      <template #search-items>
        <el-select v-model="query.status" placeholder="登录状态" clearable>
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
          delete-tip="确定删除该登录日志吗？"
          @delete-success="loadData"
      >
        <template #expand="{ row }">
          <el-row><el-form-item label="操作系统："><span class="expand-value-box">{{ row.os || '未知' }}</span></el-form-item></el-row>
          <el-row><el-form-item label="浏览器："><span class="expand-value-box">{{ row.browser || '未知' }}</span></el-form-item></el-row>
          <el-row v-if="row.userAgent"><el-form-item label="用户代理："><span class="expand-value-box">{{ row.userAgent }}</span></el-form-item></el-row>
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
import { getLoginLogPage, deleteLoginLog, deleteLoginLogs } from "@/api/admin/system/sysLoginLog.js";
import AdminSearchBar from "@/components/admin/AdminSearchBar.vue";
import AdminTable from "@/components/admin/AdminTable.vue";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";

const selectedIds = ref([]);

// 包装 API 函数以转换 dateRange 参数
const fetchApi = (params) => {
  const { dateRange, ...rest } = params;
  return getLoginLogPage({
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
} = useTable(fetchApi, { status: null, dateRange: [] });

onMounted(() => loadData());

const resetSearch = () => {
  resetQuery();
  selectedIds.value = [];
};

const deleteApi = async (id) => deleteLoginLog(id);
const batchDeleteApi = async (ids) => deleteLoginLogs(ids);

const columns = reactive([
  { prop: 'email', label: '登录账号', minWidth: '160px', align: 'left' },
  { prop: 'ip', label: '登录IP地址', minWidth: '120px' },
  { prop: 'location', label: '登录地点', minWidth: '140px' },
  {
    type: 'status', prop: 'status', label: '登录状态', minWidth: '90px',
    statusMap: { 1: { text: '成功', type: 'success' }, 0: { text: '失败', type: 'danger' } }
  },
  { prop: 'message', label: '操作信息', minWidth: '120px' },
  { prop: 'createTime', label: '登录时间', minWidth: '160px' }
]);
</script>