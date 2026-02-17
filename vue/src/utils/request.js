import axios from 'axios'
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

// 添加全局标志位，防止重复显示登录提示
let isShowingLoginMessage = false

const request = axios.create({
    baseURL: 'http://localhost:9999/',
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
        if (res.code === 401) {
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

                // 跳转到主页
                router.push("/")
            }
            return Promise.reject(new Error('Unauthorized'))
        }
        return res;
    },
    error => {
        if (error.response.status === 404) {
            ElMessage.error('未找到请求接口');
        } else if (error.response.status === 500) {
            ElMessage.error('系统异常，请联系管理员');
        } else {
            console.error(error.message);
        }
        return Promise.reject(error);
    }
)


export default request