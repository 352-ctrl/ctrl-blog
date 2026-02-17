<template>
  <div class="pagination-container" v-if="total >= pageSize">
    <el-pagination
        background
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @update:current-page="handleCurrentPageChange"
        @update:page-size="handlePageSizeChange"
        @change="handleChange"
    />
  </div>
</template>

<script setup>
/**
 * =========================================================================
 * Props 定义
 * =========================================================================
 */
const props = defineProps({
  /** 当前页码 (v-model:currentPage) */
  currentPage: { type: Number, required: true, default: 1 },

  /** 每页显示条数 (v-model:pageSize) */
  pageSize: { type: Number, required: true, default: 10 },

  /** 数据总条数 */
  total: { type: Number, required: true, default: 0 }
})

/**
 * =========================================================================
 * Events 定义
 * =========================================================================
 */
const emit = defineEmits([
  'update:currentPage', // 更新页码模型
  'update:pageSize',    // 更新条数模型
  'change'              // 分页状态改变回调（通常用于触发 API 查询）
])

/**
 * =========================================================================
 * 交互逻辑
 * =========================================================================
 */

/**
 * 处理页码变更
 * 同步更新父组件的 currentPage 状态
 * @param {Number} newPage 新的页码
 */
const handleCurrentPageChange = (newPage) => {
  emit('update:currentPage', newPage)
}

/**
 * 处理每页条数变更
 * 同步更新父组件的 pageSize 状态
 * @param {Number} newSize 新的每页条数
 */
const handlePageSizeChange = (newSize) => {
  emit('update:pageSize', newSize)
}

/**
 * 统一处理分页变更事件
 * 当页码或条数发生改变时触发，用于通知父组件重新请求数据
 */
const handleChange = () => {
  emit('change')
}
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end; /* 默认右对齐，符合一般后台管理系统规范 */
}
</style>