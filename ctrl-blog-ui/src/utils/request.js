import axios from 'axios'
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {useUserStore} from "@/store/user.js";

// 添加全局标志位，防止重复显示登录提示
let isShowingLoginMessage = false
// 全局拦截熔断开关
let isTokenExpired = false

const request = axios.create({
    baseURL: import.meta.env.DEV ? 'http://localhost:8080' : '',
    timeout: 5000 // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    // 白名单机制：如果当前请求是去登录的，立刻解除熔断状态！
    if (config.url.includes('/auth/token') || config.url.includes('/login')) {
        isTokenExpired = false;
    }

    // 短路防御：如果已经确诊 Token 过期，直接取消这批请求，绝对不发给后端！
    if (isTokenExpired) {
        return Promise.reject(new axios.Cancel("Token已过期，请求被前端拦截熔断"));
    }

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
            if (res.code === 401) {
                handleUnauthorized();
                return Promise.reject(new Error('未授权')); // 中断后续的报错弹窗
            }
            // 2. 对于 4031(封禁) 和 4032(封禁)，把完整的 res 对象抛出去
            else if (res.code === 4031 || res.code === 4032) {
                return Promise.reject(res);
            }
            // 3. 处理 HTTP 权限不足
            else if (res.code === 403) {
                ElMessage({ message: '权限不足', type: 'warning', grouping: true });
                router.replace('/403');
            }
            else if (res.code === 429) {
                ElMessage({ message: res.msg || '请求过于频繁，请稍后再试', type: 'warning', grouping: true });
            }
            // 4. 处理其他常规业务错误
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
            // 获取 HTTP 真实状态码
            const status = error.response.status;

            if (status === 401) {
                handleUnauthorized();
                return Promise.reject(error);
            } else if (status === 404) {
                errorMsg = '未找到请求接口';
            } else if (status === 500) {
                errorMsg = '系统异常，请联系管理员';
            } else if (status === 403) {
                errorMsg = '权限不足';
            } else {
                errorMsg = error.response.data?.msg || error.message;
            }
        } else {
            // 连 error.response 都没有，说明是网络断开或后端没启动 (ERR_CONNECTION_REFUSED)
            errorMsg = '无法连接到服务器，请检查网络或后端服务是否启动';
        }

        ElMessage({
            message: errorMsg,
            type: 'error',
            grouping: true // 同样开启合并
        });

        return Promise.reject(error);
    }
)

function handleUnauthorized() {
    isTokenExpired = true;

    // 500 毫秒后自动解除熔断
    setTimeout(() => {
        isTokenExpired = false;
    }, 500);

    if (!isShowingLoginMessage) {
        isShowingLoginMessage = true

        ElMessage({
            message: '登录已过期，请重新登录',
            type: 'error',
            duration: 2000,
            onClose: () => {
                isShowingLoginMessage = false
            }
        })

        const userStore = useUserStore();
        userStore.clearUserData();

        // 判断当前页面是否必须登录
        const currentRoute = router.currentRoute.value;
        const isRequiresAuth = currentRoute.matched.some(record => record.meta.requiresAuth);

        if (isRequiresAuth) {
            router.push("/")
        } else {
            userStore.openAuthDialog('login');
        }
    }
}
export default request