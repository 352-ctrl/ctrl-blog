<template>
  <div class="suspended-panel">

    <ArticleLikeButton
        v-if="articleId"
        :articleId="articleId"
        :liked="isLiked"
        :count="likeCount"
        @update:liked="(val) => $emit('update:isLiked', val)"
        @update:count="(val) => $emit('update:likeCount', val)"
    />

    <ArticleCommentButton
        :count="commentCount"
        @click="scrollToComment"
    />

    <ArticleFavoriteButton
        v-if="articleId"
        :articleId="articleId"
        :isFavorite="isFavorite"
        :count="favoriteCount"
        @update:isFavorite="(val) => $emit('update:isFavorite', val)"
        @update:count="(val) => $emit('update:favoriteCount', val)"
    />

    <div class="panel-btn share-btn-wrapper">
      <svg class="sprite-icon"><use xlink:href="#icon-share"></use></svg>
      <div class="share-popup">
        <div class="share-item">微信</div>
        <div class="share-item">微博</div>
        <div class="share-item">QQ</div>
      </div>
    </div>

    <div class="panel-btn">
      <svg class="sprite-icon"><use xlink:href="#icon-report"></use></svg>
    </div>

    <div class="panel-btn">
      <svg class="sprite-icon"><use xlink:href="#icon-immerse"></use></svg>
    </div>

    <svg style="display: none;">
      <symbol id="icon-share" viewBox="0 0 20 20"><path fill="currentColor" fill-rule="evenodd" clip-rule="evenodd" d="M10.4167 6.40399C5.35406 6.40399 1.25 10.5675 1.25 15.7035C1.25 16.0225 1.28448 16.4512 1.35344 16.9898C1.36807 17.1039 1.47243 17.1846 1.58655 17.17C1.6483 17.1621 1.70328 17.127 1.73643 17.0743L1.86865 16.8676C1.99553 16.6731 2.10644 16.5147 2.20137 16.3925C3.69554 14.4692 6.13777 13.3823 9.35515 13.3378L10.4167 13.3364V17.0757C10.4167 17.3101 10.6086 17.5 10.8453 17.5C10.959 17.5 11.068 17.4553 11.1483 17.3757L18.145 10.45C18.3961 10.2015 18.3961 9.79853 18.145 9.55L11.1483 2.62426C10.981 2.45858 10.7096 2.45858 10.5422 2.62426C10.4618 2.70383 10.4167 2.81174 10.4167 2.92426V6.40399Z"></path></symbol>
      <symbol id="icon-report" viewBox="0 0 20 20"><path fill="currentColor" fill-rule="evenodd" clip-rule="evenodd" d="M10.8389 1.89381C11.0873 2.03868 11.2939 2.24532 11.4388 2.49366L18.9369 15.0065C19.4007 15.8015 19.1322 16.8221 18.3371 17.2859C18.0822 17.4346 17.7924 17.5129 17.4973 17.5129H2.50041C1.57993 17.5129 0.83374 16.7667 0.83374 15.8462C0.83374 15.5512 0.912088 15.2614 1.06078 15.0065L8.5595 2.49366C9.0233 1.69857 10.0438 1.43001 10.8389 1.89381ZM10.3118 13.3459C10.6283 13.3459 10.8848 13.6025 10.8848 13.9189V14.4918C10.8848 14.8082 10.6283 15.0647 10.3118 15.0647H9.73893C9.42252 15.0647 9.16602 14.8082 9.16602 14.4918V13.9189C9.16602 13.6025 9.42252 13.3459 9.73893 13.3459H10.3118ZM10.8356 7.09513C10.8356 6.86502 10.6491 6.67847 10.419 6.67847H9.58512C9.35501 6.67847 9.16846 6.86502 9.16846 7.09513V12.0956C9.16846 12.3257 9.35501 12.5123 9.58512 12.5123H10.419C10.6491 12.5123 10.8356 12.3257 10.8356 12.0956V7.09513Z"></path></symbol>
      <symbol id="icon-immerse" viewBox="0 0 20 20"><path fill="currentColor" fill-rule="evenodd" clip-rule="evenodd" d="M0.25 1C0.25 0.585786 0.585786 0.25 1 0.25H4C4.41421 0.25 4.75 0.585786 4.75 1C4.75 1.41421 4.41421 1.75 4 1.75H1.75V4C1.75 4.41421 1.41421 4.75 1 4.75C0.585786 4.75 0.25 4.41421 0.25 4V1ZM0.25 19C0.25 19.4142 0.585786 19.75 1 19.75H4C4.41421 19.75 4.75 19.4142 4.75 19C4.75 18.5858 4.41421 18.25 4 18.25H1.75V16C1.75 15.5858 1.41421 15.25 1 15.25C0.585786 15.25 0.25 15.5858 0.25 16V19ZM19 0.25C19.4142 0.25 19.75 0.585786 19.75 1V4C19.75 4.41421 19.4142 4.75 19 4.75C18.5858 4.75 18.25 4.41421 18.25 4V1.75H16C15.5858 1.75 15.25 1.41421 15.25 1C15.25 0.585786 15.5858 0.25 16 0.25H19ZM19.75 19C19.75 19.4142 19.4142 19.75 19 19.75H16C15.5858 19.75 15.25 19.4142 15.25 19C15.25 18.5858 15.5858 18.25 16 18.25H18.25V16C18.25 15.5858 18.5858 15.25 19 15.25C19.4142 15.25 19.75 15.5858 19.75 16V19ZM7 5C5.89543 5 5 5.89543 5 7V13C5 14.1046 5.89543 15 7 15H13C14.1046 15 15 14.1046 15 13V7C15 5.89543 14.1046 5 13 5H7Z"></path></symbol>
    </svg>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import ArticleLikeButton from './ArticleLikeButton.vue';
import ArticleFavoriteButton from './ArticleFavoriteButton.vue';
import ArticleCommentButton from './ArticleCommentButton.vue'; // 引入新组件

// 接收外部传来的数据
const props = defineProps({
  articleId: {
    type: [String, Number],
    default: ''
  },
  isLiked: {
    type: Boolean,
    default: false
  },
  likeCount: {
    type: Number,
    default: 0
  },
  commentCount: {
    type: Number,
    default: 0
  },
  isFavorite: {
    type: Boolean,
    default: false
  },
  favoriteCount: {
    type: Number,
    default: 0
  }
});

// 定义向外抛出的事件
const emit = defineEmits([
  'update:isLiked',
  'update:likeCount',
  'update:isFavorite',
  'update:favoriteCount',
  'scrollToComment'
]);

const scrollToComment = () => {
  emit('scrollToComment');
};
</script>

<style scoped lang="scss">
/* 核心容器样式 */
.suspended-panel {
  position: fixed;
  top: 180px;
  margin-left: -5rem;
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 按钮通用样式 */
.panel-btn {
  position: relative;
  width: 50px;
  height: 50px;
  background-color: #fff;
  background-position: 50%;
  background-repeat: no-repeat;
  border-radius: 50%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.04);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: #8a919f;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.15);
    color: #515767;
  }
}

.sprite-icon, .icon-wrapper {
  width: 20px;
  height: 20px;
  font-size: 20px;
}

/* 分享弹窗 */
.share-popup {
  display: none;
  position: absolute;
  margin-left: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  white-space: nowrap;
  z-index: 101;
  left: 100%;
  top: 0;
}

.share-btn-wrapper:hover .share-popup {
  display: block;
}

.share-item {
  padding: 8px 12px;
  color: #515767;
  font-size: 14px;
  &:hover {
    background-color: #f4f5f5;
    color: #1e80ff;
  }
}
</style>