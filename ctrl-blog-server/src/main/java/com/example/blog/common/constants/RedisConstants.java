package com.example.blog.common.constants;

/**
 * Redis 缓存常量配置
 * 集中管理 Redis Key 前缀、过期时间及相关配置
 */
public final class RedisConstants {

    /**
     * 私有构造方法，防止实例化
     */
    private RedisConstants() {
        throw new IllegalStateException("RedisConstants 常量类禁止实例化");
    }

    /**
     * 防刷限流的占位符值
     */
    public static final String VIEW_LIMIT_VALUE = "1";

    /* ============================== 1. 系统安全与认证 ============================== */

    /**
     * 登录失败次数限制 Key
     */
    public static final String REDIS_LOGIN_FAIL_KEY = "login:fail:";
    /**
     * 登录失败计数窗口 (30分钟)
     */
    public static final long EXPIRE_LOGIN_FAIL_WINDOW = 30L;
    /**
     * 账号锁定惩罚时长 (30分钟)
     */
    public static final long LOGIN_LOCKED_TIME = 30L;

    /**
     * 用户在线 Token Key
     */
    public static final String REDIS_USER_TOKEN_KEY = "user:token:";
    /**
     * 用户Token过期时间 (1天)
     */
    public static final long EXPIRE_USER_TOKEN = 1L;

    /**
     * 注册邮箱验证码 Key
     */
    public static final String REDIS_EMAIL_REGISTER_CODE_KEY = "email:code:register:";

    /**
     * 重置密码邮箱验证码 Key
     */
    public static final String REDIS_EMAIL_RESET_CODE_KEY = "email:code:reset:";

    /**
     * 换绑邮箱验证码 Key
     */
    public static final String REDIS_EMAIL_BIND_CODE_KEY = "email:code:bind:";

    /**
     * 邮箱验证码过期时间 (5分钟)
     */
    public static final long EXPIRE_EMAIL_CODE = 5L;

    /**
     * Token黑名单前缀
     */
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    /**
     * Token 黑名单 Value (标识主动登出)
     */
    public static final String REDIS_TOKEN_BLACKLIST_VALUE = "logout";

    /**
     * 接口限流 Key (移到安全区)
     */
    public static final String REDIS_RATE_LIMIT_KEY = "rate_limit:";


    /* ============================== 2. 用户业务 ============================== */

    /**
     * 用户信息缓存 Key
     */
    public static final String REDIS_USER_INFO_KEY = "blog:user:info:";
    /**
     * 用户信息缓存时间 (30分钟)
     */
    public static final long EXPIRE_USER_INFO = 30L;


    /* ============================== 3. 文章与内容业务 ============================== */

    /**
     * 文章详情缓存 Key
     */
    public static final String REDIS_ARTICLE_DETAIL_PREFIX = "blog:article:detail:";
    /**
     * 文章详情缓存时间 (30分钟)
     */
    public static final long EXPIRE_ARTICLE_DETAIL = 30L;

    /**
     * 首页轮播图缓存 Key
     */
    public static final String REDIS_ARTICLE_CAROUSEL_KEY = "blog:article:carousel";
    /**
     * 轮播图缓存时间 (1小时)
     */
    public static final long EXPIRE_ARTICLE_CAROUSEL = 1L;

    /**
     * 文章浏览量 Map Key (Hash结构)
     * 结构: Key="blog:article:view:map", Field=文章ID, Value=阅读量
     */
    public static final String REDIS_VIEW_HASH_KEY = "blog:article:view:map";

    /**
     * 阅读量防刷限制 Key
     * 格式: blog:view:limit:文章ID:指纹
     */
    public static final String REDIS_VIEW_LIMIT_PREFIX = "blog:view:limit:";
    /**
     * 阅读量防刷限制时间 (1分钟)
     */
    public static final long VIEW_LIMIT_EXPIRE = 1L;

    /**
     * 阅读量脏数据集合 (Set)
     * 存放有变动的文章ID，用于定时任务同步
     */
    public static final String REDIS_VIEW_DIRTY_SET = "blog:article:view:dirty_set";

    /**
     * 文章归档 Key
     */
    public static final String REDIS_ARTICLE_ARCHIVE_KEY = "blog:article:archive";
    /**
     * 文章归档缓存时间 (1小时)
     */
    public static final long EXPIRE_ARTICLE_ARCHIVE = 1L;

    /**
     * 首页文章列表(第一页) Key
     */
    public static final String REDIS_ARTICLE_LIST_FIRST_PAGE_KEY = "blog:article:list:first_page";
    /**
     * 首页列表缓存时间 (30分钟)
     */
    public static final long EXPIRE_ARTICLE_LIST_FIRST_PAGE = 30L;

    /**
     * 热门文章 Key
     */
    public static final String REDIS_ARTICLE_HOT_KEY = "blog:article:hot";
    /**
     * 热门文章列表缓存时间 (30分钟)
     */
    public static final long EXPIRE_ARTICLE_HOT = 30L;


    /* ============================== 4. 公共元数据 (分类/标签/公告) ============================== */

    /**
     * 分类列表 Key
     */
    public static final String REDIS_CATEGORY_LIST_KEY = "blog:category:list";
    /**
     * 标签列表 Key
     */
    public static final String REDIS_TAG_LIST_KEY = "blog:tag:list";
    /**
     * 元数据缓存时间 (1天)
     */
    public static final long EXPIRE_METADATA = 1L;

    /**
     * 公告列表 Key
     */
    public static final String REDIS_NOTICE_LIST_KEY = "blog:notice:list";
    /**
     * 公告缓存时间 (1天)
     */
    public static final long EXPIRE_NOTICE_LIST = 1L;

    /**
     * 前台站长卡片信息 Key
     */
    public static final String REDIS_WEBMASTER_INFO_KEY = "blog:site:webmaster";
    /**
     * 站长卡片缓存时间 (1天)
     */
    public static final long EXPIRE_WEBMASTER_INFO = 1L;

    /* ============================== 5. 站点统计与监控 ============================== */

    /**
     * 网站总访问量 Key
     */
    public static final String VIEW_COUNT_KEY = "blog:view:site:total";

    /**
     * 每日访问量 (PV) Key
     */
    public static final String VIEW_COUNT_DAY_PREFIX = "blog:view:site:day:lock:";

    /**
     * 每日访问量缓存过期时间 (30天)
     * 用于保留近一个月的访问数据，防止 key 长期堆积
     */
    public static final long EXPIRE_VIEW_COUNT_DAY = 30L;

    /**
     * 总访问量缓存过期时间 (60分钟)
     * 既减轻了数据库 SUM 查询的压力，又能保证数据每小时更新一次
     */
    public static final long EXPIRE_VIEW_COUNT_MINUTES = 60L;

    /* ============================== 6. 互动与点赞业务 ============================== */

    /**
     * 评论点赞 Set Key 前缀
     * 结构: blog:comment:like:{commentId} -> Set<UserId>
     */
    public static final String REDIS_COMMENT_LIKE_KEY = "blog:comment:like:";

    /**
     * 文章点赞 Set Key 前缀
     * 结构: blog:article:like:{articleId} -> Set<UserId>
     */
    public static final String REDIS_ARTICLE_LIKE_KEY = "blog:article:like:";

    /**
     * 文章收藏 Set Key 前缀
     * 结构: blog:article:favorite:{articleId} -> Set<UserId>
     */
    public static final String REDIS_ARTICLE_FAVORITE_KEY = "blog:article:favorite:";

    /* ============================== 7. 弹幕与互动 ============================== */

    /**
     * 弹幕列表缓存 Key
     */
    public static final String REDIS_DANMAKU_LIST_KEY = "blog:danmaku:list";

    /**
     * 弹幕列表缓存时间 (1小时)
     */
    public static final long EXPIRE_DANMAKU_LIST = 1L;
}