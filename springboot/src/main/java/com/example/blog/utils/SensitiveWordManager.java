package com.example.blog.utils;

import com.example.blog.entity.SysSensitiveWord;
import com.example.blog.mapper.SysSensitiveWordMapper;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词管理组件 (基于 houbb/sensitive-word)
 */
@Slf4j
@Component
public class SensitiveWordManager {

    @Resource
    private SysSensitiveWordMapper sysSensitiveWordMapper;

    // 核心过滤器实例
    private SensitiveWordBs sensitiveWordBs;

    /**
     * 项目启动时自动初始化
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化敏感词库...");

        // 1. 定义动态的数据库词库加载器
        IWordDeny databaseWordDeny = () -> {
            List<SysSensitiveWord> wordList = sysSensitiveWordMapper.selectList(null);
            return wordList.stream()
                    .map(SysSensitiveWord::getWord)
                    .collect(Collectors.toList());
        };

        // 2. 初始化 SensitiveWordBs
        this.sensitiveWordBs = SensitiveWordBs.newInstance()
                // 将框架自带词库与数据库自定义词库合并
                .wordDeny(WordDenys.chains(WordDenys.defaults(), databaseWordDeny))
                // 使用默认白名单
                .wordAllow(WordAllows.defaults())
                // 开启各种防绕过特性（默认其实都是 true，这里显式列出方便了解）
                .ignoreCase(true)             // 忽略大小写
                .ignoreWidth(true)            // 忽略全角半角
                .ignoreChineseStyle(true)     // 忽略繁简互换
                .ignoreRepeat(true)           // 忽略重复词，如 "傻傻傻冒"
                // 忽略特殊字符干扰，如 "傻@冒"
                .charIgnore(SensitiveWordCharIgnores.specialChars())
                .init();

        log.info("敏感词库初始化完成！");
    }

    /**
     * 判断是否包含敏感词 (用于严格拦截)
     */
    public boolean contains(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return sensitiveWordBs.contains(text);
    }

    /**
     * 将敏感词替换为指定字符 (用于柔性过滤)
     */
    public String replace(String text, char replaceChar) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 1. 使用 houbb 框架极其强大的识别能力，找出所有命中的敏感词
        List<String> matchWords = sensitiveWordBs.findAll(text);

        if (matchWords == null || matchWords.isEmpty()) {
            return text; // 没有敏感词，直接返回原文本
        }

        // 2. 去重并按字符串长度降序排序。优先替换较长的敏感词，防止嵌套词被破坏
        List<String> distinctWords = matchWords.stream()
                .distinct()
                .sorted((a, b) -> b.length() - a.length())
                .toList();

        // 3. 使用 Hutool 快速生成指定字符并替换（完美保留了你原来的动态字符逻辑）
        for (String word : distinctWords) {
            String replacement = cn.hutool.core.util.StrUtil.repeat(replaceChar, word.length());
            text = cn.hutool.core.util.StrUtil.replaceIgnoreCase(text, word, replacement);
        }

        return text;
    }

    // ==========================================
    // 动态更新接口：后台增删敏感词时，不再需要全量加载数据库了！
    // ==========================================

    /**
     * 动态添加敏感词 (后台调用)
     */
    public void addWord(String word) {
        sensitiveWordBs.addWord(word);
    }

    /**
     * 动态删除敏感词 (后台调用)
     */
    public void removeWord(String word) {
        sensitiveWordBs.removeWord(word);
    }
}