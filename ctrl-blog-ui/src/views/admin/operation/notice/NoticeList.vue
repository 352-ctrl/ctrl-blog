<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteNoticeApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的公告吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-input v-model="query.content" placeholder="请输入内容查询" prefix-icon="Search" clearable @clear="loadData" />
        <el-select v-model="query.status" placeholder="请选择是否发布"><el-option label="是" value="1" /><el-option label="否" value="0" /></el-select>
        <el-select v-model="query.isTop" placeholder="请选择是否置顶"><el-option label="是" value="1" /><el-option label="否" value="0" /></el-select>
      </template>
      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增公告</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="noticeColumns" v-model:selectedIds="selectedIds" :delete-api="deleteNoticeApi" delete-tip="确定删除该公告吗？" @edit="handleEdit" @delete-success="loadData" />
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-lg-down" width="800px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="12" :md="12"><el-form-item label="是否置顶" prop="isTop"><el-switch v-model="dialog.rowData.value.isTop" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :xs="12" :sm="12" :md="12"><el-form-item label="是否发布" prop="status"><el-switch v-model="dialog.rowData.value.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="公告内容" prop="content">
          <MdEditor v-model="dialog.rowData.value.content" :theme="isDark ? 'dark' : 'light'" :preview="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { useDark } from '@vueuse/core';
import { getNoticePage, addNotice, updateNotice, deleteNotice, deleteNotices, getNoticeById } from "@/api/admin/operation/notice.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const isDark = useDark();
const formRef = ref(null);
const selectedIds = ref([]);

const rules = { content: [{ required: true, message: '公告内容不能为空', trigger: 'blur' }] };

// Hooks 集成
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getNoticePage, { content: '', status: null, isTop: null });
const dialog = useDialog();
const { loading: submitLoading, execute: execSubmit } = useRequest((data) => data.id ? updateNotice(data) : addNotice(data), { successMsg: '操作成功' });

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleAdd = () => dialog.openDialog('新增公告', 'add', { isTop: 0, status: 1 });

const handleEdit = async (row) => {
  const res = await getNoticeById(row.id);
  if (res.code === 200) dialog.openDialog('编辑公告', 'edit', res.data);
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      await execSubmit(dialog.rowData.value);
      dialog.closeDialog();
      await loadData();
    }
  });
};

const deleteNoticeApi = async (id) => deleteNotice(id);
const batchDeleteNoticeApi = async (ids) => deleteNotices(ids);

const noticeColumns = reactive([
  { type: 'html', prop: 'contentHtml', label: '内容', minWidth: '350px' },
  { type: 'top', prop: 'isTop', label: '置顶' },
  { type: 'status', prop: 'status', label: '状态', statusMap: { 1: { text: '发布' }, 0: { text: '隐藏' } } },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>