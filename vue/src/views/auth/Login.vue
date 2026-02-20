<template>
  <div class="auth-bg">
    <div style="width: 350px; background-color: #fffafa; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); padding: 20px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <div style="margin: 30px; text-align: center;font-weight: bold; font-size: 30px">登 录</div>
        <el-form-item prop="email">
          <el-input size="large" v-model="data.form.email" prefix-icon="User" placeholder="请输入邮箱" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="large" show-password v-model="data.form.password" prefix-icon="Lock" placeholder="请输入密码" autocomplete="off" />
        </el-form-item>
        <div style="margin-top: 20px">
          <el-button style="width: 100%" size="large" :loading="loading" type="primary" @click="handleLogin">登 录</el-button>
        </div>
        <div style="text-align: right; margin-top: 20px; margin-bottom: 15px">
          还没有账号？
          <span
            style="color: #1890ff; cursor: pointer"
            @click="$router.push('/register')"
          >
            注册
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, reactive, ref} from "vue";
import {ElMessage, ElNotification} from "element-plus";
import {useUserStore} from "@/store/user.js";
import { validateEmail, validatePasswordComplexity } from "@/utils/validate.js";
import {useRouter} from "vue-router";

const router = useRouter();
const userStore = useUserStore()

// 创建表单引用
const formRef = ref()

// 定义 loading 变量
const loading = ref(false)

const data = reactive({
  form: {},
  rules: {
    email: [
      { required: true, validator: validateEmail, trigger: 'blur' }
    ],
    password: [
      { validator: validatePasswordComplexity, trigger: 'blur' }
    ],
  }
})

// 回车事件处理函数
const onKeyUp = (e) => {
  // e.key === 'Enter' 兼容性最好
  if (e.key === 'Enter') {
    handleLogin();
  }
};

// 挂载时绑定监听
onMounted(() => {
  document.addEventListener('keyup', onKeyUp);
});

// 卸载时移除监听
onUnmounted(() => {
  document.removeEventListener('keyup', onKeyUp);
});

const handleLogin = () => {
  if (loading.value) return;

  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;

      try {
        const result = await userStore.login(data.form);

        if (result.success) {
          const isRestored = result.data.isRestored;

          if (isRestored) {
            // 撤销注销：使用 Notification 通知，更显眼，停留时间更长
            ElNotification({
              title: '欢迎回来',
              message: '您的账号注销申请已撤销，账号已恢复正常使用。',
              type: 'success',
              duration: 5000 // 5秒后自动关闭
            });
          } else {
            // 普通登录提示
            ElMessage.success('登录成功');
          }
          await router.push('/')
        } else {
          ElMessage.error(result.msg || '登录失败，请重试');
        }
      } catch (error) {
        ElMessage.error(error.msg || error.message || '系统异常，请联系管理员');
      } finally {
        // 无论成功还是失败，最后都关闭 loading
        loading.value = false;
      }
    }
  })
}
</script>

<style>

</style>