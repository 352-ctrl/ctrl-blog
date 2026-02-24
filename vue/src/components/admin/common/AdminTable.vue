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
          <slot name="expand" :row="scope.row"></slot>
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
          <el-tag :type="column.statusMap[scope.row[column.prop]].type">
            {{ column.statusMap[scope.row[column.prop]].text }}
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
        :width="actionColumnWidth"
        fixed="right"
        align="center"
    >
      <template #default="scope">
        <el-button
            v-if="editable"
            type="primary"
            icon="Edit"
            circle
            @click="handleEditClick(scope.row)"
            :class="{ 'action-btn-mr': deletable }"
        />
        <el-button
            v-if="deletable"
            type="danger"
            icon="Delete"
            circle
            @click="handleDeleteClick(scope.row.id)"
        />
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
  expandable: { type: Boolean, default: false }
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
      ElMessage.error('删除失败')
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

.action-btn-mr {
  margin-right: 8px;
}
</style>