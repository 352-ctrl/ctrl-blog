<template>
  <div class="user-center-container">
    <el-row :gutter="24">
      <el-col :xs="24" :md="6" :lg="6" class="sidebar-col">

        <el-card class="user-info-card" shadow="hover">
          <div class="user-profile-header">
            <AvatarUpload
                :model-value="userStore.userInfo?.avatar"
                :size="90"
                title="修改个人头像"
                @upload-success="onAvatarUploadSuccess"
                class="profile-avatar-wrapper"
            />
            <el-tooltip
                effect="dark"
                placement="top"
                :show-after="300"
                :disabled="isNicknameTooltipDisabled"
            >
              <template #content>
                <div class="tooltip-nickname-content">
                  {{ userStore.userInfo?.nickname || '未命名用户' }}
                </div>
              </template>
              <div class="profile-nickname" @mouseenter="checkNicknameOverflow">
                {{ userStore.userInfo?.nickname || '未命名用户' }}
              </div>
            </el-tooltip>

            <el-tooltip
                effect="dark"
                placement="bottom"
                :show-after="300"
                transition="el-fade-in-linear"
                :disabled="isBioTooltipDisabled"
            >
              <template #content>
                <div class="tooltip-bio-content">
                  {{ userStore.userInfo?.bio || '这个人很懒，什么都没写~' }}
                </div>
              </template>
              <div class="profile-bio" @mouseenter="checkBioOverflow">
                {{ userStore.userInfo?.bio || '这个人很懒，什么都没写~' }}
              </div>
            </el-tooltip>
            <div class="profile-join-time">
              <el-icon><Calendar /></el-icon>
              加入于 {{ formatDate(userStore.userInfo?.createTime) }}
            </div>
          </div>
        </el-card>

        <el-card class="user-menu-card" shadow="hover">
          <el-menu
              :default-active="activeMenu"
              router
              class="custom-user-menu"
          >
            <el-menu-item index="/user/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>个人总览</span>
            </el-menu-item>

            <el-menu-item-group title="我的互动">
              <el-menu-item index="/user/collections">
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
              </el-menu-item>
              <el-menu-item index="/user/likes">
                <el-icon><Pointer /></el-icon>
                <span>我的点赞</span>
              </el-menu-item>
              <el-menu-item index="/user/comments">
                <el-icon><ChatDotRound /></el-icon>
                <span>我的评论</span>
              </el-menu-item>
            </el-menu-item-group>

            <el-menu-item-group title="账号管理">
              <el-menu-item index="/user/settings">
                <el-icon><Setting /></el-icon>
                <span>个人设置</span>
              </el-menu-item>
            </el-menu-item-group>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="18" :lg="18" class="main-content-col">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from "element-plus";
import { useUserStore } from '@/store/user.js';
import { updateProfile } from "@/api/front/system/userInfo.js";
import AvatarUpload from '@/components/common/AvatarUpload.vue';

const route = useRoute();
const userStore = useUserStore();

const activeMenu = computed(() => route.path);

const isNicknameTooltipDisabled = ref(true); // 默认禁用
const isBioTooltipDisabled = ref(true); // 默认禁用

// 单行文本溢出判断：滚动宽度 > 可视宽度
const checkNicknameOverflow = (e) => {
  const el = e.target;
  // 如果实际宽度小于等于可视宽度，说明没被省略，禁用 Tooltip
  isNicknameTooltipDisabled.value = el.scrollWidth <= el.clientWidth;
};

// 多行文本溢出判断：滚动高度 > 可视高度
const checkBioOverflow = (e) => {
  const el = e.target;
  // 如果实际高度小于等于可视高度，说明没被省略，禁用 Tooltip
  isBioTooltipDisabled.value = el.scrollHeight <= el.clientHeight;
};

/**
 * 头像上传成功回调
 * 核心逻辑：上传文件获取URL后，自动调用修改资料接口存入数据库
 */
const onAvatarUploadSuccess = async (url) => {
  try {
    const payload = {
      id: userStore.userInfo.id,
      avatar: url,
      nickname: userStore.userInfo.nickname,
      bio: userStore.userInfo.bio
    };
    const res = await updateProfile(payload);
    if (res.code === 200) {
      ElMessage.success('头像已同步更新');
      await userStore.fetchUserInfo(); // 刷新 Pinia 状态
    }
  } catch (error) {
    console.error("同步头像失败", error);
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '未知时间';
  return dateString.split(' ')[0];
};
</script>

<style scoped>
.tooltip-nickname-content {
  max-width: 200px;
  word-wrap: break-word;
}

.tooltip-bio-content {
  max-width: 250px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-center-container { max-width: 1200px; margin: 20px auto; padding: 0 15px; }
.user-info-card { border: none; border-radius: 12px; margin-bottom: 20px; background-color: var(--el-bg-color-overlay); }
.user-profile-header { display: flex; flex-direction: column; align-items: center; padding: 10px 0; }
.profile-avatar-wrapper { margin-bottom: 15px; }
.profile-nickname {
  font-family: 'SmileySans', sans-serif;
  font-size: 20px;
  font-weight: normal;
  letter-spacing: 1px;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;

  width: 100%;           /* 确保它能占据父级宽度来计算溢出 */
  text-align: center;    /* 保持居中对齐 */
  white-space: nowrap;   /* 禁止换行 */
  overflow: hidden;      /* 隐藏超出的部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  padding: 0 10px;       /* 增加一点左右内边距，防止字贴到卡片边缘 */
}
.profile-bio {
  font-size: 13px;
  color: var(--el-text-color-regular);
  text-align: center;
  padding: 0 15px;
  margin-bottom: 15px;
  line-height: 1.5;

  /* 限制最多显示两行，超出显示省略号 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  /* 强制长单词、连续字母或数字换行 */
  word-break: break-all;
}
.profile-join-time { display: flex; align-items: center; gap: 5px; font-size: 12px; color: var(--el-text-color-secondary); background-color: var(--el-fill-color-light); padding: 4px 12px; border-radius: 12px; }
.user-menu-card { border: none; border-radius: 12px; overflow: hidden; background-color: var(--el-bg-color-overlay); }
.custom-user-menu {
  border-right: none;
  background-color: transparent;
}
:deep(.el-menu-item-group__title) { font-size: 12px; color: var(--el-text-color-secondary); padding-left: 20px; margin-top: 10px; }
.custom-user-menu .el-menu-item { height: 50px; line-height: 50px; border-radius: 8px; margin: 4px 10px; color: var(--el-text-color-regular); }
.custom-user-menu .el-menu-item.is-active { background-color: var(--el-color-primary-light-9); color: var(--el-color-primary); font-weight: 600; }
.fade-transform-leave-active, .fade-transform-enter-active { transition: all 0.3s; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }

/* ====================================
   移动端响应式优化 (Profile)
   ==================================== */
@media screen and (max-width: 768px) {
  .user-center-container {
    margin: 10px auto; /* 减小上下外边距 */
    padding: 0 10px;   /* 减小左右内边距 */
  }

  .sidebar-col {
    margin-bottom: 15px; /* 侧边栏和主内容在手机上会上下堆叠，增加适当间距 */
  }

  /* 让头部信息在手机上更紧凑 */
  .user-profile-header {
    padding: 5px 0;
  }

  .profile-avatar-wrapper {
    transform: scale(0.85); /* 手机端稍微缩小头像 */
    margin-bottom: 5px;
  }

  .profile-nickname {
    font-size: 18px;
  }

  /* 菜单卡片优化 */
  .user-menu-card {
    border-radius: 8px; /* 稍微减小圆角 */
  }

  .custom-user-menu .el-menu-item {
    height: 44px; /* 减小菜单高度 */
    line-height: 44px;
    margin: 2px 5px;
  }
}
</style>