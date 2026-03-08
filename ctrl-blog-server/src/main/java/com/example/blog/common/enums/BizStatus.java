package com.example.blog.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 业务状态枚举聚合类
 * 集中管理系统中的状态码、类型标识及相关业务配置
 */
public final class BizStatus {

    /**
     * 私有构造方法，防止实例化
     */
    private BizStatus() {
        throw new IllegalStateException("BizStatus 常量类禁止实例化");
    }

    /* ============================== 1. 通用与角色 ============================== */

    @Getter
    @AllArgsConstructor
    public enum Common implements BaseEnum<Integer> {
        DISABLE(0, "禁用/否"),
        ENABLE(1, "启用/是");

        @EnumValue // 存入数据库的值
        private final Integer code;
        @JsonValue // 返回给前端的值
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    @Getter
    @AllArgsConstructor
    public enum DeleteStatus implements BaseEnum<Integer> {
        NORMAL(0, "正常/未删除"),
        DELETED(1, "已删除");

        @EnumValue // 存入数据库的值
        private final Integer code;
        @JsonValue // 返回给前端的值
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /**
     * 角色标识：普通用户
     */
    public static final String ROLE_USER = "USER";
    /**
     * 角色标识：管理员
     */
    public static final String ROLE_ADMIN = "ADMIN";
    /**
     * 角色标识：超级管理员
     */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    @Getter
    @AllArgsConstructor
    public enum Role implements BaseEnum<String> {
        USER(BizStatus.ROLE_USER, "普通用户"),
        ADMIN(BizStatus.ROLE_ADMIN, "管理员"),
        SUPER_ADMIN(BizStatus.ROLE_SUPER_ADMIN, "超级管理员");

        @EnumValue
        @JsonValue
        private final String code;

        private final String desc;

        @Override
        public String getValue() { return code; }
    }


    /* ============================== 2. 用户业务状态 ============================== */

    @Getter
    @AllArgsConstructor
    public enum User implements BaseEnum<Integer> {
        NORMAL(0, "正常"),
        DISABLE(1, "禁用"),
        PENDING_DELETION(2, "注销冷静期");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }


    /* ============================== 3. 内容业务 (文章/公告) ============================== */

    /**
     * 文章状态
     */
    @Getter
    @AllArgsConstructor
    public enum Article implements BaseEnum<Integer> {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "发布");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /**
     * 文章摘要来源
     */
    @Getter
    @AllArgsConstructor
    public enum SummarySource implements BaseEnum<Integer> {
        HUMAN(0, "人工"),
        AI(1, "AI");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /**
     * 公告状态
     */
    @Getter
    @AllArgsConstructor
    public enum Notice implements BaseEnum<Integer> {
        HIDE(0, "隐藏"),
        SHOW(1, "显示");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }


    /* ============================== 4. 系统日志状态 ============================== */

    @Getter
    @AllArgsConstructor
    public enum Log implements BaseEnum<Integer> {
        FAIL(0, "失败"),
        SUCCESS(1, "成功");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }


    /* ============================== 5. 定时任务配置 (Quartz) ============================== */

    /**
     * 任务组名
     */
    @Getter
    @AllArgsConstructor
    public enum JobGroup implements BaseEnum<String> {
        DEFAULT("DEFAULT", "默认分组"),
        SYSTEM("SYSTEM", "系统分组");

        @EnumValue
        private final String code;
        @JsonValue
        private final String desc;

        @Override
        public String getValue() { return code; }
    }

    /**
     * 任务状态
     */
    @Getter
    @AllArgsConstructor
    public enum JobStatus implements BaseEnum<Integer> {
        NORMAL(0, "正常"),
        PAUSE(1, "暂停");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }

        public static JobStatus getByCode(Integer code) {
            if (code == null) {
                return null;
            }
            return Arrays.stream(values())
                    .filter(e -> e.getValue().equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 任务执行策略 (对应 Quartz 的 Misfire Policy)
     */
    @Getter
    @AllArgsConstructor
    public enum MisfirePolicy implements BaseEnum<Integer> {
        IGNORE(1, "立即执行"),
        FIRE_ONCE(2, "执行一次"),
        DO_NOTHING(3, "放弃执行");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /**
     * 定时任务ID前缀
     */
    public static final String JOB_NAME_PREFIX = "TASK_";
    /**
     * 任务上下文存取的 Key 名称
     */
    public static final String JOB_PROPERTIES = "JOB_PROPERTIES";

    /* ============================== 6. 评论业务 ============================== */

    /**
     * 评论排序方式
     */
    @Getter
    @AllArgsConstructor
    public enum CommentSort implements BaseEnum<Integer> {
        LATEST(1, "按时间最新"),
        HOTTEST(2, "按热度(点赞数)");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /* ============================== 7. 消息业务 ============================== */

    /**
     * 消息大类
     */
    @Getter
    @AllArgsConstructor
    public enum MessageType implements BaseEnum<String> {
        SYSTEM("SYSTEM", "系统通知"),
        LIKE("LIKE", "点赞"),
        COMMENT("COMMENT", "评论/回复");

        @EnumValue
        @JsonValue
        private final String code;

        private final String desc;

        @Override
        public String getValue() { return code; }
    }

    /**
     * 消息关联的业务类型
     */
    @Getter
    @AllArgsConstructor
    public enum MessageBizType implements BaseEnum<String> {
        ARTICLE("ARTICLE", "文章"),
        COMMENT("COMMENT", "评论"),
        FEEDBACK("FEEDBACK", "反馈");

        @EnumValue
        @JsonValue
        private final String code;

        private final String desc;

        @Override
        public String getValue() { return code; }
    }

    /**
     * 消息阅读状态
     */
    @Getter
    @AllArgsConstructor
    public enum ReadStatus implements BaseEnum<Integer> {
        UNREAD(0, "未读"),
        READ(1, "已读");

        @EnumValue
        @JsonValue
        private final Integer code;
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /* ============================== 8. 安全与系统事件 ============================== */

    /**
     * 系统安全/重要操作事件类型
     */
    @Getter
    @AllArgsConstructor
    public enum SecurityEventType implements BaseEnum<String> {
        PASSWORD_RESET("PASSWORD_RESET", "重置密码"),
        ROLE_CHANGE("ROLE_CHANGE", "角色变更");

        @EnumValue
        @JsonValue
        private final String code;
        private final String desc;

        @Override
        public String getValue() { return code; }
    }

    /* ============================== 9. 反馈与举报业务 ============================== */

    /**
     * 意见反馈类型
     */
    @Getter
    @AllArgsConstructor
    public enum FeedbackType implements BaseEnum<Integer> {
        ADVICE(0, "意见建议"),
        BUG(1, "BUG反馈"),
        APPEAL(2, "封禁申诉"),
        OTHER(3, "其他");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }
    }

    /**
     * 意见反馈处理状态
     */
    @Getter
    @AllArgsConstructor
    public enum FeedbackStatus implements BaseEnum<Integer> {
        PENDING(0, "待处理"),
        PROCESSING(1, "处理中"),
        RESOLVED(2, "已解决"),
        REJECTED(3, "已驳回");

        @EnumValue
        private final Integer code;
        @JsonValue
        private final String desc;

        @Override
        public Integer getValue() { return code; }

        public static FeedbackStatus getByCode(Integer code) {
            if (code == null) {
                return null;
            }
            return Arrays.stream(values())
                    .filter(e -> e.getValue().equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 举报目标类型
     */
    @Getter
    @AllArgsConstructor
    public enum ReportTargetType implements BaseEnum<String> {
        COMMENT("COMMENT", "评论"),
        ARTICLE("ARTICLE", "文章"),
        USER("USER", "用户");

        @EnumValue
        @JsonValue // String 类型的枚举，通常将 Code 返回给前端
        private final String code;
        private final String desc;

        @Override
        public String getValue() { return code; }
    }

    /**
     * 举报处理状态
     */
    @Getter
    @AllArgsConstructor
    public enum ReportStatus implements BaseEnum<Integer> {
        PENDING(0, "待处理"),
        VALID(1, "举报属实已处罚"),
        INVALID(2, "驳回/恶意举报");

        @EnumValue
        private final Integer code;
        @JsonValue // Integer 类型的枚举，按你当前系统规范，将 Desc 返回给前端展示
        private final String desc;

        @Override
        public Integer getValue() { return code; }

        public static ReportStatus getByCode(Integer code) {
            if (code == null) {
                return null;
            }
            return Arrays.stream(values())
                    .filter(e -> e.getValue().equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 举报原因枚举 (包含所有场景的聚合集合)
     */
    @Getter
    @AllArgsConstructor
    public enum ReportReason implements BaseEnum<String> {
        // --- 通用违规 ---
        SPAM("SPAM", "垃圾广告"),
        PORN("PORN", "色情低俗"),
        ILLEGAL("ILLEGAL", "违法违规"),

        // --- 偏向评论/互动的违规 ---
        ABUSE("ABUSE", "人身攻击/引战谩骂"),

        // --- 偏向文章的违规 ---
        COPYRIGHT("COPYRIGHT", "抄袭/洗稿/侵权"),

        // --- 偏向用户的违规 ---
        IMPERSONATION("IMPERSONATION", "冒充他人/身份造假"),
        PROFILE_VIOLATION("PROFILE_VIOLATION", "头像/昵称违规"),

        // --- 兜底 ---
        OTHER("OTHER", "其他原因");

        @EnumValue
        @JsonValue
        private final String code;
        private final String desc;

        @Override
        public String getValue() { return code; }
    }

}