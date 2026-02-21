<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的操作日志吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.module"
            placeholder="请输入操作模块"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
            @keyup.enter="loadPage"
        />
        <el-input
            v-model="data.nickname"
            placeholder="请输入操作人昵称"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
            @keyup.enter="loadPage"
        />

        <el-select
            v-model="data.status"
            placeholder="操作状态"
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
          :expandable="true"
          v-model:selectedIds="data.selectedIds"
          :editable="false"
          :delete-api="deleteApi"
          delete-tip="确定删除该操作日志吗？"
          @delete-success="loadPage"
      >
        <template #expand="{ row }">
          <el-form label-position="left" inline class="expand-form">
            <el-row>
              <el-form-item label="执行方法：">
                <span class="expand-value">{{ row.method }}</span>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="请求参数：">
                <span class="expand-value">{{ row.params || '无' }}</span>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="返回结果：">
                <span class="expand-value">{{ row.result || '无' }}</span>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item label="用户代理：">
                <span class="expand-value">{{ row.userAgent }}</span>
              </el-form-item>
            </el-row>
          </el-form>
        </template>
      </AdminTable>

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
import { getOperLogPage, deleteOperLog, deleteOperLogs } from "@/api/admin/sysOperLog.js";
import AdminSearchBar from "@/components/admin/common/AdminSearchBar.vue";
import AdminTable from "@/components/admin/common/AdminTable.vue";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const data = reactive({
  module: '',
  nickname: '',
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
    module: data.module,
    nickname: data.nickname,
    status: data.status,
    startTime: data.dateRange && data.dateRange.length === 2 ? data.dateRange[0] : null,
    endTime: data.dateRange && data.dateRange.length === 2 ? data.dateRange[1] : null,
  };
  getOperLogPage(params).then(res => {
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
  data.module = "";
  data.nickname = "";
  data.status = null;
  data.dateRange = [];
  data.pageNum = 1;
  loadPage();
};

// 单个删除 API 桥接
const deleteApi = async (id) => {
  return deleteOperLog(id);
};

// 批量删除 API 桥接
const batchDeleteApi = async (selectedIds) => {
  return deleteOperLogs(selectedIds);
};

// 精简后的主表格列配置 (去掉了超长字段，它们已移入展开行)
const columns = reactive([
  { prop: 'nickname', label: '操作人', minWidth: '100px' },
  { prop: 'module', label: '操作模块', minWidth: '100px' },
  { prop: 'type', label: '操作类型', minWidth: '90px' },
  { prop: 'description', label: '操作描述', minWidth: '160px', align: 'left' },
  {
    type: 'status',
    prop: 'requestMethod',
    label: '请求方式',
    minWidth: '100px',
    statusMap: {
      'GET': { text: 'GET', type: '' },           // 默认蓝色
      'POST': { text: 'POST', type: 'success' },  // 绿色
      'PUT': { text: 'PUT', type: 'warning' },    // 橙/黄色
      'DELETE': { text: 'DELETE', type: 'danger' },// 红色高亮
      'PATCH': { text: 'PATCH', type: 'warning' },
      'OPTIONS': { text: 'OPTIONS', type: 'info' },
      'HEAD': { text: 'HEAD', type: 'info' }
    }
  },
  { prop: 'ip', label: '操作IP', minWidth: '120px' },
  {
    type: 'status',
    prop: 'status',
    label: '操作状态',
    minWidth: '90px',
    statusMap: {
      1: { text: '成功', type: 'success' },
      0: { text: '失败', type: 'danger' }
    }
  },
  { prop: 'costTime', label: '耗时(ms)', minWidth: '90px' },
  { prop: 'createTime', label: '操作时间', minWidth: '160px' }
]);
</script>

<style scoped>
/* 展开行表单项间距 */
.expand-form {
  margin-left: 20px;
}
.expand-form .el-form-item {
  margin-bottom: 8px;
  width: 100%; /* 让每个 item 独占一行 */
}
/* 给长文本加换行，并使用等宽字体显示代码/JSON更美观 */
.expand-value {
  word-break: break-all;
  white-space: pre-wrap;
  color: #606266;
  font-family: Consolas, Monaco, monospace;
  background-color: #f8f8f8;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
  min-width: 300px;
  max-width: 800px;
}
</style>