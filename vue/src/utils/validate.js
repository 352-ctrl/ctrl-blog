/* ================= 正则表达式定义 ================= */
// 昵称：中文、字母、数字、下划线，2-20个字符
export const regNickname = /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/;
// 邮箱：标准邮箱格式
export const regEmail = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
// 密码：字母、数字、英文符号，8-20位，必须包含字母和数字
export const regPasswordStrength = /^(?=.*[a-zA-Z])(?=.*\d)[\x21-\x7E]{8,20}$/;
// 调用目标字符串：Bean名.方法名(参数)
export const regInvokeTarget = /^[A-Za-z0-9_]+\.[A-Za-z0-9_]+\(.*?\)$/;
// 验证码：6位纯数字
export const regVerifyCode = /^\d{6}$/;

/**
 * 验证昵称
 */
export const validateNickname = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入昵称"));
    }
    const nickname = value.trim();
    if (nickname.length < 2 || nickname.length > 20) {
        return callback(new Error("昵称长度应在2-20个字符之间"));
    }
    if (!regNickname.test(nickname)) {
        return callback(new Error("昵称只能包含中文、字母、数字和下划线"));
    }
    callback();
};

/**
 * 验证邮箱
 */
export const validateEmail = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入邮箱"));
    }
    const email = value.trim();
    if (email.length > 100) {
        return callback(new Error("邮箱长度不能超过100个字符"));
    }
    if (!regEmail.test(value.trim())) {
        return callback(new Error("请输入有效的邮箱地址"));
    }
    callback();
};

/**
 * 验证密码复杂度
 */
export const validatePasswordComplexity = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入密码"));
    }
    const password = value.trim();
    if (password.length < 8 || password.length > 20) {
        return callback(new Error("密码长度应在8-20位之间"));
    }
    if (!regPasswordStrength.test(password)) {
        return callback(new Error("密码必须包含字母和数字"));
    }
    callback();
};

/**
 * 验证调用目标字符串
 */
export const validateInvokeTarget = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("调用目标不能为空"));
    }
    // 长度验证交给rules里的max处理，或者在这里写也行，但分开更清晰
    if (!regInvokeTarget.test(value)) {
        return callback(new Error("调用目标格式错误，示例：beanName.methodName()"));
    }
    callback();
};

/**
 * 验证邮箱验证码
 */
export const validateVerifyCode = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入验证码"));
    }
    const code = value.trim();
    if (!regVerifyCode.test(code)) {
        return callback(new Error("验证码必须为6位数字"));
    }
    callback();
};