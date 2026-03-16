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
     * 通用：参数格式或类型错误 (反序列化失败)
     */
    public static final String MSG_PARAM_FORMAT_ERROR = "请求参数格式或类型错误，请检查传入的值";
    /**
     * 通用：参数类型转换失败模板 (供 String.format 使用)
     */
    public static final String MSG_PARAM_TYPE_MISMATCH = "参数[%s]格式错误，期望类型为[%s]，实际传入[%s]";
    /**
     * 通用：接口或资源不存在 (404)
     */
    public static final String MSG_RESOURCE_NOT_FOUND = "请求的接口或资源不存在";
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
     * 未进行安全验证（参数为空）
     */
    public static final String MSG_CAPTCHA_REQUIRE = "请先完成滑动安全验证";
    /**
     * 安全验证（滑动拼图等）失败
     */
    public static final String MSG_CAPTCHA_VERIFY_FAILED = "安全验证失败，请重新滑动拼图";
    /**
     * 核心：新增会话失效常量
     */
    public static final String MSG_SESSION_INVALID = "会话已失效，请重新登录";
    /**
     * 执行目标不能为空
     */
    public static final String MSG_INVOKE_TARGET_EMPTY = "执行目标不能为空";

    /**
     * 执行目标格式错误
     */
    public static final String MSG_INVOKE_TARGET_FORMAT_ERROR = "执行目标格式错误，正确格式应为 beanName.methodName()";

    /**
     * 找不到执行目标 Bean (格式化模板)
     */
    public static final String MSG_INVOKE_TARGET_BEAN_NOT_FOUND = "参数异常：找不到执行目标 Bean [%s]";
    /**
     * Token 解析失败或过期
     */
    public static final String MSG_TOKEN_INVALID = "token无效或已过期";
    /**
     * Token 结构非法或被伪造
     */
    public static final String MSG_TOKEN_FAKE = "无效或伪造的Token";
    /**
     * 角色信息无效（解析Token时角色无法识别）
     */
    public static final String MSG_ROLE_INVALID = "角色信息无效";
    /**
     * 账号锁定提示 (包含占位符 %d)
     */
    public static final String MSG_LOGIN_LOCKED = "密码错误次数过多，账号已被锁定，请 %d 分钟后再试";

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

    /**
     * 账号永久封禁提示 (带1个占位符：原因)
     */
    public static final String MSG_ACCOUNT_BANNED_PERMANENT = "您的账号存在严重违规，已被永久封禁。原因：%s";

    /**
     * 账号限期封禁提示 (带2个占位符：时间, 原因)
     */
    public static final String MSG_ACCOUNT_BANNED_TEMPORARY = "您的账号因违规被封禁，解封时间：%s。原因：%s";


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

    /**
     * 账号封禁默认原因
     */
    public static final String MSG_DEFAULT_BAN_REASON = "存在违规行为";

    /**
     * 账号严重违规默认原因 (用于未填写原因时的兜底)
     */
    public static final String MSG_SEVERE_VIOLATION = "严重违反社区规范";

    /**
     * 封禁类型：限制登录
     */
    public static final String MSG_BAN_TYPE_ACCOUNT = "限制登录 (账号封禁)";

    /**
     * 解封时间占位：永久封禁
     */
    public static final String MSG_PERMANENT_BAN = "永久封禁";


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
     * 敏感词已存在
     */
    public static final String MSG_WORD_EXIST = "该敏感词已存在";
    /**
     * 敏感词不存在
     */
    public static final String MSG_WORD_NOT_EXIST = "敏感词不存在";

    /**
     * 评论不存在
     */
    public static final String MSG_COMMENT_NOT_EXIST = "评论不存在";

    /**
     * 原评论已删除 (用于楼中楼引用内容的UI展示占位)
     */
    public static final String MSG_ORIGINAL_COMMENT_DELETED = "原评论已删除";

    /**
     * 公告不存在
     */
    public static final String MSG_NOTICE_NOT_EXIST = "公告不存在";

    /**
     * 分类下存在文章，无法删除
     */
    public static final String MSG_CATEGORY_HAS_ARTICLE = "该分类下存在关联文章，无法直接删除，请先移除文章关联";

    /**
     * 批量删除分类时存在文章，无法删除
     */
    public static final String MSG_CATEGORY_BATCH_HAS_ARTICLE = "选中的分类中存在关联文章，无法批量删除，请先处理文章";

    /**
     * 标签下存在文章，无法删除
     */
    public static final String MSG_TAG_HAS_ARTICLE = "该标签下存在关联文章，无法直接删除，请先移除文章关联";

    /**
     * 批量删除标签时存在文章，无法删除
     */
    public static final String MSG_TAG_BATCH_HAS_ARTICLE = "选中的部分标签下存在关联文章，无法批量删除，请先处理文章";

    /**
     * 敏感词拦截提示
     */
    public static final String MSG_CONTENT_SENSITIVE = "内容包含违规词汇，请修改后提交";


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
     * 文件上传格式错误 (非 multipart/form-data 或未选择文件)
     */
    public static final String MSG_UPLOAD_FORMAT_ERROR = "请选择要上传的文件或检查请求格式";
    /**
     * 非法的上传业务类型
     */
    public static final String MSG_INVALID_UPLOAD_TYPE = "非法的上传业务类型";

    /**
     * 云存储文件下载提示
     */
    public static final String MSG_OSS_DIRECT_ACCESS_REQUIRED = "云存储文件请直接使用URL访问";

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

    /* ============================== 7. 防越权专属提示消息 ============================== */

    /** 越权：禁止操作超级管理员 */
    public static final String MSG_CANNOT_OPERATE_SUPER_ADMIN = "越权操作：禁止修改或删除超级管理员";
    /** 越权：禁止操作其他管理员 */
    public static final String MSG_CANNOT_OPERATE_OTHER_ADMIN = "越权操作：禁止修改或删除其他管理员";
    /** 越权：禁止分配超级管理员权限 */
    public static final String MSG_CANNOT_GRANT_SUPER_ADMIN = "系统安全限制：任何人禁止通过接口分配超级管理员权限";
    /** 越权：普通管理员只能分配普通用户权限 */
    public static final String MSG_ADMIN_CANNOT_GRANT_ADMIN = "越权操作：普通管理员只能分配普通用户角色";
    /** 越权：禁止修改自己的角色权限 */
    public static final String MSG_CANNOT_CHANGE_OWN_ROLE = "安全限制：禁止修改自身的角色权限";

    /* ============================== 8. 消息中心提示消息 ============================== */

    /**
     * 消息不存在或无权操作
     */
    public static final String MSG_MESSAGE_NOT_FOUND = "消息不存在或无权操作";
    /**
     * 消息标题：新点赞 (带占位符)
     */
    public static final String TITLE_NEW_LIKE = "%s 点赞了你";
    /**
     * 消息标题：新评论回复 (带占位符)
     */
    public static final String TITLE_NEW_COMMENT = "%s 回复了你";

    /** 消息：系统欢迎语 */
    public static final String TITLE_WELCOME = "欢迎加入";
    public static final String CONTENT_WELCOME = "欢迎注册本站！快去完善你的个人资料，或者发布你的第一篇文章吧。";

    /** 消息：密码重置 */
    public static final String TITLE_PWD_RESET = "密码重置通知";
    public static final String CONTENT_PWD_RESET = "您的密码已被系统管理员强制重置，为保障账号安全，请您尽快修改密码。";

    /** 消息：权限变更 (带占位符) */
    public static final String TITLE_ROLE_CHANGE = "账号权限变更通知";
    public static final String CONTENT_ROLE_CHANGE = "您的账号角色已被管理员修改为：【%s】，请重新登录以获取最新权限。";

    /** 消息：文章下架 (带占位符) */
    public static final String TITLE_ARTICLE_DELETE = "文章下架通知";
    public static final String CONTENT_ARTICLE_DELETE = "您发布的文章《%s》因违反社区规定或管理员操作已被删除。";

    /* ============================== 9. 反馈与举报业务消息 ============================== */

    /**
     * 反馈已完结，禁止修改
     */
    public static final String MSG_FEEDBACK_ALREADY_TERMINATED = "该反馈已处于完结状态，无法再次修改";

    /**
     * 反馈记录不存在
     */
    public static final String MSG_FEEDBACK_NOT_EXIST = "反馈记录不存在";

    /**
     * 举报记录不存在
     */
    public static final String MSG_REPORT_NOT_EXIST = "举报记录不存在";

    /**
     * 反馈处理：默认已解决回复语
     */
    public static final String MSG_FEEDBACK_REPLY_RESOLVED = "您的反馈我们已处理完毕，感谢您的支持。";

    /**
     * 反馈处理：默认已驳回回复语
     */
    public static final String MSG_FEEDBACK_REPLY_REJECTED = "抱歉，您的反馈经评估暂无法处理或不予采纳。";

    /**
     * 申诉邮箱不存在或账号状态异常
     */
    public static final String MSG_APPEAL_EMAIL_INVALID = "该邮箱不存在或账号未处于封禁状态，请核实后重新提交";

    /**
     * 反馈处理：封禁申诉未找到对应邮箱账号
     */
    public static final String MSG_APPEAL_EMAIL_NOT_FOUND = "未找到该申诉邮箱关联的账号，无法自动解封";

    /* ============================== 10. 站内信通知专项消息 ============================== */
    /** 消息标题：反馈回复 */
    public static final String TITLE_FEEDBACK_REPLY = "您的反馈已收到回复";
    /** 消息内容：反馈回复 (带占位符) */
    public static final String CONTENT_FEEDBACK_REPLY = "您提交的反馈【%s】处理状态已更新为：%s。管理员回复：%s";

    /** 消息标题：举报处理结果 (发给举报人) */
    public static final String TITLE_REPORT_RESULT = "您的举报处理结果";
    /** 消息内容：举报属实 (发给举报人) */
    public static final String CONTENT_REPORT_VALID = "您举报的违规内容已核实并处理，感谢您为维护社区环境作出的贡献。处理备注：%s";
    /** 消息内容：举报驳回 (发给举报人) */
    public static final String CONTENT_REPORT_INVALID = "抱歉，您提交的举报经审核未发现明显违规。处理备注：%s";

    /** 消息标题：系统违规警告 (发给被举报人) */
    public static final String TITLE_SYSTEM_WARNING = "系统违规处罚通知";
    /** 消息内容：违规警告 (发给被举报人) */
    public static final String CONTENT_SYSTEM_WARNING = "您发布的%s存在违规行为已被处理。原因：%s。请自觉遵守社区规范，多次违规将面临封号处罚。";

}