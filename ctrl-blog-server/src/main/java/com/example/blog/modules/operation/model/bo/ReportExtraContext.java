package com.example.blog.modules.operation.model.bo;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.enums.BizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * 举报附加信息上下文
 * 内聚防空指针逻辑，负责组装举报目标的摘要展示信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportExtraContext {

    @Builder.Default
    private Map<Long, String> reporterMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, String> articleMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, String> commentMap = Collections.emptyMap();

    @Builder.Default
    private Map<Long, String> targetUserMap = Collections.emptyMap();

    // ================= 封装安全的数据获取方法 =================

    /**
     * 获取举报人昵称
     */
    public String getReporterNickname(Long userId) {
        if (userId == null || !reporterMap.containsKey(userId)) {
            return "未知用户";
        }
        return reporterMap.get(userId);
    }

    /**
     * 根据目标类型和目标ID，智能组装展示摘要
     */
    public String getTargetSummary(BizStatus.ReportTargetType type, Long targetId) {
        if (type == null || targetId == null) {
            return "目标不存在或已被删除";
        }

        if (BizStatus.ReportTargetType.ARTICLE.equals(type) && articleMap.containsKey(targetId)) {
            return "文章: 《" + articleMap.get(targetId) + "》";
        } else if (BizStatus.ReportTargetType.COMMENT.equals(type) && commentMap.containsKey(targetId)) {
            // 如果评论太长，截取前30个字符展示
            return "评论: " + StrUtil.maxLength(commentMap.get(targetId), 30);
        } else if (BizStatus.ReportTargetType.USER.equals(type) && targetUserMap.containsKey(targetId)) {
            return "用户昵称: " + targetUserMap.get(targetId);
        }

        return "目标不存在或已被删除";
    }
}