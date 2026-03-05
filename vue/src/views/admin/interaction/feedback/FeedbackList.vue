<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteFeedbackApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的反馈吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-select
            v-model="data.type"
            placeholder="反馈类型"
            clearable
            @clear="loadPage"
        >
          <el-option label="意见建议" :value="0" />
          <el-option label="BUG反馈" :value="1" />
          <el-option label="其他" :value="2" />
        </el-select>

        <el-select
            v-model="data.status"
            placeholder="处理状态"
            clearable
            @clear="loadPage"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已解决" :value="2" />
          <el-option label="已驳回" :value="3" />
        </el-select>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="feedbackColumns"
          :expandable="true"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteFeedbackApi"
          delete-tip="确定删除该反馈吗？"
          @delete-success="loadPage"
          @edit="handleProcess"
      >
        <template #expand="{ row }">
          <el-row>
            <el-form-item label="反馈内容：">
              <span class="expand-value-box">{{ row.content || '无内容' }}</span>
            </el-form-item>
          </el-row>

          <el-row v-if="row.adminReply">
            <el-form-item label="管理回复：">
              <span class="expand-value-box" style="background-color: var(--el-color-primary-light-9); border-color: var(--el-color-primary-light-7);">
                {{ row.adminReply }}
              </span>
            </el-form-item>
          </el-row>
        </template>
      </AdminTable>

      <AdminPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>

    <el-dialog v-model="data.processVisible" title="处理反馈" class="dialog-lg-down" width="800px">
      <div style="background-color: #f5f7fa; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
        <p style="margin: 0 0 10px 0;"><strong>反馈内容：</strong></p>
        <p style="margin: 0; white-space: pre-wrap; word-break: break-all;">{{ data.currentFeedback.content }}</p>
        <p v-if="data.currentFeedback.contactEmail" style="margin: 10px 0 0 0; font-size: 12px; color: #666;">
          <strong>联系邮箱：</strong> {{ data.currentFeedback.contactEmail }}
        </p>
      </div>

      <el-form ref="processFormRef" :model="data.processForm" :rules="data.processRules" label-width="100px">
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="data.processForm.status">
            <el-radio :value="1" v-if="data.currentFeedback.type !== 2">处理中</el-radio>
            <el-radio :value="2">已解决 (同意申诉/解封)</el-radio>
            <el-radio :value="3">已驳回 (维持处罚)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="管理员回复" prop="adminReply">
          <el-input
              v-model="data.processForm.adminReply"
              type="textarea"
              :rows="4"
              placeholder="请输入回复内容，如果用户留有邮箱，此回复将通过邮件发送给用户。"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.processVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcessForm" :loading="data.submitLoading">提 交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="data.imageViewerVisible" title="附加截图预览" width="60%">
      <div style="text-align: center;">
        <el-image
            v-for="(img, index) in data.previewImages"
            :key="index"
            :src="img"
            style="max-width: 100%; margin-bottom: 10px;"
            fit="contain"
            :preview-src-list="data.previewImages"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {deleteFeedback, deleteFeedbacks, getFeedbackPage, processFeedback} from "@/api/admin/feedback.js";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const processFormRef = ref(null);

const data = reactive({
  type: null,
  status: null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],

  // 处理反馈弹窗相关
  processVisible: false,
  submitLoading: false,
  currentFeedback: {}, // 用于展示用户提交的内容
  processForm: {
    id: null,
    status: 2, // 默认选中已解决
    adminReply: ''
  },
  processRules: {
    status: [
      { required: true, message: '请选择处理状态', trigger: 'change' }
    ]
  },

  // 图片预览相关
  imageViewerVisible: false,
  previewImages: []
});

// 加载分页数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    type: data.type === '' ? null : data.type,
    status: data.status === '' ? null : data.status,
  };
  getFeedbackPage(params).then(res => {
    if (res.code === 200) {
      data.tableData = res.data.records;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

onMounted(() => {
  loadPage();
});

// 重置查询条件
const resetSearch = () => {
  data.type = null;
  data.status = null;
  data.pageNum = 1;
  loadPage();
};

// 点击处理按钮
const handleProcess = (row) => {
  if (row.status === 2 || row.status === 3) {
    ElMessage.warning('该反馈已归档完结，不支持二次修改');
    return;
  }

  data.currentFeedback = row;
  data.processForm = {
    id: row.id,
    // 如果之前有状态，带入之前的状态，如果是待处理(0)，默认给个处理中(1)或已解决(2)
    status: row.status === 0 ? 2 : row.status,
    adminReply: row.adminReply || ''
  };
  data.processVisible = true;
};

// 提交处理表单
const submitProcessForm = () => {
  processFormRef.value.validate((valid) => {
    if (valid) {
      data.submitLoading = true;
      processFeedback(data.processForm).then(res => {
        if (res.code === 200) {
          ElMessage.success('处理成功');
          data.processVisible = false;
          loadPage();
        } else {
          ElMessage.error(res.msg);
        }
      }).finally(() => {
        data.submitLoading = false;
      });
    }
  });
};

// 指定删除
const deleteFeedbackApi = async (id) => {
  return deleteFeedback(id);
};

// 批量删除
const batchDeleteFeedbackApi = async (selectedIds) => {
  return deleteFeedbacks(selectedIds);
};



// 表格列配置
const feedbackColumns = reactive([
  { prop: 'userNickname', label: '反馈用户' },
  { type: 'status', prop: 'type', label: '类型', minWidth: '130px',
    statusMap: {
      0: { text: '意见建议', type: 'primary' },
      1: { text: 'BUG反馈', type: 'danger' },
      2: { text: '封禁申诉', type: 'warning' },
      3: { text: '其他', type: 'info' }
    }
  },
  { prop: 'content', label: '反馈内容', minWidth: '200px', showOverflowTooltip: true },
  { prop: 'contactEmail', label: '联系邮箱', minWidth: '160px' },
  { type: 'status', prop: 'status', label: '状态', minWidth: '130px',
    statusMap: {
      0: { text: '待处理', type: 'warning' },
      1: { text: '处理中', type: 'primary' },
      2: { text: '已解决', type: 'success' },
      3: { text: '已驳回', type: 'info' }
    }
  },
  { prop: 'createTime', label: '提交时间', minWidth: '160px' }
]);
</script>

<style scoped>
.truncate-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>