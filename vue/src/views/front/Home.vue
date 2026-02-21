<template>
  <div style="padding-top: 10px">
    <div style="cursor: pointer">
      <el-carousel v-if="data.carouselList.length > 0" height="400px" motion-blur>
        <el-carousel-item style="border-radius: 10px" v-for="item in data.carouselList" :key="item"  @click="navToArticle(item.id)">
          <el-image :src="item.cover" style="width: 100%; height: 100%; object-fit: cover;" />
          <div style="position: absolute;
              border-radius: 0 0 10px 10px;
              height: 150px;
              bottom: 0;
              left: 0;
              right: 0;
              padding: 50px;
              background: linear-gradient(to top,
                rgba(0, 0, 0, 0.8) 0%,
                rgba(0, 0, 0, 0.6) 30%,
                rgba(0, 0, 0, 0.4) 60%,
                rgba(0, 0, 0, 0) 100%
              );"
          >
            <div style="font-size: 40px; color: white; font-weight: bold">
              {{ item.title }}
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <el-tabs
        v-model="data.activeTab"
        @tab-change="handleTabChange"
        class="custom-front-tabs"
    >
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane
          v-for="item in data.categoryList"
          :key="item.id"
          :label="item.name"
          :name="item.id.toString()"
      ></el-tab-pane>
    </el-tabs>

    <ArticleListCard
        :articles="data.articleList"
        @click="navToArticle"
    />

    <FrontPagination
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :total="data.total"
        @change="loadPage"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { getArticleCarousel, getArticlePage } from "@/api/front/article.js";
import { getCategoryList } from '@/api/front/category.js'
import { ElMessage } from "element-plus";

const router = useRouter();

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  activeTab: 'all',
  categoryId: null,
  articleList: [],
  carouselList: [],
  categoryList:[],
  tagList:[],
})

const navToArticle = (id) => {
  router.push({
    path: `/post/${id}`
  });
}

// 初始化加载
onMounted(async () => {
  loadCategories();
  loadCarousel();
  loadPage();
});

// 加载数据
const loadPage = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
  };
  if (data.categoryId) {
    params.categoryId = data.categoryId;
  }
  getArticlePage(params).then(res => {
    if (res.code === 200) {
      data.articleList = res.data.records;
      data.total = res.data.total;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 加载轮播图数据
const loadCarousel = () => {
  getArticleCarousel().then(res => {
    if (res.code === 200) {
      data.carouselList = res.data;
    } else {
      ElMessage.error(res.msg);
    }
  });
}

// 加载分类选项
const loadCategories = () => {
  getCategoryList().then(res => {
    if (res.code === 200) {
      data.categoryList = res.data || [];
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 标签切换处理
const handleTabChange = (tabName) => {
  data.pageNum = 1;
  if (tabName === 'all') {
    data.categoryId = null;
  } else {
    data.categoryId = tabName;
  }
  loadPage();
};
</script>

<style scoped>
/* ==========================================
 * 前台 Tabs 样式定制 (类似掘金、B站的干净风格)
 * ========================================== */

.custom-front-tabs {
  margin-top: 30px;
  margin-bottom: 10px;
}

/* 1. 隐藏 el-tabs 底部自带的贯穿全局的灰色边框线 */
:deep(.custom-front-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

/* 2. 定制每一个 Tab 项的基础样式 */
:deep(.custom-front-tabs .el-tabs__item) {
  font-size: 16px;          /* 调大字号 */
  color: #606266;           /* 默认柔和的灰色 */
  padding: 0 20px;          /* 增加左右间距，让呼吸感更强 */
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 3. 悬浮时的样式 */
:deep(.custom-front-tabs .el-tabs__item:hover) {
  color: var(--el-color-primary);
}

/* 4. 激活选中时的样式 */
:deep(.custom-front-tabs .el-tabs__item.is-active) {
  font-size: 17px;          /* 选中时文字微放大 */
  font-weight: 600;         /* 选中时文字加粗 */
  color: #222226;           /* 选中时使用更深的近黑色或直接用主题色 var(--el-color-primary) */
}

/* 5. 定制底部的激活指示条 (小蓝条) */
:deep(.custom-front-tabs .el-tabs__active-bar) {
  height: 4px;              /* 加粗指示条 */
  border-radius: 2px;       /* 让指示条两端变圆润 */
  background-color: var(--el-color-primary);
  /* 给指示条加一个微弱的弥散发光，和你的 Pagination 保持同一种设计语言 */
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
}
</style>