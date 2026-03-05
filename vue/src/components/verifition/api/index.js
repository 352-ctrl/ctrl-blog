/**
 * =========================================================================
 * 声明：
 * 本文件属于 AJ-Captcha 行为验证码组件依赖。
 * 原项目主页：https://gitee.com/belief-team/captcha
 * =========================================================================
 */
import request from "@/utils/request.js";

//获取验证图片  以及token
export function reqGet(data) {
	return  request({
        url: '/captcha/get',
        method: 'post',
        data
    })
}

//滑动或者点选验证
export function reqCheck(data) {
	return  request({
        url: '/captcha/check',
        method: 'post',
        data
    })
}


