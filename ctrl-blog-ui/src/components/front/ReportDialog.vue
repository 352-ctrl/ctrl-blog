<template>
  <el-dialog v-model="dialogStore.reportVisible" title="违规举报" width="500px" @closed="handleClosed" :close-on-click-modal="false">
    <div class="report-header">
      <el-alert title="感谢您参与社区环境维护。请如实填写举报原因，我们将尽快处理。" type="info" show-icon :closable="false" class="info-alert" />
    </div>

    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" class="report-form">
      <el-form-item label="举报原因" prop="reason">
        <el-select v-model="formData.reason" placeholder="请选择违规类型" class="full-width-select">
          <el-option label="垃圾广告信息" value="SPAM" />
          <el-option label="涉黄/暴力/低俗内容" value="PORN" />
          <el-option label="恶意攻击/谩骂" value="ABUSE" />
          <el-option label="政治敏感/违法违规" value="ILLEGAL" />
          <el-option label="侵权或抄袭" value="COPYRIGHT" />
          <el-option label="其他违规情况" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item label="补充说明" prop="content">
        <el-input v-model="formData.content" type="textarea" :rows="4" placeholder="请详细描述违规情况，以便管理员快速核实（选填，最多200字）" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>

    <Verify ref="verifyRef" mode="pop" captchaType="blockPuzzle" @success="onVerifySuccess"></Verify>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogStore.closeReportDialog()">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确认举报</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useDialogStore } from '@/store/dialog.js';
import { addReport } from '@/api/front/interaction/report.js';
import Verify from "@/components/common/Verify/Verify.vue";
import { useRequest } from "@/composables/useRequest.js";

const dialogStore = useDialogStore();
const formRef = ref(null);
const verifyRef = ref(null);

const formData = reactive({ reason: '', content: '' });
const rules = { reason: [{ required: true, message: '请选择举报原因', trigger: 'change' }] };

const { loading: submitting, execute: execReport } = useRequest(addReport, { successMsg: '举报提交成功，管理员会尽快核实处理！' });

const handleClosed = () => {
  if (formRef.value) formRef.value.resetFields();
  dialogStore.closeReportDialog();
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate((valid) => {
    if (valid) verifyRef.value.show();
  });
};

const onVerifySuccess = async (params) => {
  try {
    const postData = {
      targetId: dialogStore.reportTargetId,
      targetType: dialogStore.reportTargetType,
      reason: formData.reason,
      content: formData.content
    };
    await execReport(postData, params.captchaVerification);
    dialogStore.closeReportDialog();
  } catch (error) {}
};
</script>

<style scoped>
.full-width-select {
  width: 100%;
}

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