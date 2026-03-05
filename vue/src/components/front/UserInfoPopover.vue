<template>
  <el-popover
      placement="top-start"
      :width="240"
      trigger="hover"
      :show-after="300"
      :hide-after="200"
  >
    <template #reference>
      <div class="avatar-trigger">
        <slot></slot>
      </div>
    </template>

    <div class="user-popover-card">
      <div class="user-header">
        <el-avatar :size="48" :src="avatar" />
        <div class="user-meta">
          <div class="nickname">{{ nickname }}</div>
          <div class="join-time">社区成员</div>
        </div>
      </div>

      <div class="user-bio">
        {{ bio || '这个人很懒，什么都没写~' }}
      </div>

      <div class="user-footer">
        <el-button text size="small" type="danger" @click="handleReport">
          <el-icon class="report-icon"><Warning /></el-icon> 举报该用户
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { useUserStore } from "@/store/user.js";
import { useDialogStore } from "@/store/dialog.js";
import { ElMessage } from "element-plus";

// 接收外部传入的用户信息
const props = defineProps({
  userId: { type: [String, Number], required: true },
  avatar: { type: String, default: '' },
  nickname: { type: String, default: '未知用户' },
  bio: { type: String, default: '' }
});

const userStore = useUserStore();
const dialogStore = useDialogStore();

// 处理点击举报按钮的事件
const handleReport = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后操作');
    return;
  }

  // 全局唤起弹窗！
  dialogStore.openReportDialog(props.userId, 'USER');
};
</script>

<style scoped>
.avatar-trigger {
  cursor: pointer;
  display: inline-block;
}

.user-popover-card {
  padding: 2px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.nickname {
  font-family: 'SmileySans', sans-serif; /* 复用你系统里的字体 */
  font-size: 16px;
  color: var(--el-text-color-primary);
  font-weight: 600;
  margin-bottom: 4px;
}

.join-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.user-bio {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  margin-bottom: 12px;
  /* 限制最多显示3行，超出省略号 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.user-footer {
  border-top: 1px dashed var(--el-border-color-lighter);
  padding-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.user-footer .el-button {
  padding: 4px 8px;
  height: auto;
  font-size: 12px;
}

.report-icon {
  margin-right: 2px;
}
</style>