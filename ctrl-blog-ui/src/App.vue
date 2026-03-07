<template>
  <RouterView />
</template>

<script setup>
import {onMounted} from "vue";
import {useUserStore} from "@/store/user.js";

const userStore = useUserStore()

onMounted(() => {
  // 如果本地有 Token，就静默请求一次用户信息
  if (userStore.token) {
    userStore.fetchUserInfo()
  }

  window.addEventListener('visibilitychange', () => {
    if (document.hidden) {
      document.title = '404 Not Found - 精神出走中...';
    } else {
      document.title = '200 OK - 重新连接成功！';
    }
  });
})
</script>