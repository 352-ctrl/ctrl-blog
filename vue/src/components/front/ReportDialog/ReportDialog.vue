<template>
  <el-dialog
      v-model="visible"
      title="内容举报"
      width="450px"
      append-to-body
      destroy-on-close
  >
    <el-alert
        title="恶意举报将导致账号被封禁，请如实填写。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
    />

    <el-form ref="formRef" :model="form" :rules="rules">
      <el-form-item label="举报原因" prop="reason">
        <el-select v-model="form.reason" placeholder="请选择举报原因">
          <el-option
              v-for="item in currentReasonOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="详细说明" prop="content">
        <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请详细说明违规情况，以便管理员核实 (必填)"
            maxlength="500"
            show-word-limit
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
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="handlePreSubmit">
        提 交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { addReport } from '@/api/front/report.js'
import Verify from "@/components/verifition/Verify.vue";

const visible = ref(false)
const loading = ref(false)
const formRef = ref(null)
const verifyRef = ref(null)

const form = reactive({
  targetType: '',
  targetId: null,
  reason: '',
  content: ''
})

const rules = reactive({
  reason: [{ required: true, message: '请选择举报类型', trigger: 'change' }],
  content: [{ required: true, message: '详细说明不能为空', trigger: 'blur' }]
})

// ==========================================
// 业务枚举字典 (Value 必须与后端 ReportReason 的 Code 一致)
// ==========================================
const REASON_DICT = {
  SPAM: { label: '垃圾广告', value: 'SPAM' },
  PORN: { label: '色情低俗', value: 'PORN' },
  ILLEGAL: { label: '违法违规', value: 'ILLEGAL' },
  ABUSE: { label: '人身攻击/引战谩骂', value: 'ABUSE' },
  COPYRIGHT: { label: '抄袭/洗稿/侵权', value: 'COPYRIGHT' },
  IMPERSONATION: { label: '冒充他人/身份造假', value: 'IMPERSONATION' },
  PROFILE_VIOLATION: { label: '头像/昵称违规', value: 'PROFILE_VIOLATION' },
  OTHER: { label: '其他原因', value: 'OTHER' }
}

// ==========================================
// 根据目标类型动态计算可选的举报原因
// ==========================================
const currentReasonOptions = computed(() => {
  const baseOptions = [REASON_DICT.SPAM, REASON_DICT.PORN, REASON_DICT.ILLEGAL]

  if (form.targetType === 'ARTICLE') {
    return [...baseOptions, REASON_DICT.COPYRIGHT, REASON_DICT.OTHER]
  } else if (form.targetType === 'COMMENT') {
    return [...baseOptions, REASON_DICT.ABUSE, REASON_DICT.OTHER]
  } else if (form.targetType === 'USER') {
    return [...baseOptions, REASON_DICT.IMPERSONATION, REASON_DICT.PROFILE_VIOLATION, REASON_DICT.OTHER]
  }

  return [...baseOptions, REASON_DICT.OTHER] // 兜底
})

// ==========================================
// 暴露给外部调用的方法
// ==========================================
const open = (targetType, targetId) => {
  form.targetType = targetType
  form.targetId = targetId
  form.reason = ''
  form.content = ''
  visible.value = true

  // 清除上一次打开可能遗留的校验报错信息
  nextTick(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  })
}

// ==========================================
// 提交与滑块验证逻辑
// ==========================================

// 1. 点击提交按钮，先验证表单，通过后弹出滑块
const handlePreSubmit = () => {
  if (loading.value) return

  formRef.value.validate((valid) => {
    if (valid) {
      // 表单验证通过，显示滑块
      verifyRef.value.show()
    }
  })
}

// 2. 滑块验证成功后的回调，真正发送请求
const onVerifySuccess = async (params) => {
  loading.value = true

  const requestData = { ...form }

  try {
    const res = await addReport(requestData, params.captchaVerification)

    if (res.code === 200) {
      ElMessage.success('举报提交成功，管理员将尽快处理！')
      visible.value = false
    } else {
      ElMessage.error(res.msg || '举报失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('系统异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>