<template>
  <div style="padding-top: 10px">
    <div style="cursor: pointer">
      <el-carousel v-if="data.carouselList.length > 0" height="400px" motion-blur>
        <el-carousel-item style="border-radius: 10px" v-for="item in data.carouselList" :key="item"  @click="navToArticle(item.id)">
          <el-image :src="item.cover"/>
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
    <el-tabs v-model="data.activeTab" @tab-change="handleTabChange" style="padding-top: 20px">
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

    <Pagination
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :total="data.total"
        @change="loadPage"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, reactive} from "vue";
import { useRouter } from "vue-router";
import {getArticleCarousel, getArticlePage} from "@/api/front/article.js";
import {getCategoryList} from '@/api/front/category.js'
import {ElMessage} from "element-plus";

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
  // 如果有分类ID，添加到查询参数
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
  // 重置页码到第一页
  data.pageNum = 1;

  if (tabName === 'all') {
    // 切换到全部文章
    data.categoryId = null;
  } else {
    // 切换到指定分类
    data.categoryId = tabName;
  }

  // 重新加载文章
  loadPage();
};
</script>


