<template>
  <div>
    <AdminSearchBar
        :on-search="loadData"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteSensitiveWordApi"
        :selected-ids="selectedIds"
        batchDeleteTip="确定批量删除选中的敏感词吗？"
        @batch-delete-success="loadData"
    >
      <template #search-items>
        <el-input v-model="query.word" placeholder="请输入敏感词查询" prefix-icon="Search" clearable @clear="loadData" />
      </template>
      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增敏感词</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable
          :table-data="tableData"
          :columns="wordColumns"
          v-model:selectedIds="selectedIds"
          :delete-api="deleteSensitiveWordApi"
          delete-tip="确定删除该敏感词吗？"
          @edit="handleEdit"
          @delete-success="loadData"
      />
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-md-down" width="500px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules">
        <el-form-item label="敏感词内容" prop="word">
          <el-input v-model="dialog.rowData.value.word" autocomplete="off" placeholder="请输入敏感词内容" />
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
import { getSensitiveWordPage, addSensitiveWord, updateSensitiveWord, deleteSensitiveWord, deleteSensitiveWords, getSensitiveWordById } from "@/api/admin/system/sysSensitiveWord.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const formRef = ref(null);
const selectedIds = ref([]);
const rules = {
  word: [
    { required: true, message: '敏感词不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '敏感词长度不能超过50个字符', trigger: 'blur' }
  ]
};

// Hooks 集成
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getSensitiveWordPage, { word: '' });
const dialog = useDialog();
const { loading: submitLoading, execute: execSubmit } = useRequest((data) => data.id ? updateSensitiveWord(data) : addSensitiveWord(data), { successMsg: '操作成功' });

onMounted(() => loadData());

const resetSearch = () => {
  resetQuery();
  selectedIds.value = [];
};

const handleAdd = () => dialog.openDialog('新增敏感词', 'add', {});

const handleEdit = async (row) => {
  const res = await getSensitiveWordById(row.id);
  if (res.code === 200) dialog.openDialog('编辑敏感词', 'edit', res.data);
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

const deleteSensitiveWordApi = async (id) => deleteSensitiveWord(id);
const batchDeleteSensitiveWordApi = async (ids) => deleteSensitiveWords(ids);

const wordColumns = reactive([
  { prop: 'word', label: '敏感词内容' },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>