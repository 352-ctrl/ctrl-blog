<template>
  <div class="board-immersive-wrapper animate__animated animate__fadeIn">
    <div class="global-dot-bg"></div>

    <div class="danmaku-stage">
      <div class="local-danmu-container">
        <div
            v-for="item in activeDanmakus"
            :key="item.renderId"
            class="dm move"
            :style="{
              top: item.top + '%',
              animationDuration: item.duration + 's',
              fontSize: item.fontSize + 'px',
              zIndex: item.zIndex
            }"
            @animationend="removeDanmaku(item.renderId)"
        >
          <span class="barrage-items">
            <img :src="item.avatar" class="dm-avatar" alt="avatar" />
            <span class="dm-nickname">{{ item.nickname }}:</span>
            <span class="dm-content">{{ item.content }}</span>
          </span>
        </div>
      </div>

      <div class="input-card-container">
        <el-card class="glass-input-card" shadow="hover" v-loading="loading">
          <div class="board-header">
            <h1 class="header-title">留言板 & 弹幕墙</h1>
            <p class="header-subtitle">
              在这里留下你的足迹吧... (支持匿名发送)
            </p>
          </div>

          <div class="input-wrapper">
            <el-input
                v-model="newContent"
                placeholder="说点什么，让大家看到你..."
                maxlength="50"
                show-word-limit
                size="large"
                class="custom-send-input"
                @keyup.enter="handleSend"
                clearable
            >
              <template #prefix>
                <el-icon><EditPen /></el-icon>
              </template>
              <template #append>
                <el-button type="primary" class="send-btn" :loading="sending" @click="handleSend">
                  <el-icon class="send-icon" v-if="!sending"><Position /></el-icon>
                  {{ sending ? '发送中' : '发送' }}
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </div>
    </div>

    <div class="wall-section">
      <div class="wall-header">
        <div class="title-with-icon">
          <el-icon class="wall-header-icon"><ChatLineRound /></el-icon>
          <span>最新留言</span>
        </div>
        <span class="wall-count">已为您呈现最新 {{ displayDanmakuList.length }} 条留言</span>
      </div>

      <el-empty
          v-if="!loading && displayDanmakuList.length === 0"
          description="暂无留言，快来抢沙发吧！"
      />

      <div v-else class="wall-grid">
        <div v-for="(item, index) in displayDanmakuList" :key="item.id || index" class="wall-item glass-wall-item">
          <div class="wall-item-header">
            <el-avatar :src="item.avatar" :size="44" class="wall-avatar" />
            <div class="wall-item-meta">
              <div class="wall-nickname">{{ item.nickname }}</div>
              <div class="wall-time">
                <el-icon><Clock /></el-icon> {{ formatTimeAgo(item.createTime) }}
              </div>
            </div>
          </div>
          <div class="wall-item-content">
            {{ item.content }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getDanmakuList, addDanmaku } from '@/api/front/interaction/danmaku.js';
import { useRequest } from '@/composables/useRequest';
import { useUserStore } from '@/store/user.js';
import { formatTimeAgo } from '@/utils/date.js';

const userStore = useUserStore();
const { loading, data: danmakuList, execute: fetchDanmakus } = useRequest(getDanmakuList);
const { loading: sending, execute: execSend } = useRequest(addDanmaku, { successMsg: '发送成功' });

const newContent = ref('');

// ==========================================
// 前端展示逻辑：只截取最新的20条展示在下方的墙上
// ==========================================
const displayDanmakuList = computed(() => {
  if (!danmakuList.value) return [];
  return danmakuList.value.slice(0, 20);
});

// ==========================================
// 弹幕引擎核心
// ==========================================
const activeDanmakus = ref([]);
let danmakuTimer = null;
let renderIdCounter = 0;

const maxTracks = 10;
let lastTrack = -1;

onMounted(async () => {
  await fetchDanmakus();
  startDanmakuEngine();
});

onUnmounted(() => {
  if (danmakuTimer) clearTimeout(danmakuTimer);
});

const startDanmakuEngine = () => {
  if (danmakuTimer) clearTimeout(danmakuTimer);

  const loop = () => {
    const randomDelay = Math.random() * 2000 + 500;

    danmakuTimer = setTimeout(() => {
      if (!document.hidden && danmakuList.value && danmakuList.value.length > 0) {

        let randomIndex = 0;
        const total = danmakuList.value.length;

        // 60% 的概率从最新的 20 条留言里抽，40% 的概率从全局历史记录里抽
        if (Math.random() < 0.6 && total > 20) {
          randomIndex = Math.floor(Math.random() * 20);
        } else {
          randomIndex = Math.floor(Math.random() * total);
        }

        const item = danmakuList.value[randomIndex];
        pushToScreen(item);
      }

      if (activeDanmakus.value.length > 40) {
        activeDanmakus.value.shift();
      }

      loop();
    }, randomDelay);
  };

  loop();
};

const pushToScreen = (item, isSelf = false) => {
  let track;
  do {
    track = Math.floor(Math.random() * maxTracks);
  } while (track === lastTrack && maxTracks > 1);
  lastTrack = track;

  const trackHeight = 90 / maxTracks;
  const topPosition = 5 + (track * trackHeight) + (Math.random() * 2 - 1);

  const baseDuration = isSelf ? 14 : Math.floor(Math.random() * 6) + 10;
  const fontSize = isSelf ? 16 : Math.floor(Math.random() * 3) + 14;
  const zIndex = isSelf ? 99 : Math.floor(Math.random() * 10);

  activeDanmakus.value.push({
    ...item,
    renderId: renderIdCounter++,
    top: topPosition,
    duration: baseDuration,
    fontSize: fontSize,
    zIndex: zIndex
  });
};

const removeDanmaku = (id) => {
  const index = activeDanmakus.value.findIndex(d => d.renderId === id);
  if (index !== -1) {
    activeDanmakus.value.splice(index, 1);
  }
};

const handleSend = async () => {
  if (!newContent.value.trim()) {
    ElMessage.warning('留言内容不能为空');
    return;
  }

  try {
    // 1. 发送给后端保存
    await execSend({ content: newContent.value });

    // 2. 先清空输入框，给用户即时反馈
    newContent.value = '';

    // 3. 重新拉取后端最新列表（此时数据包含了后端真实生成的专属随机头像和昵称）
    await fetchDanmakus();

    // 4. 拿到后端返回的最新第一条数据，立刻作为“自己的专属弹幕”发射到屏幕上
    if (danmakuList.value && danmakuList.value.length > 0) {
      pushToScreen(danmakuList.value[0], true);
    }

  } catch (error) {
    console.error('发送失败', error);
  }
};
</script>

<style scoped>
/* ====================================
   1. 沉浸式全局布局 (优化 3: 贯穿全屏的背景)
   ==================================== */
.board-immersive-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  min-height: 100%;
  z-index: 10;
  padding-bottom: 80px;

  /* 全局 Mesh Gradient 背景 */
  background-color: var(--el-bg-color-page);
  background-image:
      radial-gradient(circle at 15% 20%, var(--el-color-primary-light-8), transparent 40%),
      radial-gradient(circle at 85% 60%, var(--el-color-success-light-8), transparent 40%);
  /* 让背景固定，不随页面滚动，质感更好 */
  background-attachment: fixed;
}

/* 全局点阵纹理层 */
.global-dot-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  background-image: radial-gradient(var(--el-border-color-lighter) 1px, transparent 1px);
  background-size: 24px 24px;
  opacity: 0.8;
}

/* 暗黑模式背景适配 */
html.dark .board-immersive-wrapper {
  background-color: #141414;
  background-image:
      radial-gradient(circle at 15% 20%, rgba(var(--el-color-primary-rgb), 0.12), transparent 40%),
      radial-gradient(circle at 85% 60%, rgba(var(--el-color-success-rgb), 0.12), transparent 40%);
}

html.dark .global-dot-bg {
  background-image: radial-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px);
}

/* ====================================
   2. 弹幕舞台区
   ==================================== */
.danmaku-stage {
  position: relative;
  width: 100%;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 30px;
  z-index: 2; /* 确保在点阵图之上 */
}

.local-danmu-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 20;
}

/* ====================================
   3. 弹幕基础样式
   ==================================== */
.dm {
  position: absolute;
  white-space: nowrap;
  will-change: transform;
  left: 100%;
}

.dm.move {
  animation-name: danmu-move;
  animation-timing-function: linear;
  animation-fill-mode: forwards;
}

@keyframes danmu-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(calc(-100vw - 100%)); }
}

.barrage-items {
  display: inline-flex;
  align-items: center;
  padding: 4px 16px 4px 6px;
  background-color: rgba(0, 0, 0, 0.45);
  border-radius: 40px;
  color: #fff;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: opacity 0.3s;
}

.dm-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.8);
}

.dm-nickname {
  font-weight: 600;
  opacity: 0.85;
  margin-right: 8px;
}

.dm-content {
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* ====================================
   4. 输入交互区 (毛玻璃卡片)
   ==================================== */
.input-card-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
  transform: translateY(60px);
}

.glass-input-card {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.04);
  padding: 10px;
}

html.dark .glass-input-card {
  background: rgba(30, 30, 32, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.2);
}

.board-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-title {
  font-family: 'SmileySans', 'Inter', sans-serif;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(120deg, var(--el-color-primary), var(--el-color-success));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 8px 0;
}

.header-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

.input-wrapper {
  max-width: 600px;
  margin: 0 auto;
}

.custom-send-input :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  box-shadow: 0 0 0 1px var(--el-border-color-lighter) inset;
  background-color: var(--el-fill-color-blank);
  padding-left: 15px;
}

.custom-send-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--el-color-primary) inset;
}

.custom-send-input :deep(.el-input-group__append) {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: white;
  border-radius: 0 12px 12px 0;
  padding: 0;
  overflow: hidden;
}

.send-btn {
  width: 110px;
  height: 100%;
  border-radius: 0;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

html.dark .send-btn {
  /* 调整为更柔和的深蓝色或符合主题的绿色 */
  --el-button-bg-color: var(--el-color-primary-light-3);
  --el-button-border-color: var(--el-color-primary-light-3);
  --el-button-hover-bg-color: var(--el-color-primary);
  --el-button-hover-border-color: var(--el-color-primary);
  --el-button-active-bg-color: var(--el-color-primary-dark-2);

  /* 文字颜色保持清晰 */
  color: #fff;
}

html.dark .custom-send-input :deep(.el-input-group__append) {
  background-color: rgba(var(--el-color-primary-rgb), 0.8) !important;
  border-color: transparent;
  backdrop-filter: blur(4px);
}

html.dark .custom-send-input :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.2);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
}

html.dark .custom-send-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

html.dark .send-btn {
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-success-light-3)) !important;
  border: none !important;
  transition: all 0.3s ease;
}

html.dark .send-btn:hover {
  filter: brightness(1.2);
  transform: scale(1.02);
}

.send-btn:hover {
  background-color: var(--el-color-primary-light-3);
}

.send-icon {
  margin-right: 6px;
  font-size: 16px;
  vertical-align: middle;
}

/* ====================================
   5. 留言墙区域
   ==================================== */
.wall-section {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  margin: 80px auto 0;
  padding: 0 20px;
}

.wall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding-bottom: 15px;
}

.title-with-icon {
  display: flex;
  align-items: center;
  font-family: 'SmileySans', 'Inter', sans-serif;
  font-size: 22px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  gap: 10px;
}

.wall-header-icon {
  color: var(--el-color-primary);
  font-size: 26px;
}

/* 优化 2: 气泡状的计数字段样式 */
.wall-count {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid var(--el-color-primary-light-8);
}

html.dark .wall-count {
  background-color: rgba(var(--el-color-primary-rgb), 0.15);
  border-color: rgba(var(--el-color-primary-rgb), 0.3);
}

.wall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

/* 为留言卡片增加玻璃拟物感，呼应背景 */
.glass-wall-item {
  background-color: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
}

html.dark .glass-wall-item {
  background-color: rgba(30, 30, 32, 0.6) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.wall-item {
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  display: flex;
  flex-direction: column;
}

.wall-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.05);
  border-color: var(--el-color-primary-light-5) !important;
}

html.dark .wall-item:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.4);
}

.wall-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.wall-avatar {
  border: 2px solid var(--el-border-color-extra-light);
}

.wall-item-meta {
  margin-left: 14px;
}

.wall-nickname {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.wall-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.wall-item-content {
  font-size: 15px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  word-break: break-word;
  background-color: rgba(0, 0, 0, 0.03); /* 配合毛玻璃卡片稍微加深一点 */
  padding: 16px;
  border-radius: 0 16px 16px 16px;
  position: relative;
}

html.dark .wall-item-content {
  background-color: rgba(255, 255, 255, 0.05);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .danmaku-stage {
    height: 300px;
  }

  .input-card-container {
    transform: translateY(40px);
    padding: 0 10px;
  }

  .wall-section {
    margin-top: 60px;
  }

  .wall-grid {
    grid-template-columns: 1fr;
  }

  .barrage-items {
    padding: 3px 10px 3px 4px; /* 减小内边距 */
    border-radius: 20px;
  }

  .dm {
    font-size: 12px !important;
  }

  .dm-avatar {
    width: 22px;  /* 缩小头像 */
    height: 22px;
    margin-right: 6px;
  }

  .dm-nickname {
    font-size: 11px;
  }

  .danmaku-stage {
    height: 300px;
  }

  .input-card-container {
    transform: translateY(40px);
    padding: 0 10px;
    width: calc(100% - 20px);
  }
}
</style>