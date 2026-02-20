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

/**
 * =========================================================================
 * Props 定义
 * =========================================================================
 */
const props = defineProps({
  /**
   * 查询触发回调
   * 点击"查询"按钮时调用，通常对应父组件的 loadData 方法
   */
  onSearch: { type: Function, required: true, },

  /**
   * 重置触发回调
   * 点击"重置"按钮时调用，通常对应父组件的 resetSearch 方法
   */
  onReset: { type: Function, required: true, },

  /**
   * 批量删除 API 接口函数
   * 需接收 IDs 数组作为参数，并返回 Promise 响应对象
   */
  batchDeleteApi: { type: Function, required: true, },

  /**
   * 当前选中的数据 ID 集合
   * 用于批量删除时的参数传递
   */
  selectedIds: { type: Array, required: true, default: () => [] },

  /** 删除确认弹窗的内容提示 */
  batchDeleteTip: { type: String, default: '确定批量删除选中的数据吗？' },

  /** 删除确认弹窗的标题 */
  batchDeleteTitle: { type: String, default: '提示' }
})

/**
 * =========================================================================
 * Events 定义
 * =========================================================================
 */
const emit = defineEmits(['batch-delete-success', 'batch-delete-fail'])

/**
 * =========================================================================
 * 交互逻辑 (Interaction Logic)
 * =========================================================================
 */

/**
 * 处理查询操作
 * 代理父组件传递的查询方法
 */
const handleSearch = () => {
  props.onSearch()
}

/**
 * 处理重置操作
 * 代理父组件传递的重置方法
 */
const handleReset = () => {
  props.onReset()
}

/**
 * 处理批量删除流程
 * 1. 校验是否选择数据
 * 2. 弹出二次确认框
 * 3. 调用 API 执行删除
 * 4. 处理响应结果并通知父组件刷新
 * @emits batch-delete-success 删除成功后触发
 * @emits batch-delete-fail 删除失败后触发
 */
const handleBatchDelete = async () => {
  try {
    // 1. 校验选中状态 (虽然按钮禁用了，但保留逻辑校验更严谨)
    if (props.selectedIds.length === 0) {
      ElMessage.warning('请选择要删除的数据');
      return;
    }

    // 2. 二次确认
    await ElMessageBox.confirm(
        props.batchDeleteTip,
        props.batchDeleteTitle,
        { type: 'warning' }
    )

    // 3. 调用接口
    const res = await props.batchDeleteApi(props.selectedIds)

    // 4. 处理结果
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
  background-color: #f5f7fa;
  color: #606266;
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

/* * ==========================================
 * 响应式布局适配
 * ==========================================
 */

/* 大屏幕 (PC): 搜索项四列布局 */
@media (min-width: 1200px) {
  .search-container {
    display: contents; /* 消除容器层级，使子元素直接参与外层 search-box 的 Flex 布局 */
  }

  /* 强制每个搜索项占据 20% 宽度 (根据你之前的代码保留的比例) */
  .search-container :deep(> *) {
    flex: 0 0 calc(20% - 6px);
    max-width: calc(20% - 6px); /* 防止内部超长内容撑破列宽 */
  }

  .button-container {
    flex: 1;
    display: flex;
    justify-content: flex-end; /* 确保按钮始终靠右侧对齐 */
    min-width: 160px; /* 兜底宽度，防止按钮被过度挤压变形 */
  }
}

/* 中等屏幕 (平板): 搜索项双列布局 */
@media (min-width: 768px) and (max-width: 1199px) {
  .search-container {
    display: contents; /* 消除容器层级，使子元素参与父级 Flex */
  }

  /* 强制每个搜索项占据 50% 宽度 */
  .search-container :deep(> *) {
    flex: 0 0 calc(50% - 6px);
  }

  .button-container {
    flex: 1;
  }
}

/* 小屏幕 (手机): 垂直堆叠布局 */
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