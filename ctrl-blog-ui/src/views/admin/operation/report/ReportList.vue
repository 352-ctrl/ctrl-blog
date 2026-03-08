<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteReportApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的举报吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-select v-model="query.targetType" placeholder="举报目标" clearable @clear="loadData">
          <el-option label="评论" value="COMMENT" /><el-option label="文章" value="ARTICLE" /><el-option label="用户" value="USER" />
        </el-select>
        <el-select v-model="query.status" placeholder="处理状态" clearable @clear="loadData">
          <el-option label="待处理" :value="0" /><el-option label="属实已处罚" :value="1" /><el-option label="驳回/恶意举报" :value="2" />
        </el-select>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="reportColumns" :expandable="true" v-model:selectedIds="selectedIds" :delete-api="deleteReportApi" delete-tip="确定删除该举报吗？" @delete-success="loadData" @edit="handleProcess">
        <template #expand="{ row }">
          <el-row><el-form-item label="目标内容："><span class="expand-value-box">{{ row.targetSummary || '无内容摘要' }}</span></el-form-item></el-row>
          <el-row><el-form-item label="详细说明："><span class="expand-value-box">{{ row.content || '未填写详细说明' }}</span></el-form-item></el-row>
          <el-row v-if="row.status !== 0">
            <el-form-item label="处理备注：">
              <span class="expand-value-box admin-note-box">{{ row.adminNote || '无内部处理备注' }}</span>
            </el-form-item>
          </el-row>
        </template>
      </AdminTable>
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" title="审核举报" class="dialog-md-down" width="600px">
      <div class="report-info-container">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="举报目标">
            <el-tag size="small" :type="getTargetTypeColor(dialog.rowData.value.targetType)">{{ getTargetTypeText(dialog.rowData.value.targetType) }}</el-tag>
            <span class="target-id-text">ID: {{ dialog.rowData.value.targetId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="目标内容">
            <span class="highlight-danger">{{ dialog.rowData.value.targetSummary || '无摘要内容' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="举报原因"><strong>{{ dialog.rowData.value.reason }}</strong></el-descriptions-item>
          <el-descriptions-item label="详细说明">{{ dialog.rowData.value.content || '未填写详细说明' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form ref="processFormRef" :model="dialog.rowData.value" :rules="processRules" label-width="100px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="dialog.rowData.value.status">
            <el-radio :label="1">举报属实 (已处罚)</el-radio><el-radio :label="2">驳回/无效举报</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封禁时间" prop="disableDays" v-if="dialog.rowData.value.status === 1 && dialog.rowData.value.targetType === 'USER'">
          <el-select v-model="dialog.rowData.value.disableDays" placeholder="请选择封禁时长" class="full-width-select">
            <el-option label="不封禁 (仅警告)" :value="0" /><el-option label="封禁 1 天" :value="1" /><el-option label="封禁 3 天" :value="3" /><el-option label="封禁 7 天" :value="7" /><el-option label="封禁 30 天" :value="30" /><el-option label="永久封禁" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注" prop="adminNote">
          <el-input v-model="dialog.rowData.value.adminNote" type="textarea" :rows="3" placeholder="请输入内部处理备注（仅管理员可见），例如：已封禁该账号、已删除违规评论等" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" @click="submitProcessForm" :loading="submitLoading">确 认 处 理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { deleteReport, deleteReports, getReportPage, processReport } from "@/api/admin/operation/report.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const processFormRef = ref(null);
const selectedIds = ref([]);
const REASON_DICT = { SPAM: '垃圾广告', PORN: '色情低俗', ILLEGAL: '违法违规', ABUSE: '人身攻击/引战谩骂', COPYRIGHT: '抄袭/洗稿/侵权', IMPERSONATION: '冒充他人/身份造假', PROFILE_VIOLATION: '头像/昵称违规', OTHER: '其他原因' };

const fetchApi = async (params) => {
  const res = await getReportPage(params);
  if (res.code === 200) {
    res.data.records = res.data.records.map(item => ({ ...item, reason: REASON_DICT[item.reason] || item.reason }));
  }
  return res;
};

const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(fetchApi, { targetType: null, status: null });
const dialog = useDialog();
const { loading: submitLoading, execute: execProcess } = useRequest(processReport, { successMsg: '处理成功' });

const processRules = {
  status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  disableDays: [{ required: true, message: '请选择封禁时长', trigger: 'change' }]
};

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleProcess = (row) => {
  if (row.status === 1 || row.status === 2) {
    ElMessage.warning('该举报已审核完毕，不可重复处理');
    return;
  }
  dialog.openDialog('审核举报', 'edit', { ...row, status: 1, disableDays: 7, adminNote: row.adminNote || '' });
  processFormRef.value?.clearValidate();
};

const submitProcessForm = () => {
  processFormRef.value.validate(async (valid) => {
    if (valid) {
      const { id, status, adminNote, disableDays } = dialog.rowData.value;
      await execProcess({ id, status, adminNote, disableDays });
      dialog.closeDialog();
      await loadData();
    }
  });
};

const getTargetTypeText = (type) => ({ 'COMMENT': '评论', 'ARTICLE': '文章', 'USER': '用户' }[type] || '其他');
const getTargetTypeColor = (type) => ({ 'COMMENT': 'info', 'ARTICLE': 'primary', 'USER': 'warning' }[type] || 'info');

const deleteReportApi = async (id) => deleteReport(id);
const batchDeleteReportApi = async (ids) => deleteReports(ids);

const reportColumns = reactive([
  { prop: 'userNickname', label: '举报人', minWidth: '120px' },
  { type: 'status', prop: 'targetType', label: '目标类型', statusMap: { 'COMMENT': { text: '评论', type: 'info' }, 'ARTICLE': { text: '文章', type: 'primary' }, 'USER': { text: '用户', type: 'warning' } } },
  { prop: 'targetSummary', label: '被举报内容', minWidth: '150px', showOverflowTooltip: true },
  { prop: 'reason', label: '违规原因', minWidth: '120px' },
  { prop: 'content', label: '详细说明', minWidth: '150px', showOverflowTooltip: true },
  { type: 'status', prop: 'status', label: '状态', minWidth: '130px', statusMap: { 0: { text: '待处理', type: 'warning' }, 1: { text: '属实已处罚', type: 'success' }, 2: { text: '驳回/恶意举报', type: 'info' } } },
  { prop: 'createTime', label: '提交时间', minWidth: '160px' }
]);
</script>

<style scoped>
.admin-note-box {
  background-color: var(--el-color-info-light-9) !important;
}

.report-info-container {
  background-color: var(--el-fill-color-light); /* 适配暗黑模式，替换固定的 #f5f7fa */
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.target-id-text {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary); /* 适配暗黑模式 */
}

.highlight-danger {
  color: var(--el-color-danger);
}

.full-width-select {
  width: 100%;
}
</style>