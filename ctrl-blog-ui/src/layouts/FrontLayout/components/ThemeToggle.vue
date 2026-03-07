<template>
  <el-button
      circle
      @click="toggleDark()"
      :icon="isDark ? 'Sunny' : 'Moon'"
      class="theme-btn"
      title="切换暗黑模式"
  />
</template>

<script setup>
import { useDark, useToggle } from '@vueuse/core'

// 抽取暗黑模式切换逻辑
const isDark = useDark()
const toggleDark = useToggle(isDark)
</script>

<style scoped>
/* 继承父级传入的 class 样式，这里只需保证基本属性 */
.theme-btn {
  transition: all 0.3s ease;
}
</style>