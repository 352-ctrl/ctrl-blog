<template>
  <div class="img-cropper-component">
    <div
        class="avatar-trigger"
        :style="{ width: size + 'px', height: size + 'px' }"
        @click="openDialog"
    >
      <el-avatar :size="size" :src="modelValue || defaultAvatar" shape="circle" />
      <div class="avatar-mask">
        <el-icon class="mask-icon"><Camera /></el-icon>
        <span class="mask-text">修改头像</span>
      </div>
    </div>

    <el-dialog
        :title="title"
        v-model="dialogVisible"
        width="700px"
        append-to-body
        :close-on-click-modal="false"
        destroy-on-close
        @opened="modalOpened"
        @closed="handleClosed"
        class="cropper-dialog"
    >
      <el-row>
        <el-col :xs="24" :sm="12" style="height: 300px">
          <vue-cropper
              ref="cropperRef"
              :img="options.img"
              :info="true"
              :autoCrop="options.autoCrop"
              :autoCropWidth="options.autoCropWidth"
              :autoCropHeight="options.autoCropHeight"
              :fixedBox="options.fixedBox"
              :outputType="options.outputType"
              @realTime="realTime"
              v-if="visible"
              style="width: 100%; height: 100%;"
          />
        </el-col>

        <el-col :xs="24" :sm="12" style="height: 300px; position: relative;">
          <div class="preview-box">
            <div :style="previews.div" class="preview-wrapper">
              <img
                  v-if="previews.url"
                  :src="previews.url"
                  :style="previews.img"
              />
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row style="margin-top: 12px">
        <el-col :xs="20" :sm="12">
          <el-row>
            <el-col :xs="6" :sm="8">
              <el-upload
                  action="#"
                  :http-request="() => {}"
                  :before-upload="beforeUpload"
                  :show-file-list="false"
              >
                <el-button>选择</el-button>
              </el-upload>
            </el-col>
            <el-col :span="4">
              <el-button :icon="Plus" @click="changeScale(1)"></el-button>
            </el-col>
            <el-col :span="4">
              <el-button :icon="Minus" @click="changeScale(-1)"></el-button>
            </el-col>
            <el-col :span="4">
              <el-button :icon="RefreshLeft" @click="rotateLeft"></el-button>
            </el-col>
            <el-col :span="4">
              <el-button :icon="RefreshRight" @click="rotateRight"></el-button>
            </el-col>
          </el-row>
        </el-col>
        <el-col :span="4" class="desktop-offset">
          <el-button type="primary" @click="uploadImg" :loading="loading">提 交</el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import { Plus, Minus, RefreshLeft, RefreshRight, Camera } from "@element-plus/icons-vue";
import { VueCropper } from "vue-cropper";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/store/user.js";
import { uploadFile } from "@/api/file.js";

/**
 * =========================================================================
 * 1. Props 定义
 * =========================================================================
 */
const props = defineProps({
  /** 当前头像 URL (v-model 绑定) */
  modelValue: { type: String, default: '' },
  /** 弹窗标题 */
  title: { type: String, default: "修改头像" },
  /** 头像显示尺寸 (px) */
  size: { type: Number, default: 100 },
  /** 上传接口地址 */
  uploadUrl: { type: String, default: 'http://localhost:9999/api/files/upload' }
});

/**
 * =========================================================================
 * 2. Events 定义
 * =========================================================================
 */
const emit = defineEmits([
  'update:modelValue', // 更新绑定的图片地址
  'upload-success'     // 上传成功回调，返回新图片数据
]);

/**
 * =========================================================================
 * 3. 状态管理
 * =========================================================================
 */
const userStore = useUserStore();
const dialogVisible = ref(false); // 控制弹窗显隐
const visible = ref(false);       // 控制 cropper 组件渲染（确保弹窗打开后渲染）
const loading = ref(false);       // 提交按钮 loading 状态
const cropperRef = ref(null);     // 裁剪组件引用
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 裁剪配置项
const options = reactive({
  img: props.modelValue || defaultAvatar, // 裁剪图片的地址
  autoCrop: true,         // 是否默认生成截图框
  autoCropWidth: 200,     // 默认生成截图框宽度
  autoCropHeight: 200,    // 默认生成截图框高度
  fixedBox: true,         // 固定截图框大小 不允许改变
  outputType: "png",      // 默认生成截图为PNG格式
  centerBox: true,        // 截图框是否被限制在图片里面
  info: true              // 裁剪框的大小信息
});

// 实时预览数据
const previews = ref({});

/**
 * =========================================================================
 * 4. 交互逻辑
 * =========================================================================
 */

/**
 * 打开裁剪弹窗
 * 初始化图片为当前 modelValue 或默认图
 */
const openDialog = () => {
  options.img = props.modelValue || defaultAvatar;
  dialogVisible.value = true;
};

/**
 * 弹窗完全打开回调
 * 延迟渲染 vue-cropper 以避免宽高计算错误
 */
const modalOpened = () => {
  visible.value = true;
};

const closeDialog = () => {
  dialogVisible.value = false;
};

/**
 * 弹窗关闭回调
 * 重置状态与 loading
 */
const handleClosed = () => {
  visible.value = false;
  loading.value = false;
};

/**
 * 上传前校验
 * @param {File} file - 上传的文件对象
 * @returns {boolean} - 返回 false 阻止 element-upload 默认上传行为
 */
const beforeUpload = (file) => {
  if (file.type.indexOf("image/") === -1) {
    ElMessage.error("请上传图片类型文件!");
    return false;
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error("文件大小不能超过5MB!");
    return false;
  }
  // 使用 FileReader 读取本地文件并在裁剪区预览
  const reader = new FileReader();
  reader.readAsDataURL(file);
  reader.onload = () => {
    options.img = reader.result;
  };
  return false; // 阻止默认上传
};

/**
 * 缩放图片
 * @param {number} num - 正数放大，负数缩小
 */
const changeScale = (num) => {
  cropperRef.value?.changeScale(num || 1);
};

/** 左旋转 90度 */
const rotateLeft = () => {
  cropperRef.value?.rotateLeft();
};

/** 右旋转 90度 */
const rotateRight = () => {
  cropperRef.value?.rotateRight();
};

/**
 * 实时预览回调
 * @param {Object} data - vue-cropper 返回的预览样式数据
 */
const realTime = (data) => {
  previews.value = data;
};

/**
 * 提交裁剪并上传
 * 流程：获取裁剪 Blob -> 封装 FormData -> Axios 上传 -> 触发回调
 */
const uploadImg = () => {
  if (!options.img) {
    ElMessage.warning('请先选择图片');
    return;
  }
  loading.value = true;
  cropperRef.value.getCropBlob(async (data) => {
    let formData = new FormData();
    formData.append("file", data, "avatar.png");
    try {
      const res = await uploadFile(formData);
      const result = res.data;
      if (result.code === 200) {
        ElMessage.success('头像修改成功，请点击保存按钮生效');
        emit('update:modelValue', result.data.avatar);
        emit('upload-success', result.data.avatar);
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
   1. 外部触发器样式
   ========================================= */
.avatar-trigger {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
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
    background: rgba(0, 0, 0, 0.5);
    display: flex; flex-direction: column; justify-content: center; align-items: center;
    opacity: 0; transition: opacity 0.3s; color: #fff;
    .mask-icon { font-size: 24px; margin-bottom: 5px; }
    .mask-text { font-size: 12px; }
  }
  &:hover .avatar-mask { opacity: 1; }
}

/* =========================================
   2. 弹窗内部样式
   ========================================= */
/* 圆形预览框容器 */
.preview-box {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  border: 1px solid #ccc;
  overflow: hidden;

  .preview-wrapper {
    width: 100%;
    height: 100%;
  }
}

/* Vue Cropper 样式覆盖：修复网格背景和定位问题 */
:deep(.vue-cropper) {
  position: relative;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  user-select: none;
  direction: ltr;
  touch-action: none;
  text-align: left;

  /* 棋盘格背景 */
  background-image: linear-gradient(45deg, #eee 25%, transparent 25%, transparent 75%, #eee 75%, #eee),
  linear-gradient(45deg, #eee 25%, transparent 25%, transparent 75%, #eee 75%, #eee);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
  background-color: #fff;
}

:deep(.cropper-box), :deep(.cropper-box-canvas), :deep(.cropper-drag-box), :deep(.cropper-crop-box), :deep(.cropper-face) {
  position: absolute;
  top: 0; right: 0; bottom: 0; left: 0;
  user-select: none;
}
:deep(.cropper-view-box) {
  display: block;
  overflow: hidden;
  width: 100%;
  height: 100%;
  outline: rgba(51, 153, 255, 0.75) solid 1px;
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
  opacity: 0.1;
}

/* 桌面端提交按钮偏移 */
.desktop-offset {
  margin-left: 20%;
}

/* 响应式处理 */
.cropper-dialog {
  width: 700px;
}

@media screen and (max-width: 768px) {
  .desktop-offset {
    margin-left: 0 !important;
  }

  /* 使用 :global 穿透 scoped，确保移动端 dialog 宽度覆盖 Element 默认样式 */
  :global(.cropper-dialog) {
    width: 400px !important;
    max-width: 95vw !important;
  }
}
</style>