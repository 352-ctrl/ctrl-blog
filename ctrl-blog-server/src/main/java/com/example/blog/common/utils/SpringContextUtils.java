package com.example.blog.common.utils;

import com.example.blog.common.constants.MessageConstants;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文工具类
 * 职责：让非 Spring 管理的类（如静态工具类）也能获取 Spring 容器中的 Bean
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    /**
     * 使用 Lombok @Getter 自动生成 public static getApplicationContext() 方法
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * Spring 容器启动时会自动调用此方法，注入上下文
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

    /**
     * 核心方法：通过 name 获取 Bean (对应 invokeTarget 中的 bean 名)
     */
    public static Object getBean(String name) {
        assertContextInjected();
        return applicationContext.getBean(name);
    }

    /**
     * 通过 class 获取 Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过 name, class 获取 Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 检查上下文是否注入，防止项目启动过早调用导致空指针
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException(MessageConstants.MSG_ERR_SPRING_CONTEXT_NULL);
        }
    }
}
