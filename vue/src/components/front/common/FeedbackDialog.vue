<template>
  <el-button
      circle
      @click="openDialog"
      icon="ChatDotRound"
      title="意见反馈"
      class="feedback-btn"
  />

  <el-dialog
      v-model="dialogVisible"
      title="意见反馈"
      width="500px"
      class="dialog-md-down"
      destroy-on-close
      append-to-body
  >
    <el-alert
        title="欢迎提出宝贵的意见或BUG，如果您留下了有效的邮箱，我们会将处理结果发送给您。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
    />

    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
    >
      <el-form-item label="反馈类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择反馈类型" style="width: 100%;">
          <el-option label="意见建议" :value="0" />
          <el-option label="BUG反馈" :value="1" />
          <el-option label="封禁申诉" :value="2" />
          <el-option label="其他" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="反馈内容" prop="content">
        <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题或建议 (不超过1000字)"
            maxlength="1000"
            show-word-limit
        />
      </el-form-item>

      <el-form-item label="联系邮箱" prop="contactEmail">
        <el-input
            v-model="form.contactEmail"
            placeholder="请输入您的邮箱地址，方便我们联系您"
            maxlength="100"
        />
      </el-form-item>

    </el-form>

    <Verify
        ref="verifyRef"
        mode="pop"
        captchaType="blockPuzzle"
        @success="onVerifySuccess"
    ></Verify>

    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading">
        提 交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addFeedback } from '@/api/front/feedback.js'
import { validateEmail } from '@/utils/validate.js'
import Verify from "@/components/verifition/Verify.vue";

const route = useRoute()
const router = useRouter()

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const verifyRef = ref(null)

const form = reactive({
  type: null,
  content: '',
  contactEmail: ''
})

const validateOptionalEmail = (rule, value, callback) => {
  // 1. 获取当前反馈类型：2 代表“封禁申诉”
  const isAppeal = form.type === 2

  // 2. 逻辑判断
  if (!value || value.trim() === '') {
    if (isAppeal) {
      // 如果是申诉且为空，强制报错
      callback(new Error('申诉结果将通过邮箱通知，请务必填写联系邮箱'))
    } else {
      // 其他类型为空，直接放行
      callback()
    }
  } else {
    // 3. 如果有值（无论什么类型），都必须符合邮箱格式
    validateEmail(rule, value, callback)
  }
}

const rules = reactive({
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '反馈内容不能为空', trigger: 'blur' },
    { max: 1000, message: '内容长度不能超过1000个字符', trigger: 'blur' }
  ],
  contactEmail: [
    { validator: validateOptionalEmail, trigger: ['blur', 'change'] }
  ]
})

// 监听 URL 携带的申诉参数，实现自动弹出
const checkAppealParams = () => {
  const queryType = route.query.type
  const queryEmail = route.query.email

  if (queryType !== undefined && queryType !== null) {
    // 自动填充类型
    form.type = Number(queryType)

    // 如果路由中带了邮箱，自动填充到表单中
    if (queryEmail) {
      form.contactEmail = queryEmail
    }

    // 自动打开弹窗
    dialogVisible.value = true

    // 清理地址栏的 query 参数，避免用户刷新页面时再次弹窗
    router.replace({ path: route.path })
  }
}

onMounted(() => {
  checkAppealParams()
})

watch(() => route.query, () => {
  checkAppealParams()
})

const openDialog = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  } else {
    form.type = null
    form.content = ''
    form.contactEmail = ''
  }
  dialogVisible.value = true
}

// 1. 预提交：先校验表单，通过后弹出滑块
const submitForm = () => {
  if (loading.value) return;

  formRef.value.validate((valid) => {
    if (valid) {
      // 表单校验通过，唤起滑块
      verifyRef.value.show()
    }
  })
}

// 2. 真正提交：滑块验证成功后的回调
const onVerifySuccess = (params) => {
  loading.value = true

  const submitData = { ...form }

  addFeedback(submitData, params.captchaVerification).then(res => {
    if (res.code === 200) {
      ElMessage.success('反馈提交成功，感谢您的支持！')
      dialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.feedback-btn {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
  color: inherit;
  font-size: inherit;
}
</style>