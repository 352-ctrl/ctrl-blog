<template>
  <div class="admin-profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8" :lg="7">
        <el-card class="profile-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon color-primary icon-swing"><Postcard /></el-icon>
              <span class="header-title">个人信息</span>
            </div>
          </template>

          <div class="avatar-wrapper">
            <AvatarUpload
                v-model="profileData.avatar"
                :size="120"
                @upload-success="handleAvatarSuccess"
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

        <el-card class="settings-card mb-20" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon color-success icon-swing"><User /></el-icon>
              <span class="header-title">基本资料</span>
            </div>
          </template>

          <div class="tab-content">
            <el-form ref="profileFormRef" :rules="profileRules" :model="profileData" label-width="80px" label-position="right">
              <el-form-item label="用户昵称" prop="nickname">
                <el-input v-model="profileData.nickname" placeholder="请输入您的昵称" maxlength="20" show-word-limit class="max-w-400" />
              </el-form-item>
              <el-form-item label="个人简介" prop="bio">
                <el-input v-model="profileData.bio" type="textarea" :rows="4" placeholder="记录一下你的个性签名吧" maxlength="200" show-word-limit class="max-w-400" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileLoading" @click="submitProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <el-card class="settings-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon color-warning icon-swing"><Setting /></el-icon>
              <span class="header-title">安全设置</span>
            </div>
          </template>

          <div class="setting-list">
            <div class="setting-item">
              <div class="item-left">
                <el-icon class="item-icon"><Lock /></el-icon>
                <div class="item-info">
                  <div class="title">登录密码</div>
                  <div class="desc">建议定期更换密码，以保障账号安全</div>
                </div>
              </div>
              <div class="item-right">
                <el-button round @click="openPwdDialog">修改密码</el-button>
              </div>
            </div>

            <div class="setting-item">
              <div class="item-left">
                <el-icon class="item-icon"><Message /></el-icon>
                <div class="item-info">
                  <div class="title">绑定邮箱</div>
                  <div class="desc">
                    已绑定：<span class="highlight-text">{{ userStore.email || '未绑定' }}</span>
                  </div>
                </div>
              </div>
              <div class="item-right">
                <el-button round @click="openEmailDialog">换绑邮箱</el-button>
              </div>
            </div>
          </div>
        </el-card>

      </el-col>
    </el-row>

    <el-dialog
        v-model="pwdDialogVisible"
        title="修改登录密码"
        width="450px"
        :close-on-click-modal="false"
        @closed="resetPasswordForm"
        append-to-body
    >
      <el-alert title="修改成功后，当前账号会被强制下线，需重新登录。" type="warning" show-icon :closable="false" class="dialog-alert" />
      <el-form ref="passwordFormRef" :rules="passwordRules" :model="passwordData" label-width="85px" label-position="right">
        <el-form-item label="当前密码" prop="oldPassword"><el-input v-model="passwordData.oldPassword" type="password" show-password placeholder="请输入当前使用的密码" /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="passwordData.newPassword" type="password" show-password placeholder="请输入新密码" /></el-form-item>
        <el-form-item label="确认密码" prop="new2Password"><el-input v-model="passwordData.new2Password" type="password" show-password placeholder="请再次输入新密码" /></el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="pwdLoading" @click="submitPasswordForm">确认修改</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
        v-model="emailDialogVisible"
        title="换绑邮箱"
        width="450px"
        :close-on-click-modal="false"
        @closed="resetEmailForm"
        append-to-body
    >
      <el-alert title="换绑邮箱后，下次请使用新邮箱登录当前账号。" type="info" show-icon :closable="false" class="dialog-alert" />
      <el-form ref="emailFormRef" :rules="emailRules" :model="emailData" label-width="85px" label-position="right">
        <el-form-item label="当前邮箱">
          <span class="current-email-text">{{ userStore.email }}</span>
        </el-form-item>
        <el-form-item label="新邮箱" prop="newEmail"><el-input v-model="emailData.newEmail" placeholder="请输入新邮箱地址" /></el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-group">
            <el-input v-model="emailData.code" placeholder="请输入验证码" />
            <el-button type="primary" :disabled="isSendingCode" @click="sendEmailCode" class="send-code-btn">{{ codeBtnText }}</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="emailDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="emailLoading" @click="submitEmailForm">确认换绑</el-button>
        </span>
      </template>
    </el-dialog>

    <Verify ref="verifyRef" mode="pop" captchaType="blockPuzzle" :imgSize="{ width: '310px', height: '155px' }" @success="handleCaptchaSuccess" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useUserStore } from '@/store/user.js';
import { validateNickname, validatePasswordComplexity, validateEmail, validateVerifyCode } from "@/utils/validate.js";
import { updateProfile, changePassword, sendBindEmailCode, changeEmail } from "@/api/admin/userInfo.js";
import { useRequest } from '@/composables/useRequest.js';

const userStore = useUserStore();

// ================== 基本资料逻辑 ==================
const profileFormRef = ref(null);
const profileData = reactive({
  id: '',
  nickname: '',
  avatar: '',
  bio: ''
});

const profileRules = {
  nickname: [{ required: true, validator: validateNickname, trigger: 'blur' }]
};

onMounted(() => {
  if (userStore.userInfo) {
    profileData.id = userStore.userInfo.id;
    profileData.nickname = userStore.userInfo.nickname;
    profileData.avatar = userStore.userInfo.avatar;
    profileData.bio = userStore.userInfo.bio;
  }
});

const { loading: profileLoading, execute: execUpdateProfile } = useRequest(updateProfile, { successMsg: '个人资料保存成功' });

const submitProfile = () => {
  profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await execUpdateProfile(profileData);
        await userStore.fetchUserInfo(); // 同步刷新左侧卡片信息
      } catch (error) {}
    }
  });
};

const handleAvatarSuccess = async (url) => {
  profileData.avatar = url;
  // 头像上传后自动提交保存
  await submitProfile();
};

// ================== 安全设置逻辑 ==================
const pwdDialogVisible = ref(false);
const emailDialogVisible = ref(false);
const passwordFormRef = ref(null);
const emailFormRef = ref(null);
const verifyRef = ref(null);

const passwordData = reactive({ oldPassword: '', newPassword: '', new2Password: '' });
const emailData = reactive({ newEmail: '', code: '' });

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) callback(new Error("请再次确认新密码"));
  else if (value !== passwordData.newPassword) callback(new Error("两次输入的密码不一致"));
  else callback();
};

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validatePasswordComplexity, trigger: 'blur' }],
  new2Password: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
};
const emailRules = {
  newEmail: [{ required: true, validator: validateEmail, trigger: 'blur' }],
  code: [{ required: true, validator: validateVerifyCode, trigger: 'blur' }]
};

const { loading: pwdLoading, execute: execChangePwd } = useRequest(changePassword, { successMsg: '密码修改成功，安全起见请重新登录' });
const { loading: emailLoading, execute: execChangeEmail } = useRequest(changeEmail, { successMsg: '换绑成功，安全起见请重新登录' });
const { execute: execSendCode } = useRequest(sendBindEmailCode, { successMsg: '验证码发送成功，请前往新邮箱查收' });

const openPwdDialog = () => pwdDialogVisible.value = true;
const resetPasswordForm = () => passwordFormRef.value?.resetFields();

const submitPasswordForm = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await execChangePwd({
          oldPassword: passwordData.oldPassword,
          newPassword: passwordData.newPassword
        });
        pwdDialogVisible.value = false;
        setTimeout(() => userStore.logout(), 1500);
      } catch (error) {}
    }
  });
};

const isSendingCode = ref(false);
const codeBtnText = ref('获取验证码');
let codeTimer = null;

const openEmailDialog = () => emailDialogVisible.value = true;
const resetEmailForm = () => {
  emailFormRef.value?.resetFields();
  clearInterval(codeTimer);
  isSendingCode.value = false;
  codeBtnText.value = '获取验证码';
};

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
        emailDialogVisible.value = false;
        setTimeout(() => userStore.logout(), 1500);
      } catch (error) {}
    }
  });
};
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.max-w-400 { max-width: 400px; }

/* 弹窗样式 */
.dialog-alert { margin-bottom: 20px; }
.current-email-text { color: var(--el-text-color-primary); font-weight: 500; }
.code-input-group { display: flex; gap: 10px; width: 100%; }
.send-code-btn { width: 130px; }

/* ====================================
   基础布局与卡片样式
   ==================================== */
.admin-profile-container { padding: 0; }

.profile-card, .settings-card {
  border-radius: 4px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon { font-size: 18px; }
.color-primary { color: var(--el-color-primary); }
.color-success { color: var(--el-color-success); }
.color-warning { color: var(--el-color-warning); }

.header-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
}

:deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-card__body) { padding: 20px; }

/* ====================================
   左侧：个人信息预览样式
   ==================================== */
.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 0 20px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.user-role-badge { margin-top: 15px; }

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
.info-item:last-child { border-bottom: none; }

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

/* ====================================
   右侧：列表与表单样式
   ==================================== */
.tab-content {
  padding: 10px 0;
}

.setting-list { display: flex; flex-direction: column; }
.setting-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 10px; border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background-color 0.3s ease;
}
.setting-item:last-child { border-bottom: none; }
.setting-item:hover { background-color: var(--el-fill-color-light); border-radius: 8px; }

.item-left { display: flex; align-items: center; gap: 12px; }
.item-icon { font-size: 24px; color: var(--el-text-color-regular); }
.item-info { display: flex; flex-direction: column; gap: 6px; }
.item-info .title { font-size: 15px; font-weight: 500; color: var(--el-text-color-primary); }
.item-info .desc { font-size: 13px; color: var(--el-text-color-secondary); }
.highlight-text {
  color: var(--el-text-color-regular);
  background-color: var(--el-fill-color-light);
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

@media screen and (max-width: 768px) {
  .setting-item { flex-direction: column; align-items: flex-start; gap: 15px; padding: 15px 5px; }
  .item-right { align-self: flex-end; }
  .max-w-400 { max-width: 100%; }
}

:deep(.mask) { z-index: 9998 !important; }
:deep(.verifybox) { z-index: 9999 !important; }
</style>