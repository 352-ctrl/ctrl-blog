<template>
  <el-button circle @click="openDialog" icon="ChatDotRound" title="意见反馈" class="feedback-btn" />

  <el-dialog v-model="dialogVisible" title="意见反馈" width="500px" class="dialog-md-down" destroy-on-close append-to-body>
    <el-alert
        title="欢迎提出宝贵的意见或BUG，如果您留下了有效的邮箱，我们会将处理结果发送给您。"
        type="info"
        show-icon
        :closable="false"
        class="feedback-alert"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="反馈类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择反馈类型" class="full-width-select">
          <el-option label="意见建议" :value="0" />
          <el-option label="BUG反馈" :value="1" />
          <el-option label="封禁申诉" :value="2" />
          <el-option label="其他" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="反馈内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请详细描述您遇到的问题或建议 (不超过1000字)" maxlength="1000" show-word-limit />
      </el-form-item>

      <el-form-item label="联系邮箱" prop="contactEmail">
        <el-input v-model="form.contactEmail" placeholder="请输入您的邮箱地址，方便我们联系您" maxlength="100" />
      </el-form-item>
    </el-form>

    <Verify ref="verifyRef" mode="pop" captchaType="blockPuzzle" @success="onVerifySuccess"></Verify>

    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm" :loading="loading">提 交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addFeedback } from '@/api/front/interaction/feedback.js'
import { validateEmail } from '@/utils/validate.js'
import Verify from "@/components/common/Verify/Verify.vue";
import { useRequest } from "@/composables/useRequest.js";

const route = useRoute()
const router = useRouter()

const dialogVisible = ref(false)
const formRef = ref(null)
const verifyRef = ref(null)

const form = reactive({ type: null, content: '', contactEmail: '' })

// ======= Hooks 接入 =======
const { loading, execute: execFeedback } = useRequest(addFeedback, { successMsg: '反馈提交成功，感谢您的支持！' });

const validateOptionalEmail = (rule, value, callback) => {
  const isAppeal = form.type === 2
  if (!value || value.trim() === '') {
    if (isAppeal) callback(new Error('申诉结果将通过邮箱通知，请务必填写联系邮箱'))
    else callback()
  } else {
    validateEmail(rule, value, callback)
  }
}

const rules = reactive({
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  content: [{ required: true, message: '反馈内容不能为空', trigger: 'blur' }, { max: 1000, message: '内容长度不能超过1000个字符', trigger: 'blur' }],
  contactEmail: [{ validator: validateOptionalEmail, trigger: ['blur', 'change'] }]
})

const checkAppealParams = () => {
  const queryType = route.query.type
  const queryEmail = route.query.email
  if (queryType !== undefined && queryType !== null) {
    form.type = Number(queryType)
    if (queryEmail) form.contactEmail = queryEmail
    dialogVisible.value = true
    router.replace({ path: route.path })
  }
}

onMounted(() => checkAppealParams())
watch(() => route.query, () => checkAppealParams())

const openDialog = () => {
  if (formRef.value) formRef.value.resetFields()
  else { form.type = null; form.content = ''; form.contactEmail = '' }
  dialogVisible.value = true
}

const submitForm = () => {
  if (loading.value) return;
  formRef.value.validate((valid) => {
    if (valid) verifyRef.value.show()
  })
}

const onVerifySuccess = async (params) => {
  try {
    const submitData = { ...form }
    await execFeedback(submitData, params.captchaVerification)
    dialogVisible.value = false
  } catch (error) {}
}
</script>

<style scoped>
.feedback-alert {
  margin-bottom: 20px;
}

.full-width-select {
  width: 100%;
}

.feedback-btn {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
  color: inherit;
  font-size: inherit;
}
</style>