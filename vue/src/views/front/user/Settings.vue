<template>
  <el-card class="user-page-card" shadow="never">
    <template #header>
      <div class="page-header">
        <el-icon class="header-icon color-primary"><Setting /></el-icon>
        <span class="header-title">安全设置</span>
      </div>
    </template>

    <div class="page-content">
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

        <div class="setting-item danger-zone">
          <div class="item-left">
            <el-icon class="item-icon color-danger"><Warning /></el-icon>
            <div class="item-info">
              <div class="title color-danger">注销账号</div>
              <div class="desc">注销后您的所有数据将被永久删除，此操作不可逆，请谨慎操作</div>
            </div>
          </div>
          <div class="item-right">
            <el-button type="danger" plain round @click="handleCancelAccount">注销账号</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
        v-model="pwdDialogVisible"
        title="修改登录密码"
        width="450px"
        :close-on-click-modal="false"
        @closed="resetPasswordForm"
        append-to-body
    >
      <el-alert
          title="修改成功后，当前账号会被强制下线，需重新登录。"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
      />
      <el-form
          ref="passwordFormRef"
          :rules="passwordRules"
          :model="passwordData"
          label-width="85px"
          label-position="right"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordData.oldPassword" type="password" show-password placeholder="请输入当前使用的密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordData.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="new2Password">
          <el-input v-model="passwordData.new2Password" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
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
      <el-alert
          title="换绑邮箱后，下次请使用新邮箱登录当前账号。"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
      />
      <el-form
          ref="emailFormRef"
          :rules="emailRules"
          :model="emailData"
          label-width="85px"
          label-position="right"
      >
        <el-form-item label="当前邮箱">
          <span style="color: var(--el-text-color-primary); font-weight: 500;">
            {{ userStore.email }}
          </span>
        </el-form-item>
        <el-form-item label="新邮箱" prop="newEmail">
          <el-input v-model="emailData.newEmail" placeholder="请输入新邮箱地址" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input v-model="emailData.code" placeholder="请输入验证码" />
            <el-button type="primary" :disabled="isSendingCode" @click="sendEmailCode" style="width: 130px;">
              {{ codeBtnText }}
            </el-button>
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

    <Verify
        ref="verifyRef"
        mode="pop"
        captchaType="blockPuzzle"
        :imgSize="{ width: '310px', height: '155px' }"
        @success="handleCaptchaSuccess"
    />
  </el-card>
</template>

<script setup>
import { reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from '@/store/user.js';
// 按需引入 Element Plus 图标
import { Setting, Lock, Message, Warning } from '@element-plus/icons-vue';
// 验证规则与前台 API
import { validatePasswordComplexity, validateEmail, validateVerifyCode } from "@/utils/validate.js";
import { changePassword, sendBindEmailCode, changeEmail } from "@/api/front/userInfo.js";
// 引入验证码组件（请确保路径正确）
import Verify from "@/components/Verifition/Verify.vue";

const userStore = useUserStore();

// ==================== 状态控制 ====================
const pwdDialogVisible = ref(false);
const emailDialogVisible = ref(false);

const pwdLoading = ref(false);
const emailLoading = ref(false);

const passwordFormRef = ref(null);
const emailFormRef = ref(null);
const verifyRef = ref(null);

// ==================== 数据模型 ====================
const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  new2Password: ''
});

const emailData = reactive({
  newEmail: '',
  code: ''
});

// ==================== 表单校验规则 ====================
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请再次确认新密码"));
  } else if (value !== passwordData.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
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

// ==================== 修改密码逻辑 ====================
const openPwdDialog = () => {
  pwdDialogVisible.value = true;
};

const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields();
  }
};

const submitPasswordForm = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      pwdLoading.value = true;
      const requestData = {
        oldPassword: passwordData.oldPassword,
        newPassword: passwordData.newPassword
      };

      changePassword(requestData).then(res => {
        if (res.code === 200) {
          pwdDialogVisible.value = false;
          ElMessage.success('密码修改成功，请重新登录');
          setTimeout(() => {
            userStore.logout();
          }, 1500);
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(error => {
        ElMessage.error('请求失败: ' + error.message);
      }).finally(() => {
        pwdLoading.value = false;
      });
    }
  });
};

// ==================== 换绑邮箱逻辑 ====================
const isSendingCode = ref(false);
const codeBtnText = ref('获取验证码');
let codeTimer = null;

const openEmailDialog = () => {
  emailDialogVisible.value = true;
};

const resetEmailForm = () => {
  if (emailFormRef.value) {
    emailFormRef.value.resetFields();
  }
  // 重置验证码倒计时状态
  clearInterval(codeTimer);
  isSendingCode.value = false;
  codeBtnText.value = '获取验证码';
};

const sendEmailCode = () => {
  emailFormRef.value.validateField('newEmail', (valid) => {
    if (valid) {
      verifyRef.value.show();
    }
  });
};

const handleCaptchaSuccess = (params) => {
  isSendingCode.value = true;
  codeBtnText.value = '发送中...';

  sendBindEmailCode(emailData.newEmail, params.captchaVerification).then(res => {
    if (res.code === 200) {
      ElMessage.success('验证码发送成功，请前往新邮箱查收');
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
    } else {
      isSendingCode.value = false;
      codeBtnText.value = '获取验证码';
      ElMessage.error(res.msg);
    }
  }).catch(error => {
    isSendingCode.value = false;
    codeBtnText.value = '获取验证码';
    ElMessage.error('请求失败: ' + error.message);
  });
};

const submitEmailForm = () => {
  emailFormRef.value.validate((valid) => {
    if (valid) {
      emailLoading.value = true;
      changeEmail(emailData).then(res => {
        if(res.code === 200) {
          emailDialogVisible.value = false;
          ElMessage.success('换绑成功，安全起见请重新登录');
          setTimeout(() => {
            userStore.logout();
          }, 1500);
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(error => {
        ElMessage.error('请求失败: ' + error.message);
      }).finally(() => {
        emailLoading.value = false;
      });
    }
  });
};

// ==================== 注销账号逻辑 ====================
const handleCancelAccount = () => {
  ElMessageBox.confirm(
      '注销账号为不可逆操作，注销后您将无法找回账号数据，是否继续？',
      '危险操作确认',
      {
        confirmButtonText: '确定注销',
        cancelButtonText: '暂不注销',
        type: 'error',
        buttonSize: 'default'
      }
  ).then(() => {
    // 💡 这里暂时放置提示，如果后端有对应的自主注销接口，可以在这里调用
    ElMessage.info('注销功能正在开发中，如有需要请联系管理员。');
  }).catch(() => {});
};
</script>

<style scoped>
/* 继承 UserLayout.vue 的卡片风格 */
.user-page-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background-color: var(--el-bg-color-overlay);
  min-height: 500px;
}

:deep(.user-page-card .el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
}

.header-title {
  font-family: 'SmileySans', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 1px;
}

.page-content {
  padding: 10px;
}

/* ==================== 设置列表样式 ==================== */
.setting-list {
  display: flex;
  flex-direction: column;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background-color 0.3s ease;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item:hover {
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
}

.item-left {
  display: flex;
  align-items: center;
  /* 稍微缩小一点间距，因为没有圆形背景了 */
  gap: 12px;
}

/* ✨ 优化：去除了背景色、内边距和圆角，只保留纯图标 */
.item-icon {
  font-size: 24px;
  color: var(--el-text-color-regular);
  /* 移除了 background-color, padding, border-radius */
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-info .title {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.item-info .desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ✨ 优化：确保只是高亮文字，没有背景色 */
.highlight-text {
  color: var(--el-color-primary);
  font-weight: 500;
  background-color: transparent; /* 明确指定透明背景 */
  padding: 0; /* 确保没有意外的内边距 */
}

/* 危险操作区特殊样式 */
.danger-zone .item-icon {
  color: var(--el-color-danger);
  /* 移除了 background-color */
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding: 15px 5px;
  }
  .item-right {
    align-self: flex-end;
  }
}
</style>