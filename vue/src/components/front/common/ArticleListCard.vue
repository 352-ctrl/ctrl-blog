<template>
  <el-row v-for="item in articles" :key="item.id">
    <el-col @click="handleCardClick(item.id)">
      <el-card class="article-card" shadow="hover">
        <div class="content-wrapper">
          <div class="image-container">
            <el-image
                :src="item.cover"
                class="article-image"
                fit="cover"
            />
          </div>

          <div class="text-content">
            <div class="article-title">{{ item.title }}</div>
            <div class="article-summary">{{ item.summary }}</div>
          </div>
        </div>

        <hr class="divider"/>

        <div class="meta-info">
          <el-avatar :size="25" :src="item.userAvatar"/>
          {{ item.userNickname }}
          {{ item.createTime }}
          <el-icon><View /></el-icon>{{ item.viewCount }}

          <div style="flex: 1"></div>

          <div>
            <el-tag @click.stop="handleCategoryClick(item.categoryId)">
              <span>{{ item.categoryName }}</span>
            </el-tag>
          </div>
        </div>
      </el-card>

    </el-col>
  </el-row>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

/**
 * @typedef {Object} ArticleItem
 * @property {number} id - 文章唯一 ID
 * @property {string} cover - 封面图 URL
 * @property {string} title - 标题
 * @property {string} summary - 摘要内容
 * @property {string} userAvatar - 作者头像 URL
 * @property {string} userNickname - 作者昵称
 * @property {string} createTime - 发布时间
 * @property {number} viewCount - 浏览量
 * @property {number} categoryId - 所属分类ID
 * @property {string} categoryName - 所属分类名称
 */

/**
 * =========================================================================
 * Props 定义
 * =========================================================================
 */
const props = defineProps({
  /**
   * 文章数据列表
   * @type {ArticleItem[]}
   */
  articles: {
    type: Array,
    default: () => []
  }
})

/**
 * =========================================================================
 * Events 定义
 * =========================================================================
 */
const emit = defineEmits([
  /**
   * 点击卡片时触发，用于跳转详情页
   * @arg {number} id - 文章 ID
   */
  'click'
])

// 内部处理卡片点击逻辑
const handleCardClick = (articleId) => {
  emit('click', articleId)
}

// 处理标签点击逻辑
const handleCategoryClick = (categoryId) => {
  // 如果 ID 不存在，防止报错
  if (!categoryId) return;

  router.push({
    name: 'FrontCategories',
    query: { id: categoryId }
  });
}
</script>

<style scoped>
.content-wrapper {
  display: flex;
  align-items: flex-start; /* 顶部对齐，防止摘要过少时布局塌陷 */
  padding-bottom: 5px;
  min-height: 100px;
}

.image-container {
  flex: 0 0 auto; /* 禁止缩放，固定左侧封面区域 */
  padding-right: 10px;
}

.article-image {
  border-radius: 5px;
  width: 100px;
  height: 100px;
  object-fit: cover; /* 保持比例裁剪 */
}

.text-content {
  flex: 1;
  align-self: stretch; /* 垂直方向充满父容器 */
  display: flex;
  flex-direction: column;
}

.article-title {
  font-weight: bold;
  font-size: 25px;
  margin-bottom: 8px;
}

.article-summary {
  color: #666;
  font-size: 15px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3; /* 多行文本截断：最多显示3行 */
  -webkit-box-orient: vertical;
}

.meta-info {
  display: flex;
  align-items: center;
  padding-top: 5px;
}
</style>