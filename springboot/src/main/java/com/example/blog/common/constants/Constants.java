package com.example.blog.common.constants;

/**
 * 系统全局通用常量配置
 * 包含 HTTP 协议、JWT 令牌、基础配置等不属于特定业务模块的
 */
public final class Constants {

    /**
     * 私有构造方法，防止实例化
     */
    private Constants() {
        throw new IllegalStateException("SystemConstants 常量类禁止实例化");
    }

    // ============================== HTTP 与 基础协议 ==============================
    /**
     * HTTP请求头中的Token名称
     */
    public static final String HEADER_TOKEN = "token";

    /**
     * JWT载荷 Key：用户ID
     */
    public static final String CLAIM_ID = "id";

    /**
     * JWT载荷 Key：用户角色
     */
    public static final String CLAIM_ROLE = "role";

    /**
     * JWT载荷 Key：用户昵称
     */
    public static final String CLAIM_NICKNAME = "nickname";

    /**
     * HTTP请求头：User-Agent
     */
    public static final String HEADER_USER_AGENT = "User-Agent";


    // ============================== 业务逻辑与状态控制 ==============================

    /* ---------- 用户与封禁配置 ---------- */
    /**
     * 默认昵称前缀
     */
    public static final String DEFAULT_NICKNAME_PREFIX = "用户_";

    /**
     * 未知/已注销用户的默认占位昵称
     */
    public static final String DEFAULT_UNKNOWN_NICKNAME = "账号已注销";

    /**
     * 匿名游客的默认占位昵称
     */
    public static final String DEFAULT_GUEST_NICKNAME = "匿名游客";

    /**
     * 永久封禁年份判定阈值
     */
    public static final int PERMANENT_BAN_YEAR_THRESHOLD = 2090;

    /**
     * 账号默认无封禁原因时的占位符
     */
    public static final String DEFAULT_NO_REASON = "无";

    /**
     * 拼接封禁原因时的前缀
     */
    public static final String BAN_REASON_PREFIX = "。原因：";


    /* ---------- 时间格式 ---------- */
    /**
     * 短日期时间格式 (不含秒)
     */
    public static final String FORMAT_DATETIME_SHORT = "yyyy-MM-dd HH:mm";


    /* ---------- 邮件模板与标题配置 ---------- */
    public static final String TEMPLATE_REGISTER_CODE = "register_code.ftl";
    public static final String TEMPLATE_FEEDBACK_ADMIN = "feedback_admin_notice.ftl";
    public static final String TEMPLATE_FEEDBACK_REPLY = "feedback_reply.ftl";

    public static final String EMAIL_SUBJECT_REGISTER = "【CtrlBlog】注册验证码";
    public static final String EMAIL_SUBJECT_RESET = "找回密码验证码";
    public static final String EMAIL_SUBJECT_BIND = "【CtrlBlog】换绑邮箱验证码";
    public static final String EMAIL_TITLE_BIND = "换绑邮箱验证码";
    public static final String EMAIL_SUBJECT_FEEDBACK_ADMIN = "【系统通知】收到新的用户反馈";
    public static final String EMAIL_SUBJECT_FEEDBACK_REPLY = "【CtrlBlog】您的反馈已收到回复";


    /* ---------- 分页通用配置 ---------- */
    public static final Integer PAGE_NUM_DEFAULT = 1;
    public static final Integer PAGE_SIZE_DEFAULT = 10;
    public static final Long COMMENT_ROOT_PARENT_ID = 0L;


    /* ---------- 逻辑删除配置 ---------- */
    public static final String DELETE_PREFIX = "#deleted_";
    public static final String DELETED_EMAIL_SUFFIX = "@null.com";


    // ============================== 外部资源与配置 ==============================

    /* ---------- UI 颜色 ---------- */
    public static final String COLOR_SUCCESS = "#67c23a";
    public static final String COLOR_INFO = "#909399";

    /* ---------- 数据脱敏与掩码 ---------- */
    public static final String MASK_PASSWORD = "******";
    public static final char SENSITIVE_REPLACE_CHAR = '*';
    public static final char SENSITIVE_REPLACE_ARTICLE = '█';
    public static final String[] SENSITIVE_KEYS = {"password", "confirmPassword", "oldPassword", "newPassword"};


    // ============================== 日志与网络 ==============================

    /* ---------- 内部日志记录 ---------- */
    public static final String LOG_LOGIN_SUCCESS = "登录成功";
    public static final String LOG_LOGIN_PWD_ERROR = "密码错误";
    public static final String LOG_LOGIN_LOCKED = "账号被锁定，限制登录";
    public static final String LOG_LOGIN_BANNED = "账号已被封禁";
    public static final String LOG_REGISTER_AND_LOGIN_SUCCESS = "注册并自动登录成功";
    public static final int SYS_LOG_MAX_LENGTH = 2000;


    /* ---------- IP 定位与网络标识 ---------- */
    public static final String IP_UNKNOWN = "unknown";
    public static final String UNKNOWN = "Unknown";
    public static final String LOCATION_UNKNOWN = "未知位置";
    public static final String LOCATION_LOCAL_IP = "内网IP";

    public static final String IP_LOCAL_V6 = "0:0:0:0:0:0:0:1";
    public static final String IP_LOCAL_V4 = "127.0.0.1";
    public static final String IP_LOCAL_PREFIX_192 = "192.168.";
    public static final String IP_LOCAL_PREFIX_10 = "10.";

    public static final String IP2REGION_FILE_PATH = "ip2region_v4.xdb";

    public static final String[] IP_HEADER_CANDIDATES = {
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };
}