<template>
  <div class="message-page-container animate__animated animate__fadeIn">
    <el-card class="message-card" shadow="never">
      <div class="card-header">
        <div class="header-title">
          <el-icon class="header-icon"><ChatDotRound /></el-icon>
          <span>消息中心</span>
        </div>
        <el-button
            type="primary"
            link
            @click="handleMarkAllRead"
            :disabled="allMessageList.length === 0"
        >
          <el-icon><Check /></el-icon> 全部已读
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="custom-front-tabs">
        <el-tab-pane name="ALL">
          <template #label>
            <el-badge :value="unreadMap.ALL" :hidden="unreadMap.ALL <= 0" :max="99" class="tab-badge">
              全部消息
            </el-badge>
          </template>
        </el-tab-pane>

        <el-tab-pane name="SYSTEM">
          <template #label>
            <el-badge :value="unreadMap.SYSTEM" :hidden="unreadMap.SYSTEM <= 0" :max="99" class="tab-badge">
              系统通知
            </el-badge>
          </template>
        </el-tab-pane>

        <el-tab-pane name="COMMENT">
          <template #label>
            <el-badge :value="unreadMap.COMMENT" :hidden="unreadMap.COMMENT <= 0" :max="99" class="tab-badge">
              评论回复
            </el-badge>
          </template>
        </el-tab-pane>

        <el-tab-pane name="LIKE">
          <template #label>
            <el-badge :value="unreadMap.LIKE" :hidden="unreadMap.LIKE <= 0" :max="99" class="tab-badge">
              收到的赞
            </el-badge>
          </template>
        </el-tab-pane>
      </el-tabs>

      <div class="message-list" v-loading="loading">
        <el-empty
            v-if="allMessageList.length === 0"
            description="暂无消息"
            :image-size="100"
        />

        <div
            v-else
            v-for="msg in displayMessageList"
            :key="msg.id"
            class="message-item"
            :class="{ 'is-unread': msg.isRead === 0 || msg.isRead === 'UNREAD' || msg.isRead === '未读' }"
            @click="handleMessageClick(msg)"
        >
          <div class="avatar-box">
            <el-avatar v-if="msg.type === 'SYSTEM'" class="system-avatar" :size="45">
              <el-icon :size="24"><BellFilled /></el-icon>
            </el-avatar>

            <UserInfoPopover
                v-else
                :user-id="msg.fromUserId"
                :avatar="msg.fromUserAvatar"
                :nickname="msg.fromUserNickname"
                :bio="msg.fromUserBio"
            >
              <el-avatar :src="msg.fromUserAvatar" :size="45" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
            </UserInfoPopover>
          </div>

          <div class="info-box">
            <div class="meta-row">
              <span class="action-title" :title="msg.title">{{ msg.title }}</span>
              <span class="time">{{ formatTimeAgo(msg.createTime) }}</span>
            </div>

            <div class="msg-content" v-if="msg.content">
              {{ msg.content }}
            </div>
          </div>

          <div v-if="msg.isRead === 0 || msg.isRead === 'UNREAD' || msg.isRead === '未读'" class="unread-dot"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessagePage, markMessageAsRead, markAllAsRead } from '@/api/front/interaction/message.js'
import { useTable } from '@/composables/useTable.js'
import { formatTimeAgo } from '@/utils/date.js';

const router = useRouter()
const activeTab = ref('ALL')

const {
  loading,
  list: allMessageList,
  loadData
} = useTable(getMessagePage)

const displayMessageList = computed(() => {
  if (activeTab.value === 'ALL') {
    return allMessageList.value
  }
  return allMessageList.value.filter(msg => msg.type === activeTab.value)
})

const unreadMap = reactive({
  ALL: 0,
  SYSTEM: 0,
  COMMENT: 0,
  LIKE: 0
})

const calculateUnread = () => {
  let all = 0, system = 0, comment = 0, like = 0;
  allMessageList.value.forEach(msg => {
    if (msg.isRead === 0 || msg.isRead === 'UNREAD') {
      all++;
      if (msg.type === 'SYSTEM') system++;
      if (msg.type === 'COMMENT') comment++;
      if (msg.type === 'LIKE') like++;
    }
  })
  unreadMap.ALL = all;
  unreadMap.SYSTEM = system;
  unreadMap.COMMENT = comment;
  unreadMap.LIKE = like;
}

const fetchMessagesAndCalc = async () => {
  await loadData()
  calculateUnread()
}

onMounted(() => {
  fetchMessagesAndCalc()
})

const handleMessageClick = async (msg) => {
  if (msg.isRead === 0 || msg.isRead === 'UNREAD') {
    try {
      await markMessageAsRead(msg.id)
      msg.isRead = 1
      calculateUnread()
      window.dispatchEvent(new CustomEvent('update-unread-count'))
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }

  if (!msg.bizId) return

  if (msg.targetId) {
    router.push({
      path: `/post/${msg.bizId}`,
      query: { commentId: msg.targetId }
    })
  } else {
    router.push(`/post/${msg.bizId}`)
  }
}

const handleMarkAllRead = async () => {
  try {
    const typeParam = activeTab.value === 'ALL' ? undefined : activeTab.value
    const res = await markAllAsRead(typeParam)
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      await fetchMessagesAndCalc()
      window.dispatchEvent(new CustomEvent('update-unread-count'))
    }
  } catch (error) {
    console.error('一键已读失败', error)
  }
}
</script>

<style scoped>
.message-page-container {
  max-width: 800px;
  margin: 0 auto;
}
.message-card {
  border-radius: 12px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  min-height: 500px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.header-title {
  font-size: 20px;
  font-weight: bold;
  display: flex;
  align-items: center;
  color: var(--el-text-color-primary);
}
.header-icon {
  margin-right: 8px;
  font-size: 24px;
  color: var(--el-color-primary);
}
.custom-front-tabs { margin-top: 10px; }
:deep(.custom-front-tabs .el-tabs__nav-wrap::after) { display: none; }
:deep(.custom-front-tabs .el-tabs__item) { font-size: 15px; color: var(--el-text-color-regular); }
:deep(.custom-front-tabs .el-tabs__item.is-active) { font-weight: 600; color: var(--el-text-color-primary); }
:deep(.custom-front-tabs .el-tabs__active-bar) { background-color: var(--el-color-primary); height: 3px; border-radius: 2px; }
:deep(.tab-badge .el-badge__content.is-fixed) {
  top: 10px; right: -5px; transform: translateY(-50%) translateX(100%) scale(0.85); border: none;
}

/* ==================================
   专属：消息列表样式
   ================================== */
.message-list {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 20px;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.message-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--el-box-shadow-light);
}

.message-item:active {
  transform: scale(0.99);
  transition: transform 0.1s ease;
}

.message-item.is-unread {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
}
html.dark .message-item.is-unread {
  background-color: rgba(64, 158, 255, 0.08);
}

.avatar-box {
  margin-right: 15px;
  flex-shrink: 0;
}

/* 提取自行内样式 */
.user-avatar {
  cursor: pointer;
}

.system-avatar {
  background: linear-gradient(135deg, var(--el-color-danger), #ff8a80);
  color: white;
}
.info-box {
  flex: 1;
  min-width: 0;
}
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-right: 25px;
}
.action-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
  margin-right: 15px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}
.msg-content {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color);
  padding: 10px 12px;
  border-radius: 6px;
  border-left: 3px solid var(--el-border-color-dark);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-top: 4px;
}

.unread-dot {
  position: absolute;
  right: 16px;
  top: 24px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--el-color-danger);
  box-shadow: 0 0 5px var(--el-color-danger-light-5);
}

@media screen and (max-width: 768px) {
  .message-item {
    padding: 15px;
  }
  .unread-dot {
    right: 12px;
    top: 20px;
  }
}
</style>