<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteTagApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的标签吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-input v-model="query.name" placeholder="请输入标签名称查询" prefix-icon="Search" clearable @clear="loadData" />
      </template>
      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增标签</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="tagColumns" v-model:selectedIds="selectedIds" :delete-api="deleteTagApi" delete-tip="确定删除该标签吗？" @edit="handleEdit" @delete-success="loadData" />
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-md-down" width="500px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="dialog.rowData.value.name" autocomplete="off" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { getTagPage, addTag, updateTag, deleteTag, deleteTags, getTagById } from "@/api/admin/content/tag.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const formRef = ref(null);
const selectedIds = ref([]);
const rules = { name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }, { min: 1, max: 20, message: '标签名称长度不能超过20个字符', trigger: 'blur' }] };

// ======= Hook 接入 =======
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getTagPage, { name: '' });
const dialog = useDialog();
const { loading: submitLoading, execute: execSubmit } = useRequest((data) => data.id ? updateTag(data) : addTag(data), { successMsg: '操作成功' });

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleAdd = () => dialog.openDialog('新增标签', 'add', {});

const handleEdit = async (row) => {
  const res = await getTagById(row.id);
  if (res.code === 200) dialog.openDialog('编辑标签', 'edit', res.data);
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

const deleteTagApi = async (id) => deleteTag(id);
const batchDeleteTagApi = async (ids) => deleteTags(ids);

const tagColumns = reactive([
  { prop: 'name', label: '名称' },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>