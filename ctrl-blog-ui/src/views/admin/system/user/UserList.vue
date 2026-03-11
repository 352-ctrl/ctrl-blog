<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteUserApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的用户吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-input v-model="query.nickname" placeholder="请输入昵称查询" prefix-icon="Search" clearable @clear="loadData" />
      </template>
      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="userColumns" :expandable="true" v-model:selectedIds="selectedIds" :delete-api="deleteUserApi" delete-tip="确定删除该用户吗？" :action-width="190" @edit="handleEdit" @delete-success="loadData">
        <template #expand="{ row }">
          <el-row>
            <el-form-item label="个人简介："><span class="expand-value-box">{{ row.bio || '该用户很懒，什么都没写~' }}</span></el-form-item>
          </el-row>
          <el-row v-if="row.status === 1 || row.disableReason || row.disableEndTime">
            <el-form-item label="封禁详情：">
              <div class="expand-value-box ban-box">
                <div class="ban-item">
                  <span class="ban-label">解封时间：</span>
                  <span class="ban-text" :class="{ 'is-bold': row.disableEndTime?.startsWith('2099') }">
                    {{ !row.disableEndTime ? '未设置 (需手动解封)' : (row.disableEndTime.startsWith('2099') ? '永久封禁' : row.disableEndTime) }}
                  </span>
                </div>
                <div class="ban-item"><span class="ban-label">处罚原因：</span><span class="ban-text">{{ row.disableReason || '未填写具体原因' }}</span></div>
              </div>
            </el-form-item>
          </el-row>
        </template>
        <template #custom-actions="{ row }">
          <el-tooltip content="重置密码" placement="top"><el-button type="warning" icon="Key" circle class="action-btn-mr" @click="handleResetPwd(row)" /></el-tooltip>
        </template>
      </AdminTable>
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-md-down" width="550px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules" label-width="80px">
        <el-form-item label="邮箱" prop="email"><el-input v-model="dialog.rowData.value.email" :disabled="!!dialog.rowData.value.id" autocomplete="off" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="昵称" prop="nickname"><el-input v-model="dialog.rowData.value.nickname" autocomplete="off" placeholder="请输入昵称" /></el-form-item>
        <el-form-item label="密码" prop="password" v-if="!dialog.rowData.value.id"><el-input v-model="dialog.rowData.value.password" type="password" show-password autocomplete="off" placeholder="请输入密码" /></el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="dialog.rowData.value.role" placeholder="请选择角色" class="full-width-input">
            <el-option label="超级管理员" value="SUPER_ADMIN" disabled />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialog.rowData.value.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">禁用</el-radio>
            <el-radio :label="2" disabled v-if="dialog.rowData.value.status === 2">注销冷静期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="解封时间" prop="disableEndTime" v-if="dialog.rowData.value.status === 1">
          <el-date-picker v-model="dialog.rowData.value.disableEndTime" type="datetime" placeholder="请选择自动解封时间" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" class="full-width-input" clearable />
          <div class="form-tip-text">留空代表无限期封禁(需手动解封)；选择未来的时间，系统将在该时间自动解封。</div>
        </el-form-item>
        <el-form-item label="封禁原因" prop="disableReason" v-if="dialog.rowData.value.status === 1"><el-input v-model="dialog.rowData.value.disableReason" type="textarea" :rows="2" maxlength="255" show-word-limit placeholder="请输入封禁原因" /></el-form-item>
        <el-form-item label="个人简介" prop="bio"><el-input v-model="dialog.rowData.value.bio" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="请输入个人简介" /></el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload-container">
            <el-image
                v-if="dialog.rowData.value.avatar"
                :src="dialog.rowData.value.avatar"
                class="avatar-preview-img"
                :preview-src-list="[dialog.rowData.value.avatar]"
                fit="cover"
            />
            <div v-else class="avatar-placeholder">
              <el-icon><User /></el-icon>
            </div>

            <div class="upload-actions">
              <el-upload
                  action="#"
                  :http-request="customUploadAvatar"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
              >
                <el-button type="primary" plain>选择图片</el-button>
              </el-upload>
              <div class="upload-tip">建议尺寸 200x200 像素，支持 jpg、png 格式，大小不超过 10MB。</div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { addUser, deleteUser, deleteUsers, getUserById, getUserPage, resetPassword, updateUser } from "@/api/admin/system/user.js";
import { validateEmail, validateNickname, validatePasswordComplexity, regPasswordStrength } from "@/utils/validate.js";
import { useUserStore } from "@/store/user.js";
import { uploadFile } from "@/api/common/file.js";
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const formRef = ref(null);
const selectedIds = ref([]);

// Hooks 接入
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getUserPage, { nickname: '' });
const dialog = useDialog();
const { loading: submitLoading, execute: execSubmit } = useRequest((data) => data.id ? updateUser(data) : addUser(data), { successMsg: '操作成功' });
const { execute: execResetPwd } = useRequest(resetPassword, { successMsg: '重置成功！该用户已被强制下线。' });

const rules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur', validator: (rule, value, callback) => dialog.rowData.value.id ? callback() : validateEmail(rule, value, callback) }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { validator: validateNickname, trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { validator: validatePasswordComplexity, trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
};

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleAdd = () => dialog.openDialog('新增用户', 'add', { status: 0 });

const handleEdit = async (row) => {
  const res = await getUserById(row.id);
  if (res.code === 200) dialog.openDialog('编辑用户', 'edit', res.data);
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      await execSubmit(dialog.rowData.value);
      dialog.closeDialog();
      await loadData();
    }
  });
};

const deleteUserApi = async (id) => deleteUser(id);
const batchDeleteUserApi = async (ids) => deleteUsers(ids);

const handleResetPwd = (row) => {
  ElMessageBox.prompt(`正在为用户【${row.nickname}】重置密码，请输入新密码：`, '强制重置密码', {
    confirmButtonText: '确定重置', cancelButtonText: '取消', inputType: 'password', inputPattern: regPasswordStrength, inputErrorMessage: '密码需8-20位，必须包含字母和数字'
  }).then(({ value }) => execResetPwd({ id: row.id, newPassword: value })).catch(() => {});
};

const customUploadAvatar = async (options) => {
  const formData = new FormData();
  formData.append('file', options.file);

  try {
    const res = await uploadFile(formData, 'avatar');
    if (res.code === 200) {
      dialog.rowData.value.avatar = res.data;
      ElMessage.success('头像上传成功');
      options.onSuccess(res);
    } else {
      ElMessage.error('头像上传失败：' + res.msg);
      options.onError(new Error(res.msg));
    }
  } catch (error) {
    options.onError(error);
  }
};

const beforeUpload = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片格式文件！'); return false; }
  if (file.size / 1024 / 1024 > 10) { ElMessage.error('图片大小不能超过10MB！'); return false; }
  return true;
};

const userColumns = reactive([
  { type: 'avatar', prop: 'avatar', label: '头像' },
  { prop: 'nickname', label: '昵称' },
  { prop: 'email', label: '邮箱' },
  { type: 'status', prop: 'role', label: '权限', statusMap: { "SUPER_ADMIN": { text: '超级管理员', type: 'warning' }, "ADMIN": { text: '管理员', type: 'danger' }, "USER": { text: '用户', type: 'success' } } },
  { type: 'status', prop: 'status', label: '状态', statusMap: { 0: { text: '正常', type: 'success' }, 1: { text: '禁用', type: 'danger' }, 2: { text: '注销冷静期', type: 'info' } } },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>

<style scoped>
.full-width-input {
  width: 100%;
}

.form-tip-text {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399); /* 优先使用 Element Plus 主题变量 */
  margin-top: 4px;
  line-height: 1.4;
}

.is-bold {
  font-weight: bold;
}

/* --- 封禁详情专属警示样式 --- */
.ban-box {
  background-color: var(--el-color-danger-light-9) !important;
  border-color: var(--el-color-danger-light-7) !important;
  color: var(--el-color-danger);
  padding: 10px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ban-item {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
  line-height: 1.6;
}

.ban-label {
  font-weight: bold;
  margin-right: 8px;
  white-space: nowrap;
  opacity: 0.9;
}

.ban-text {
  flex: 1;
  word-break: break-all;
  color: var(--el-color-danger);
}

/* --- 头像上传区域样式优化 --- */
.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview-img, .avatar-placeholder {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  border: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  box-shadow: var(--el-box-shadow-light);
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-placeholder);
  font-size: 32px;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>