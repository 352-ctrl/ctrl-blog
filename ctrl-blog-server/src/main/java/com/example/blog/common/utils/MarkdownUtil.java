package com.example.blog.common.utils;

import cn.hutool.core.util.StrUtil;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.mapstruct.Named;

/**
 * Markdown文本处理工具类
 * 提供Markdown到HTML的转换功能
 */
public class MarkdownUtil {

    // 创建Markdown解析器
    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();

    private MarkdownUtil() {
        // 工具类，防止实例化
    }

    /**
     * 将Markdown文本转换为HTML
     *
     * @param markdown Markdown格式文本
     * @return 转换后的HTML文本，转换失败时返回原文本
     */

    @Named("markdownToHtml")
    public static String markdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return StrUtil.EMPTY;
        }

        try {
            Node document = PARSER.parse(markdown);
            // 渲染HTML
            return RENDERER.render(document);
        } catch (Exception e) {
            // 转换失败时返回原始内容
            return markdown;
        }
    }
}