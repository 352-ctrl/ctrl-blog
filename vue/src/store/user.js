import { defineStore } from 'pinia'
import {computed, ref} from 'vue'
import { getUserProfile } from '@/api/userInfo.js'
import { login as loginApi } from '@/api/auth.js'
import router from "@/router/index.js";

export const useUserStore = defineStore('user', () => {
    // 1. State: 定义数据
    // 初始化时尝试从本地缓存读取，防止刷新丢失
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
    const isLoading = ref(false)

    // 全局 AuthDialog 弹窗状态
    const showAuthDialog = ref(false)
    const authDialogMode = ref('login')

    // 2. Getters - 提供常用属性的快捷访问
    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
    const userId = computed(() => userInfo.value?.id)
    const nickname = computed(() => userInfo.value?.nickname)
    const avatar = computed(() => userInfo.value?.avatar)
    const email = computed(() => userInfo.value?.email)
    const role = computed(() => userInfo.value?.role)
    const createTime = computed(() => userInfo.value?.createTime)

    // 3. Actions: 定义操作

    // 新增：快捷唤起弹窗的方法
    const openAuthDialog = (mode = 'login') => {
        authDialogMode.value = mode
        showAuthDialog.value = true
    }

    // 登录动作
    const login = async (loginForm) => {
        isLoading.value = true
        try {
            const res = await loginApi(loginForm)
            if (res.code === 200) {
                // 保存 token
                token.value = res.data.token
                localStorage.setItem('token', res.data.token)

                if (res.data.userInfo) {
                    userInfo.value = res.data.userInfo
                    localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
                }

                return {
                    success: true,
                    data: res.data
                }
            } else {
                return {
                    success: false,
                    message: res.msg || '登录失败'
                }
            }
        } catch (error) {
            console.error('登录失败:', error)
            return {
                success: false,
                message: error.message || '登录失败，请检查网络'
            }
        } finally {
            isLoading.value = false
        }
    }

    // 获取最新用户信息（用于页面刷新或更新个人资料后）
    const fetchUserInfo = async () => {
        if (!token.value) return null
        try {
            const res = await getUserProfile()
            if (res.code === 200) {
                if (res.data != null) {
                    userInfo.value = res.data
                    // 同步更新本地缓存
                    localStorage.setItem('userInfo', JSON.stringify(res.data))
                } else {
                    console.warn('警告：fetchUserInfo 接口返回成功，但 data 为空！', res)
                }
            }
        } catch (error) {
            console.error('获取用户信息失败', error)
            if (error.response?.status === 401) {
                clearUserData()
            }
        }
        return null
    }

    // 退出登录
    const logout = () => {
        clearUserData()
        router.push('/')
    }

    // 清除用户数据
    const clearUserData = () => {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return {
        // State
        token,
        userInfo,
        isLoading,
        showAuthDialog,
        authDialogMode,

        // Getters
        isLoggedIn,
        isAdmin,
        userId,
        nickname,
        avatar,
        email,
        role,
        createTime,

        // Actions
        openAuthDialog,
        login,
        fetchUserInfo,
        logout,
        clearUserData
    }
})