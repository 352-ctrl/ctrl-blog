<template>
  <div>
    <AdminSearchBar
        :on-search="loadPage"
        :on-reset="resetSearch"
        :batch-delete-api="batchDeleteArticleApi"
        :selected-ids="data.selectedIds"
        batchDeleteTip="确定批量删除选中的用户吗？"
        @batch-delete-success="loadPage"
    >
      <template #search-items>
        <el-input
            v-model="data.title"
            placeholder="请输入标题查询"
            prefix-icon="Search"
            clearable
            @clear="loadPage"
        />
        <el-select
            v-model="data.categoryId"
            placeholder="请选择文章分类"
            clearable
        >
          <el-option
              v-for="item in data.categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
        <el-select
            v-model="data.tagIds"
            multiple
            clearable
            collapse-tags
            placeholder="请选择文章标签"
            :max-collapse-tags="2"
        >
          <el-option
              v-for="item in data.tagOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
      </template>

      <template #operate-buttons>
        <el-button type="primary" icon="Plus" @click="handleAdd">新增文章</el-button>
      </template>
    </AdminSearchBar>

    <div class="card">
      <AdminTable
          :table-data="data.tableData"
          :columns="articleColumns"
          :expandable="true"   v-model:selectedIds="data.selectedIds"
          :delete-api="deleteArticleApi"
          delete-tip="确定删除该文章吗？"
          @edit="handleEdit"
          @delete-success="loadPage"
      >
        <template #expand="{ row }">
          <el-row>
            <el-form-item label="文章摘要：">
              <span class="expand-value-box">{{ row.summary || '暂无摘要' }}</span>
            </el-form-item>
          </el-row>

          <el-row>
            <el-form-item label="文章标签：">
              <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                <template v-if="row.tagNames && row.tagNames.length > 0">
                  <el-tag v-for="tag in row.tagNames" :key="tag" size="small">{{ tag }}</el-tag>
                </template>
                <span v-else style="color: #909399; font-size: 13px;">无标签</span>
              </div>
            </el-form-item>
          </el-row>

          <el-row>
            <el-form-item label="附加属性：">
              <div style="display: flex; gap: 10px;">
                <el-tag size="small" :type="row.isTop === 1 ? 'danger' : 'info'">
                  置顶: {{ row.isTop === 1 ? '是' : '否' }}
                </el-tag>
                <el-tag size="small" :type="row.isCarousel === 1 ? 'success' : 'info'">
                  首页轮播: {{ row.isCarousel === 1 ? '是' : '否' }}
                </el-tag>
                <el-tag size="small" :type="row.isAiSummary === 1 ? 'primary' : 'info'" v-if="row.summary">
                  摘要来源: {{ row.isAiSummary === 1 ? 'AI 自动生成' : '人工编写' }}
                </el-tag>
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

    <el-dialog v-model="data.formVisible" :title="data.form.id ? '编辑文章' : '新增文章'" class="dialog-lg-down" width="800px">
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="标题" prop="title">
              <el-input v-model="data.form.title" placeholder="请输入文章标题" />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="封面">
              <el-upload
                  :action="uploadFileUrl"
                  :on-success="handleFileSuccess"
                  :headers="{ token: userStore.token }"
                  :show-file-list="false"
                  :before-upload="beforeUpload"
              >
                <el-button type="primary">上传封面</el-button>
              </el-upload>
              <div class="cover-preview" v-if="data.form.cover">
                <el-image
                    :src="data.form.cover"
                    style="width: 100px; height: 60px; margin-left: 10px; border-radius: 4px;"
                    :preview-src-list="[data.form.cover]"
                />
              </div>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="摘要" prop="summary">
              <el-input
                  v-model="data.form.summary"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入文章摘要"
                  maxlength="255"
                  show-word-limit
              />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="data.form.categoryId" placeholder="请选择分类">
                <el-option
                    v-for="item in data.categoryOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-form-item label="标签" prop="tagIds">
              <el-select
                  v-model="data.form.tagIds"
                  multiple
                  placeholder="请选择标签"
              >
                <el-option
                    v-for="item in data.tagOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6" :md="6">
            <el-form-item label="是否置顶" prop="isTop">
              <el-switch
                  v-model="data.form.isTop"
                  :active-value="1"
                  :inactive-value="0"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6" :md="6">
            <el-form-item label="首页轮播" prop="isCarousel">
              <el-switch
                  v-model="data.form.isCarousel"
                  :active-value="1"
                  :inactive-value="0"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6" :md="6">
            <el-form-item label="是否发布" prop="status">
              <el-switch
                  v-model="data.form.status"
                  :active-value="1"
                  :inactive-value="0"
              />
            </el-form-item>
          </el-col>

          <el-col :xs="12" :sm="6" :md="6">
            <el-form-item label="AI摘要" prop="isAiSummary">
              <el-switch
                  v-model="data.form.isAiSummary"
                  :active-value="1"
                  :inactive-value="0"
                  inline-prompt
                  :loading="summaryLoading"
                  @change="handleAiSwitchChange"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="文章内容" prop="content">
              <MdEditor
                  v-model="data.form.content"
                  :theme="isDark ? 'dark' : 'light'"
                  :onUploadImg="onUploadImg"
                  :preview="false"
              />
            </el-form-item>
          </el-col>

        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import {nextTick, onMounted, reactive, ref, watch} from "vue";
import {ElMessage} from "element-plus";
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { useDark } from '@vueuse/core';
import { useUserStore } from '@/store/user.js'
// 引入 API 模块
import {
  addArticle,
  deleteArticle,
  deleteArticles, generateArticleSummary,
  getArticleById,
  getArticlePage,
  updateArticle
} from "@/api/admin/article.js";
import {getCategoryList} from "@/api/admin/category.js";
import {getTagList} from "@/api/admin/tag.js";

// 实例化暗黑模式响应式变量
const isDark = useDark();

const userStore = useUserStore()

const BASE_API = 'http://localhost:9999'

// 定义上传接口地址
const uploadFileUrl = `${BASE_API}/api/files/upload`;

const formRef = ref(null);

// loading 状态
const summaryLoading = ref(false);

const data = reactive({
  title: '',
  categoryId: null,
  tagIds: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  formVisible: false,
  form: {},
  selectedIds: [],
  categoryOptions: [],
  tagOptions: [],
  imageUrl:'',
  activeTab: 'edit',  // 默认激活编辑标签页
  rules: {
    cover: [
      { type: 'url', message: '封面URL格式不合法', trigger: 'blur' },
      { max: 500, message: '封面URL长度不能超过500个字符', trigger: 'blur' }
    ],
    title: [
      { required: true, message: '文章标题不能为空', trigger: 'blur' },
      { max: 255, message: '文章标题长度不能超过255个字符', trigger: 'blur' }
    ],
    summary: [
      { max: 255, message: '文章摘要长度不能超过255个字符', trigger: 'blur' }
    ],
    content: [
      { required: true, message: '文章内容不能为空', trigger: 'blur' }
    ],
    categoryId: [
      { required: true, message: '请选择分类', trigger: 'change' }
    ],
    tagIds: [
      { type: 'array', required: true, message: '请选择标签', trigger: 'change' },
      { type: 'array', max: 3, message: '标签ID数量不能大于3个', trigger: 'change' }
    ]
  }
});

const handleFileSuccess = (res) => {
  if (res.code === 200) {
    data.form.cover = res.data; // 绑定封面URL到表单
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error('封面上传失败：' + res.msg);
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/'); // 校验是否为图片格式
  if (!isImage) {
    ElMessage.error('只能上传图片格式文件！');
    return false; // 终止上传
  }
  const isLt10M = file.size / 1024 / 1024 < 10; // 校验图片大小不超过10MB
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB！');
    return false; // 终止上传
  }
  return true; // 校验通过，允许上传
};

// 上传图片处理
const onUploadImg = async (files, callback) => {
  const formData = new FormData();
  formData.append('file', files[0]);

  try {
    const response = await fetch(`${BASE_API}/api/files/wang/upload`, {
      method: 'POST',
      headers: {
        'token': userStore.token
      },
      body: formData
    });
    const result = await response.json();
    if (result.code === 200) {
      const url = result.data?.[0]?.url || result.data;
      callback([url]);
    } else {
      ElMessage.error('图片上传失败：' + result.msg);
    }
  } catch (error) {
    ElMessage.error('图片上传失败：' + error.message);
    callback([]);
  }
};

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    title: data.title,
    categoryId: data.categoryId,
    tagIds: data.tagIds
  };
  getArticlePage(params).then(res => {
    if (res.code === 200) {
      // 1. 获取原始列表
      const records = res.data.records;

      // 2. 数据清洗：把 tags 对象数组转为字符串数组
      records.forEach(item => {
        if (item.tags && Array.isArray(item.tags)) {
          // 提取 name 属性组成新数组，赋值给 tagNames
          item.tagNames = item.tags.map(tag => tag.name);
        } else {
          item.tagNames = [];
        }
      });

      // 3. 赋值给表格数据
      data.tableData = records;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载分类和标签选项
const loadOptions = () => {
  getCategoryList().then(res => {
    if (res.code === 200) {
      data.categoryOptions = res.data;
    } else {
      ElMessage.error('加载分类失败：' + res.msg);
    }
  });

  getTagList().then(res => {
    if (res.code === 200) {
      data.tagOptions = res.data;
    } else {
      ElMessage.error('加载标签失败：' + res.msg);
    }
  });
};

// 初始化加载
onMounted(() => {
  loadOptions();
  loadPage();
});

// 重置搜索
const resetSearch = () => {
  data.title = "";
  data.categoryId = null;
  data.tagIds = [];
  data.pageNum = 1;
  loadPage();
};

// 新增按钮逻辑
const handleAdd = () => {
  data.form = {
    isTop: 0,
    isCarousel: 0,
    status: 1,
    isAiSummary: 0,
    tagIds: []
  };
  data.formVisible = true;
  // 清除校验状态
  if (formRef.value) {
    formRef.value.clearValidate();
  }
};

// 编辑按钮逻辑
const handleEdit = async (row) => {
  try {
    const res = await getArticleById(row.id);

    if (res.code === 200) {
      // 将数据库返回的完整详情赋值给表单
      data.form = res.data;

      // 如果后端返回了 tags 对象数组，需要提取 id 组成数组赋值给 tagIds
      if (Array.isArray(data.form.tags)) {
        data.form.tagIds = data.form.tags.map(tag => tag.id);
      } else if (!Array.isArray(data.form.tagIds)) {
        // 如果 tags 也没数据，且 tagIds 不是数组，初始化为空数组
        data.form.tagIds = [];
      }
      // 确保 ID 类型与 el-select 的 option value 类型一致（通常是数字）
      data.form.tagIds = data.form.tagIds.map(id => Number(id));

      // 数据清洗与空值处理 (移动到成功回调内部)
      // 使用 ?? 运算符处理 null/undefined，0 会被保留
      data.form.isTop = data.form.isTop ?? 0;
      data.form.status = data.form.status ?? 0;
      data.form.viewCount = data.form.viewCount ?? 0;
      data.form.isAiSummary = data.form.isAiSummary ?? 0;

      // 确保 categoryId 类型一致 (防止 Select 回显失败)
      if (data.form.categoryId) {
        data.form.categoryId = Number(data.form.categoryId);
      }

      // 数据获取成功后，再显示弹窗
      data.formVisible = true;

      // 等待 DOM 更新后清除校验
      await nextTick();

      // DOM 更新后清除校验
      if (formRef.value) {
        formRef.value.clearValidate();
      }

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
      // 构造提交数据
      const postData = {
        ...data.form,
        userId: userStore.userId
      }

      const apiMethod = data.form.id ? updateArticle : addArticle;
      apiMethod(postData).then(res => {
        if (res.code === 200) {
          ElMessage.success('操作成功');
          data.formVisible = false;
          loadPage();
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(error => {
        ElMessage.error('操作失败：' + error.message);
      });
    }
  });
};

// 指定删除
const deleteArticleApi = async (id) => {
  return await deleteArticle(id);
};

// 批量删除
const batchDeleteArticleApi = async (ids) => {
  return deleteArticles(ids);
};

// 处理 AI 开关变化
const handleAiSwitchChange = async (val) => {
  // === Case 1: 用户关闭开关 (1 -> 0) ===
  if (val === 0) {
    // 仅提示用户现在转为人工模式
    return;
  }

  // === Case 2: 用户开启开关 (0 -> 1) ===

  // 1. 基础校验：必须有标题和内容
  if (!data.form.title || !data.form.content) {
    ElMessage.warning('请先填写文章标题和内容，才能生成摘要');
    // 回弹开关状态
    data.form.isAiSummary = 0;
    return;
  }

  // 2. 判断摘要是否为空
  if (data.form.summary && data.form.summary.trim() !== '') {
    // 摘要不为空，弹窗确认
    try {
      await ElMessageBox.confirm(
          '当前摘要已有内容，AI 生成将覆盖原有内容，是否继续？',
          '覆盖确认',
          {
            confirmButtonText: '确认覆盖',
            cancelButtonText: '取消',
            type: 'warning',
          }
      );
      // 用户点击确认，执行生成
      await generateSummary();
    } catch (e) {
      // 用户点击取消，回弹开关
      data.form.isAiSummary = 0;
    }
  } else {
    // 摘要为空，直接生成
    await generateSummary();
  }
};

// 封装生成摘要的逻辑
const generateSummary = async () => {
  summaryLoading.value = true;
  try {
    const res = await generateArticleSummary({
      title: data.form.title,
      content: data.form.content
    });

    if (res.code === 200) {
      data.form.summary = res.data;
      ElMessage.success('AI 摘要生成成功');
    } else {
      ElMessage.error(res.msg || '生成失败');
      // 生成失败，回弹开关
      data.form.isAiSummary = 0;
    }
  } catch (error) {
    console.error(error);
    ElMessage.error('请求异常，请稍后重试');
    data.form.isAiSummary = 0;
  } finally {
    summaryLoading.value = false;
  }
};

// 添加 watch 监听器
// 监听 data.form.summary 的变化
watch(
    () => data.form.summary,
    (newVal) => {
      // 如果当前是 AI 摘要模式 (isAiSummary === 1) 且内容被清空
      if (data.form.isAiSummary === 1 && (!newVal || newVal.trim() === '')) {
        data.form.isAiSummary = 0;
        ElMessage.info('摘要已清空，自动关闭 AI 摘要标识');
      }
    }
);

// 文章列表列配置
const articleColumns = reactive([
  { type: 'cover', prop: 'cover', label: '封面' },
  { prop: 'title', label: '标题', showOverflowTooltip: true },
  { prop: 'userNickname', label: '作者' },
  { prop: 'categoryName', label: '分类' },
  { prop: 'viewCount', label: '浏览量' },
  { type: 'status', prop: 'status', label: '状态',
    statusMap: {
      1: { text: '发布' },
      0: { text: '草稿' }
    }
  },
  { prop: 'createTime', label: '创建时间', minWidth: '160px' }
])
</script>

<style scoped>

</style>