<template>
  <div class="home-container">
    <div class="carousel-wrapper">
      <el-carousel v-if="data.carouselList.length > 0" height="400px" motion-blur class="custom-carousel">
        <el-carousel-item
            v-for="item in data.carouselList"
            :key="item.id"
            class="carousel-item-box"
            @click="navToArticle(item.id)"
        >
          <el-image :src="item.cover" class="carousel-image" />

          <div class="carousel-overlay">
            <div class="carousel-title">
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

    <div class="pagination-wrapper">
      <FrontPagination
          v-model:current-page="data.pageNum"
          v-model:page-size="data.pageSize"
          :total="data.total"
          @change="loadPage"
      />
    </div>
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
/* ====================================
   全局外层容器
   ==================================== */
.home-container {
  padding-top: 10px;
}

/* ====================================
   轮播图区域样式
   ==================================== */
.carousel-wrapper {
  cursor: pointer;
}

/* 覆盖 el-carousel 的默认样式，增加圆角和悬浮动画 */
.custom-carousel {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--el-box-shadow-light);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.custom-carousel:hover {
  box-shadow: var(--el-box-shadow);
}

.carousel-item-box {
  border-radius: 12px;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

/* 鼠标悬浮时，背景图极其轻微地放大，增加沉浸感 */
.carousel-item-box:hover .carousel-image {
  transform: scale(1.02);
}

/* 底部渐变黑膜（不受暗黑模式影响，因为图片永远是亮的） */
.carousel-overlay {
  position: absolute;
  height: 150px;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px 40px; /* 优化了内边距，使其更协调 */
  background: linear-gradient(to top,
  rgba(0, 0, 0, 0.8) 0%,
  rgba(0, 0, 0, 0.5) 40%,
  rgba(0, 0, 0, 0) 100%
  );
  display: flex;
  align-items: flex-end; /* 让标题贴紧底部边缘 */
  pointer-events: none; /* 让鼠标点击穿透遮罩层 */
}

.carousel-title {
  font-size: 36px;
  color: #ffffff; /* 必须是纯白，与黑膜形成高对比 */
  font-weight: bold;
  line-height: 1.3;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5); /* 增加文字阴影，边缘更清晰 */

  /* 防止标题过长换行破坏布局 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

/* ====================================
   Tabs 分类导航美化
   ==================================== */
.custom-front-tabs {
  margin-top: 30px;
  margin-bottom: 10px;
}

:deep(.custom-front-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.custom-front-tabs .el-tabs__item) {
  font-size: 16px;
  color: var(--el-text-color-regular); /* 自动适配暗黑模式 */
  padding: 0 20px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

:deep(.custom-front-tabs .el-tabs__item:hover) {
  color: var(--el-color-primary);
}

:deep(.custom-front-tabs .el-tabs__item.is-active) {
  font-size: 17px;
  font-weight: 600;
  color: var(--el-text-color-primary); /* 选中时使用主文本色 */
}

:deep(.custom-front-tabs .el-tabs__active-bar) {
  height: 4px;
  border-radius: 2px;
  background-color: var(--el-color-primary);
  box-shadow: 0 2px 6px var(--el-color-primary-light-5);
}

/* ====================================
   分页区域
   ==================================== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 30px;
}

/* ====================================
   移动端响应式适配
   ==================================== */
@media screen and (max-width: 768px) {
  :deep(.custom-carousel) {
    height: 220px !important; /* 手机端减小轮播图高度 */
  }

  .carousel-overlay {
    height: 100px;
    padding: 15px 20px;
  }

  .carousel-title {
    font-size: 20px; /* 手机端缩小标题字号 */
  }

  :deep(.custom-front-tabs .el-tabs__item) {
    padding: 0 12px;
    font-size: 14px;
  }

  :deep(.custom-front-tabs .el-tabs__item.is-active) {
    font-size: 15px;
  }
}
</style>