<template>
  <div>
    <AdminSearchBar
        :on-search="loadData"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteCategoryApi"
        :selected-ids="selectedIds"
        batchDeleteTip="确定批量删除选中的分类吗？"
        @batch-delete-success="loadData"
    >
      <template #search-items>
        <el-input
            v-model="query.name"
            placeholder="请输入分类名称查询"
            prefix-icon="Search"
            clearable
            @clear="loadData"
        />
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable
          :table-data="tableData"
          :columns="categoryColumns"
          v-model:selectedIds="selectedIds"
          :delete-api="deleteCategoryApi"
          delete-tip="确定删除该分类吗？"
          @edit="handleEdit"
          @delete-success="loadData"
      />

      <AdminPagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          @change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-md-down" width="500px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="dialog.rowData.value.name" autocomplete="off" placeholder="请输入分类名称" />
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
import { getCategoryPage, addCategory, updateCategory, deleteCategory, deleteCategories, getCategoryById } from "@/api/admin/content/category.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";

// 引入封装好的 Hook
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const formRef = ref(null);
const selectedIds = ref([]);
const rules = {
  name: [
    { required: true, message: '分类名称不能为空', trigger: 'blur' },
    { min: 1, max: 20, message: '分类名称长度不能超过20个字符', trigger: 'blur' }
  ]
};

// ==================== Hooks 集成 ====================
// 1. 接管表格分页和数据
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getCategoryPage, { name: '' });

// 2. 接管弹窗状态
const dialog = useDialog();

// 3. 接管表单提交
const { loading: submitLoading, execute: execSubmit } = useRequest(
    (data) => data.id ? updateCategory(data) : addCategory(data),
    { successMsg: '操作成功' }
);

onMounted(() => {
  loadData();
});

const resetSearch = () => {
  resetQuery();
  selectedIds.value = [];
};

const handleAdd = () => {
  dialog.openDialog('新增分类', 'add', {});
};

const handleEdit = async (row) => {
  const res = await getCategoryById(row.id);
  if (res.code === 200) {
    dialog.openDialog('编辑分类', 'edit', res.data);
  }
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      await execSubmit(dialog.rowData.value);
      dialog.closeDialog();
      loadData();
    }
  });
};

const deleteCategoryApi = async (id) => deleteCategory(id);
const batchDeleteCategoryApi = async (ids) => deleteCategories(ids);

// 分类列表列配置
const categoryColumns = reactive([
  { prop: 'name', label: '名称' },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>

<style scoped>

</style>