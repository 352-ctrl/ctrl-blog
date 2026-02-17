<template>
  <div class="panel-btn" @click="handleClick">
    <el-icon size="20" class="icon-wrapper">
      <ChatDotRound />
    </el-icon>
    <span class="badge" v-if="count > 0">{{ formatCount(count) }}</span>
  </div>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue';

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
  background-color: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.04);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: #8a919f;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.15);
    color: #515767;
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
  background-color: #c2c8d1;
  color: #fff;
  transform: translateY(-20%);
}
</style>