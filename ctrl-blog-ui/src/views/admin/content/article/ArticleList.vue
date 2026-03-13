<template>
  <div>
    <AdminSearchBar :on-search="loadData" :on-reset="resetSearch" :batch-delete-api="batchDeleteArticleApi" :selected-ids="selectedIds" batchDeleteTip="确定批量删除选中的文章吗？" @batch-delete-success="loadData">
      <template #search-items>
        <el-input v-model="query.title" placeholder="请输入标题查询" prefix-icon="Search" clearable @clear="loadData" />
        <el-select v-model="query.status" placeholder="文章状态" clearable>
          <el-option label="已发布" :value="1" /><el-option label="草稿" :value="0" />
        </el-select>
        <el-select v-model="query.isTop" placeholder="置顶状态" clearable>
          <el-option label="已置顶" :value="1" /><el-option label="未置顶" :value="0" />
        </el-select>
        <el-select v-model="query.categoryId" placeholder="请选择文章分类" clearable>
          <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="query.tagIds" multiple clearable collapse-tags placeholder="请选择文章标签" :max-collapse-tags="2">
          <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </template>
      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增文章</el-button>
      </template>
    </AdminSearchBar>

    <div class="card" v-loading="loading">
      <AdminTable :table-data="tableData" :columns="articleColumns" :expandable="true" v-model:selectedIds="selectedIds" :delete-api="deleteArticleApi" delete-tip="确定删除该文章吗？" @edit="handleEdit" @delete-success="loadData">
        <template #expand="{ row }">
          <el-row><el-form-item label="文章摘要："><span class="expand-value-box">{{ row.summary || '暂无摘要' }}</span></el-form-item></el-row>
          <el-row>
            <el-form-item label="文章标签：">
              <div class="tag-wrapper">
                <template v-if="row.tagNames && row.tagNames.length > 0">
                  <el-tag v-for="tag in row.tagNames" :key="tag" size="small">{{ tag }}</el-tag>
                </template>
                <span v-else class="empty-tag-text">无标签</span>
              </div>
            </el-form-item>
          </el-row>
          <el-row>
            <el-form-item label="附加属性：">
              <div class="attr-wrapper">
                <el-tag size="small" :type="row.isTop === 1 ? 'danger' : 'info'">置顶: {{ row.isTop === 1 ? '是' : '否' }}</el-tag>
                <el-tag size="small" :type="row.isCarousel === 1 ? 'success' : 'info'">首页轮播: {{ row.isCarousel === 1 ? '是' : '否' }}</el-tag>
                <el-tag size="small" :type="row.isAiSummary === 1 ? 'primary' : 'info'" v-if="row.summary">摘要来源: {{ row.isAiSummary === 1 ? 'AI 自动生成' : '人工编写' }}</el-tag>
              </div>
            </el-form-item>
          </el-row>
        </template>
      </AdminTable>
      <AdminPagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="handlePageChange" />
    </div>

    <el-dialog v-model="dialog.visible.value" :title="dialog.title.value" class="dialog-lg-down" width="800px">
      <el-form ref="formRef" :model="dialog.rowData.value" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="24"><el-form-item label="标题" prop="title"><el-input v-model="dialog.rowData.value.title" placeholder="请输入文章标题" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="封面" prop="cover">
              <div class="cover-upload-container">
                <el-image
                    v-if="dialog.rowData.value.cover"
                    :src="dialog.rowData.value.cover"
                    class="cover-preview-img"
                    :preview-src-list="[dialog.rowData.value.cover]"
                    fit="cover"
                />
                <div v-else class="cover-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>

                <div class="upload-actions">
                  <el-upload
                      action="#"
                      :http-request="customUploadCover"
                      :show-file-list="false"
                      :before-upload="beforeUpload"
                  >
                    <el-button type="primary" plain>选择封面</el-button>
                  </el-upload>
                  <div class="upload-tip">建议尺寸 800x450 像素 (16:9比例)，支持 jpg、png 格式，大小不超过 10MB。</div>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="摘要" prop="summary">
              <el-input v-model="dialog.rowData.value.summary" type="textarea" :rows="3" placeholder="请输入文章摘要" maxlength="255" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="dialog.rowData.value.categoryId" placeholder="请选择分类">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="标签" prop="tagIds">
              <el-select v-model="dialog.rowData.value.tagIds" multiple placeholder="请选择标签">
                <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6" :md="6"><el-form-item label="是否置顶" prop="isTop"><el-switch v-model="dialog.rowData.value.isTop" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :xs="12" :sm="6" :md="6"><el-form-item label="首页轮播" prop="isCarousel"><el-switch v-model="dialog.rowData.value.isCarousel" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :xs="12" :sm="6" :md="6"><el-form-item label="是否发布" prop="status"><el-switch v-model="dialog.rowData.value.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :xs="12" :sm="6" :md="6">
            <el-form-item label="AI摘要" prop="isAiSummary">
              <el-switch v-model="dialog.rowData.value.isAiSummary" :active-value="1" :inactive-value="0" inline-prompt :loading="summaryLoading" @change="handleAiSwitchChange" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="文章内容" prop="content">
              <MdEditor v-model="dialog.rowData.value.content" :theme="isDark ? 'dark' : 'light'" :onUploadImg="onUploadImg" :preview="false" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { useDark } from '@vueuse/core';
import { useUserStore } from '@/store/user.js';
import { uploadWangImage, uploadFile } from '@/api/common/file.js';
import { addArticle, deleteArticle, deleteArticles, generateArticleSummary, getArticleById, getArticlePage, updateArticle } from "@/api/admin/content/article.js";
import { getCategoryList } from "@/api/admin/content/category.js";
import { getTagList } from "@/api/admin/content/tag.js";
import AdminPagination from "@/components/admin/AdminPagination.vue";

// ======= 引入 Hook =======
import { useTable } from "@/composables/useTable.js";
import { useDialog } from "@/composables/useDialog.js";
import { useRequest } from "@/composables/useRequest.js";

const isDark = useDark();
const userStore = useUserStore();

const formRef = ref(null);
const selectedIds = ref([]);
const categoryOptions = ref([]);
const tagOptions = ref([]);

const rules = {
  cover: [{ type: 'url', message: '封面URL格式不合法', trigger: 'blur' }, { max: 500, message: '封面URL长度不能超过500个字符', trigger: 'blur' }],
  title: [{ required: true, message: '文章标题不能为空', trigger: 'blur' }, { max: 255, message: '文章标题长度不能超过255个字符', trigger: 'blur' }],
  summary: [{ max: 255, message: '文章摘要长度不能超过255个字符', trigger: 'blur' }],
  content: [{ required: true, message: '文章内容不能为空', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  tagIds: [{ type: 'array', required: true, message: '请选择标签', trigger: 'change' }, { type: 'array', max: 3, message: '标签ID数量不能大于3个', trigger: 'change' }]
};

// ===================== Hooks 初始化 =====================
// 1. 数据包装函数，处理 tagNames
const fetchApi = async (params) => {
  const res = await getArticlePage(params);
  if (res.code === 200 && res.data.records) {
    res.data.records.forEach(item => {
      item.tagNames = item.tags && Array.isArray(item.tags) ? item.tags.map(tag => tag.name) : [];
    });
  }
  return res;
};

// 2. 接管表格分页和数据
const { loading, list: tableData, total, query, loadData, handlePageChange, resetQuery } = useTable(fetchApi, { title: '', categoryId: null, tagIds: [], status: null, isTop: null });

// 3. 接管弹窗和表单数据
const dialog = useDialog();

// 4. 接管提交状态
const { loading: submitLoading, execute: execSubmit } = useRequest((data) => data.id ? updateArticle(data) : addArticle(data), { successMsg: '操作成功' });
const { loading: summaryLoading, execute: execSummary } = useRequest(generateArticleSummary);

onMounted(() => {
  loadOptions();
  loadData();
});

const loadOptions = async () => {
  const [catRes, tagRes] = await Promise.all([getCategoryList(), getTagList()]);
  if (catRes.code === 200) categoryOptions.value = catRes.data;
  if (tagRes.code === 200) tagOptions.value = tagRes.data;
};

const resetSearch = () => { resetQuery(); selectedIds.value = []; };

const handleAdd = () => {
  dialog.openDialog('新增文章', 'add', { isTop: 0, isCarousel: 0, status: 1, isAiSummary: 0, tagIds: [] });
  nextTick(() => formRef.value?.clearValidate());
};

const handleEdit = async (row) => {
  const res = await getArticleById(row.id);
  if (res.code === 200) {
    const formData = res.data;
    formData.tagIds = Array.isArray(formData.tags) ? formData.tags.map(t => Number(t.id)) : [];
    formData.isTop = formData.isTop ?? 0;
    formData.status = formData.status ?? 0;
    formData.isAiSummary = formData.isAiSummary ?? 0;
    if (formData.categoryId) formData.categoryId = Number(formData.categoryId);

    dialog.openDialog('编辑文章', 'edit', formData);
    nextTick(() => formRef.value?.clearValidate());
  }
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      await execSubmit({ ...dialog.rowData.value, userId: userStore.userId });
      dialog.closeDialog();
      await loadData();
    }
  });
};

const deleteArticleApi = async (id) => await deleteArticle(id);
const batchDeleteArticleApi = async (ids) => deleteArticles(ids);

// ===================== 图片与 AI 相关逻辑 =====================

// 自定义上传封面逻辑
const customUploadCover = async (options) => {
  const formData = new FormData();
  formData.append('file', options.file);

  try {
    const res = await uploadFile(formData, 'cover');
    if (res.code === 200) {
      dialog.rowData.value.cover = res.data;
      ElMessage.success('封面上传成功');
      options.onSuccess(res);
    } else {
      ElMessage.error('封面上传失败：' + res.msg);
      options.onError(new Error(res.msg));
    }
  } catch (error) {
    options.onError(error);
  }
};

const beforeUpload = (file) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片格式文件！'); return false; }
  if (file.size / 1024 / 1024 > 10) { ElMessage.error('图片大小不能超过10MB！'); return false; }
  return true;
};

const onUploadImg = async (files, callback) => {
  const formData = new FormData();
  formData.append('file', files[0]);
  try {
    const res = await uploadWangImage(formData);
    if (res.code === 200) {
      callback([res.data?.[0]?.url || res.data]);
    } else {
      ElMessage.error('图片上传失败：' + res.msg);
    }
  } catch (error) {
    callback([]);
  }
};

const handleAiSwitchChange = async (val) => {
  if (val === 0) return;
  if (!dialog.rowData.value.title || !dialog.rowData.value.content) {
    ElMessage.warning('请先填写文章标题和内容，才能生成摘要');
    dialog.rowData.value.isAiSummary = 0;
    return;
  }
  if (dialog.rowData.value.summary && dialog.rowData.value.summary.trim() !== '') {
    try {
      await ElMessageBox.confirm('当前摘要已有内容，AI 生成将覆盖原有内容，是否继续？', '覆盖确认', {type: 'warning'});
      await generateSummary();
    } catch { dialog.rowData.value.isAiSummary = 0; }
  } else {
    await generateSummary();
  }
};

const generateSummary = async () => {
  try {
    const resData = await execSummary({ title: dialog.rowData.value.title, content: dialog.rowData.value.content });
    if (resData) {
      dialog.rowData.value.summary = resData;
      ElMessage.success('AI 摘要生成成功');
    }
  } catch (error) {
    dialog.rowData.value.isAiSummary = 0; // 失败回弹
  }
};

watch(() => dialog.rowData.value.summary, (newVal) => {
  if (dialog.rowData.value.isAiSummary === 1 && (!newVal || newVal.trim() === '')) {
    dialog.rowData.value.isAiSummary = 0;
    ElMessage.info('摘要已清空，自动关闭 AI 摘要标识');
  }
});

const articleColumns = reactive([
  { type: 'cover', prop: 'cover', label: '封面', minWidth: '130px' },
  { type: 'title', prop: 'title', label: '标题', minWidth: '220px' },
  { prop: 'userNickname', label: '作者' },
  { prop: 'categoryName', label: '分类' },
  { prop: 'viewCount', label: '浏览量' },
  { type: 'status', prop: 'status', label: '状态', statusMap: { 1: { text: '发布' }, 0: { text: '草稿' } } },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
]);
</script>

<style scoped>
/* --- 封面上传区域样式优化 --- */
.cover-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cover-preview-img, .cover-placeholder {
  width: 160px;
  height: 90px; /* 16:9 比例 */
  border-radius: 6px; /* 文章封面通常用小圆角矩形 */
  border: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  box-shadow: var(--el-box-shadow-light);
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-placeholder);
  font-size: 32px;
}

.upload-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

/* --- 展开行样式 --- */
.tag-wrapper {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-tag-text {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.attr-wrapper {
  display: flex;
  gap: 10px;
}
</style>