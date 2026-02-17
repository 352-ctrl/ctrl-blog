<template>
  <div :class="[isManager ? null : 'content-container']">
    <el-row :gutter="20">
      <el-col :xs="24" :md="7">
        <el-card class="box-card">
          <template #header>
            <span>个人信息</span>
          </template>

          <div style="text-align: center;">
            <AvatarUpload
                v-model="data.userForm.avatar"
                :size="100"
                @upload-success="handleSuccess"
            />

            <ul class="user-info-list">
              <li class="list-group-item">
                <div><el-icon><User /></el-icon>用户名称</div>
                <div>{{ userStore.nickname || '未设置' }}</div>
              </li>
              <li class="list-group-item">
                <div><el-icon><Message /></el-icon>用户邮箱</div>
                <div>{{ userStore.email || '未设置' }}</div>
              </li>
              <li v-if="isManager" class="list-group-item">
                <div><el-icon><UserFilled /></el-icon>所属角色</div>
                <div>{{ userStore.role }}</div>
              </li>
              <li class="list-group-item">
                <div><el-icon><Calendar /></el-icon>创建日期</div>
                <div>{{ userStore.createTime }}</div>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="17">
        <el-card class="box-card">
          <el-tabs v-model="activeTab" style="width: 100%">
            <el-tab-pane label="基本设置" name="base">
              <div style="font-size: 20px; font-weight: bold;margin-bottom: 20px">基本设置</div>

              <el-form ref="baseFormRef" :rules="baseRules" :model="data.userForm" label-width="100px">
                <el-form-item label="昵称">
                  <el-input v-model="data.userForm.nickname" autocomplete="off" placeholder="请输入昵称" />
                </el-form-item>
              </el-form>

              <div style="text-align: right">
                <el-button type="primary" style="padding: 18px 35px" @click="submitBaseForm">保存</el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <div style="font-size: 20px; font-weight: bold;margin-bottom: 20px">修改密码</div>
              <el-form ref="passwordFormRef" :rules="passwordRules" :model="passwordData" label-width="100px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordData.oldPassword" type="password" show-password autocomplete="off" placeholder="请输入当前密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordData.newPassword" type="password" show-password autocomplete="off" placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="new2Password">
                  <el-input v-model="passwordData.new2Password" type="password" show-password autocomplete="off" placeholder="请确认新密码" />
                </el-form-item>
              </el-form>

              <div style="text-align: right">
                <el-button type="primary" style="padding: 18px 35px" @click="submitPasswordForm">保存</el-button>
              </div>
            </el-tab-pane>

          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from '@/store/user.js'
import AvatarUpload from "./components/AvatarUpload.vue";
import {validateNickname, validatePasswordComplexity} from "@/utils/validate.js";
// 引入 API 模块
import {updateProfile,changePassword} from "@/api/userInfo.js"
import {useRoute} from "vue-router";

const userStore = useUserStore()

// 根据路由路径判断当前环境
const route = useRoute();
const isManager = computed(() => route.path.startsWith('/manager'));

const BASE_API = 'http://localhost:9999'
// 定义上传接口地址
const uploadFileUrl = `${BASE_API}/api/files/upload`;

// 默认选中基本设置
const activeTab = ref('base');
const baseFormRef = ref(null);
const passwordFormRef = ref(null);

// 单独为密码表单创建数据
const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  new2Password: ''
});

const data = reactive({
  userForm:{}
});

onMounted(() => {
  initBaseForm()
})

const handleSuccess = (url) => {
  // 构造参数，只更新头像
  if (data.userForm) {
    data.userForm.avatar = url;
  }
}

const initBaseForm = () => {
  // 确保 store 里有数据
  if (userStore.userInfo) {
    // 这种解构赋值方式，假设 Store 里的 userInfo 就是扁平的对象（id, nickname, avatar...）
    // 如果你的 Store 结构是 { user: { ... } }，请改为 userStore.userInfo.user
    data.userForm.id = userStore.userInfo.id
    data.userForm.nickname = userStore.userInfo.nickname
    data.userForm.avatar = userStore.userInfo.avatar
  }
}

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error("请再次确认新密码"));
  } else if (value !== data.userForm.newPassword) {
    callback(new Error("两次输入的密码不匹配"));
  } else {
    callback();
  }
};

const baseRules = {
  nickname: [
      { required: true, validator: validateNickname, trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePasswordComplexity, trigger: 'blur' }
  ],
  new2Password: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 修改基本信息
const submitBaseForm = () => {
  // 直接发送更新请求，因为没有验证规则
  updateProfile(data.userForm).then(async res => {
    if (res.code === 200) {
      ElMessage.success('保存成功');
      await userStore.fetchUserInfo();
    } else {
      ElMessage.error(res.msg);
    }
  }).catch(error => {
    ElMessage.error('请求失败: ' + error.message);
  });
};

// 修改密码
const submitPasswordForm = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      // 构造正确的请求参数
      const requestData = {
        id: data.userForm.id,
        oldPassword: passwordData.oldPassword,
        newPassword: passwordData.newPassword
      };

      changePassword(requestData).then(res => {
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          setTimeout(() => {
            userStore.logout();
          }, 1000);
        } else {
          ElMessage.error(res.msg);
        }
      }).catch(error => {
        ElMessage.error('请求失败: ' + error.message);
      });
    }
  });
};

</script>


<style scoped>
/* 右侧卡片头部文字大小 */
.box-card :deep(.el-card__header) {
  padding: 14px 15px 7px;
  min-height: 40px;
}
/* 左侧头像区域居中 */
.avatar-box {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}
/* 列表样式 */
.user-info-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.list-group-item {
  border-bottom: 1px solid #e7eaec;
  padding: 11px 0;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>