<template>
  <el-row v-for="item in articles" :key="item.id" class="article-row">
    <el-col @click="handleCardClick(item.id)">

      <el-card class="article-card" shadow="hover">
        <div class="content-wrapper">
          <div class="image-container">
            <el-image
                :src="item.cover || defaultCoverUrl"
                class="article-image"
                fit="cover"
                lazy
            >
              <template #error>
                <div class="image-error-slot">
                  <img :src="defaultCoverUrl" alt="默认封面" class="fallback-img" />
                </div>
              </template>
            </el-image>
          </div>

          <div class="text-content">
            <div class="article-title">
              <el-tag
                  v-if="item.isTop === 1"
                  class="top-tag"
                  type="danger"
                  effect="light"
                  size="small"
              >
                <div class="top-tag-content">
                  <el-icon><Promotion /></el-icon>
                  <span>置顶</span>
                </div>
              </el-tag>
              <span class="title-text" :title="item.title">{{ item.title }}</span>
            </div>
            <div class="article-summary" :title="item.summary">{{ item.summary }}</div>
          </div>
        </div>

        <el-divider class="custom-divider" />

        <div class="meta-info">
          <div class="meta-left">
            <el-avatar :size="24" :src="item.userAvatar" class="avatar">
              <el-icon><User /></el-icon>
            </el-avatar>

            <span class="nickname">{{ item.userNickname }}</span>

            <el-divider direction="vertical" border-style="dashed" />

            <span class="meta-item">
              <el-icon class="meta-icon"><Clock /></el-icon>
              <span class="meta-text">{{ formatTimeAgo(item.createTime) }}</span>
            </span>

            <el-divider direction="vertical" border-style="dashed" />

            <span class="meta-item">
              <el-icon class="meta-icon"><View /></el-icon>
              <span class="meta-text">{{ item.viewCount }}</span>
            </span>
          </div>

          <div class="meta-right" @click.stop>
            <el-tag
                class="category-tag"
                effect="plain"
                round
                @click="handleCategoryClick(item.categoryId)"
            >
              {{ item.categoryName }}
            </el-tag>
          </div>
        </div>
      </el-card>

    </el-col>
  </el-row>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { formatTimeAgo } from '@/utils/date.js';
import defaultCoverUrl from '@/assets/images/default-cover.png';

const router = useRouter()

const props = defineProps({
  articles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['click'])

const handleCardClick = (articleId) => {
  emit('click', articleId)
}

const handleCategoryClick = (categoryId) => {
  if (!categoryId) return;
  router.push({
    name: 'FrontCategories',
    query: { id: categoryId }
  });
}
</script>

<style scoped>
/* ==================== 1. 卡片整体与交互效果 ==================== */
.article-row {
  margin-bottom: 20px;
}

.article-card {
  cursor: pointer;
  border-radius: 8px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  background-color: var(--el-bg-color-overlay);
  border-color: var(--el-border-color-light);
}

:deep(.el-card__body) {
  padding: 20px;
}

.article-card:hover {
  transform: translateY(-2px);
}

.article-card:active {
  transform: scale(0.99);
  transition: transform 0.1s ease;
}

/* ==================== 2. 顶部内容区（图片与文本） ==================== */
.content-wrapper {
  display: flex;
  align-items: flex-start;
}

.image-container {
  flex: 0 0 auto;
  padding-right: 20px;
}

.article-image {
  border-radius: 6px;
  width: 172px;
  height: 90px;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.image-error-slot {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.fallback-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.text-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 90px;
  justify-content: space-between;
  overflow: hidden;
}

.article-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: -2px;
  margin-bottom: 8px;
}

.title-text {
  font-family: 'SmileySans', sans-serif;
  color: var(--el-text-color-primary);
  font-weight: 600;
  font-size: 18px;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-tag {
  cursor: default;
  border-radius: 4px;
  padding: 0 6px;
}

.top-tag:hover {
  transform: none;
}

.top-tag-content {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.top-tag-content .el-icon {
  font-size: 12px;
}

.article-summary {
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.6;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.custom-divider {
  margin: 16px 0;
  border-top-color: var(--el-border-color-lighter);
}

/* ==================== 3. 底部元信息区（头像、图标、标签） ==================== */
.meta-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.meta-left {
  display: flex;
  align-items: center;
}

.avatar {
  margin-right: 8px;
}

.nickname {
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  font-size: 15px;
}

.category-tag {
  transition: all 0.2s ease;
  cursor: pointer;
}

.category-tag:hover {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
  transform: translateY(-1px);
}
</style>