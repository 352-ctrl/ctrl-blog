import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getUserProfile } from '@/api/front/system/userInfo.js'
import { login as loginApi, logout as logoutApi } from '@/api/common/auth.js'
import { getUnreadCount } from '@/api/front/interaction/message.js'
import router from "@/router/index.js";

export const useUserStore = defineStore('user', () => {
    // 1. State: 定义数据
    // 初始化时尝试从本地缓存读取，防止刷新丢失
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
    const isLoading = ref(false)

    // 全局未读消息数与定时器
    const unreadCount = ref(0)

    let unreadTimer = null
    let isFetching = false
    // 全局 AuthDialog 弹窗状态
    const showAuthDialog = ref(false)
    const authDialogMode = ref('login')

    // 2. Getters - 提供常用属性的快捷访问
    const isLoggedIn = computed(() => !!token.value)

    // 超级管理员专属判断
    const isSuperAdmin = computed(() => userInfo.value?.role === 'SUPER_ADMIN')
    // 只要是 ADMIN 或 SUPER_ADMIN，都属于“广义的管理员”
    const isAdmin = computed(() => userInfo.value?.role === 'ADMIN' || userInfo.value?.role === 'SUPER_ADMIN')

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

    // 获取未读消息数
    const fetchUnreadCount = async () => {
        // 如果未登录，或正在请求中，则拦截（完美解决请求发两次的问题）
        if (!token.value || isFetching) return
        isFetching = true
        try {
            const res = await getUnreadCount()
            if (res.code === 200) {
                unreadCount.value = res.data
            }
        } catch (error) {
            console.error('获取未读消息失败', error)
        } finally {
            isFetching = false
        }
    }

    // 启动未读消息轮询
    const startUnreadPolling = () => {
        if (!token.value) return
        fetchUnreadCount() // 立即查一次
        // 如果还没有定时器，则开启 3 分钟轮询
        if (!unreadTimer) {
            unreadTimer = setInterval(fetchUnreadCount, 3 * 60 * 1000)
        }
    }

    // 停止未读消息轮询
    const stopUnreadPolling = () => {
        if (unreadTimer) {
            clearInterval(unreadTimer)
            unreadTimer = null
        }
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

                // 登录成功后启动未读消息轮询
                startUnreadPolling()

                return {
                    success: true,
                    data: res.data
                }
            } else {
                return {
                    success: false,
                    code: res.code,
                    msg: res.msg || '登录失败'
                }
            }
        } catch (error) {
            console.error('登录失败:', error)
            return {
                success: false,
                code: error.code,
                msg: error.msg
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
                    console.log("前端收到的处理后的数据：", res.data);
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
    const logout = async () => {
        try {
            // 如果存在 token，则调用后端注销接口，将 token 放入 Redis 黑名单
            if (token.value) {
                await logoutApi()
            }
        } catch (error) {
            // 即使后端请求失败（例如网络断开或 token 已经过期），也要继续执行清理动作
            console.error('后端退出登录状态异常', error)
        } finally {
            // 无论请求结果如何，前端必须无条件清空本地状态并重定向
            clearUserData()
            router.push('/')
        }
    }

    // 清除用户数据
    const clearUserData = () => {
        stopUnreadPolling()

        isFetching = false

        token.value = ''
        userInfo.value = {}
        unreadCount.value = 0

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
        unreadCount,

        // Getters
        isLoggedIn,
        isSuperAdmin, // 暴露给外部组件使用
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
        clearUserData,
        fetchUnreadCount,
        startUnreadPolling,
        stopUnreadPolling
    }
})