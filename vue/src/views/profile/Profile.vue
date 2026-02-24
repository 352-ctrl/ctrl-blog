<template>
  <div :class="[isManager ? null : 'content-container']">
    <el-row :gutter="20">
      <el-col :xs="24" :md="7">
        <el-card class="box-card" shadow="never">
          <template #header>
            <span class="card-header-text">个人信息</span>
          </template>

          <div class="avatar-container">
            <AvatarUpload
                v-model="data.userForm.avatar"
                :size="100"
                @upload-success="handleSuccess"
            />

            <ul class="user-info-list">
              <li class="list-group-item">
                <div class="item-label"><el-icon><User /></el-icon>用户名称</div>
                <div class="item-value">{{ userStore.nickname || '未设置' }}</div>
              </li>
              <li class="list-group-item">
                <div class="item-label"><el-icon><Message /></el-icon>用户邮箱</div>
                <div class="item-value">{{ userStore.email || '未设置' }}</div>
              </li>
              <li v-if="isManager" class="list-group-item">
                <div class="item-label"><el-icon><UserFilled /></el-icon>所属角色</div>
                <div class="item-value">
                  <el-tag size="small" type="success">{{ userStore.role }}</el-tag>
                </div>
              </li>
              <li class="list-group-item">
                <div class="item-label"><el-icon><Calendar /></el-icon>创建日期</div>
                <div class="item-value">{{ userStore.createTime }}</div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="17">
        <el-card class="box-card" shadow="never">
          <el-tabs v-model="activeTab" class="settings-tabs">

            <el-tab-pane label="基本设置" name="base">
              <div class="tab-title">基本设置</div>

              <el-form ref="baseFormRef" :rules="baseRules" :model="data.userForm" label-width="100px">
                <el-form-item label="昵称">
                  <el-input v-model="data.userForm.nickname" autocomplete="off" placeholder="请输入昵称" />
                </el-form-item>
              </el-form>

              <div class="form-actions">
                <el-button type="primary" class="submit-btn" @click="submitBaseForm">保存</el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <div class="tab-title">修改密码</div>

              <el-form ref="passwordFormRef" :rules="passwordRules" :model="passwordData" label-width="100px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordData.oldPassword" type="password" show-password autocomplete="off" placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordData.newPassword" type="password" show-password autocomplete="off" placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="new2Password">
                  <el-input v-model="passwordData.new2Password" type="password" show-password autocomplete="off" placeholder="请确认新密码" />
                </el-form-item>
              </el-form>

              <div class="form-actions">
                <el-button type="primary" class="submit-btn" @click="submitPasswordForm">保存</el-button>
              </div>
            </el-tab-pane>

          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from '@/store/user.js'
import AvatarUpload from "./components/AvatarUpload.vue";
import { validateNickname, validatePasswordComplexity } from "@/utils/validate.js";
import { updateProfile, changePassword } from "@/api/userInfo.js"
import { useRoute } from "vue-router";

const userStore = useUserStore()
const route = useRoute();
const isManager = computed(() => route.path.startsWith('/manager'));

const BASE_API = 'http://localhost:9999'
const uploadFileUrl = `${BASE_API}/api/files/upload`;

const activeTab = ref('base');
const baseFormRef = ref(null);
const passwordFormRef = ref(null);

const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  new2Password: ''
});

const data = reactive({
  userForm: {}
});

onMounted(() => {
  initBaseForm()
})

const handleSuccess = (url) => {
  if (data.userForm) {
    data.userForm.avatar = url;
  }
}

const initBaseForm = () => {
  if (userStore.userInfo) {
    data.userForm.id = userStore.userInfo.id
    data.userForm.nickname = userStore.userInfo.nickname
    data.userForm.avatar = userStore.userInfo.avatar
  }
}

// 确认密码验证 (修复了变量引用错误)
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请再次确认新密码"));
  } else if (value !== passwordData.newPassword) {
    callback(new Error("两次输入的密码不匹配"));
  } else {
    callback();
  }
};

const baseRules = {
  nickname: [
    { required: true, validator: validateNickname, trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePasswordComplexity, trigger: 'blur' }
  ],
  new2Password: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 修改基本信息
const submitBaseForm = () => {
  updateProfile(data.userForm).then(async res => {
    if (res.code === 200) {
      ElMessage.success('保存成功');
      await userStore.fetchUserInfo();
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(error => {
    ElMessage.error('请求失败: ' + error.message);
  });
};

// 修改密码
const submitPasswordForm = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      const requestData = {
        id: data.userForm.id,
        oldPassword: passwordData.oldPassword,
        newPassword: passwordData.newPassword
      };

      changePassword(requestData).then(res => {
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          setTimeout(() => {
            userStore.logout();
          }, 1000);
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(error => {
        ElMessage.error('请求失败: ' + error.message);
      });
    }
  });
};
</script>

<style scoped>
/* ==========================================
 * 全局卡片样式
 * ========================================== */
.box-card {
  border-radius: 8px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  margin-bottom: 20px;
}

.card-header-text {
  font-weight: bold;
  color: var(--el-text-color-primary);
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

/* ==========================================
 * 左侧：个人信息区域
 * ========================================== */
.avatar-container {
  text-align: center;
  padding-top: 10px;
}

.user-info-list {
  list-style: none;
  padding: 0;
  margin: 25px 0 0 0;
}

.list-group-item {
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 15px 0;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-group-item:last-child {
  border-bottom: none;
  padding-bottom: 5px;
}

/* 列表项的标签与图标 */
.item-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

/* 列表项的值 */
.item-value {
  color: var(--el-text-color-regular);
}

/* ==========================================
 * 右侧：设置区域卡片
 * ========================================== */
.settings-tabs {
  width: 100%;
}

.tab-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 25px;
  color: var(--el-text-color-primary);
}

.form-actions {
  text-align: right;
  margin-top: 30px;
}

.submit-btn {
  padding: 18px 35px;
  border-radius: 6px;
}
</style>