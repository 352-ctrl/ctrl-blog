<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteSensitiveWordApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的敏感词吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.word"
            placeholder="请输入敏感词查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增敏感词</el-button>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="wordColumns"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteSensitiveWordApi"
          delete-tip="确定删除该敏感词吗？"
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

    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑敏感词' : '新增敏感词'" class="dialog-md-down" width="500px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules">
        <el-form-item label="敏感词内容" prop="word">
          <el-input v-model="data.form.word" autocomplete="off" placeholder="请输入敏感词内容" />
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
import { ElMessage } from "element-plus";
// 引入敏感词 API 模块 (你需要确保这个文件和这些方法已创建)
import {
  getSensitiveWordPage,
  addSensitiveWord,
  updateSensitiveWord,
  deleteSensitiveWord,
  deleteSensitiveWords,
  getSensitiveWordById
} from "@/api/admin/sysSensitiveWord.js";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const formRef = ref(null);

const data = reactive({
  word: '', // 搜索字段由 name 改为 word
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: [],
  rules: {
    word: [
      { required: true, message: '敏感词不能为空', trigger: 'blur' },
      { min: 1, max: 50, message: '敏感词长度不能超过50个字符', trigger: 'blur' }
    ]
  }
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    word: data.word, // 传递 word 参数
  };
  getSensitiveWordPage(params).then(res => {
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
  data.word = "";
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
    const res = await getSensitiveWordById(row.id);

    if (res.code === 200) {
      data.form = res.data;
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
      const apiMethod = data.form.id ? updateSensitiveWord : addSensitiveWord;
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
const deleteSensitiveWordApi = async (id) => {
  return deleteSensitiveWord(id);
};

// 批量删除
const batchDeleteSensitiveWordApi = async (selectedIds) => {
  return deleteSensitiveWords(selectedIds);
};

// 敏感词列表列配置
const wordColumns = reactive([
  { prop: 'word', label: '敏感词内容' },
  { prop: 'createTime', label: '创建时间' }
])
</script>

<style scoped>

</style>