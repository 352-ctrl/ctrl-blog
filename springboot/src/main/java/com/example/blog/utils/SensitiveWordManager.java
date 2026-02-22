package com.example.blog.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.example.blog.entity.SysSensitiveWord;
import com.example.blog.mapper.SysSensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 敏感词管理组件 (基于 Hutool DFA 算法)
 */
@Slf4j
@Component
public class SensitiveWordManager {

    @Resource
    private SysSensitiveWordMapper sysSensitiveWordMapper;

    // Hutool 提供的字典树，查询极快
    private volatile WordTree wordTree = new WordTree();

    /**
     * 项目启动时自动执行，或者在后台添加/删除敏感词后手动调用刷新
     */
    @PostConstruct
    public void initWordTree() {
        log.info("开始初始化敏感词库...");
        // 1. 从数据库查出所有敏感词
        // 注意：如果你用了 MyBatis-Plus，可以直接用 BaseMapper 的 selectList
        List<SysSensitiveWord> wordList = sysSensitiveWordMapper.selectList(null);

        // 2. 构建新的字典树
        WordTree newTree = new WordTree();
        for (SysSensitiveWord word : wordList) {
            if (word != null && StrUtil.isNotBlank(word.getWord())) {
                newTree.addWord(word.getWord().trim());
            }
        }

        // 3. 替换旧树 (保证线程安全)
        this.wordTree = newTree;
        log.info("敏感词库初始化完成，共加载 {} 个词汇", wordList.size());
    }

    /**
     * 判断是否包含敏感词 (用于严格拦截)
     */
    public boolean contains(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return wordTree.isMatch(text);
    }

    /**
     * 将敏感词替换为指定字符 (用于柔性过滤)
     */
    public String replace(String text, char replaceChar) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 1. 使用 WordTree 找出文本中包含的所有敏感词 (-1表示匹配所有, true表示开启密集匹配和贪婪匹配)
        List<String> matchWords = wordTree.matchAll(text, -1, true, true);
        if (matchWords == null || matchWords.isEmpty()) {
            return text; // 没有敏感词，直接返回原文本
        }

        // 2. 按字符串长度降序排序。优先替换较长的敏感词，防止嵌套词被破坏
        // 例如同时包含 "坏蛋" 和 "大坏蛋"，优先把 "大坏蛋" 替换为 "***"
        matchWords.sort((a, b) -> b.length() - a.length());

        for (String word : matchWords) {
            // 使用 Hutool 的 StrUtil.repeat 快速生成替代符
            String replacement = StrUtil.repeat(replaceChar, word.length());
            // 使用 Hutool 的忽略大小写替换，防止英文大小写绕过
            text = StrUtil.replaceIgnoreCase(text, word, replacement);
        }

        return text;
    }

}