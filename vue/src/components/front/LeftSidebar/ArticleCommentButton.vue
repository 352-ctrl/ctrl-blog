<template>
  <div class="panel-btn" @click="handleClick">
    <el-icon size="20" class="icon-wrapper">
      <ChatDotRound />
    </el-icon>
    <span class="badge" v-if="count > 0">{{ formatCount(count) }}</span>
  </div>
</template>

<script setup>
// 接收外部传来的数据
const props = defineProps({
  count: {
    type: Number,
    default: 0
  }
});

// 定义事件
const emit = defineEmits(['click']);

const handleClick = () => {
  emit('click');
};

// 格式化数字显示（例如超过99显示99+）
const formatCount = (num) => {
  if (num > 99) return '99+';
  return num;
};
</script>

<style scoped lang="scss">
.panel-btn {
  position: relative;
  width: 50px;
  height: 50px;
  background-color: var(--el-bg-color-overlay);
  border-radius: 50%;
  box-shadow: var(--el-box-shadow-light);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--el-text-color-secondary);
  transition: all 0.3s;

  &:hover {
    box-shadow: var(--el-box-shadow);
    color: var(--el-text-color-primary);
  }
}

.icon-wrapper {
  width: 20px;
  height: 20px;
  font-size: 20px;
}

.badge {
  position: absolute;
  top: 0;
  left: 75%;
  height: 17px;
  line-height: 17px;
  padding: 0 5px;
  border-radius: 9px;
  font-size: 11px;
  text-align: center;
  white-space: nowrap;
  background-color: var(--el-text-color-disabled);
  color: #fff;
  transform: translateY(-20%);
}
</style>