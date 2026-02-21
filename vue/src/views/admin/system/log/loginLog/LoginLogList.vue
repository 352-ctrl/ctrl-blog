<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的登录日志吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-select
            v-model="data.status"
            placeholder="登录状态"
            clearable
            @change="loadPage"
        >
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>

        <el-date-picker
            v-model="data.dateRange"
            type="datetimerange"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            clearable
            @change="loadPage"
        />
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="columns"
          v-model:selectedIds="data.selectedIds"
          :editable="false"
          :delete-api="deleteApi"
          delete-tip="确定删除该登录日志吗？"
          @delete-success="loadPage"
      />

      <AdminPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getLoginLogPage, deleteLoginLog, deleteLoginLogs } from "@/api/admin/sysLoginLog.js";
import AdminSearchBar from "@/components/admin/common/AdminSearchBar.vue";
import AdminTable from "@/components/admin/common/AdminTable.vue";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const data = reactive({
  status: null,
  dateRange: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  selectedIds: []
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    status: data.status,
    startTime: data.dateRange && data.dateRange.length === 2 ? data.dateRange[0] : null,
    endTime: data.dateRange && data.dateRange.length === 2 ? data.dateRange[1] : null,
  };
  getLoginLogPage(params).then(res => {
    if (res.code === 200) {
      data.tableData = res.data.records;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 初始化加载
onMounted(() => {
  loadPage();
});

// 重置搜索
const resetSearch = () => {
  data.status = null;
  data.dateRange = [];
  data.pageNum = 1;
  loadPage();
};

// 单个删除 API 桥接
const deleteApi = async (id) => {
  return deleteLoginLog(id);
};

// 批量删除 API 桥接
const batchDeleteApi = async (selectedIds) => {
  return deleteLoginLogs(selectedIds);
};

// 表格列配置
const columns = reactive([
  { prop: 'email', label: '登录账号', minWidth: '160px', align: 'left' },
  { prop: 'ip', label: '登录IP地址', minWidth: '120px' },
  { prop: 'location', label: '登录地点', minWidth: '140px' },
  { prop: 'os', label: '操作系统', minWidth: '160px', align: 'left' },
  { prop: 'browser', label: '浏览器', minWidth: '100px' },
  {
    type: 'status',
    prop: 'status',
    label: '登录状态',
    minWidth: '90px',
    statusMap: {
      1: { text: '成功', type: 'success' },
      0: { text: '失败', type: 'danger' }
    }
  },
  { prop: 'message', label: '操作信息', minWidth: '120px' },
  { prop: 'createTime', label: '登录时间', minWidth: '160px' }
]);
</script>