<template>
  <div class="comment-box">
    <div class="box-wrapper">
      <el-avatar :size="35" :src="userStore.avatar" />

      <div class="input-container">
        <el-input
            v-model="content"
            type="textarea"
            maxlength="500"
            show-word-limit
            :rows="rows"
            :placeholder="placeholder"
        />

        <div class="action-row">
          <el-popover
              v-model:visible="emojiVisible"
              placement="bottom-start"
              trigger="click"
              width="auto"
          >
            <template #reference>
              <el-button text class="emoji-btn" title="插入表情">
                <IconSmile size="24"/>
              </el-button>
            </template>

            <emoji-picker
                ref="pickerRef"
                :class="isDark ? 'dark' : 'light'"
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
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user.js";
import { useDark } from '@vueuse/core';
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
const isDark = useDark();

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
const onEmojiSelect = (event) => {
  const emoji = event.detail.unicode;
  content.value += emoji;
  emojiVisible.value = false;
};

const handleSubmit = () => {
  if (!content.value.trim()) {
    ElMessage.warning('评论内容不能为空');
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
/* 提取后的排版样式 */
.box-wrapper {
  display: flex;
  gap: 15px;
}

.input-container {
  flex: 1;
}

.action-row {
  display: flex;
  justify-content: space-between;
  padding-top: 10px;
}

.emoji-btn {
  padding: 0;
}
</style>