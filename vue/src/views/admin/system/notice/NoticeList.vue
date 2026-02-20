<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteNoticeApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的公告吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.content"
            placeholder="请输入内容查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
        <el-select v-model="data.status" placeholder="请选择是否发布">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
        <el-select v-model="data.isTop" placeholder="请选择是否置顶">
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增公告</el-button>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="noticeColumns"
          v-model:selectedIds="data.selectedIds"
          :delete-api="deleteNoticeApi"
          delete-tip="确定删除该公告吗？"
          @edit="handleEdit"
          @delete-success="loadPage"
      />

      <Pagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>

    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑公告' : '新增公告'" class="dialog-lg-down" width="800px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="12" :md="12">
            <el-form-item label="是否置顶" prop="isTop">
              <el-switch
                  v-model="data.form.isTop"
                  :active-value="1"
                  :inactive-value="0"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="12" :md="12">
            <el-form-item label="是否发布" prop="status">
              <el-switch
                  v-model="data.form.status"
                  :active-value="1"
                  :inactive-value="0"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="公告内容" prop="content">
          <MdEditor
              v-model="data.form.content"
              :preview="false"
              style="width: 100%; max-width: 100%;"
          />
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
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
// 引入 API 模块
import {
  getNoticePage,
  addNotice,
  updateNotice,
  deleteNotice,
  deleteNotices,
  getNoticeById
} from "@/api/admin/notice.js";
import Pagination from "@/components/common/Pagination.vue";

const formRef = ref(null);

const data = reactive({
  content: '',
  status:null,
  isTop:null,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: [],
  rules: {
    content: [
      { required: true, message: '公告内容不能为空', trigger: 'blur' }
    ]
  }
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    content: data.content,
    status: data.status,
    isTop: data.isTop,
  };
  getNoticePage(params).then(res => {
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
  data.content = "";
  data.status = null;
  data.isTop = null;
  data.pageNum = 1;
  loadPage();
};

// 新增按钮逻辑
const handleAdd = () => {
  data.form = {
    isTop: 0,
  };
  data.formVisible = true;
};

// 编辑按钮逻辑
const handleEdit = async (row) => {
  try {
    const res = await getNoticeById(row.id);

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
      const apiMethod = data.form.id ? updateNotice : addNotice;
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
const deleteNoticeApi = async (id) => {
  const res = await deleteNotice(id);
  return res;
};

// 批量删除
const batchDeleteNoticeApi = async (selectedIds) => {
  return deleteNotices(selectedIds);
};

// 公告列表列配置
const noticeColumns = reactive([
  { type: 'html', prop: 'contentHtml', label: '内容' },
  { type: 'top', prop: 'isTop', label: '置顶' },
  { type: 'status', prop: 'status', label: '状态',
    statusMap: {
      1: { text: '发布' },
      0: { text: '隐藏' }
    }
  },
  { prop: 'createTime', label: '创建时间' }
])
</script>

<style scoped>

</style>