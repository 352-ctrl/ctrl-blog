package com.example.blog.modules.article.service;

/**
 * AI 摘要生成业务接口
 */
public interface AiSummaryService {

    /**
     * 根据标题和正文生成文章摘要
     *
     * @param title   文章标题
     * @param content 文章正文
     * @return AI 生成的摘要内容
     */
    String generateSummary(String title, String content);
}