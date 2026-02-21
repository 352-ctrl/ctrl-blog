<template>
  <el-card>
    <div class="tag-filter-box">
      <div class="tag-list">
        <el-tag
            class="tag-item"
            :effect="data.activeTab === 'all' ? 'dark' : 'plain'"
            :type="data.activeTab === 'all' ? 'primary' : 'info'"
            size="large"
            @click="handleTagClick('all')"
        >
          全部
        </el-tag>

        <el-tag
            v-for="item in data.tagList"
            :key="item.id"
            class="tag-item"
            :effect="data.activeTab === item.id.toString() ? 'dark' : 'plain'"
            :type="data.activeTab === item.id.toString() ? 'primary' : 'info'"
            size="large"
            @click="handleTagClick(item.id.toString())"
        >
          {{ item.name }}
        </el-tag>
      </div>
    </div>
  </el-card>

  <ArticleListCard
      :articles="data.articleList"
      @click="navToArticle"
  />

  <AdminPagination
      v-model:current-page="data.pageNum"
      v-model:page-size="data.pageSize"
      :total="data.total"
      @change="loadPage"
  />
</template>

<script setup>
import {onMounted, reactive} from "vue";
import {getArticlePage} from "@/api/front/article.js";
import {ElMessage} from "element-plus";
import {getTagList} from "@/api/front/tag.js";
import {getHomepageNotices} from "@/api/front/notice.js";
import {useRoute, useRouter} from "vue-router";

const route = useRoute();
const router = useRouter();

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  activeTab: 'all',
  tagId: '',
  articleList: [],
  tagList:[],
  noticeList:[]
})

const navToArticle = (id) => {
  router.push({
    path: `/post/${id}`
  });
}

// 初始化加载
onMounted(async () => {
  await loadTag();
  // 检查 URL 是否带有 id 参数
  const queryId = route.query.id;
  if (queryId) {
    // 如果有 ID，设置当前激活的 Tab 和查询参数
    data.activeTab = queryId.toString();
    data.tagId = queryId;
  } else {
    // 如果没有，默认全部
    data.activeTab = 'all';
    data.tagId = '';
  }
  await loadPage();
});

// 加载数据
const loadPage = async () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
  };
  // 如果有分类ID，添加到查询参数
  const tagIdNum = Number(data.tagId);
  if (tagIdNum && tagIdNum > 0) {
    params.tagIds = [tagIdNum]; // 单标签ID封装成数组，适配后端List<Integer>
  }
  try {
    // 使用 await 等待接口返回
    const res = await getArticlePage(params);
    if (res.code === 200) {
      data.articleList = res.data.records || [];
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    console.error(error);
  }
};

const loadTag = async () => {
  try {
    const res = await getTagList();
    if (res.code === 200) {
      data.tagList = res.data || [];
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    console.error(error);
  }
};

// 标签切换处理
const handleTagClick = (tagKey) => {
  // 更新选中状态
  data.activeTab = tagKey;

  // 重置页码到第一页
  data.pageNum = 1;

  if (tagKey === 'all') {
    // 切换到全部文章
    data.tagId = null;
  } else {
    // 切换到指定分类
    data.tagId = tagKey;
  }

  // 重新加载文章
  loadPage();
};
</script>

<style scoped>
.tag-list {
  display: flex;       /* 弹性布局 */
  flex-wrap: wrap;     /* 允许换行 */
  gap: 12px;           /* 标签之间的间距 */
  align-items: center;
}

.tag-item {
  cursor: pointer;     /* 鼠标移上去变成手型 */
  border: none;        /* 可选：去掉边框，让样式更简洁 */
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px); /* 可选：悬停时轻微上浮效果 */
}
</style>