<template>
  <div class="form-wrapper">
    <el-form class="custom-form" ref="formRef" :model="data.form" :rules="data.rules" @keyup.enter="handleLogin">

      <el-form-item prop="email">
        <el-input size="large" v-model="data.form.email" prefix-icon="User" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item prop="password" class="pwd-form-item">
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
import { reactive, ref, onMounted, onUnmounted, h } from "vue";
import { ElMessage, ElNotification, ElMessageBox } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store/user.js";
import { validateEmail, validatePasswordComplexity } from "@/utils/validate.js";
import Verify from "@/components/common/Verify/Verify.vue";

const emit = defineEmits(['success']);
const userStore = useUserStore();
const router = useRouter();
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

const handleBanException = (errorMsg) => {
  ElMessageBox.confirm(
      // 使用 h 函数构建层次分明、带有危险警告 UI 的弹窗
      h('div', { style: 'text-align: left;' }, [
        h('p', {
          style: 'margin: 0 0 16px 0; font-size: 15px; color: var(--el-text-color-primary); line-height: 1.6;'
        }, '很抱歉，系统检测到您的账号存在违规行为，目前处于限制登录状态。'),

        // 核心优化：仿邮件模板的左侧红边框引用块，完美适配暗黑模式
        h('div', {
          style: 'padding: 12px 16px; background-color: var(--el-fill-color-light); border: 1px solid var(--el-border-color-lighter); border-left: 4px solid var(--el-color-danger); border-radius: 6px; font-size: 14px; line-height: 1.6; word-break: break-all;'
        }, [
          h('div', {
            style: 'color: var(--el-color-danger); font-weight: 600; margin-bottom: 6px; font-size: 13px;'
          }, '封禁原因：'),
          h('div', {
            style: 'color: var(--el-text-color-primary);'
          }, errorMsg)
        ])
      ]),
      '账号登录受限',
      {
        confirmButtonText: '我要申诉',
        cancelButtonText: '我知道了',
        type: 'error', // ⚠️ 修改点：封禁是严重事件，使用 error 红色交叉图标更符合直觉
        center: true,
        customClass: 'ban-message-box' // 预留 customClass，防止影响全局其他 confirm 弹窗
      }
  ).then(() => {
    // 用户点击了“我要申诉”
    // 1. 关闭登录弹窗
    userStore.showAuthDialog = false;

    // 2. 跳转到意见反馈页，同时携带 type=2 (封禁申诉) 和刚才填写的 email 参数
    router.push({
      path: '/',
      query: {
        type: 2,
        email: data.form.email
      }
    });

  }).catch(() => {
    // 用户点击了“我知道了”或关闭了弹窗，什么都不做
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
      if (result.code === 4031) {
        handleBanException(result.msg);
      }
    }
  } catch (error) {
    const errorMsg = error.msg || error.message || '系统异常，请联系管理员';
    if (error.code === 4031) {
      handleBanException(errorMsg);
    } else {
      ElMessage.error(errorMsg);
    }
  } finally {
    loading.value = false;
  }
};

const handleForgotPwd = () => {
  userStore.authDialogMode = 'forgotPwd';
};
</script>

<style scoped>
.form-wrapper {
  width: 100%;
}

.pwd-form-item {
  margin-bottom: 10px;
}

.forgot-pwd-row {
  display: flex;
  justify-content: flex-end; /* 靠右对齐 */
  margin-bottom: 20px; /* 控制与登录按钮的间距 */
}

.forgot-pwd-link {
  font-size: 13px;
  color: var(--el-text-color-secondary); /* 适配暗黑模式 */
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