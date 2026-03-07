import axios from 'axios'
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

// 添加全局标志位，防止重复显示登录提示
let isShowingLoginMessage = false

const request = axios.create({
    baseURL: 'http://localhost:8080/',
    timeout: 5000 // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
    }
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['token'] = token
    }
    return config;
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }

        if (res.code && res.code !== 200) {

            // 1. 处理 401 登录过期
            if (res.code === 401 || res.code === 4031 || res.code === 4032) {
                if (!isShowingLoginMessage) {
                    isShowingLoginMessage = true

                    ElMessage({
                        message: res.msg || '登录已过期，请重新登录',
                        type: 'error',
                        duration: 2000,
                        onClose: () => {
                            // 重置标志位，允许下次再显示
                            isShowingLoginMessage = false
                        }
                    })

                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')

                    // 判断当前页面是否必须登录
                    const currentRoute = router.currentRoute.value;
                    // 使用 matched 兼容嵌套路由的 meta 继承
                    const isRequiresAuth = currentRoute.matched.some(record => record.meta.requiresAuth);

                    if (isRequiresAuth) {
                        // 必须登录的页面（如后台、消息中心），将其踢回主页（或者登录页）
                        router.push("/")
                    } else {
                        // 如果是前台无需登录的页面，刷新页面让其变为游客状态
                        window.location.reload();
                    }
                }
            }
            // 2. 处理 HTTP 权限不足
            else if (res.code === 403) {
                ElMessage({ message: '权限不足', type: 'warning', grouping: true });
                router.replace('/403');
            }
            // 3. 处理其他常规业务错误
            else {
                ElMessage({ message: res.msg || '操作失败', type: 'error', grouping: true });
            }

            // 抛出异常，中断 Promise 链。
            // 这样 Vue 组件里就不会再进入 .then() 的成功逻辑了
            return Promise.reject(new Error(res.msg || 'Error'))
        }

        // 状态码 200，正常放行
        return res;
    },
    error => {
        // ================= 全局 HTTP 状态码拦截 =================
        let errorMsg = '网络请求异常';
        if (error.response) {
            if (error.response.status === 404) {
                errorMsg = '未找到请求接口';
            } else if (error.response.status === 500) {
                errorMsg = '系统异常，请联系管理员';
            } else if (error.response.status === 403) {
                errorMsg = '权限不足';
            } else {
                errorMsg = error.message;
            }
        }

        ElMessage({
            message: errorMsg,
            type: 'error',
            grouping: true // 同样开启合并
        });

        return Promise.reject(error);
    }
)


export default request