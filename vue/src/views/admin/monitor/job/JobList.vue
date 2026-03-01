<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteJobApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的任务吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.jobName"
            placeholder="请输入任务名称查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
        <el-select
            v-model="data.jobGroup"
            placeholder="请选择任务组名"
            clearable
        >
          <el-option
              v-for="item in data.JobGroupOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-select
            v-model="data.status"
            placeholder="请选择任务状态"
            clearable
        >
          <el-option
              v-for="item in data.JobStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增任务</el-button>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="JobColumns"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteJobApi"
          delete-tip="确定删除该任务吗？"
          @edit="handleEdit"
          @delete-success="loadPage"
          @status-change="handleStatusChange"
      />

      <AdminPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>

    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑任务' : '新增任务'" class="dialog-md-down" width="800px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :sm="24" :md="12">
            <el-form-item label="任务名称" prop="jobName">
              <el-input v-model="data.form.jobName" autocomplete="off" placeholder="请输入任务名称" />
            </el-form-item>
          </el-col>

          <el-col :sm="24" :md="12">
            <el-form-item label="任务分组" prop="jobGroup">
              <el-select v-model="data.form.jobGroup" placeholder="请选择分组" style="width: 100%;">
                <el-option
                    v-for="item in data.JobGroupOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="调用方法" prop="invokeTarget">
              <el-input v-model="data.form.invokeTarget" autocomplete="off" placeholder="请输入调用目标字符串" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="cron表达式" prop="cronExpression">
              <el-input
                  v-model="data.form.cronExpression"
                  placeholder="请输入cron表达式"
                  clearable
              >
                <template #append>
                  <el-button type="primary" @click="handleShowCron">
                    <el-icon style="margin-right: 5px"><Clock /></el-icon>
                    生成表达式
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="data.form.id != null">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="data.form.status">
                <el-radio
                    v-for="item in data.JobStatusOptions"
                    :key="item.value"
                    :value="item.value"
                >
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :sm="24" :md="12">
            <el-form-item label="执行策略" prop="misfirePolicy">
              <el-radio-group v-model="data.form.misfirePolicy">
                <el-radio-button :label="1">立即执行</el-radio-button>
                <el-radio-button :label="2">执行一次</el-radio-button>
                <el-radio-button :label="3">放弃执行</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注信息" prop="remark">
              <el-input
                  v-model="data.form.remark"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  autocomplete="off"
                  placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog
        v-model="showCron"
        title="Cron 表达式生成器"
        width="700px"
        append-to-body
        destroy-on-close
    >
      <Crontab
          @hide="showCron = false"
          @fill="handleCronFill"
          :expression="data.form.cronExpression"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";
import Crontab from '@/views/admin/monitor/job/components/Crontab/Crontab.vue';

import {
  addJob, changeStatus,
  deleteJob,
  deleteJobs,
  getJobById,
  getJobPage,
  updateJob
} from "@/api/admin/job.js";
import { validateInvokeTarget } from "@/utils/validate.js";

const formRef = ref(null);

// 控制弹窗显示的变量
const showCron = ref(false);

const data = reactive({
  jobName: '',
  jobGroup: '',
  status: null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: [],

  JobGroupOptions: [
    { label: '默认分组', value: 'DEFAULT' },
    { label: '系统分组', value: 'SYSTEM' }
  ],

  JobStatusOptions: [
    { label: '正常', value: 0 },
    { label: '暂停', value: 1 }
  ],

  rules: {
    jobName: [
      { required: true, message: '请输入任务名称', trigger: 'blur' },
      { max: 64, message: '任务名称不能超过64个字符', trigger: 'blur' }
    ],
    jobGroup: [
      { required: true, message: '请选择任务组名', trigger: 'change' },
      { max: 64, message: '任务组名不能超过64个字符', trigger: 'blur' }
    ],
    invokeTarget: [
      { required: true, message: '请输入调用目标字符串', trigger: 'blur' },
      { max: 500, message: '调用目标字符串长度不能超过500', trigger: 'blur' },
      { validator: validateInvokeTarget, trigger: 'blur' }
    ],
    cronExpression: [
      { required: true, message: '请输入Cron执行表达式', trigger: 'blur' },
      { max: 255, message: 'Cron表达式不能超过255个字符', trigger: 'blur' }
    ],
    remark: [
      { max: 500, message: '备注信息不能超过500个字符', trigger: 'blur' }
    ]
  }
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    jobName: data.jobName,
    jobGroup: data.jobGroup,
    status: data.status,
  };
  getJobPage(params).then(res => {
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
  data.jobName = "";
  data.jobGroup = "";
  data.status = null;
  data.pageNum = 1;
  loadPage();
};

// 新增按钮逻辑
const handleAdd = () => {
  data.form = {
    misfirePolicy: 1,      // 默认选中 "立即执行"
    status: 0,             // 默认状态 "正常"
    jobGroup: 'DEFAULT',   // 默认分组 "DEFAULT"
  };
  data.formVisible = true;
};

// 编辑按钮逻辑
const handleEdit = async (row) => {
  try {
    const res = await getJobById(row.id);

    if (res.code === 200) {
      // 将数据库返回的完整详情赋值给表单
      data.form = res.data;

      // 数据获取成功后，再显示弹窗
      data.formVisible = true;
    } else {
      ElMessage.error(res.msg || '获取详情失败');
    }
  } catch (error) {
    console.error('获取详情异常:', error);
    ElMessage.error('网络异常，无法获取数据');
  }
};

// 点击按钮：显示弹窗
const handleShowCron = () => {
  showCron.value = true;
}

const handleCronFill = (value) => {
  data.form.cronExpression = value;
  showCron.value = false;
}

// 提交表单 (新增或修改)
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const apiMethod = data.form.id ? updateJob : addJob;
      apiMethod(data.form).then(res => {
        if (res.code === 200) {
          ElMessage.success('操作成功');
          data.formVisible = false;
          loadPage();
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  });
};

// 指定删除
const deleteJobApi = async (id) => {
  return deleteJob(id);
};

// 批量删除
const batchDeleteJobApi = async (selectedIds) => {
  return deleteJobs(selectedIds);
};

// 修改状态
const handleStatusChange = (row) => {
  const text = row.status === 0 ? "启用" : "停用";

  ElMessageBox.confirm(
      `确认要"${text}""${row.jobName}"任务吗？`,
      "警告",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }
  )
      .then(() => {
        return changeStatus(row.id, row.status);
      })
      .then((res) => {
        if (res.code === 200) {
          ElMessage.success(`任务已${text}`);
        } else {
          row.status = row.status === 0 ? 1 : 0;
          ElMessage.error(res.msg);
        }
      })
      .catch((err) => {
        row.status = row.status === 0 ? 1 : 0;

        if (err !== 'cancel') {
          console.error(err);
          ElMessage.error('系统异常，操作失败');
        } else {
          ElMessage.info('已取消操作');
        }
      })
      .finally(() => {
        row.loading = false;
      });
};

// 任务列表列配置
const JobColumns = reactive([
  { prop: 'jobName', label: '任务名称' },
  { type: 'status', prop: 'jobGroup', label: '任务组名',
    statusMap: {
      "DEFAULT": { text: '默认分组', type: 'info' },
      "SYSTEM": { text: '系统分组', type: 'primary' }
    }
  },
  { prop: 'invokeTarget', label: '调用目标字符串' },
  { prop: 'cronExpression', label: 'cron执行表达式' },
  { type: 'switch', prop: 'status', label: '状态' }
]);
</script>

<style scoped>

</style>