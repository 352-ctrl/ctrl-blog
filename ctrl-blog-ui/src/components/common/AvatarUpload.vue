<template>
  <div class="img-cropper-component">
    <div
        class="avatar-trigger"
        @click="openDialog"
    >
      <el-avatar :size="size" :src="modelValue" shape="circle">
        <el-icon :size="size * 0.5"><User /></el-icon>
      </el-avatar>
      <div class="avatar-mask">
        <el-icon class="mask-icon"><Camera /></el-icon>
        <span class="mask-text">修改头像</span>
      </div>
    </div>

    <el-dialog
        :title="title"
        v-model="dialogVisible"
        append-to-body
        :close-on-click-modal="false"
        destroy-on-close
        @opened="modalOpened"
        @closed="handleClosed"
        class="cropper-dialog"
    >
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" class="cropper-area">
          <vue-cropper
              v-if="visible"
              ref="cropperRef"
              class="cropper-instance"
              :img="options.img"
              :info="true"
              :autoCrop="options.autoCrop"
              :autoCropWidth="options.autoCropWidth"
              :autoCropHeight="options.autoCropHeight"
              :fixedBox="options.fixedBox"
              :outputType="options.outputType"
              @realTime="realTime"
          />
        </el-col>

        <el-col :xs="24" :sm="12" class="preview-area">
          <div class="preview-box">
            <div :style="previews.div" class="preview-wrapper">
              <img
                  v-if="previews.url"
                  :src="previews.url"
                  :style="previews.img"
                  alt="头像预览"
              />
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row class="action-row" :gutter="10">
        <el-col :xs="24" :sm="16" class="tool-buttons">
          <el-upload
              class="upload-btn"
              action="#"
              :http-request="() => {}"
              :before-upload="beforeUpload"
              :show-file-list="false"
          >
            <el-button type="primary" plain>选择图片</el-button>
          </el-upload>

          <el-button-group class="btn-group">
            <el-button :icon="Plus" @click="changeScale(1)"></el-button>
            <el-button :icon="Minus" @click="changeScale(-1)"></el-button>
            <el-button :icon="RefreshLeft" @click="rotateLeft"></el-button>
            <el-button :icon="RefreshRight" @click="rotateRight"></el-button>
          </el-button-group>
        </el-col>

        <el-col :xs="24" :sm="8" class="submit-button-col">
          <el-button type="primary" @click="uploadImg" :loading="loading" class="submit-btn">
            确认提交
          </el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { Minus, Plus, RefreshLeft, RefreshRight } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { uploadFile } from "@/api/common/file.js";
import 'vue-cropper/dist/index.css';
import { VueCropper } from "vue-cropper";

const props = defineProps({
  modelValue: { type: String, default: '' },
  title: { type: String, default: "修改头像" },
  size: { type: Number, default: 100 }
});

const emit = defineEmits([
  'update:modelValue',
  'upload-success'
]);

const dialogVisible = ref(false);
const visible = ref(false);
const loading = ref(false);
const cropperRef = ref(null);

const options = reactive({
  img: props.modelValue,
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: "png",
  centerBox: true,
  info: true,
  checkOrientation: true,
  high: true,
});

const previews = ref({});

const openDialog = () => {
  options.img = props.modelValue || '';
  dialogVisible.value = true;
};

const modalOpened = () => {
  visible.value = true;
};

const closeDialog = () => {
  dialogVisible.value = false;
};

const handleClosed = () => {
  visible.value = false;
  loading.value = false;
};

const beforeUpload = (file) => {
  if (file.type.indexOf("image/") === -1) {
    ElMessage.error("请上传图片类型文件!");
    return false;
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error("文件大小不能超过5MB!");
    return false;
  }
  const reader = new FileReader();
  reader.readAsDataURL(file);
  reader.onload = () => {
    options.img = reader.result;
  };
  return false;
};

const changeScale = (num) => {
  cropperRef.value?.changeScale(num || 1);
};

const rotateLeft = () => {
  cropperRef.value?.rotateLeft();
};

const rotateRight = () => {
  cropperRef.value?.rotateRight();
};

const realTime = (data) => {
  previews.value = data;
};

const uploadImg = () => {
  if (!options.img) {
    ElMessage.warning('请先选择图片');
    return;
  }
  loading.value = true;
  cropperRef.value.getCropBlob(async (data) => {
    if (!data) {
      ElMessage.warning('未能获取到裁剪图片，请重新选择');
      loading.value = false; // 必须手动关闭 loading
      return;
    }

    let formData = new FormData();
    formData.append("file", data, "avatar.png");

    try {
      const res = await uploadFile(formData, 'avatar');
      const result = res.code !== undefined ? res : res.data;
      if (result.code === 200) {
        ElMessage.success('头像修改成功');
        const avatarUrl = typeof result.data === 'string'
            ? result.data
            : (result.data?.url || result.data?.avatar);
        emit('update:modelValue', avatarUrl);
        emit('upload-success', avatarUrl);
        closeDialog();
      } else {
        ElMessage.error(result.msg || '上传失败');
      }
    } catch (error) {
      console.error(error);
      ElMessage.error('网络错误，上传失败');
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
/* =========================================
   1. 外部触发器样式 (头像预览图)
   ========================================= */
.avatar-trigger {
  /* 使用 Vue 3 的 v-bind 特性将 JavaScript 变量动态绑定到 CSS */
  width: v-bind('size + "px"');
  height: v-bind('size + "px"');

  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  /* 适配暗黑模式的淡化边框 */
  border: 1px solid var(--el-border-color-lighter);
  overflow: hidden;
  user-select: none;

  :deep(.el-avatar) {
    width: 100%;
    height: 100%;
    display: block;
  }

  /* 悬停遮罩层 */
  .avatar-mask {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0, 0, 0, 0.5); /* 遮罩层在白天和黑夜都适用纯黑透明 */
    display: flex; flex-direction: column; justify-content: center; align-items: center;
    opacity: 0; transition: opacity 0.3s; color: #fff;
    .mask-icon { font-size: 24px; margin-bottom: 5px; }
    .mask-text { font-size: 12px; }
  }
  &:hover .avatar-mask { opacity: 1; }
}

/* =========================================
   2. 裁剪框与预览区整体容器
   ========================================= */
.cropper-area,
.preview-area {
  height: 300px;
  position: relative;
}

.cropper-instance {
  width: 100%;
  height: 100%;
}

/* 圆形预览框容器 */
.preview-box {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  /* 适配暗黑模式下的圆形边框和底层背景色 */
  border: 1px solid var(--el-border-color);
  background-color: var(--el-bg-color-page);
  overflow: hidden;
  box-shadow: var(--el-box-shadow-light);

  .preview-wrapper {
    width: 100%;
    height: 100%;
  }
}

/* =========================================
   3. Vue Cropper 样式深度覆盖与暗黑适配
   ========================================= */
:deep(.vue-cropper) {
  position: relative;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  user-select: none;
  direction: ltr;
  touch-action: none;
  text-align: left;
  /* 圆角适配 */
  border-radius: 8px;
  overflow: hidden;

  /* 核心：动态适应明暗模式的透明棋盘格背景 */
  background-image: linear-gradient(45deg, var(--el-fill-color-light) 25%, transparent 25%, transparent 75%, var(--el-fill-color-light) 75%, var(--el-fill-color-light)),
  linear-gradient(45deg, var(--el-fill-color-light) 25%, transparent 25%, transparent 75%, var(--el-fill-color-light) 75%, var(--el-fill-color-light));
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
  background-color: var(--el-bg-color-overlay);
}

:deep(.cropper-box),
:deep(.cropper-box-canvas),
:deep(.cropper-drag-box),
:deep(.cropper-crop-box),
:deep(.cropper-face) {
  position: absolute;
  top: 0; right: 0; bottom: 0; left: 0;
  user-select: none;
}

:deep(.cropper-view-box) {
  display: block;
  overflow: hidden;
  width: 100%;
  height: 100%;
  /* 裁剪框边框使用主题色 */
  outline: var(--el-color-primary) solid 1px;
  user-select: none;
}

:deep(.cropper-view-box img) {
  user-select: none;
  text-align: left;
  max-width: none;
  max-height: none;
}

:deep(.cropper-face) {
  top: 0; left: 0;
  background-color: #fff;
  opacity: 0.1; /* 因为只有 0.1 的透明度，黑白模式下都适用 */
}

/* =========================================
   4. 底部工具栏布局
   ========================================= */
.action-row {
  margin-top: 20px;
  display: flex;
  align-items: center;
}

.tool-buttons {
  display: flex;
  align-items: center;
  gap: 15px; /* 利用 gap 取代 col 分栏，弹性更好 */
}

.btn-group {
  display: flex;
}

.submit-button-col {
  display: flex;
  justify-content: flex-end; /* 桌面端靠右对齐 */
}

.submit-btn {
  width: 120px;
}

/* =========================================
   5. 响应式处理
   ========================================= */
/* 使用 :global 穿透设置 Dialog 宽度 */
:global(.cropper-dialog) {
  width: 700px;
}

@media screen and (max-width: 768px) {
  :global(.cropper-dialog) {
    width: 90vw !important;
  }

  .cropper-area {
    height: 250px;
  }

  .preview-area {
    height: 200px;
    margin-top: 15px;
  }

  .preview-box {
    width: 150px;
    height: 150px;
  }

  /* 移动端操作栏适配 */
  .action-row {
    flex-direction: column;
    gap: 15px;
  }

  .tool-buttons {
    width: 100%;
    justify-content: space-between;
  }

  .submit-button-col {
    width: 100%;
    justify-content: center;
  }

  .submit-btn {
    width: 100%; /* 移动端提交按钮铺满 */
  }
}
</style>