<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteTagApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的标签吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.name"
            placeholder="请输入标签名称查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增标签</el-button>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="tagColumns"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteTagApi"
          delete-tip="确定删除该标签吗？"
          @edit="handleEdit"
          @delete-success="loadPage"
      />

      <AdminPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>

    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑标签' : '新增标签'" class="dialog-md-down" width="500px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
// 引入 API 模块
import {getTagPage, addTag, updateTag, deleteTag, deleteTags, getTagById} from "@/api/admin/tag.js";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const formRef = ref(null);

const data = reactive({
  name: '',
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: [],
  rules: {
    name: [
      { required: true, message: '标签名称不能为空', trigger: 'blur' },
      { min: 1, max: 20, message: '标签名称长度不能超过20个字符', trigger: 'blur' }
    ]
  }
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    name: data.name,
  };
  getTagPage(params).then(res => {
    if (res.code === 200) {
      data.tableData = res.data.records;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 初始化加载
onMounted(() => {
  loadPage();
});

// 重置搜索
const resetSearch = () => {
  data.name = "";
  data.pageNum = 1;
  loadPage();
};

// 新增按钮逻辑
const handleAdd = () => {
  data.form = {};
  data.formVisible = true;
};

// 编辑按钮逻辑
const handleEdit = async (row) => {
  try {
    const res = await getTagById(row.id);

    if (res.code === 200) {
      // 将数据库返回的完整详情赋值给表单
      data.form = res.data;

      // 数据获取成功后，再显示弹窗
      data.formVisible = true;
    } else {
      ElMessage.error(res.msg || '获取详情失败');
    }
  } catch (error) {
    console.error('获取详情异常:', error);
    ElMessage.error('网络异常，无法获取数据');
  }
};

// 提交表单 (新增或修改)
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const apiMethod = data.form.id ? updateTag : addTag;
      apiMethod(data.form).then(res => {
        if (res.code === 200) {
          ElMessage.success('操作成功');
          data.formVisible = false;
          loadPage();
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  });
};

// 指定删除
const deleteTagApi = async (id) => {
  return deleteTag(id);
};

// 批量删除
const batchDeleteTagApi = async (selectedIds) => {
  return deleteTags(selectedIds);
};

// 标签列表列配置
const tagColumns = reactive([
  { prop: 'name', label: '名称' },
  { prop: 'createTime', label: '创建时间' }
])
</script>

<style scoped>

</style>