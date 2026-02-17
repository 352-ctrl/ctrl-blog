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

    // ============================== 业务逻辑与状态控制 ==============================

    /* ---------- 用户默认配置 ---------- */
    /**
     * 默认昵称前缀
     */
    public static final String DEFAULT_NICKNAME_PREFIX = "用户_";

    /**
     * 默认用户头像
     */
    public static final String DEFAULT_AVATAR = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";


    /* ---------- 分页通用配置 ---------- */
    /**
     * 默认页码
     */
    public static final Integer PAGE_NUM_DEFAULT = 1;

    /**
     * 默认每页条数
     */
    public static final Integer PAGE_SIZE_DEFAULT = 10;

    /**
     * 每页条数最小值 (用于校验)
     */
    public static final int PAGE_SIZE_MIN = 10;

    /**
     * 每页条数最大值 (用于校验)
     */
    public static final int PAGE_SIZE_MAX = 100;

    /**
     * 顶级评论的父ID (根节点)
     */
    public static final Long COMMENT_ROOT_PARENT_ID = 0L;

    /* ---------- 逻辑删除配置 ---------- */
    /**
     * 删除标记前缀（用于逻辑删除防唯一键冲突）
     */
    public static final String DELETE_PREFIX = "#deleted_";

    // ============================== 外部资源与配置 ==============================

    /* ---------- 数据脱敏与掩码 ---------- */
    /**
     * 脱敏后的密码占位符
     */
    public static final String MASK_PASSWORD = "******";

    /**
     * 需要脱敏的字段 Key 列表 (用于 JSON 遍历)
     */
    public static final String[] SENSITIVE_KEYS = {"password", "confirmPassword", "oldPassword", "newPassword"};

    // ============================== 日志与网络 ==============================

    /**
     * 日志内容最大长度 (防止字段超长报错)
     */
    public static final int SYS_LOG_MAX_LENGTH = 2000;

    /**
     * 未知 IP 标识
     */
    public static final String IP_UNKNOWN = "unknown";

    /**
     * 本地 IPv6 地址
     */
    public static final String IP_LOCAL_V6 = "0:0:0:0:0:0:0:1";

    /**
     * 本地 IPv4 地址
     */
    public static final String IP_LOCAL_V4 = "127.0.0.1";

    /**
     * 常见的代理 IP 请求头
     */
    public static final String[] IP_HEADER_CANDIDATES = {
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };

}