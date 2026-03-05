<template>
  <el-dialog
      v-model="dialogStore.reportVisible"
      title="违规举报"
      width="500px"
      @closed="handleClosed"
      :close-on-click-modal="false"
  >
    <div class="report-header">
      <el-alert
          title="感谢您参与社区环境维护。请如实填写举报原因，我们将尽快处理。"
          type="info"
          show-icon
          :closable="false"
          class="info-alert"
      />
    </div>

    <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
        class="report-form"
    >
      <el-form-item label="举报原因" prop="reason">
        <el-select v-model="formData.reason" placeholder="请选择违规类型" style="width: 100%;">
          <el-option label="垃圾广告信息" value="SPAM" />

          <el-option label="涉黄/暴力/低俗内容" value="PORN" />

          <el-option label="恶意攻击/谩骂" value="ABUSE" />

          <el-option label="政治敏感/违法违规" value="ILLEGAL" />

          <el-option label="侵权或抄袭" value="COPYRIGHT" />

          <el-option label="其他违规情况" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item label="补充说明" prop="description">
        <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述违规情况，以便管理员快速核实（选填，最多200字）"
            maxlength="200"
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
      <div class="dialog-footer">
        <el-button @click="dialogStore.closeReportDialog()">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确认举报
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useDialogStore } from '@/store/dialog.js';
import { ElMessage } from 'element-plus';
import { addReport } from '@/api/front/report.js';
import Verify from "@/components/verifition/Verify.vue";

const dialogStore = useDialogStore();
const formRef = ref(null);
const verifyRef = ref(null);
const submitting = ref(false);

// 表单数据绑定
const formData = reactive({
  reason: '',
  description: ''
});

// 表单校验规则
const rules = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ]
};

// 弹窗完全关闭后的回调：负责打扫战场
const handleClosed = () => {
  // 1. 清空表单验证状态和填写的数据
  if (formRef.value) {
    formRef.value.resetFields();
  }
  // 2. 调用 store 方法，清空目标 ID，彻底重置弹窗状态
  dialogStore.closeReportDialog();
};

// 提交举报
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate((valid) => {
    if (valid) {
      // 校验通过，弹出滑块验证码
      verifyRef.value.show();
    }
  });
};

// 滑块验证成功后的回调方法，在这里发送真实请求
const onVerifySuccess = async (params) => {
  submitting.value = true;

  try {
    const postData = {
      targetId: dialogStore.reportTargetId,
      targetType: dialogStore.reportTargetType,
      reason: formData.reason,
      description: formData.description
    };

    // 传入 postData 以及 滑块组件返回的 token
    const res = await addReport(postData, params.captchaVerification);

    if (res.code === 200) {
      ElMessage.success('举报提交成功，管理员会尽快核实处理！');
      dialogStore.closeReportDialog();
    } else {
      ElMessage.error(res.msg || '提交失败');
    }
  } catch (error) {
    console.error('举报异常:', error);
    ElMessage.error('网络异常，请稍后再试');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.report-header {
  margin-bottom: 20px;
}

.info-alert {
  border-radius: 6px;
}

.report-form {
  padding-right: 15px; /* 右侧留白，让 textarea 不会太贴边 */
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>