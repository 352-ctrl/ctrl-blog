package com.example.blog.modules.system.validator;

import com.example.blog.modules.system.model.entity.SysSensitiveWord;
import com.example.blog.modules.system.mapper.SysSensitiveWordMapper;
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

    // 核心过滤器实例(加上 volatile 保证多线程下的内存可见性)
    private volatile SensitiveWordBs sensitiveWordBs;

    /**
     * 全量热更新词库
     * 采用加锁重构的方式，规避 addWord 导致的分词器状态不一致 Bug
     */
    public synchronized void refresh() {
        log.info("开始加载/刷新底层敏感词库 DFA 树...");

        // 1. 从 MySQL 实时拉取最新全量词库
        IWordDeny databaseWordDeny = () -> {
            List<SysSensitiveWord> wordList = sysSensitiveWordMapper.selectList(null);
            return wordList.stream()
                    .map(SysSensitiveWord::getWord)
                    .collect(Collectors.toList());
        };

        // 2. 构建一个全新的实例替换旧实例 (这就是高并发下极其安全的 Copy-On-Write 思想)
        this.sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.chains(WordDenys.defaults(), databaseWordDeny))
                .wordAllow(WordAllows.defaults())
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreChineseStyle(true)
                .ignoreRepeat(true)
                .charIgnore(SensitiveWordCharIgnores.specialChars())
                .init(); // init() 会走一遍最完整的规则预处理流水线

        log.info("敏感词库热更新完毕，当前生效实例已替换！");
    }

    /**
     * 项目启动时自动初始化
     */
    @PostConstruct
    public void init() {
        this.refresh();
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

}