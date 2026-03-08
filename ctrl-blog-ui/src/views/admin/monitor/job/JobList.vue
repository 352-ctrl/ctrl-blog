<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteJobApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的任务吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-input v-model="query.jobName" placeholder="请输入任务名称查询" prefix-icon="Search" clearable @clear="loadData" />
        <el-select v-model="query.jobGroup" placeholder="请选择任务组名" clearable>
          <el-option v-for="item in JobGroupOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.status" placeholder="请选择任务状态" clearable>
          <el-option v-for="item in JobStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </template>
      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增任务</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="JobColumns" v-model:selectedIds="selectedIds" :delete-api="deleteJobApi" delete-tip="确定删除该任务吗？" @edit="handleEdit" @delete-success="loadData" @status-change="handleStatusChange" />
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-md-down" width="800px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :sm="24" :md="12"><el-form-item label="任务名称" prop="jobName"><el-input v-model="dialog.rowData.value.jobName" autocomplete="off" placeholder="请输入任务名称" /></el-form-item></el-col>
          <el-col :sm="24" :md="12">
            <el-form-item label="任务分组" prop="jobGroup">
              <el-select v-model="dialog.rowData.value.jobGroup" placeholder="请选择分组" class="full-width">
                <el-option v-for="item in JobGroupOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="调用方法" prop="invokeTarget"><el-input v-model="dialog.rowData.value.invokeTarget" autocomplete="off" placeholder="请输入调用目标字符串" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="cron表达式" prop="cronExpression">
              <el-input v-model="dialog.rowData.value.cronExpression" placeholder="请输入cron表达式" clearable>
                <template #append><el-button type="primary" @click="showCron = true"><el-icon class="btn-icon"><Clock /></el-icon>生成表达式</el-button></template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="dialog.rowData.value.id != null">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="dialog.rowData.value.status">
                <el-radio v-for="item in JobStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :sm="24" :md="12">
            <el-form-item label="执行策略" prop="misfirePolicy">
              <el-radio-group v-model="dialog.rowData.value.misfirePolicy">
                <el-radio-button :label="1">立即执行</el-radio-button><el-radio-button :label="2">执行一次</el-radio-button><el-radio-button :label="3">放弃执行</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="备注信息" prop="remark"><el-input v-model="dialog.rowData.value.remark" type="textarea" :rows="3" maxlength="500" placeholder="请输入备注" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCron" title="Cron 表达式生成器" width="700px" append-to-body destroy-on-close>
      <Crontab @hide="showCron = false" @fill="(v) => { dialog.rowData.value.cronExpression = v; showCron = false; }" :expression="dialog.rowData.value.cronExpression" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import Crontab from '@/views/admin/monitor/job/components/Crontab/Crontab.vue';
import { addJob, changeStatus, deleteJob, deleteJobs, getJobById, getJobPage, updateJob } from "@/api/admin/monitor/job.js";
import { validateInvokeTarget } from "@/utils/validate.js";
// 引入组合式函数 Hooks
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const formRef = ref(null);
const showCron = ref(false);
const selectedIds = ref([]);

const JobGroupOptions = [ { label: '默认分组', value: 'DEFAULT' }, { label: '系统分组', value: 'SYSTEM' } ];
const JobStatusOptions = [ { label: '正常', value: 0 }, { label: '暂停', value: 1 } ];

const rules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }, { max: 64, message: '任务名称不能超过64个字符', trigger: 'blur' }],
  jobGroup: [{ required: true, message: '请选择任务组名', trigger: 'change' }, { max: 64, message: '任务组名不能超过64个字符', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '请输入调用目标字符串', trigger: 'blur' }, { max: 500, message: '调用目标字符串长度不能超过500', trigger: 'blur' }, { validator: validateInvokeTarget, trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入Cron执行表达式', trigger: 'blur' }, { max: 255, message: 'Cron表达式不能超过255个字符', trigger: 'blur' }],
  remark: [{ max: 500, message: '备注信息不能超过500个字符', trigger: 'blur' }]
};

// Hooks 集成管理全局状态
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getJobPage, { jobName: '', jobGroup: '', status: null });
const dialog = useDialog();
const { loading: submitLoading, execute: execSubmit } = useRequest((data) => data.id ? updateJob(data) : addJob(data), { successMsg: '操作成功' });

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleAdd = () => dialog.openDialog('新增任务', 'add', { misfirePolicy: 1, status: 0, jobGroup: 'DEFAULT' });

const handleEdit = async (row) => {
  const res = await getJobById(row.id);
  if (res.code === 200) dialog.openDialog('编辑任务', 'edit', res.data);
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      await execSubmit(dialog.rowData.value);
      dialog.closeDialog();
      loadData();
    }
  });
};

const deleteJobApi = async (id) => deleteJob(id);
const batchDeleteJobApi = async (ids) => deleteJobs(ids);

const handleStatusChange = (row) => {
  const text = row.status === 0 ? "启用" : "停用";
  ElMessageBox.confirm(`确认要"${text}""${row.jobName}"任务吗？`, "警告", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" })
      .then(() => changeStatus(row.id, row.status))
      .then((res) => {
        if (res.code === 200) ElMessage.success(`任务已${text}`);
        else { row.status = row.status === 0 ? 1 : 0; ElMessage.error(res.msg); }
      })
      .catch((err) => {
        row.status = row.status === 0 ? 1 : 0;
        err !== 'cancel' ? ElMessage.error('系统异常，操作失败') : ElMessage.info('已取消操作');
      })
      .finally(() => row.loading = false);
};

const JobColumns = reactive([
  { prop: 'jobName', label: '任务名称' },
  { type: 'status', prop: 'jobGroup', label: '任务组名', statusMap: { "DEFAULT": { text: '默认分组', type: 'info' }, "SYSTEM": { text: '系统分组', type: 'primary' } } },
  { prop: 'invokeTarget', label: '调用目标字符串' },
  { prop: 'cronExpression', label: 'cron执行表达式' },
  { type: 'switch', prop: 'status', label: '状态' }
]);
</script>

<style scoped>
/* 宽度撑满父容器 */
.full-width {
  width: 100%;
}

/* 按钮内图标的右侧间距 */
.btn-icon {
  margin-right: 5px;
}
</style>