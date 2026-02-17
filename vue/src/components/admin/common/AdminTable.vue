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
          <el-avatar v-if="scope.row[column.prop]" :preview-src-list="[scope.row[column.prop]]" :size="40" :src="scope.row[column.prop]" />
          <el-avatar v-else :size="40" >
            <img src="../../../assets/images/default-avatar.png">
          </el-avatar>
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
              style="width: 80px; height: 60px; object-fit: cover;"
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
                style="margin-right: 5px; margin-bottom: 5px;"
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
              <span style="color: #409EFF; font-weight: bold;">
                @{{ scope.row[column.prop] }}
              </span>
              <span v-if="scope.row[column.contentProp]" style="color: #909399; font-size: 12px; margin-left: 5px;">
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
        v-if="showAction"
        label="操作"
        width="150"
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
            style="margin-right: 8px;"
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
import { defineProps, defineEmits } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import {sanitizeHtml} from "@/utils/filter.js";

/**
 * =========================================================================
 * 类型定义 (Type Definitions)
 * =========================================================================
 */

/**
 * @typedef {Object} TableColumn
 * @property {string} prop - 字段名
 * @property {string} label - 表头文案
 * @property {'avatar'|'cover'|'tags'|'status'|'top'|'html'|'reply'} [type] - 列类型，留空为默认文本
 * @property {number} [width] - 固定宽度 (px)
 * @property {number} [minWidth] - 最小宽度 (px)
 * @property {string} [align='center'] - 对齐方式
 * @property {Object} [statusMap] - 状态映射配置 { [key]: { text: string, type: string } }
 */

/**
 * =========================================================================
 * Props 定义
 * =========================================================================
 */
const props = defineProps({
  /** 表格数据源 */
  tableData: { type: Array, required: true, default: () => [] },

  /** 列配置数组 */
  columns: { type: Array, required: true, default: () => [] },

  /** 是否显示最左侧多选列 */
  showSelection: { type: Boolean, default: true },

  /** 是否显示最右侧操作列 */
  showAction: { type: Boolean, default: true },

  /** v-model:selectedIds 绑定的选中行 ID 集合 */
  selectedIds: { type: Array, default: () => [] },

  /** 是否开启编辑功能 */
  editable: { type: Boolean, default: true },

  /** 是否开启删除功能 */
  deletable: { type: Boolean, default: true },

  /** * 删除接口函数
   * @param {number} id - 数据 ID
   * @returns {Promise} 返回含 code 字段的响应对象
   */
  deleteApi: { type: Function, required: true },

  /** 删除确认弹窗内容 */
  deleteTip: { type: String, default: '确定删除该数据吗？' },

  /** 删除确认弹窗标题 */
  deleteTitle: { type: String, default: '提示' }
})

/**
 * =========================================================================
 * Events 定义
 * =========================================================================
 */
const emit = defineEmits([
  'update:selectedIds', // 双向绑定选中 ID
  'selection-change',   // 透传原生多选事件
  'edit',               // 触发编辑
  'delete-success',     // 删除成功回调
  'status-change'       // 状态修改事件 (Switch 开关切换时触发，回传当前行数据 row)
])

/**
 * =========================================================================
 * 交互逻辑 (Interaction Logic)
 * =========================================================================
 */

/**
 * 处理多选变更
 * 同步更新父组件的 selectedIds 数据模型
 */
const handleSelectionChange = (selectedRows) => {
  const ids = selectedRows.map(row => row.id)
  emit('update:selectedIds', ids)
  emit('selection-change', selectedRows)
}

/**
 * 处理编辑操作
 * 将当前行数据回传给父组件
 */
const handleEditClick = (row) => {
  emit('edit', row)
}

/**
 * 处理删除操作
 * 包含二次确认弹窗和接口调用逻辑
 */
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

/**
 * 处理 Switch 状态变更
 * @param {Object} row 当前行数据
 */
const handleSwitchChange = (row) => {
  // 可以在这里加个 loading 效果防止重复点击
  row.loading = true;

  // 抛出事件给父组件，父组件负责调用 API
  // 传递 row 对象，父组件处理完后记得把 loading 改回 false
  emit('status-change', row);
}
</script>

<style scoped>
/* 强制统一表头背景色，增强视觉识别度 */
:deep(.common-table-header) {
  background-color: #f8f8f9 !important;
  color: #515a6e;
  font-weight: 600;
}
</style>