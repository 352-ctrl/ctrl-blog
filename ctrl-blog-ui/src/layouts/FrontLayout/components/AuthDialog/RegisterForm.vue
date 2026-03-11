<template>
  <div class="form-wrapper">
    <el-form class="custom-form" status-icon ref="formRef" :model="data.form" :rules="data.rules" @keyup.enter="handleRegister">

      <el-form-item prop="email">
        <el-input size="large" v-model="data.form.email" prefix-icon="Message" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item prop="code">
        <div class="code-input-row">
          <el-input size="large" v-model="data.form.code" prefix-icon="CircleCheck" placeholder="请输入验证码" class="code-input-field" />
          <el-button class="send-code-btn" type="primary" plain :disabled="data.sendBtnDisabled" @click="sendCode">
            {{ data.sendBtnText }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item prop="password">
        <el-input size="large" show-password v-model="data.form.password" prefix-icon="Lock" placeholder="请输入密码" />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input size="large" show-password v-model="data.form.confirmPassword" prefix-icon="Lock" placeholder="请再次确认密码" />
      </el-form-item>

      <Verify
          ref="verifyRef"
          mode="pop"
          captchaType="blockPuzzle"
          @success="onVerifySuccess"
      ></Verify>

      <div class="bottom-action-area">
        <el-button class="submit-btn" :loading="loading" type="primary" @click="handleRegister">
          注 册
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from "vue";
import { register, sendRegisterEmailCode } from "@/api/common/auth.js";
import { useUserStore } from "@/store/user.js";
import { ElMessage } from "element-plus";
import { validateEmail, validatePasswordComplexity, validateVerifyCode } from "@/utils/validate.js";
import Verify from "@/components/common/Verify/Verify.vue";
// 引入 Hook
import { useRequest } from "@/composables/useRequest.js";

const emit = defineEmits(['success']);
const userStore = useUserStore();
const formRef = ref();
const verifyRef = ref(null);

// ======= Hooks 接入 =======
const { loading, execute: execRegister } = useRequest(register, { successMsg: '注册成功，已为您自动登录' });
const { execute: execSendCode } = useRequest(sendRegisterEmailCode);

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) callback(new Error("请再次确认密码"));
  else if (value !== data.form.password) callback(new Error("两次输入的密码不匹配"));
  else callback();
};

const data = reactive({
  form: { email: '', code: '', password: '', confirmPassword: '' },
  sendBtnText: '获取验证码',
  sendBtnDisabled: false,
  rules: {
    email: [{ required: true, validator: validateEmail, trigger: 'blur' }],
    code: [{ required: true, validator: validateVerifyCode, trigger: 'blur' }],
    password: [{ validator: validatePasswordComplexity, trigger: 'blur' }],
    confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
  }
});

const onKeyUp = (e) => { if (e.key === 'Enter') handleRegister(); };
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
    data.sendBtnDisabled = false;
    data.sendBtnText = '获取验证码';
  }
};

const handleRegister = () => {
  if (loading.value) return;
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 请求成功时直接返回数据主体 (res.data)
        const resData = await execRegister(data.form);
        if (resData) {
          const { token, userInfo } = resData;
          userStore.token = token;
          userStore.userInfo = userInfo;
          localStorage.setItem('token', token);
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          emit('success');
        }
      } catch (error) {
        // 异常已被拦截
      }
    }
  });
};
</script>

<style scoped>
.form-wrapper {
  width: 100%;
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
</style>