package com.example.blog.common.constants;

/**
 * 响应提示消息常量配置
 * 集中管理后端返回给前端的提示语（Message）
 */
public final class MessageConstants {

    /**
     * 私有构造方法，防止实例化
     */
    private MessageConstants() {
        throw new IllegalStateException("MessageConstants 常量类禁止实例化");
    }

    /* ============================== 1. 系统与通用消息 ============================== */

    /**
     * 系统监控：游客/未登录标识
     */
    public static final String MSG_LOG_GUEST = "游客/未登录";
    /**
     * 系统监控：日志记录失败
     */
    public static final String MSG_LOG_ERROR = "记录操作日志失败";

    /**
     * 通用：操作成功
     */
    public static final String MSG_SUCCESS = "操作成功";
    /**
     * 通用：参数错误
     */
    public static final String MSG_PARAM_ERROR = "参数错误";
    /**
     * 通用：系统内部异常
     */
    public static final String MSG_SYSTEM_ERROR = "系统异常，请联系管理员";
    /**
     * 通用：Spring Context 为空
     */
    public static final String MSG_ERR_SPRING_CONTEXT_NULL = "ApplicationContext 为空，Spring 上下文尚未初始化";
    /**
     * 通用：新增失败
     */
    public static final String MSG_SAVE_FAILED = "新增失败";
    /**
     * 通用：更新失败
     */
    public static final String MSG_UPDATE_FAILED = "更新失败";
    /**
     * 通用：权限不足
     */
    public static final String MSG_NO_PERMISSION = "权限不足";
    /**
     * 通用：批量删除失败
     */
    public static final String MSG_BATCH_DELETE_FAILED = "批量删除失败";

    /**
     * IP解析：未知位置
     */
    public static final String MSG_UNKNOWN_LOCATION = "未知位置";

    /**
     * IP解析：内网IP
     */
    public static final String MSG_LOCAL_IP = "内网IP";


    /* ============================== 2. 认证与授权消息 ============================== */

    /**
     * 未登录提示
     */
    public static final String MSG_NOT_LOGIN = "请先登录";
    /**
     * 登录失败：账号密码错误
     */
    public static final String MSG_LOGIN_ERROR = "账号或密码错误";
    /**
     * 认证失败：Token 无效或由过滤器拦截
     */
    public static final String MSG_AUTH_FAILED = "认证失败，请重新登录";
    /**
     * Token 解析失败或过期
     */
    public static final String MSG_TOKEN_INVALID = "token无效或已过期";
    /**
     * 角色信息无效（解析Token时角色无法识别）
     */
    public static final String MSG_ROLE_INVALID = "角色信息无效";
    /**
     * 账号锁定提示 (包含占位符 %d)
     */
    public static final String MSG_LOGIN_LOCKED = "密码错误次数过多，账号已被锁定，请 %d 分钟后再试";
    /**
     * 注册邮件标题
     */
    public static final String MSG_EMAIL_SUBJECT_REGISTER = "【您的博客】注册验证码";
    /**
     * 验证码已过期
     */
    public static final String MSG_CODE_EXPIRED = "验证码已过期，请重新获取";
    /**
     * 验证码错误
     */
    public static final String MSG_CODE_ERROR = "验证码错误";
    /**
     * 发送频率限制
     */
    public static final String MSG_SEND_FREQUENTLY = "请求过于频繁，请稍后再试";
    /**
     * 账号封禁
     */
    public static final String MSG_ACCOUNT_BANNED = "账号已被封禁，请联系管理员";


    /* ============================== 3. 用户与账号消息 ============================== */

    /**
     * 用户已存在
     */
    public static final String MSG_USER_EXIST = "该用户已存在";
    /**
     * 用户不存在
     */
    public static final String MSG_USER_NOT_EXIST = "用户不存在";
    /**
     * 邮箱已被注册
     */
    public static final String MSG_EMAIL_EXIST = "该邮箱已被注册";
    /**
     * 修改密码：两次输入不一致
     */
    public static final String MSG_PASSWORD_NOT_SAME = "新密码与确认密码不一致";
    /**
     * 修改密码：旧密码错误
     */
    public static final String MSG_OLD_PASSWORD_ERROR = "原密码错误";
    /**
     * 修改密码：新旧密码相同
     */
    public static final String MSG_NEW_PASSWORD_SAME_AS_OLD = "新密码不能与旧密码相同";
    /**
     * 删除用户：无法删除自己
     */
    public static final String MSG_CANNOT_DELETE_SELF = "无法删除当前登录账户";
    /**
     * 删除用户：无法删除超级管理员
     */
    public static final String MSG_CANNOT_DELETE_ADMIN = "无法删除系统超级管理员";


    /* ============================== 4. 内容业务消息 ============================== */

    /**
     * 文章不存在
     */
    public static final String MSG_ARTICLE_NOT_EXIST = "文章不存在";

    /**
     * 分类已存在
     */
    public static final String MSG_CATEGORY_EXIST = "该分类已存在";
    /**
     * 分类不存在
     */
    public static final String MSG_CATEGORY_NOT_EXIST = "分类不存在";

    /**
     * 标签已存在
     */
    public static final String MSG_TAG_EXIST = "该标签已存在";
    /**
     * 标签不存在
     */
    public static final String MSG_TAG_NOT_EXIST = "标签不存在";

    /**
     * 评论不存在
     */
    public static final String MSG_COMMENT_NOT_EXIST = "评论不存在";

    /**
     * 公告不存在
     */
    public static final String MSG_NOTICE_NOT_EXIST = "公告不存在";


    /* ============================== 5. 文件与任务消息 ============================== */

    /**
     * 文件不存在
     */
    public static final String MSG_FILE_NOT_EXIST = "文件不存在";
    /**
     * 上传成功
     */
    public static final String MSG_UPLOAD_SUCCESS = "上传成功";
    /**
     * 上传失败
     */
    public static final String MSG_UPLOAD_FAILURE = "上传失败";
    /**
     * 上传文件为空
     */
    public static final String MSG_FILE_IS_EMPTY = "上传文件不能为空";

    /**
     * Cron 表达式格式错误
     */
    public static final String MSG_CRON_FORMAT_ERROR = "Cron表达式格式错误";
    /**
     * 定时任务不存在
     */
    public static final String MSG_JOB_NOT_EXIST = "任务不存在";

    /* ============================== 6. 互动与点赞消息 ============================== */

    /**
     * 重复点赞提示
     */
    public static final String MSG_ALREADY_LIKED = "您已经点过赞了";

    /**
     * 取消点赞失败提示 (未找到记录)
     */
    public static final String MSG_LIKE_NOT_FOUND = "未找到点赞记录，无法取消";

    /**
     * 重复收藏提示
     */
    public static final String MSG_ALREADY_FAVORITE = "您已经收藏过该文章";

    /**
     * 取消收藏失败提示 (未找到记录)
     */
    public static final String MSG_FAVORITE_NOT_FOUND = "未找到收藏记录，无法取消";

    /* ============================== 7. 日志专属提示消息 ============================== */
    /** 日志：登录成功 */
    public static final String LOG_LOGIN_SUCCESS = "登录成功";
    /** 日志：密码错误 */
    public static final String LOG_LOGIN_PWD_ERROR = "密码错误";
    /** 日志：账号被锁定 */
    public static final String LOG_LOGIN_LOCKED = "账号被锁定，限制登录";
    /** 日志：账号已封禁 */
    public static final String LOG_LOGIN_BANNED = "账号已被封禁";
}