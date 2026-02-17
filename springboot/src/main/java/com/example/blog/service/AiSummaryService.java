package com.example.blog.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiSummaryService {

    private final ChatClient chatClient;

    public AiSummaryService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 生成文章摘要
     * @param title 文章标题
     * @param content 文章正文
     * @return 摘要内容
     */
    public String generateSummary(String title, String content) {
        String prompt = """
                请你担任一个专业的编辑。请阅读以下文章内容，并生成一段简短的摘要（150字以内）。
                摘要要求：结合文章标题，抓住核心观点，语言精炼，通俗易懂，不要输出多余的寒暄语。
                
                文章标题：%s
                文章内容：
                %s
                """.formatted(title, content);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}