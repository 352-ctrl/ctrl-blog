<template>
  <footer class="front-footer">
    <div class="footer-content">
      <div class="run-time">
        <el-icon class="heart-icon"><Trophy /></el-icon>
        <span>本站已勉强运行：{{ runTimeText }}</span>
      </div>

      <div class="copyright">
        © {{ startYear }} - {{ currentYear }} By
        <span class="author-name">神秘站长</span>
      </div>

      <div class="beian-info">
        <a href="https://beian.miit.gov.cn/" target="_blank" class="beian-link">
          蜀ICP备12345678号-1
        </a>
        <el-divider direction="vertical" class="hide-on-mobile" />
        <a href="http://www.beian.gov.cn/portal/registerSystemInfo" target="_blank" class="beian-link">
          川公网安备 510123456789号
        </a>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// ==========================================
// 1. 基础信息配置
// ==========================================
const startYear = 2023;
const currentYear = new Date().getFullYear();

// 设置建站时间 (格式：YYYY/MM/DD HH:mm:ss)
const siteStartDate = new Date('2026/01/01 00:00:00').getTime();

// ==========================================
// 2. 运行时间计算逻辑
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

  const leave1 = diff % (24 * 3600 * 1000); // 计算天数后剩余的毫秒数
  const hours = Math.floor(leave1 / (3600 * 1000));

  const leave2 = leave1 % (3600 * 1000); // 计算小时数后剩余的毫秒数
  const minutes = Math.floor(leave2 / (60 * 1000));

  const leave3 = leave2 % (60 * 1000); // 计算分钟数后剩余的毫秒数
  const seconds = Math.floor(leave3 / 1000);

  // 补零补齐
  const h = hours < 10 ? `0${hours}` : hours;
  const m = minutes < 10 ? `0${minutes}` : minutes;
  const s = seconds < 10 ? `0${seconds}` : seconds;

  runTimeText.value = `${days} 天 ${h} 小时 ${m} 分 ${s} 秒`;
};

onMounted(() => {
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
  padding: 20px;
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
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.run-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: monospace; /* 时间数字用等宽字体更好看 */
}

.heart-icon {
  color: var(--el-color-danger);
  animation: heartbeat 1.5s infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.copyright .author-name {
  font-weight: bold;
  color: var(--el-text-color-primary);
  font-family: 'SmileySans', sans-serif;
  letter-spacing: 1px;
}

.beian-info {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap; /* 移动端换行 */
  gap: 5px;
}

.beian-link {
  color: var(--el-text-color-secondary);
  text-decoration: none;
  transition: color 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.beian-link:hover {
  color: var(--el-color-primary);
}

.gongan-icon {
  width: 14px;
  height: 14px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .hide-on-mobile {
    display: none;
  }
  .beian-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style>