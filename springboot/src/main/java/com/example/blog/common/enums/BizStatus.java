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

    /**
     * 角色标识：普通用户
     */
    public static final String ROLE_USER = "USER";
    /**
     * 角色标识：管理员
     */
    public static final String ROLE_ADMIN = "ADMIN";

    @Getter
    @AllArgsConstructor
    public enum Role implements BaseEnum<String> {
        USER(BizStatus.ROLE_USER, "普通用户"),
        ADMIN(BizStatus.ROLE_ADMIN, "管理员");

        @EnumValue
        private final String code;
        @JsonValue
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
        COOL_OFF(2, "注销冷静期");

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
}