<template>
  <div class="comment-box">
    <div style="display: flex; gap: 15px">
      <el-avatar v-if="userStore.avatar" :size="35" :src="userStore.avatar" />
      <el-avatar v-else :size="35" :src="defaultAvatar" />

      <div style="flex: 1">
        <el-input
            v-model="content"
            type="textarea"
            maxlength="500"
            show-word-limit
            :rows="rows"
            :placeholder="placeholder"
        />

        <div style="display: flex; justify-content: space-between; padding-top: 10px">
          <el-popover
              v-model:visible="emojiVisible"
              placement="bottom-start"
              trigger="click"
              width="auto"
          >
            <template #reference>
              <el-button text style="padding: 0" title="插入表情">
                <IconSmile size="24"/>
              </el-button>
            </template>

            <emoji-picker
                ref="pickerRef"
                class="light"
                locale="zh"
                @emoji-click="onEmojiSelect"
            ></emoji-picker>
          </el-popover>

          <div>
            <el-button v-if="showCancel" @click="$emit('cancel')" size="small">取消</el-button>
            <el-button type="primary" @click="handleSubmit" size="small" :loading="submitting">
              {{ submitText }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";
import { useUserStore } from "@/store/user.js";
import defaultAvatar from '@/assets/images/default-avatar.png';
import IconSmile from "@/components/common/Icon/IconSmile.vue";
import 'emoji-picker-element'; // 导入自定义元素
import zh_CN from 'emoji-picker-element/i18n/zh_CN'; // 导入中文包

const props = defineProps({
  placeholder: { type: String, default: '说点什么吧...' },
  submitText: { type: String, default: '发送' },
  rows: { type: Number, default: 3 },
  showCancel: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false }
});

const emit = defineEmits(['submit', 'cancel']);
const userStore = useUserStore();

const content = ref('');
const emojiVisible = ref(false);
const pickerRef = ref(null);

// 初始化表情包语言
const initEmojiPicker = () => {
  nextTick(() => {
    if (pickerRef.value) {
      pickerRef.value.i18n = zh_CN;
    }
  });
};

// 监听 Popover 显示，初始化 Picker
// 注意：emoji-picker-element 是 Web Component，有时需要延迟设置属性
const onEmojiSelect = (event) => {
  const emoji = event.detail.unicode;
  content.value += emoji;
  emojiVisible.value = false;
};

const handleSubmit = () => {
  if (!content.value.trim()) {
    return; // 父组件处理空校验或这里处理
  }
  emit('submit', content.value);
};

// 暴露清理方法给父组件
const clear = () => {
  content.value = '';
  emojiVisible.value = false;
};

defineExpose({ clear });
</script>

<style scoped>
/* 简单的样式调整 */
</style>