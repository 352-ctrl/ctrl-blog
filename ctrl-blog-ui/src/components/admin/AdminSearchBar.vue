<template>
  <div class="card search-box">
    <div class="search-container">
      <slot name="search-items"></slot>
    </div>
    <div class="button-container">
      <el-button type="primary" @click="handleSearch">查 询</el-button>
      <el-button @click="handleReset" class="reset-btn">重 置</el-button>
    </div>
  </div>

  <div class="card operate-box">
    <slot name="operate-buttons"></slot>
    <el-button
        type="danger"
        icon="Delete"
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
    >
      批量删除
    </el-button>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

const props = defineProps({
  onSearch: { type: Function, required: true, },
  onReset: { type: Function, required: true, },
  batchDeleteApi: { type: Function, required: true, },
  selectedIds: { type: Array, required: true, default: () => [] },
  batchDeleteTip: { type: String, default: '确定批量删除选中的数据吗？' },
  batchDeleteTitle: { type: String, default: '提示' }
})

const emit = defineEmits(['batch-delete-success', 'batch-delete-fail'])

const handleSearch = () => {
  props.onSearch()
}

const handleReset = () => {
  props.onReset()
}

const handleBatchDelete = async () => {
  try {
    if (props.selectedIds.length === 0) {
      ElMessage.warning('请选择要删除的数据');
      return;
    }

    await ElMessageBox.confirm(
        props.batchDeleteTip,
        props.batchDeleteTitle,
        { type: 'warning' }
    )

    const res = await props.batchDeleteApi(props.selectedIds)

    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      emit('batch-delete-success');
    } else {
      ElMessage.error(res.msg || '批量删除失败');
      emit('batch-delete-fail', res.msg || '批量删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('Delete operation error:', error)
    }
  }
};
</script>

<style scoped>
.reset-btn {
  background-color: var(--el-fill-color-light); /* 适配暗黑模式的填充色 */
  color: var(--el-text-color-regular);
  border-color: var(--el-border-color-light);
  transition: all 0.3s;
}

.reset-btn:hover {
  background-color: var(--el-fill-color);
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
}

.search-box {
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.operate-box {
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.search-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.button-container {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end
}

/* 响应式布局适配 */
@media (min-width: 1200px) {
  .search-container {
    display: contents;
  }
  .search-container :deep(> *) {
    flex: 0 0 calc(20% - 8px);
    max-width: calc(20% - 8px);
  }
  .button-container {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    min-width: 160px;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .search-container {
    display: contents;
  }
  .search-container :deep(> *) {
    flex: 0 0 calc(50% - 6px);
  }
  .button-container {
    flex: 1;
  }
}

@media (max-width: 767px) {
  .search-box {
    flex-direction: column;
  }
  .search-container > * {
    width: 100%;
  }
  .button-container {
    width: 100%;
  }
}
</style>