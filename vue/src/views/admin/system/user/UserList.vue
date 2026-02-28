<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteUserApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的用户吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.nickname"
            placeholder="请输入昵称查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="userColumns"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteUserApi"
          delete-tip="确定删除该用户吗？"
          :action-width="190"
          @edit="handleEdit"
          @delete-success="loadPage"
      >
        <template #custom-actions="{ row }">
          <el-tooltip content="重置密码" placement="top">
            <el-button
                type="warning"
                icon="Key"
                circle
                class="action-btn-mr"
                @click="handleResetPwd(row)"
            />
          </el-tooltip>
        </template>
      </AdminTable>

      <AdminPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>

    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑用户' : '新增用户'" class="dialog-md-down" width="550px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="data.form.email" :disabled="!!data.form.id" autocomplete="off" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="data.form.nickname" autocomplete="off" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!data.form.id">
          <el-input v-model="data.form.password" type="password" show-password autocomplete="off" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="data.form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" disabled />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input
              v-model="data.form.bio"
              type="textarea"
              :rows="3"
              maxlength="200"
              show-word-limit
              placeholder="请输入个人简介"
          />
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
              :action="uploadFileUrl"
              :on-success="handleFileSuccess"
              :headers="{ token: userStore.token }"
              :before-upload="beforeUpload"
              @delete-success="loadPage"
              list-type="picture"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
// 引入 API 模块
import {
  addUser,
  deleteUser,
  deleteUsers,
  getUserById,
  getUserPage,
  resetPassword,
  updateUser
} from "@/api/admin/user.js";
import {
  validateEmail,
  validateNickname,
  validatePasswordComplexity,
  regPasswordStrength
} from "@/utils/validate.js";
import { useUserStore } from "@/store/user.js";

const BASE_API = 'http://localhost:9999'
const uploadFileUrl = `${BASE_API}/api/files/upload`;
const userStore = useUserStore()
const formRef = ref(null);

const data = reactive({
  nickname: '',
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: [],
  rules: {
    email: [
      {
        required: true,
        message: '请输入邮箱',
        trigger: 'blur',
        validator: (rule, value, callback) => {
          if (data.form.id) {
            callback();
            return;
          }
          validateEmail(rule, value, callback);
        }
      }
    ],
    nickname: [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { validator: validateNickname, trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { validator: validatePasswordComplexity, trigger: 'blur' }
    ],
    role: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }
});

onMounted(() => {
  loadPage();
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    nickname: data.nickname
  };
  getUserPage(params).then(res => {
    if (res.code === 200) {
      data.tableData = res.data.records;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 重置搜索
const resetSearch = () => {
  data.nickname = "";
  data.pageNum = 1;
  loadPage();
};

// 新增按钮逻辑
const handleAdd = () => {
  data.form = {};
  data.formVisible = true;
};

// 编辑按钮逻辑
const handleEdit = async (row) => {
  try {
    const res = await getUserById(row.id);

    if (res.code === 200) {
      // 将数据库返回的完整详情赋值给表单
      data.form = res.data;
      data.formVisible = true;
    } else {
      ElMessage.error(res.msg || '获取详情失败');
    }
  } catch (error) {
    console.error('获取详情异常:', error);
    ElMessage.error('网络异常，无法获取数据');
  }
}

// 提交表单 (新增或修改)
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const apiMethod = data.form.id ? updateUser : addUser;
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
const deleteUserApi = async (id) => {
  return deleteUser(id);
};

// 批量删除
const batchDeleteUserApi = async (selectedIds) => {
  return deleteUsers(selectedIds);
};

// 补充重置密码的具体业务逻辑
const handleResetPwd = (row) => {
  ElMessageBox.prompt(`正在为用户【${row.nickname}】重置密码，请输入新密码：`, '强制重置密码', {
    confirmButtonText: '确定重置',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: regPasswordStrength,
    inputErrorMessage: '密码需8-20位，必须包含字母和数字'
  }).then(({ value }) => {
    resetPassword({ id: row.id, newPassword: value }).then(() => {
      ElMessage.success('重置成功！该用户已被强制下线。');
    });
  }).catch(() => {});
};

// 头像上传成功回调
const handleFileSuccess = (res) => {
  if (res.code === 200) {
    data.form.avatar = res.data; // 绑定封面URL到表单
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error('头像上传失败：' + res.msg);
  }
}

// 头像上传前的校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/'); // 校验是否为图片格式
  if (!isImage) {
    ElMessage.error('只能上传图片格式文件！');
    return false; // 终止上传
  }
  const isLt10M = file.size / 1024 / 1024 < 10; // 校验图片大小不超过10MB
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB！');
    return false; // 终止上传
  }
  return true; // 校验通过，允许上传
};

// 用户列表列配置
const userColumns = reactive([
  { type: 'avatar', prop: 'avatar', label: '头像' },
  { prop: 'nickname', label: '昵称' },
  { prop: 'email', label: '邮箱' },
  { type: 'status', prop: 'role', label: '权限',
    statusMap: {
      "SUPER_ADMIN": { text: '超级管理员', type: 'warning' },
      "ADMIN": { text: '管理员', type: 'danger' },
      "USER": { text: '用户', type: 'success' }
    }
  },
  { prop: 'createTime', label: '创建时间' }
])
</script>