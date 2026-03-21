<template>
  <el-card class="tag-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="icon-tag icon-swing"><Collection /></el-icon>
          标签云
        </span>
      </div>
    </template>

    <div class="card-body">
      <div v-if="tagList.length === 0" class="empty-state">
        <el-empty description="暂无标签" :image-size="60" />
      </div>

      <div v-else ref="tagCloudRef" class="tag-cloud-container"></div>
    </div>
  </el-card>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from "vue";
import { getTagList } from '@/api/front/article/tag.js';
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";

const router = useRouter();

const CONFIG = {
  RADIUS_RATIO: 0.9,
  PERSPECTIVE: 500,
  BASE_FONT_SIZE: 15,
  FONT_WEIGHT_FACTOR: 10,
  AUTO_ROTATE_SPEED: 0.002,
  DRAG_SENSITIVITY: 0.005,
  RESUME_DELAY: 2000,
};

const tagCloudRef = ref(null);
const tagList = ref([]);
let tagObjects = [];
let animationFrameId = null;
let resizeObserver = null;
let isAutoRotating = true;
let isDragging = false;
let lastMousePos = { x: 0, y: 0 };
let resumeTimer = null;
let activeRotation = { x: CONFIG.AUTO_ROTATE_SPEED, y: CONFIG.AUTO_ROTATE_SPEED };

onMounted(() => {
  loadTags();
});

onUnmounted(() => {
  stopAnimation();
  if (resizeObserver) resizeObserver.disconnect();
  unbindGlobalEvents();
  if (resumeTimer) clearTimeout(resumeTimer);
});

const loadTags = async () => {
  try {
    const res = await getTagList();
    if (res.code === 200) {
      tagList.value = res.data || [];
      await nextTick();

      if (tagList.value.length > 0) {
        setTimeout(() => {
          initResizeObserver();
          initTagCloud();
          bindGlobalEvents();
        }, 100);
      }
    } else {
      ElMessage.error(res.msg);
    }
  } catch (error) {
    console.error('Failed to load tags:', error);
  }
};

const initResizeObserver = () => {
  if (!tagCloudRef.value) return;
  resizeObserver = new ResizeObserver((entries) => {
    for (const entry of entries) {
      const { width, height } = entry.contentRect;
      if (width > 0 && height > 0 && tagList.value.length > 0) {
        initTagCloud();
      }
    }
  });
  resizeObserver.observe(tagCloudRef.value);
};

const initTagCloud = () => {
  const container = tagCloudRef.value;
  if (!container || tagList.value.length === 0 || container.offsetWidth === 0) return;

  stopAnimation();
  container.innerHTML = '';
  tagObjects = [];

  const width = container.clientWidth;
  const height = container.clientHeight;
  const radius = Math.min(width, height) / 2 * CONFIG.RADIUS_RATIO;
  const fragment = document.createDocumentFragment();

  tagList.value.forEach((tag, index) => {
    const tagEl = document.createElement('div');
    tagEl.className = 'tag-cloud-word';
    tagEl.textContent = tag.name;

    tagEl.style.position = 'absolute';
    tagEl.style.left = '45%';
    tagEl.style.top = '50%';
    tagEl.style.whiteSpace = 'nowrap';
    tagEl.style.fontWeight = 'bold';
    tagEl.style.fontFamily = "'SmileySans', sans-serif";
    tagEl.style.cursor = 'pointer';
    tagEl.style.color = getRandomColor();
    tagEl.style.transition = 'none';

    const fontSize = CONFIG.BASE_FONT_SIZE + (tag.value || 50) / CONFIG.FONT_WEIGHT_FACTOR;
    tagEl.dataset.fontSize = fontSize;

    const phi = Math.acos(-1 + (2 * index) / tagList.value.length);
    const theta = Math.sqrt(tagList.value.length * Math.PI) * phi;

    const x = radius * Math.sin(phi) * Math.cos(theta);
    const y = radius * Math.sin(phi) * Math.sin(theta);
    const z = radius * Math.cos(phi);

    const tagObj = { el: tagEl, x, y, z, radius };
    tagObjects.push(tagObj);
    bindTagEvents(tagEl, tag);
    fragment.appendChild(tagEl);
  });

  container.appendChild(fragment);
  tagObjects.forEach(tag => updateTagStyle(tag));
  requestAnimationFrame(() => {
    tagObjects.forEach(tag => {
      tag.el.style.transition = 'all 0.3s ease';
    });
  });
  startAnimation();
};

const updateFrame = () => {
  let speedX = 0;
  let speedY = 0;

  if (isDragging) {
    speedX = activeRotation.x;
    speedY = activeRotation.y;
  } else if (isAutoRotating) {
    speedX = CONFIG.AUTO_ROTATE_SPEED;
    speedY = CONFIG.AUTO_ROTATE_SPEED;
  }

  const sinX = Math.sin(speedY);
  const cosX = Math.cos(speedY);
  const sinY = Math.sin(speedX);
  const cosY = Math.cos(speedX);

  tagObjects.forEach(tag => {
    if (tag.el.dataset.hover === 'true') return;
    let rx1 = tag.x * cosY - tag.z * sinY;
    let rz1 = tag.z * cosY + tag.x * sinY;
    let ry2 = tag.y * cosX - rz1 * sinX;
    let rz2 = rz1 * cosX + tag.y * sinX;
    tag.x = rx1;
    tag.y = ry2;
    tag.z = rz2;
    updateTagStyle(tag);
  });
  animationFrameId = requestAnimationFrame(updateFrame);
};

const updateTagStyle = (tag) => {
  const { el, x, y, z, radius } = tag;
  const scale = CONFIG.PERSPECTIVE / (CONFIG.PERSPECTIVE + z);
  const projX = x * scale;
  const projY = y * scale;

  const distance = Math.sqrt(x * x + y * y + z * z);
  const viewDot = z / (distance || 1);
  const frontFactor = Math.max(0, viewDot);
  const enhancedFactor = Math.pow(frontFactor, 0.8);

  const baseFontSize = parseFloat(el.dataset.fontSize);
  const sizeMultiplier = 0.2 + enhancedFactor * 0.9;
  const scaledFontSize = baseFontSize * sizeMultiplier;
  el.style.fontSize = `${scaledFontSize}px`;

  const opacityMultiplier = Math.pow(enhancedFactor, 0.5);
  const opacity = 0.05 + opacityMultiplier * 0.95;

  const distanceFromCenter = Math.sqrt(projX * projX + projY * projY);
  const maxRadius = radius * scale;
  const distanceRatio = Math.min(1, distanceFromCenter / (maxRadius || 1));
  const centerScale = 1.3 - (distanceRatio * 0.5);
  const finalScale = scale * centerScale;

  el.style.transform = `translate3d(${projX}px, ${projY}px, 0) scale(${finalScale})`;
  el.style.zIndex = Math.floor(z + 100);
  el.style.opacity = opacity;
  el.style.pointerEvents = opacity > 0.1 ? 'auto' : 'none';
};

const startAnimation = () => {
  stopAnimation();
  animationFrameId = requestAnimationFrame(updateFrame);
};

const stopAnimation = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }
};

const bindTagEvents = (tagEl, tagData) => {
  tagEl.addEventListener('click', (e) => {
    e.stopPropagation();
    router.push({
      name: 'FrontTags',
      query: { id: tagData.id }
    });
  });
  tagEl.addEventListener('mouseenter', () => {
    tagEl.dataset.hover = 'true';
    tagEl.style.fontSize = `${parseFloat(tagEl.dataset.fontSize) * 1.2}px`;
    tagEl.style.zIndex = '9999';
    tagEl.style.opacity = '1';
  });
  tagEl.addEventListener('mouseleave', () => {
    tagEl.dataset.hover = 'false';
  });
};

const bindGlobalEvents = () => {
  const container = tagCloudRef.value;
  if (!container) return;
  const onStart = (cx, cy) => {
    isDragging = true;
    isAutoRotating = false;
    lastMousePos.x = cx;
    lastMousePos.y = cy;
    container.style.cursor = 'grabbing';
    activeRotation = { x: 0, y: 0 };
  };
  const onMove = (cx, cy) => {
    if (!isDragging) return;
    const deltaX = cx - lastMousePos.x;
    const deltaY = cy - lastMousePos.y;
    activeRotation.x = deltaX * CONFIG.DRAG_SENSITIVITY;
    activeRotation.y = deltaY * CONFIG.DRAG_SENSITIVITY;
    lastMousePos.x = cx;
    lastMousePos.y = cy;
  };
  const onEnd = () => {
    isDragging = false;
    container.style.cursor = 'grab';
    if (resumeTimer) clearTimeout(resumeTimer);
    resumeTimer = setTimeout(() => {
      isAutoRotating = true;
      activeRotation = { x: CONFIG.AUTO_ROTATE_SPEED, y: CONFIG.AUTO_ROTATE_SPEED };
    }, CONFIG.RESUME_DELAY);
  };
  container.addEventListener('mousedown', (e) => onStart(e.clientX, e.clientY));
  window.addEventListener('mousemove', (e) => onMove(e.clientX, e.clientY));
  window.addEventListener('mouseup', onEnd);
  container.addEventListener('touchstart', (e) => {
    if (e.touches.length === 1) onStart(e.touches[0].clientX, e.touches[0].clientY);
  }, { passive: false });
  window.addEventListener('touchmove', (e) => {
    if (e.touches.length === 1) onMove(e.touches[0].clientX, e.touches[0].clientY);
  }, { passive: false });
  window.addEventListener('touchend', onEnd);
};

const unbindGlobalEvents = () => {};

const getRandomColor = () => {
  const r = Math.floor(Math.random() * 200);
  const g = Math.floor(Math.random() * 200);
  const b = Math.floor(Math.random() * 200);
  return `rgb(${r}, ${g}, ${b})`;
};
</script>

<style scoped lang="scss">
.tag-card {
  border: 1px solid var(--el-border-color-light);
  margin: 0;
  border-radius: 8px;
  background: var(--el-bg-color-overlay);

  :deep(.el-card__header) {
    padding: 15px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-family: 'SmileySans', sans-serif;
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .icon-tag {
        margin-right: 10px;
        color: var(--el-color-primary);
        font-size: 18px;
        transform-origin: top center;
      }
    }
  }

  .card-body {
    min-height: 100px;
    position: relative;
  }

  .tag-cloud-container {
    position: relative;
    width: 100%;
    height: 300px;
    overflow: hidden;
    user-select: none;
  }

  .empty-state {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    :deep(.el-empty__description) {
      margin-top: 10px;
    }
  }
}
</style>