<template>
  <footer class="front-footer">
    <div class="footer-content">
      <div class="run-time">
        <el-icon class="heart-icon"><Trophy /></el-icon>
        <span>本站已勉强运行：{{ runTimeText }}</span>
      </div>

      <div class="copyright">
        © {{ startYear }} - {{ currentYear }} By
        <a
            v-if="webmaster.github"
            :href="webmaster.github"
            target="_blank"
            rel="noopener noreferrer"
            class="author-name hover-link"
        >
          {{ webmaster.nickname || '神秘站长' }}
        </a>
        <span v-else class="author-name">{{ webmaster.nickname || '神秘站长' }}</span>
      </div>

      <div class="tech-stack hide-on-mobile">
        Powered by Spring Boot 3 & Vue 3
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { getWebmasterInfo } from '@/api/front/system/site.js';

// ==========================================
// 1. 基础信息配置
// ==========================================
const startYear = 2026;
const currentYear = new Date().getFullYear();

// 建站时间 (建议未来也可以做成后端系统配置接口返回，目前先写死)
const siteStartDate = new Date('2026/03/19 00:00:00').getTime();

// 站长信息响应式对象
const webmaster = ref({
  nickname: '加载中...',
  github: 'https://github.com/352-ctrl' // 默认占位，接口返回后会被覆盖
});

// ==========================================
// 2. 数据获取逻辑
// ==========================================
const fetchWebmasterInfo = async () => {
  try {
    const res = await getWebmasterInfo();
    // 假设你的 request 拦截器已经处理了 code === 200 的解构，直接拿 data
    if (res && res.data) {
      webmaster.value = res.data;
    }
  } catch (error) {
    console.error('获取站长信息失败:', error);
    webmaster.value.nickname = '神秘站长'; // 降级处理
  }
};

// ==========================================
// 3. 运行时间计算逻辑
// ==========================================
const runTimeText = ref('');
let timer = null;

const updateRunTime = () => {
  const now = new Date().getTime();
  const diff = now - siteStartDate;

  if (diff < 0) {
    runTimeText.value = '即将上线';
    return;
  }

  const days = Math.floor(diff / (24 * 3600 * 1000));
  const leave1 = diff % (24 * 3600 * 1000);
  const hours = Math.floor(leave1 / (3600 * 1000));
  const leave2 = leave1 % (3600 * 1000);
  const minutes = Math.floor(leave2 / (60 * 1000));
  const leave3 = leave2 % (60 * 1000);
  const seconds = Math.floor(leave3 / 1000);

  const h = hours < 10 ? `0${hours}` : hours;
  const m = minutes < 10 ? `0${minutes}` : minutes;
  const s = seconds < 10 ? `0${seconds}` : seconds;

  runTimeText.value = `${days} 天 ${h} 小时 ${m} 分 ${s} 秒`;
};

onMounted(() => {
  fetchWebmasterInfo();
  updateRunTime();
  timer = setInterval(updateRunTime, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.front-footer {
  width: 100%;
  padding: 24px 20px;
  margin-top: auto;
  flex-shrink: 0;
  background-color: var(--el-bg-color-overlay);
  border-top: 1px solid var(--el-border-color-light);
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.run-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: monospace;
}

.heart-icon {
  color: var(--el-color-danger);
  animation: heartbeat 1.5s infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.copyright {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.author-name {
  font-weight: bold;
  color: var(--el-text-color-primary);
  font-family: 'SmileySans', sans-serif;
  letter-spacing: 1px;
  text-decoration: none;
  font-size: 15px;
}

.hover-link {
  transition: color 0.3s ease;
}

.hover-link:hover {
  color: var(--el-color-primary);
}

.tech-stack {
  font-size: 12px;
  opacity: 0.8;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .hide-on-mobile {
    display: none;
  }
}
</style>