<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteCommentApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的评论吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.userNickname"
            placeholder="请输入用户昵称查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
        <el-input
            v-model="data.articleTitle"
            placeholder="请输入文章标题查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
      </template>

      <template #operate-buttons>
      </template>
    </AdminSearchBar>
    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="commentColumns"
          :expandable="true"   v-model:selectedIds="data.selectedIds"
          :editable="false"
          :delete-api="deleteCommentApi"
          delete-tip="确定删除该评论吗？"
          @delete-success="loadPage"
      >
        <template #expand="{ row }">
          <el-row v-if="row.replyUserNickname">
            <el-form-item label="引用内容：">
              <div class="expand-value-box reply-quote-box">
                <span class="reply-target">@{{ row.replyUserNickname }}</span>：
                {{ row.replyContent || '原评论已被删除或为空' }}
              </div>
            </el-form-item>
          </el-row>

          <el-row>
            <el-form-item label="完整评论：">
              <div class="expand-value-box">
                {{ row.content }}
              </div>
            </el-form-item>
          </el-row>
        </template>
      </AdminTable>

      <AdminPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>

  </div>
</template>

<script setup>
import {reactive, ref, onMounted, computed} from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
// 引入 API 模块
import {getCommentPage,deleteComment,deleteComments} from "@/api/admin/comment.js";
import AdminPagination from "@/components/admin/AdminPagination/AdminPagination.vue";

const data = reactive({
  userNickname: '',
  articleTitle:'',
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: []
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    userNickname: data.userNickname,
    articleTitle: data.articleTitle,
  };
  getCommentPage(params).then(res => {
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
  data.userNickname = "";
  data.articleTitle = "";
  data.pageNum = 1;
  loadPage();
};

// 指定删除
const deleteCommentApi = async (id) => {
  return deleteComment(id);
};

// 批量删除
const batchDeleteCommentApi = async (selectedIds) => {
  return deleteComments(selectedIds);
};

// 评论列表列配置
const commentColumns = reactive([
  { type: 'avatar', prop: 'userAvatar', label: '用户头像' },
  { prop: 'userNickname', label: '用户昵称' },
  { type: 'reply', prop: 'replyUserNickname', contentProp: 'replyContent', label: '回复对象' },
  { prop: 'articleTitle', align: 'left', label: '文章标题', showOverflowTooltip: true },
  { prop: 'content', minWidth: '200px', align: 'left', label: '评论内容', showOverflowTooltip: true },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
])
</script>

<style scoped>
.reply-quote-box {
  border-left: 3px solid var(--el-border-color-darker) !important;
  background-color: var(--el-fill-color-light) !important;
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.reply-target {
  color: var(--el-color-primary);
  font-weight: 500;
}
</style>