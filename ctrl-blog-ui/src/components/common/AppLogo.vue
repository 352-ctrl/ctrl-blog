<template>
  <div class="ctrl-blog-logo" :class="{ 'is-collapse': isCollapse }" :style="{ '--logo-size': size + 'px' }" @click="goToHome">
    <svg
        class="logo-icon"
        viewBox="0 0 100 100"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
    >
      <rect x="5" y="5" width="90" height="90" rx="18" stroke="currentColor" stroke-width="10" />
      <rect x="25" y="70" width="50" height="8" fill="currentColor" class="cursor-line" />
    </svg>

    <div class="logo-text">
      <span class="text-ctrl">Ctrl</span>
      <span class="text-blog">Blog</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, withDefaults } from 'vue';
import { useRouter } from 'vue-router';

// 定义组件属性，允许外部控制大小
interface Props {
  size?: number; // Logo 的整体高度 (像素)
  isCollapse?: boolean;
}

// 设置默认值，默认高度为 40px
const props = withDefaults(defineProps<Props>(), {
  size: 40,
  isCollapse: false,
});

const router = useRouter();

// 点击 Logo 跳转回首页
const goToHome = () => {
  // 只有当路由实例存在时才尝试跳转
  if (router) {
    router.push('/');
  }
};
</script>

<style scoped>
/* 使用 CSS 变量控制整体大小
  CSS 变量的值由 Props 动态传入
*/
.ctrl-blog-logo {
  --logo-height: var(--logo-size);

  display: inline-flex;
  align-items: center; /* 垂直居中对齐，比 baseline 更稳重 */
  height: var(--logo-height);
  cursor: pointer;
  background: transparent;
  user-select: none; /* 防止文字被选中 */
  transition: opacity 0.2s ease, transform 0.2s ease;
  vertical-align: middle; /* 改善在行内元素中的对齐 */
}

.ctrl-blog-logo:hover {
  opacity: 0.85; /* 悬停时稍微变淡 */
  transform: translateY(-1px); /* 轻微上浮，增加动感 */
}

/* SVG 图形样式 */
.logo-icon {
  height: 80%; /* 图形高度略小于容器 */
  width: auto;
  margin-right: 8px;
  color: var(--el-color-primary);
  flex-shrink: 0; /* 防止 SVG 被压缩 */
  transition: margin-right 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 折叠时：图标右边距归零 */
.is-collapse .logo-icon {
  margin-right: 0 !important;
}

/* 光标闪烁动画 */
.cursor-line {
  animation: blink 1.2s infinite; /* 稍慢的闪烁，更沉稳 */
}

@keyframes blink {
  50% { opacity: 0; }
}

.logo-text {
  display: flex;
  align-items: center;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  font-weight: 700;
  letter-spacing: -0.5px;
  font-size: calc(var(--logo-height) * 0.6);

  line-height: 1.2;
  padding-bottom: 2px;

  max-width: 150px;
  opacity: 1;
  white-space: nowrap;

  overflow: hidden;

  transition: max-width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1), opacity 0.2s ease;
}

/* 折叠时：文字宽度收缩至0，同时透明度消失 */
.is-collapse .logo-text {
  max-width: 0;
  opacity: 0;
}

/* "Ctrl" 的样式 */
.text-ctrl {
  color: var(--el-color-primary);
}

/* "Blog" 的样式 */
.text-blog {
  color: var(--el-text-color-regular);
  margin-left: 2px; /* Ctrl 和 Blog 之间的微小间距 */
}
</style>