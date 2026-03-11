<template>
  <div class="form-wrapper">
    <div class="form-title">找回密码</div>

    <el-form class="custom-form" status-icon ref="formRef" :model="data.form" :rules="data.rules" @keyup.enter="handleReset">

      <el-form-item prop="email">
        <el-input size="large" v-model="data.form.email" prefix-icon="Message" placeholder="请输入注册邮箱" />
      </el-form-item>

      <el-form-item prop="code">
        <div class="code-input-row">
          <el-input size="large" v-model="data.form.code" prefix-icon="CircleCheck" placeholder="请输入验证码" class="code-input-field" />
          <el-button class="send-code-btn" type="primary" plain :disabled="data.sendBtnDisabled" @click="sendCode">
            {{ data.sendBtnText }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item prop="newPassword">
        <el-input size="large" show-password v-model="data.form.newPassword" prefix-icon="Lock" placeholder="请输入新密码" />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input size="large" show-password v-model="data.form.confirmPassword" prefix-icon="Lock" placeholder="请再次确认新密码" />
      </el-form-item>

      <Verify
          ref="verifyRef"
          mode="pop"
          captchaType="blockPuzzle"
          @success="onVerifySuccess"
      ></Verify>

      <div class="bottom-action-area">
        <el-button class="submit-btn" :loading="loading" type="primary" @click="handleReset">
          重 置 密 码
        </el-button>
      </div>

      <div class="bottom-links">
        <span class="back-login-link" @click="$emit('switch-mode', 'login')">想起密码了？去登录</span>
      </div>

    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from "vue";
import { sendForgotPwdEmailCode, resetPasswordByEmail } from "@/api/common/auth.js";
import { ElMessage } from "element-plus";
import { validateEmail, validatePasswordComplexity, validateVerifyCode } from "@/utils/validate.js";
import Verify from "@/components/common/Verify/Verify.vue";
// 引入 Hook
import { useRequest } from "@/composables/useRequest.js";

const emit = defineEmits(['success', 'switch-mode']);
const formRef = ref();
const verifyRef = ref(null);

// ======= Hooks 接入 =======
// 1. 重置密码请求
const { loading, execute: execReset } = useRequest(resetPasswordByEmail, { successMsg: '密码重置成功，请使用新密码登录' });
// 2. 发送验证码请求
const { execute: execSendCode } = useRequest(sendForgotPwdEmailCode);

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) callback(new Error("请再次确认新密码"));
  else if (value !== data.form.newPassword) callback(new Error("两次输入的密码不匹配"));
  else callback();
};

const data = reactive({
  form: { email: '', code: '', newPassword: '', confirmPassword: '' },
  sendBtnText: '获取验证码',
  sendBtnDisabled: false,
  rules: {
    email: [{ required: true, validator: validateEmail, trigger: 'blur' }],
    code: [{ required: true, validator: validateVerifyCode, trigger: 'blur' }],
    newPassword: [{ validator: validatePasswordComplexity, trigger: 'blur' }],
    confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
  }
});

const onKeyUp = (e) => { if (e.key === 'Enter') handleReset(); };
onMounted(() => document.addEventListener('keyup', onKeyUp));
onUnmounted(() => document.removeEventListener('keyup', onKeyUp));

const sendCode = () => {
  if (!formRef.value) return;
  if (!data.form.email) {
    ElMessage.warning('请先输入邮箱地址');
    return;
  }
  formRef.value.validateField('email', (valid) => {
    if (valid) verifyRef.value.show();
  });
}

const onVerifySuccess = async (params) => {
  data.sendBtnDisabled = true;
  data.sendBtnText = '发送中...';

  try {
    await execSendCode({
      email: data.form.email,
      captchaToken: params.captchaVerification
    });

    ElMessage.success("验证码已发送，请查收邮件");
    let count = 60;
    data.sendBtnText = `${count}s后重发`;
    const timer = setInterval(() => {
      count--;
      if (count > 0) {
        data.sendBtnText = `${count}s后重发`;
      } else {
        clearInterval(timer);
        data.sendBtnText = '获取验证码';
        data.sendBtnDisabled = false;
      }
    }, 1000);
  } catch (error) {
    // 请求失败时恢复按钮状态 (错误提示 Hook 已代劳)
    data.sendBtnDisabled = false;
    data.sendBtnText = '获取验证码';
  }
};

const handleReset = () => {
  if (loading.value) return;
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await execReset({
          email: data.form.email,
          code: data.form.code,
          newPassword: data.form.newPassword
        });
        emit('success'); // 通知父组件重置成功，切换回登录页
      } catch (error) {
        // 异常已被 Hook 捕获拦截
      }
    }
  });
};
</script>

<style scoped>
.form-wrapper {
  width: 100%;
}

/* 标题居中样式 */
.form-title {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-top: -10px; /* 稍微向上提一点 */
  margin-bottom: 25px;
  letter-spacing: 2px; /* 字间距让标题更大气 */
}

.code-input-row {
  display: flex;
  width: 100%;
  gap: 12px;
}

.code-input-field {
  flex: 1;
}

.bottom-action-area {
  margin-top: 25px;
}

.send-code-btn {
  width: 110px;
  height: 40px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.bottom-links {
  display: flex;
  justify-content: center;
  margin-top: 15px;
}

.back-login-link {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  transition: color 0.3s;
  user-select: none;
}

.back-login-link:hover {
  color: var(--el-color-primary);
}
</style>