<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteCommentApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的评论吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-input v-model="query.userNickname" placeholder="请输入用户昵称查询" prefix-icon="Search" clearable @clear="loadData" />
        <el-input v-model="query.articleTitle" placeholder="请输入文章标题查询" prefix-icon="Search" clearable @clear="loadData" />
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="commentColumns" :expandable="true" v-model:selectedIds="selectedIds" :editable="false" :delete-api="deleteCommentApi" delete-tip="确定删除该评论吗？" @delete-success="loadData">
        <template #expand="{ row }">
          <el-row v-if="row.replyUserNickname">
            <el-form-item label="引用内容：">
              <div class="expand-value-box reply-quote-box">
                <span class="reply-target">@{{ row.replyUserNickname }}</span>：{{ row.replyContent || '原评论已被删除或为空' }}
              </div>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="完整评论："><div class="expand-value-box">{{ row.content }}</div></el-form-item>
          </el-row>
        </template>
      </AdminTable>

      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { getCommentPage, deleteComment, deleteComments } from "@/api/admin/operation/comment.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";

const selectedIds = ref([]);

// Hooks 集成
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(getCommentPage, { userNickname: '', articleTitle: '' });

onMounted(() => loadData());

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const deleteCommentApi = async (id) => deleteComment(id);
const batchDeleteCommentApi = async (ids) => deleteComments(ids);

const commentColumns = reactive([
  { type: 'avatar', prop: 'userAvatar', label: '用户头像' },
  { prop: 'userNickname', label: '用户昵称' },
  { type: 'reply', prop: 'replyUserNickname', contentProp: 'replyContent', label: '回复对象' },
  { prop: 'articleTitle', align: 'left', label: '文章标题', showOverflowTooltip: true },
  { prop: 'content', minWidth: '200px', align: 'left', label: '评论内容', showOverflowTooltip: true },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>

<style scoped>
.reply-quote-box { border-left: 3px solid var(--el-border-color-darker) !important; background-color: var(--el-fill-color-light) !important; color: var(--el-text-color-regular); font-size: 13px; }
.reply-target { color: var(--el-color-primary); font-weight: 500; }
</style>

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