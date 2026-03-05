<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteReportApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的举报吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-select
            v-model="data.targetType"
            placeholder="举报目标"
            clearable
            @clear="loadPage"
        >
          <el-option label="评论" value="COMMENT" />
          <el-option label="文章" value="ARTICLE" />
          <el-option label="用户" value="USER" />
        </el-select>

        <el-select
            v-model="data.status"
            placeholder="处理状态"
            clearable
            @clear="loadPage"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="属实已处罚" :value="1" />
          <el-option label="驳回/恶意举报" :value="2" />
        </el-select>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="reportColumns"
          :expandable="true"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteReportApi"
          delete-tip="确定删除该举报吗？"
          @delete-success="loadPage"
          @edit="handleProcess"
      >
        <template #expand="{ row }">
          <el-row>
            <el-form-item label="目标内容：">
              <span class="expand-value-box">{{ row.targetSummary || '无内容摘要' }}</span>
            </el-form-item>
          </el-row>

          <el-row>
            <el-form-item label="详细说明：">
              <span class="expand-value-box">{{ row.content || '未填写详细说明' }}</span>
            </el-form-item>
          </el-row>

          <el-row v-if="row.status !== 0">
            <el-form-item label="处理备注：">
              <span class="expand-value-box" style="background-color: var(--el-color-info-light-9);">
                {{ row.adminNote || '无内部处理备注' }}
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

    <el-dialog v-model="data.processVisible" title="审核举报" class="dialog-md-down" width="600px">
      <div style="background-color: #f5f7fa; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="举报目标">
            <el-tag size="small" :type="getTargetTypeColor(data.currentReport.targetType)">
              {{ getTargetTypeText(data.currentReport.targetType) }}
            </el-tag>
            <span style="margin-left: 10px; font-size: 12px; color: #999;">ID: {{ data.currentReport.targetId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="目标内容">
            <span style="color: #f56c6c;">{{ data.currentReport.targetSummary || '无摘要内容' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="举报原因">
            <strong>{{ data.currentReport.reason }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="详细说明">
            {{ data.currentReport.content || '未填写详细说明' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form ref="processFormRef" :model="data.processForm" :rules="data.processRules" label-width="100px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="data.processForm.status">
            <el-radio :label="1">举报属实 (已处罚)</el-radio>
            <el-radio :label="2">驳回/无效举报</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
            label="封禁时间"
            prop="disableDays"
            v-if="data.processForm.status === 1 && data.currentReport.targetType === 'USER'"
        >
          <el-select v-model="data.processForm.disableDays" placeholder="请选择封禁时长" style="width: 100%;">
            <el-option label="不封禁 (仅警告)" :value="0" />
            <el-option label="封禁 1 天" :value="1" />
            <el-option label="封禁 3 天" :value="3" />
            <el-option label="封禁 7 天" :value="7" />
            <el-option label="封禁 30 天" :value="30" />
            <el-option label="永久封禁" :value="-1" />
          </el-select>
        </el-form-item>

        <el-form-item label="处理备注" prop="adminNote">
          <el-input
              v-model="data.processForm.adminNote"
              type="textarea"
              :rows="3"
              placeholder="请输入内部处理备注（仅管理员可见），例如：已封禁该账号、已删除违规评论等"
              maxlength="255"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.processVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcessForm" :loading="data.submitLoading">确 认 处 理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {deleteReport, deleteReports, getReportPage, processReport} from "@/api/admin/report.js";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const processFormRef = ref(null);

const data = reactive({
  targetType: null,
  status: null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],

  // 审核弹窗相关
  processVisible: false,
  submitLoading: false,
  currentReport: {},
  processForm: {
    id: null,
    status: 1, // 默认选中属实
    adminNote: '',
    disableDays: 7 // 默认封禁 7 天
  },
  processRules: {
    status: [
      { required: true, message: '请选择审核结果', trigger: 'change' }
    ],
    disableDays: [
      { required: true, message: '请选择封禁时长', trigger: 'change' }
    ]
  }
});

const REASON_DICT = {
  SPAM: '垃圾广告',
  PORN: '色情低俗',
  ILLEGAL: '违法违规',
  ABUSE: '人身攻击/引战谩骂',
  COPYRIGHT: '抄袭/洗稿/侵权',
  IMPERSONATION: '冒充他人/身份造假',
  PROFILE_VIOLATION: '头像/昵称违规',
  OTHER: '其他原因'
}

// 加载分页数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    targetType: data.targetType === '' ? null : data.targetType,
    status: data.status === '' ? null : data.status,
  };
  getReportPage(params).then(res => {
    if (res.code === 200) {
      data.tableData = res.data.records.map(item => {
        return {
          ...item,
          // 如果字典里有，就用字典的中文；如果没有，就显示原英文
          reason: REASON_DICT[item.reason] || item.reason
        };
      });
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
  data.targetType = null;
  data.status = null;
  data.pageNum = 1;
  loadPage();
};

// 点击审核处理按钮
const handleProcess = (row) => {
  // 终态拦截：如果已经处理过，禁止二次修改
  if (row.status === 1 || row.status === 2) {
    ElMessage.warning('该举报已审核完毕，不可重复处理');
    return;
  }

  data.currentReport = row;
  data.processForm = {
    id: row.id,
    status: 1, // 默认属实
    adminNote: row.adminNote || '',
    disableDays: 7
  };

  if (processFormRef.value) {
    processFormRef.value.clearValidate();
  }

  data.processVisible = true;
};

// 提交审核表单
const submitProcessForm = () => {
  processFormRef.value.validate((valid) => {
    if (valid) {
      data.submitLoading = true;
      processReport(data.processForm).then(res => {
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

// 辅助方法：获取目标类型文本
const getTargetTypeText = (type) => {
  const map = { 'COMMENT': '评论', 'ARTICLE': '文章', 'USER': '用户' };
  return map[type] || '其他';
};

// 辅助方法：获取目标类型颜色
const getTargetTypeColor = (type) => {
  const map = { 'COMMENT': 'info', 'ARTICLE': 'primary', 'USER': 'warning' };
  return map[type] || 'info';
};

// 指定删除
const deleteReportApi = async (id) => {
  return deleteReport(id);
};

// 批量删除
const batchDeleteReportApi = async (selectedIds) => {
  return deleteReports(selectedIds);
};

// 表格列配置
const reportColumns = reactive([
  { prop: 'userNickname', label: '举报人', minWidth: '120px' },
  { type: 'status', prop: 'targetType', label: '目标类型',
    statusMap: {
      'COMMENT': { text: '评论', type: 'info' },
      'ARTICLE': { text: '文章', type: 'primary' },
      'USER': { text: '用户', type: 'warning' }
    }
  },
  { prop: 'targetSummary', label: '被举报内容', minWidth: '150px', showOverflowTooltip: true },
  { prop: 'reason', label: '违规原因', minWidth: '120px' },
  { prop: 'content', label: '详细说明', minWidth: '150px', showOverflowTooltip: true },
  { type: 'status', prop: 'status', label: '状态', minWidth: '130px',
    statusMap: {
      0: { text: '待处理', type: 'warning' },
      1: { text: '属实已处罚', type: 'success' },
      2: { text: '驳回/恶意举报', type: 'info' }
    }
  },
  { prop: 'createTime', label: '提交时间', minWidth: '160px' }
]);
</script>

<style scoped>

</style>