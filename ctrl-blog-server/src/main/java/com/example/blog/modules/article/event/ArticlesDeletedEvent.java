package com.example.blog.modules.article.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 文章删除事件 (用于发送系统通知)
 */
@Getter
public class ArticlesDeletedEvent extends ApplicationEvent {
    // Key: 用户ID, Value: 文章标题
    private final Map<Long, String> deletedArticlesMap;

    public ArticlesDeletedEvent(Object source, Map<Long, String> deletedArticlesMap) {
        super(source);
        this.deletedArticlesMap = deletedArticlesMap;
    }
}