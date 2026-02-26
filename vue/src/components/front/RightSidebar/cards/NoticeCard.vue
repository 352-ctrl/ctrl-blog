<template>
  <el-card class="notice-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="icon-bell"><Bell /></el-icon>
          公告通知
        </span>
      </div>
    </template>

    <div class="notice-body">
      <div v-if="noticeList.length === 0" class="empty-state">
        <el-empty description="暂无公告" :image-size="60" />
      </div>

      <div v-else class="notice-list">
        <div
            v-for="(item) in noticeList"
            :key="item.id"
            class="notice-item animate__animated animate__fadeIn"
        >
          <div class="notice-content" v-html="sanitizeHtml(item.contentHtml)"></div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { sanitizeHtml } from "@/utils/filter.js";
import { onMounted, ref } from "vue";
import { getHomepageNotices } from "@/api/front/notice.js";
import { ElMessage } from "element-plus";

const noticeList = ref([])

onMounted(async () => {
  loadNotice();
});

const loadNotice = () => {
  getHomepageNotices().then(res => {
    if (res.code === 200) {
      noticeList.value = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
}
</script>

<style scoped lang="scss">
/* 卡片整体样式 */
.notice-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color-overlay);

  /* 头部样式 */
  :deep(.el-card__header) {
    padding: 15px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-family: 'SmileySans', sans-serif;
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .icon-bell {
        margin-right: 10px;
        color: var(--el-color-danger);
        font-size: 18px;
        animation: swing 2s infinite linear;
        transform-origin: top center;
      }
    }
  }

  .notice-body {
    min-height: 100px;
  }

  /* 列表项样式 */
  .notice-item {
    padding: 10px 0;
    border-bottom: 1px dashed var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    &:first-child {
      padding-top: 0;
    }
  }

  /* v-html 内容样式控制 (必须使用 :deep) */
  .notice-content {
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.6;
    word-break: break-all;

    /* 防止富文本中的图片撑破布局 */
    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 4px;
      margin: 8px 0;
    }

    :deep(p) {
      margin: 0 0 8px 0;
    }

    :deep(a) {
      color: var(--el-color-primary);
      text-decoration: none;
      &:hover {
        text-decoration: underline;
      }
    }
  }

  /* 空状态微调 */
  .empty-state {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    :deep(.el-empty__description) {
      margin-top: 10px;
    }
  }
}

@keyframes swing {
  0% { transform: rotate(0deg); }
  10% { transform: rotate(15deg); }
  20% { transform: rotate(-10deg); }
  30% { transform: rotate(5deg); }
  40% { transform: rotate(-5deg); }
  50% { transform: rotate(0deg); }
  100% { transform: rotate(0deg); }
}
</style>