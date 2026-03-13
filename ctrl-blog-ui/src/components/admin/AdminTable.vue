<template>
  <el-table
      :data="tableData"
      @selection-change="handleSelectionChange"
      header-cell-class-name="common-table-header"
      v-bind="$attrs"
      stripe
  >
    <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
    />

    <el-table-column v-if="expandable" type="expand">
      <template #default="scope">
        <div class="expand-wrapper">
          <el-form label-position="left" inline class="admin-table-expand-form">
            <slot name="expand" :row="scope.row"></slot>
          </el-form>
        </div>
      </template>
    </el-table-column>

    <template v-for="column in columns" :key="column.prop || column.label">

      <el-table-column
          v-if="column.type === 'avatar'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 80"
          :align="column.align || 'center'"
      >
        <template #default="scope">
          <el-avatar :preview-src-list="[scope.row[column.prop]]" :size="40" :src="scope.row[column.prop]" />
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'cover'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 100"
          :align="column.align || 'center'"
      >
        <template #default="scope">
          <el-image
              v-if="scope.row[column.prop]"
              :src="scope.row[column.prop]"
              class="cover-image"
              :preview-src-list="[scope.row[column.prop]]"
              preview-teleported
          />
          <span v-else>无封面</span>
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'tags'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 180"
          :align="column.align || 'center'"
      >
        <template #default="scope">
          <div v-if="scope.row[column.prop] && scope.row[column.prop].length > 0">
            <el-tag
                v-for="(tag, index) in scope.row[column.prop]"
                :key="index"
                size="small"
                class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
          <span v-else>无标签</span>
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'status'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 80"
          :align="column.align || 'center'"
      >
        <template #default="scope">
          <el-tag :type="column.statusMap[scope.row[column.prop]]?.type || 'info'">
            {{ column.statusMap[scope.row[column.prop]]?.text || scope.row[column.prop] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'top'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 80"
          :align="column.align || 'center'"
      >
        <template #default="scope">
          <el-tag :type="scope.row[column.prop] === 1 ? 'success' : 'info'">
            {{ scope.row[column.prop] === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'switch'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 80"
          :align="column.align || 'center'"
      >
        <template #default="scope">
          <el-switch
              v-model="scope.row[column.prop]"
              :active-value="0"
              :inactive-value="1"
              @change="handleSwitchChange(scope.row)"
              :loading="scope.row.loading"
          />
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'html'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'center'"
          show-overflow-tooltip
      >
        <template #default="scope">
          <div v-html="sanitizeHtml(scope.row[column.prop])"></div>
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'reply'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 100"
          :align="column.align || 'left'"
          show-overflow-tooltip
      >
        <template #default="scope">
          <slot :name="column.slotName || 'reply'" :row="scope.row" :index="scope.$index">
            <div v-if="scope.row[column.prop] && scope.row[column.prop] !== '--'">
              <span class="reply-user">
                @{{ scope.row[column.prop] }}
              </span>
              <span v-if="scope.row[column.contentProp]" class="reply-content">
                : {{ scope.row[column.contentProp] }}
              </span>
            </div>
            <el-tag v-else type="info" size="small" effect="plain">文章</el-tag>
          </slot>
        </template>
      </el-table-column>

      <el-table-column
          v-else-if="column.type === 'title'"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth || 200"
          :align="column.align || 'left'"
      >
        <template #default="scope">
          <div style="display: flex; align-items: center; gap: 6px;">
            <el-tag v-if="scope.row.isTop === 1" type="danger" size="small" effect="dark" style="flex-shrink: 0;">
              置顶
            </el-tag>
            <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" :title="scope.row[column.prop]">
              {{ scope.row[column.prop] }}
            </span>
          </div>
        </template>
      </el-table-column>

      <el-table-column
          v-else
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'center'"
          show-overflow-tooltip
      >
        <template #default="scope">
          <div>{{ scope.row[column.prop] }}</div>
        </template>
      </el-table-column>
    </template>

    <el-table-column
        v-if="realShowAction"
        label="操作"
        :width="actionWidth || actionColumnWidth" fixed="right"
        align="center"
    >
      <template #default="scope">
        <div class="table-actions">
          <slot name="custom-actions" :row="scope.row"></slot>

          <el-button
              v-if="editable"
              type="primary"
              icon="Edit"
              circle
              @click="handleEditClick(scope.row)"
          />
          <el-button
              v-if="deletable"
              type="danger"
              icon="Delete"
              circle
              @click="handleDeleteClick(scope.row.id)"
          />
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { sanitizeHtml } from "@/utils/filter.js";

const props = defineProps({
  tableData: { type: Array, required: true, default: () => [] },
  columns: { type: Array, required: true, default: () => [] },
  showSelection: { type: Boolean, default: true },
  showAction: { type: Boolean, default: true },
  selectedIds: { type: Array, default: () => [] },
  editable: { type: Boolean, default: true },
  deletable: { type: Boolean, default: true },
  deleteApi: { type: Function, required: true },
  deleteTip: { type: String, default: '确定删除该数据吗？' },
  deleteTitle: { type: String, default: '提示' },
  expandable: { type: Boolean, default: false },
  actionWidth: { type: Number }
})

const emit = defineEmits([
  'update:selectedIds',
  'selection-change',
  'edit',
  'delete-success',
  'status-change'
])

const realShowAction = computed(() => {
  return props.showAction && (props.editable || props.deletable)
})

const actionColumnWidth = computed(() => {
  if (props.editable && props.deletable) {
    return 150
  }
  return 90
})

const handleSelectionChange = (selectedRows) => {
  const ids = selectedRows.map(row => row.id)
  emit('update:selectedIds', ids)
  emit('selection-change', selectedRows)
}

const handleEditClick = (row) => {
  emit('edit', row)
}

const handleDeleteClick = async (id) => {
  try {
    await ElMessageBox.confirm(
        props.deleteTip,
        props.deleteTitle,
        { type: 'warning' }
    )

    const res = await props.deleteApi(id)

    if (res.code === 200) {
      ElMessage.success('删除成功')
      emit('delete-success')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete operation failed:', error)
    }
  }
}

const handleSwitchChange = (row) => {
  row.loading = true;
  emit('status-change', row);
}
</script>

<style scoped>
/* 强制统一表头背景色，自适应暗黑模式 */
:deep(.common-table-header) {
  background-color: var(--el-fill-color-light) !important;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

/* 提取的行内样式 */
.expand-wrapper {
  padding: 20px;
  background-color: var(--el-fill-color-lighter); /* 浅灰/深灰自适应 */
}

.cover-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.tag-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.reply-user {
  color: var(--el-color-primary);
  font-weight: bold;
}

.reply-content {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-left: 5px;
}

.table-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px; /* 统一控制所有按钮之间的间距 */
}

/* 重置 Element Plus 默认的兄弟按钮左边距，把间距控制权完全交给上一层的 gap */
.table-actions :deep(.el-button) {
  margin-left: 0 !important;
  margin-right: 0 !important;
}

/* ==========================================
   展开行 (Expand) 全局通用样式
   ========================================== */
.expand-wrapper {
  padding: 15px 30px;
  background-color: var(--el-fill-color-lighter);
}

.admin-table-expand-form {
  margin-left: 20px;
}

:deep(.admin-table-expand-form .el-form-item) {
  margin-bottom: 8px;
  margin-right: 0;
  width: 100%;
  display: flex;
}

:deep(.admin-table-expand-form .el-form-item__label) {
  color: var(--el-text-color-secondary);
  font-weight: bold;
  width: 90px; /* 统一下巴宽度，如需特殊宽度在业务侧覆盖 */
}

/* 展开内容的通用包裹框样式 */
:deep(.expand-value-box) {
  display: inline-block;
  width: 800px;
  padding: 8px 14px;
  border-radius: 6px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  background-color: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-lighter);
  word-break: break-all;
  white-space: pre-wrap;
  font-size: 14px;
}
</style>