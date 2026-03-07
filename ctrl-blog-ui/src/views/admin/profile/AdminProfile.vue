<template>
  <div class="admin-profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8" :lg="7">
        <el-card class="profile-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="header-title">个人信息</span>
            </div>
          </template>

          <div class="avatar-wrapper">
            <AvatarUpload
                v-model="data.userForm.avatar"
                :size="120"
                @upload-success="handleSuccess"
            />
            <div class="user-role-badge">
              <el-tag :type="userStore.role === 'SUPER_ADMIN' ? 'danger' : 'success'" effect="light" round>
                {{ userStore.role === 'SUPER_ADMIN' ? '超级管理员' : (userStore.role === 'ADMIN' ? '管理员' : userStore.role) }}
              </el-tag>
            </div>
          </div>

          <ul class="user-info-list">
            <li class="info-item">
              <span class="item-label"><el-icon><User /></el-icon> 账户昵称</span>
              <span class="item-value">{{ userStore.nickname || '未设置' }}</span>
            </li>
            <li class="info-item">
              <span class="item-label"><el-icon><Message /></el-icon> 绑定邮箱</span>
              <span class="item-value">{{ userStore.email }}</span>
            </li>
            <li class="info-item">
              <span class="item-label"><el-icon><Calendar /></el-icon> 注册时间</span>
              <span class="item-value">{{ userStore.createTime }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16" :lg="17">
        <el-card class="settings-card" shadow="never">
          <el-tabs v-model="activeTab" class="settings-tabs">

            <el-tab-pane label="基本资料" name="base">
              <div class="tab-content">
                <el-form ref="baseFormRef" :rules="baseRules" :model="data.userForm" label-width="80px" label-position="right">
                  <el-form-item label="用户昵称" prop="nickname"><el-input v-model="data.userForm.nickname" placeholder="请输入您的昵称" maxlength="20" show-word-limit /></el-form-item>
                  <el-form-item label="个人简介" prop="bio"><el-input v-model="data.userForm.bio" type="textarea" :rows="4" placeholder="记录一下你的个性签名吧" maxlength="200" show-word-limit /></el-form-item>
                  <el-form-item>
                    <el-button type="primary" :loading="baseLoading" @click="submitBaseForm" icon="Select">保存修改</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <el-tab-pane label="邮箱设置" name="email">
              <div class="tab-content">
                <el-alert title="换绑邮箱后，下次请使用新邮箱登录。" type="info" show-icon :closable="false" class="security-alert" />
                <el-form ref="emailFormRef" :rules="emailRules" :model="emailData" label-width="80px" label-position="right">
                  <el-form-item label="当前邮箱"><span style="color: var(--el-text-color-regular); font-weight: 500;">{{ userStore.email }}</span></el-form-item>
                  <el-form-item label="新邮箱" prop="newEmail"><el-input v-model="emailData.newEmail" placeholder="请输入新邮箱地址" /></el-form-item>
                  <el-form-item label="验证码" prop="code">
                    <div style="display: flex; gap: 10px; width: 100%;">
                      <el-input v-model="emailData.code" placeholder="请输入验证码" />
                      <el-button type="primary" :disabled="isSendingCode" @click="sendEmailCode" style="width: 120px;">{{ codeBtnText }}</el-button>
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" :loading="emailLoading" @click="submitEmailForm" icon="Message">确认换绑</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <div class="tab-content">
                <el-alert title="密码修改成功后，当前账号会被强制下线，需重新登录。" type="warning" show-icon :closable="false" class="security-alert" />
                <el-form ref="passwordFormRef" :rules="passwordRules" :model="passwordData" label-width="80px" label-position="right">
                  <el-form-item label="当前密码" prop="oldPassword"><el-input v-model="passwordData.oldPassword" type="password" show-password placeholder="请输入当前使用的密码" /></el-form-item>
                  <el-form-item label="新密码" prop="newPassword"><el-input v-model="passwordData.newPassword" type="password" show-password placeholder="请输入新密码" /></el-form-item>
                  <el-form-item label="确认密码" prop="new2Password"><el-input v-model="passwordData.new2Password" type="password" show-password placeholder="请再次输入新密码以确认" /></el-form-item>
                  <el-form-item>
                    <el-button type="danger" :loading="pwdLoading" @click="submitPasswordForm" icon="Lock">确认修改密码</el-button>
                    <el-button @click="resetPasswordForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <Verify ref="verifyRef" mode="pop" captchaType="blockPuzzle" :imgSize="{ width: '310px', height: '155px' }" @success="handleCaptchaSuccess" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from '@/store/user.js';
import { validateNickname, validatePasswordComplexity, validateEmail, validateVerifyCode } from "@/utils/validate.js";
import { updateProfile, changePassword, sendBindEmailCode, changeEmail } from "@/api/admin/userInfo.js";
// 引入基础请求 Hook
import { useRequest } from '@/composables/useRequest.js';

const userStore = useUserStore();

const activeTab = ref('base');
const baseFormRef = ref(null);
const passwordFormRef = ref(null);
const emailFormRef = ref(null);
const verifyRef = ref(null);

const passwordData = reactive({ oldPassword: '', newPassword: '', new2Password: '' });
const emailData = reactive({ newEmail: '', code: '' });
const data = reactive({
  userForm: { id: '', nickname: '', avatar: '', bio: '' }
});

// ====== 使用 useRequest 统一管理 API 请求状态 ======
const { loading: baseLoading, execute: execUpdateProfile } = useRequest(updateProfile, { successMsg: '个人资料已更新' });
const { loading: pwdLoading, execute: execChangePwd } = useRequest(changePassword, { successMsg: '密码修改成功，安全起见请重新登录' });
const { loading: emailLoading, execute: execChangeEmail } = useRequest(changeEmail, { successMsg: '换绑成功，安全起见请重新登录' });
const { execute: execSendCode } = useRequest(sendBindEmailCode, { successMsg: '验证码发送成功，请前往新邮箱查收' });


onMounted(() => {
  initBaseForm();
});

const handleSuccess = (url) => {
  if (data.userForm) {
    data.userForm.avatar = url;
    submitBaseForm();
  }
};

const initBaseForm = () => {
  if (userStore.userInfo) {
    data.userForm.id = userStore.userInfo.id;
    data.userForm.nickname = userStore.userInfo.nickname;
    data.userForm.avatar = userStore.userInfo.avatar;
    data.userForm.bio = userStore.userInfo.bio;
  }
};

// ================= 表单验证规则 =================
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) callback(new Error("请再次确认新密码"));
  else if (value !== passwordData.newPassword) callback(new Error("两次输入的密码不一致"));
  else callback();
};
const baseRules = { nickname: [{ required: true, validator: validateNickname, trigger: 'blur' }] };
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validatePasswordComplexity, trigger: 'blur' }],
  new2Password: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
};
const emailRules = {
  newEmail: [{ required: true, validator: validateEmail, trigger: 'blur' }],
  code: [{ required: true, validator: validateVerifyCode, trigger: 'blur' }]
}

// ================= 提交基本信息 =================
const submitBaseForm = () => {
  baseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await execUpdateProfile(data.userForm);
        await userStore.fetchUserInfo(); // 刷新全局状态
      } catch (error) {}
    }
  });
};

// ================= 换绑邮箱逻辑 =================
const isSendingCode = ref(false);
const codeBtnText = ref('获取验证码');
let codeTimer = null;

const sendEmailCode = () => {
  emailFormRef.value.validateField('newEmail', (valid) => {
    if (valid) verifyRef.value.show();
  });
};

const handleCaptchaSuccess = async (params) => {
  isSendingCode.value = true;
  codeBtnText.value = '发送中...';

  try {
    await execSendCode(emailData.newEmail, params.captchaVerification);
    let count = 60;
    codeBtnText.value = `${count}s后重新获取`;
    codeTimer = setInterval(() => {
      count--;
      if (count > 0) {
        codeBtnText.value = `${count}s后重新获取`;
      } else {
        clearInterval(codeTimer);
        isSendingCode.value = false;
        codeBtnText.value = '获取验证码';
      }
    }, 1000);
  } catch (error) {
    isSendingCode.value = false;
    codeBtnText.value = '获取验证码';
  }
};

const submitEmailForm = () => {
  emailFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await execChangeEmail(emailData);
        setTimeout(() => userStore.logout(), 1500);
      } catch (error) {}
    }
  });
}

// ================= 修改密码逻辑 =================
const submitPasswordForm = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await execChangePwd({
          oldPassword: passwordData.oldPassword,
          newPassword: passwordData.newPassword
        });
        setTimeout(() => userStore.logout(), 1500);
      } catch (error) {}
    }
  });
};

const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields();
};
</script>

<style scoped>
.admin-profile-container {
  padding: 0;
}

/* 卡片通用基础样式 */
.profile-card, .settings-card {
  border-radius: 4px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  margin-bottom: 20px;
}

/* 头部样式 */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
}

:deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-card__body) {
  padding: 20px;
}

/* ==========================================
 * 左侧：个人信息区域
 * ========================================== */
.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 0 20px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.user-role-badge {
  margin-top: 15px;
}

.user-info-list {
  list-style: none;
  padding: 0;
  margin: 15px 0 0 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 13px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
}

.info-item:last-child {
  border-bottom: none;
}

.item-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--el-text-color-regular);
}

.item-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

/* ==========================================
 * 右侧：设置面板区域
 * ========================================== */
.settings-tabs {
  width: 100%;
}

.tab-content {
  padding: 20px 10px 10px 10px;
  max-width: 500px; /* 限制表单最大宽度，防止在宽屏下过长 */
}

.security-alert {
  margin-bottom: 25px;
}
</style>