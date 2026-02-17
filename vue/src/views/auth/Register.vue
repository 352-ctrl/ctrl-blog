<template>
  <div class="auth-bg">
    <div style="width: 350px; background-color: #fffafa; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); padding: 20px">
      <el-form status-icon ref="formRef" :model="data.form" :rules="data.rules">
        <div style="margin: 30px; text-align: center;font-weight: bold; font-size: 30px">注    册</div>
        <el-form-item prop="nickname">
          <el-input size="large" v-model="data.form.nickname" prefix-icon="User" placeholder="请输入昵称" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input size="large" v-model="data.form.email" prefix-icon="Message" placeholder="请输入邮箱" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="code">
          <div style="display: flex; width: 100%; gap: 10px;">
            <el-input size="large" v-model="data.form.code" prefix-icon="CircleCheck" placeholder="请输入验证码" style="flex: 1" />
            <el-button size="large" type="primary" :disabled="data.sendBtnDisabled" @click="sendCode" style="width: 80px">
              {{ data.sendBtnText }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="large" show-password v-model="data.form.password" prefix-icon="Lock" placeholder="请输入密码" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input size="large" show-password v-model="data.form.confirmPassword" prefix-icon="Lock" placeholder="请再次确认密码" autocomplete="off" />
        </el-form-item>
        <div style="margin-top: 20px">
          <el-button style="width: 100%" size="large" type="primary" @click="handleRegister">注 册</el-button>
        </div>
        <div style="text-align: right; margin-top: 20px; margin-bottom: 15px">
          已有账号？
          <span
              style="color: #1890ff; cursor: pointer"
              @click="$router.push('/login')"
          >
            登录
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, reactive, ref} from "vue";
import {register, sendEmailCode} from "@/api/auth.js";
import {ElMessage} from "element-plus";
import { validateNickname, validateEmail, validatePasswordComplexity } from "@/utils/validate.js";
import {useRouter} from "vue-router";

const router = useRouter();

// 创建表单引用
const formRef = ref();

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请再次确认密码"));
  } else if (value !== data.form.password) {
    callback(new Error("两次输入的密码不匹配"));
  } else {
    callback();
  }
};

const data = reactive({
  form: {},
  sendBtnText: '获取验证码',
  sendBtnDisabled: false,
  rules: {
    nickname: [
      { required: true, validator: validateNickname, trigger: 'blur' }
    ],
    email: [
      { required: true, validator: validateEmail, trigger: 'blur' }
    ],
    code: [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { min: 6, max: 6, message: '验证码为6位数字', trigger: 'blur' }
    ],
    password: [
      { validator: validatePasswordComplexity, trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, validator: validateConfirmPassword, trigger: 'blur' }
    ]
  }
})

// 回车事件处理函数
const onKeyUp = (e) => {
  // e.key === 'Enter' 兼容性最好
  if (e.key === 'Enter') {
    handleRegister();
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

// 发送验证码逻辑
const sendCode = () => {
  if (!formRef.value) return;
  formRef.value.validateField('email', (valid) => {
    if (valid) {
      sendEmailCode(data.form.email).then(res => {
        if (res.code === 200) {
          ElMessage.success("验证码已发送，请查收邮件");
          // 开始倒计时
          data.sendBtnDisabled = true;
          let count = 60;
          data.sendBtnText = `${count}s后重发`; // 立即更新文本，提升交互感

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
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(() => {
        data.sendBtnDisabled = false;
        data.sendBtnText = '获取验证码';
      });
    }
  })
}

// 注册
const handleRegister = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      register(data.form).then(res => {
        if (res.code === 200) {
          ElMessage.success('注册成功');
          router.push('/login')
        } else {
          ElMessage.error(res.msg);
        }
      })
    }
  })
}
</script>

<style>

</style>