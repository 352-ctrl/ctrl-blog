<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteFeedbackApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的反馈吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-select v-model="query.type" placeholder="反馈类型" clearable @clear="loadData">
          <el-option label="意见建议" :value="0" /><el-option label="BUG反馈" :value="1" /><el-option label="其他" :value="2" />
        </el-select>
        <el-select v-model="query.status" placeholder="处理状态" clearable @clear="loadData">
          <el-option label="待处理" :value="0" /><el-option label="处理中" :value="1" /><el-option label="已解决" :value="2" /><el-option label="已驳回" :value="3" />
        </el-select>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="feedbackColumns" :expandable="true" v-model:selectedIds="selectedIds" :delete-api="deleteFeedbackApi" delete-tip="确定删除该反馈吗？" @delete-success="loadData" @edit="handleProcess">
        <template #expand="{ row }">
          <el-row><el-form-item label="反馈内容："><span class="expand-value-box">{{ row.content || '无内容' }}</span></el-form-item></el-row>
          <el-row v-if="row.adminReply">
            <el-form-item label="管理回复：">
              <span class="expand-value-box admin-reply-box">{{ row.adminReply }}</span>
            </el-form-item>
          </el-row>
        </template>
      </AdminTable>
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" title="处理反馈" class="dialog-lg-down" width="800px">
      <div class="feedback-info-container">
        <p class="info-label"><strong>反馈内容：</strong></p>
        <p class="info-content">{{ dialog.rowData.value.content }}</p>
        <p v-if="dialog.rowData.value.contactEmail" class="contact-email-text">
          <strong>联系邮箱：</strong> {{ dialog.rowData.value.contactEmail }}
        </p>
      </div>

      <el-form ref="processFormRef" :model="dialog.rowData.value" :rules="processRules" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="dialog.rowData.value.status">
            <el-radio :value="1" v-if="dialog.rowData.value.type !== 2">处理中</el-radio>
            <el-radio :value="2">已解决 (同意申诉/解封)</el-radio>
            <el-radio :value="3">已驳回 (维持处罚)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="管理员回复" prop="adminReply">
          <el-input v-model="dialog.rowData.value.adminReply" type="textarea" :rows="4" placeholder="请输入回复内容，如果用户留有邮箱，此回复将通过邮件发送给用户。" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" @click="submitProcessForm" :loading="submitLoading">提 交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { deleteFeedback, deleteFeedbacks, getFeedbackPage, processFeedback } from "@/api/admin/operation/feedback.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const processFormRef = ref(null);
const selectedIds = ref([]);
const processRules = { status: [{ required: true, message: '请选择处理状态', trigger: 'change' }] };

const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getFeedbackPage, { type: null, status: null });
const dialog = useDialog();
const { loading: submitLoading, execute: execProcess } = useRequest(processFeedback, { successMsg: '处理成功' });

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleProcess = (row) => {
  if (row.status === 2 || row.status === 3) {
    ElMessage.warning('该反馈已归档完结，不支持二次修改');
    return;
  }
  dialog.openDialog('处理反馈', 'edit', { ...row, status: row.status === 0 ? 2 : row.status, adminReply: row.adminReply || '' });
};

const submitProcessForm = () => {
  processFormRef.value.validate(async (valid) => {
    if (valid) {
      await execProcess({ id: dialog.rowData.value.id, status: dialog.rowData.value.status, adminReply: dialog.rowData.value.adminReply });
      dialog.closeDialog();
      loadData();
    }
  });
};

const deleteFeedbackApi = async (id) => deleteFeedback(id);
const batchDeleteFeedbackApi = async (ids) => deleteFeedbacks(ids);

const feedbackColumns = reactive([
  { prop: 'userNickname', label: '反馈用户' },
  { type: 'status', prop: 'type', label: '类型', minWidth: '130px', statusMap: { 0: { text: '意见建议', type: 'primary' }, 1: { text: 'BUG反馈', type: 'danger' }, 2: { text: '封禁申诉', type: 'warning' }, 3: { text: '其他', type: 'info' } } },
  { prop: 'content', label: '反馈内容', minWidth: '200px', showOverflowTooltip: true },
  { prop: 'contactEmail', label: '联系邮箱', minWidth: '160px' },
  { type: 'status', prop: 'status', label: '状态', minWidth: '130px', statusMap: { 0: { text: '待处理', type: 'warning' }, 1: { text: '处理中', type: 'primary' }, 2: { text: '已解决', type: 'success' }, 3: { text: '已驳回', type: 'info' } } },
  { prop: 'createTime', label: '提交时间', minWidth: '160px' }
]);
</script>

<style scoped>
.admin-reply-box {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-7);
}

.feedback-info-container {
  background-color: var(--el-fill-color-light); /* 适配暗黑模式 */
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.info-label {
  margin: 0 0 10px 0;
}

.info-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.contact-email-text {
  margin: 10px 0 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>