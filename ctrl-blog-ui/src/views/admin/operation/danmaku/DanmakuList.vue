<template>
  <div>
    <AdminSearchBar
        :on-search="loadData"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteDanmakuApi"
        :selected-ids="selectedIds"
        batchDeleteTip="确定批量删除选中的弹幕吗？"
        @batch-delete-success="loadData"
    >
      <template #search-items>
        <el-input v-model="query.nickname" placeholder="请输入用户昵称查询" prefix-icon="Search" clearable @clear="loadData" />
        <el-input v-model="query.content" placeholder="请输入弹幕内容查询" prefix-icon="Search" clearable @clear="loadData" />
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable
          :table-data="tableData"
          :columns="danmakuColumns"
          v-model:selectedIds="selectedIds"
          :editable="false"
          :delete-api="deleteDanmakuApi"
          delete-tip="确定删除该弹幕吗？"
          @delete-success="loadData"
      />

      <AdminPagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { getDanmakuPage, deleteDanmaku, deleteDanmakus } from "@/api/admin/operation/danmaku.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";
import { useTable } from "@/composables/useTable.js";

// 选中的行 ID 数组，用于批量删除
const selectedIds = ref([]);

// 接入 useTable Hook 处理列表拉取、分页和加载状态
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(
    getDanmakuPage,
    { nickname: '', content: '' } // 对应 DanmakuQueryDTO 的查询字段
);

onMounted(() => {
  loadData();
});

// 重置搜索
const resetSearch = () => {
  resetQuery();
  selectedIds.value = [];
};

// API 包装：单删与批量删除
const deleteDanmakuApi = async (id) => deleteDanmaku(id);
const batchDeleteDanmakuApi = async (ids) => deleteDanmakus(ids);

// 表格列配置
const danmakuColumns = reactive([
  { type: 'avatar', prop: 'avatar', label: '用户头像' },
  { prop: 'nickname', label: '用户昵称', minWidth: '120px' },
  { prop: 'content', align: 'left', label: '弹幕内容', showOverflowTooltip: true, minWidth: '250px' },
  { prop: 'createTime', label: '发送时间', minWidth: '160px' }
]);
</script>

<style scoped>

</style>