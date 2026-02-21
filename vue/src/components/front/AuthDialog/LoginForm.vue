<template>
  <div class="form-wrapper">
    <el-form class="custom-form" ref="formRef" :model="data.form" :rules="data.rules" @keyup.enter="handleLogin">

      <el-form-item prop="email">
        <el-input size="large" v-model="data.form.email" prefix-icon="User" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item prop="password" style="margin-bottom: 10px;">
        <el-input size="large" show-password v-model="data.form.password" prefix-icon="Lock" placeholder="请输入密码" />
      </el-form-item>

      <div class="forgot-pwd-row">
        <span class="forgot-pwd-link" @click="handleForgotPwd">忘记密码？</span>
      </div>

      <Verify
          ref="verifyRef"
          mode="pop"
          captchaType="blockPuzzle"
          @success="onVerifySuccess"
      ></Verify>

      <div class="bottom-action-area">
        <el-button class="submit-btn" :loading="loading" type="primary" @click="handleLogin">
          登 录
        </el-button>
      </div>

    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from "vue";
import { ElMessage, ElNotification } from "element-plus";
import { useUserStore } from "@/store/user.js";
import { validateEmail, validatePasswordComplexity } from "@/utils/validate.js";
import Verify from "@/components/verifition/Verify.vue";

const emit = defineEmits(['success']);
const userStore = useUserStore();
const formRef = ref();
const verifyRef = ref(null);
const loading = ref(false);

const data = reactive({
  form: { email: '', password: '' },
  rules: {
    email: [{ required: true, validator: validateEmail, trigger: 'blur' }],
    password: [{ validator: validatePasswordComplexity, trigger: 'blur' }],
  }
});

const onKeyUp = (e) => { if (e.key === 'Enter') handleLogin(); };
onMounted(() => document.addEventListener('keyup', onKeyUp));
onUnmounted(() => document.removeEventListener('keyup', onKeyUp));

const handleLogin = () => {
  if (loading.value) return;
  formRef.value.validate((valid) => {
    if (valid) {
      verifyRef.value.show();
    }
  });
};

const onVerifySuccess = async (params) => {
  loading.value = true;
  try {
    const loginPayload = { ...data.form, captchaVerification: params.captchaVerification };
    const result = await userStore.login(loginPayload);
    if (result.success) {
      if (result.data.isRestored) {
        ElNotification({
          title: '欢迎回来',
          message: '您的账号注销申请已撤销，账号已恢复正常使用。',
          type: 'success', duration: 5000
        });
      } else {
        ElMessage.success('登录成功');
      }
      emit('success');
    } else {
      ElMessage.error(result.msg || '登录失败，请重试');
    }
  } catch (error) {
    ElMessage.error(error.msg || error.message || '系统异常，请联系管理员');
  } finally {
    loading.value = false;
  }
};

const handleForgotPwd = () => {
  ElMessage.info("找回密码功能开发中...");
};
</script>

<style scoped>
.form-wrapper {
  width: 100%;
}

.forgot-pwd-row {
  display: flex;
  justify-content: flex-end; /* 靠右对齐 */
  margin-bottom: 20px; /* 控制与登录按钮的间距 */
}

.forgot-pwd-link {
  font-size: 13px;
  color: #8a919f;
  cursor: pointer;
  transition: color 0.3s;
  user-select: none;
}

.forgot-pwd-link:hover {
  color: var(--el-color-primary);
}

.bottom-action-area {
  width: 100%;
}
</style>