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
})
</script>