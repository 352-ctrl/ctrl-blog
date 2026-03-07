package com.example.blog.modules.system.task;

import cn.hutool.core.util.StrUtil;
import com.example.blog.common.utils.SpringContextUtils;
import com.example.blog.modules.system.model.entity.SysJob;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务执行工具类
 * * 通过反射机制调用具体的 Bean 方法
 */
public class JobInvokeUtil {
    /**
     * 执行目标方法
     */
    public static void invokeMethod(SysJob sysJob) throws Exception {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        // 获取 Spring 容器中的 Bean
        Object bean = SpringContextUtils.getBean(beanName);

        if (methodParams != null && !methodParams.isEmpty()) {
            // 带参数调用逻辑
            invokeMethod(bean, methodName, methodParams);
        } else {
            // 无参调用
            Method method = bean.getClass().getDeclaredMethod(methodName);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean);
        }
    }

    /**
     * 调用带有参数的方法
     *
     * @param bean         目标对象
     * @param methodName   方法名称
     * @param methodParams 参数列表
     */
    private static void invokeMethod(Object bean, String methodName, List<Object[]> methodParams) throws Exception {
        Class<?>[] paramTypes = new Class<?>[methodParams.size()];
        Object[] args = new Object[methodParams.size()];

        for (int i = 0; i < methodParams.size(); i++) {
            Object[] param = methodParams.get(i);
            args[i] = param[0];       // 参数值
            paramTypes[i] = (Class<?>) param[1]; // 参数类型
        }

        Method method = bean.getClass().getDeclaredMethod(methodName, paramTypes);
        ReflectionUtils.makeAccessible(method);
        method.invoke(bean, args);
    }

    /**
     * 获取 Bean 名称
     */
    private static String getBeanName(String invokeTarget) {
        return StrUtil.subBefore(invokeTarget, ".", false);
    }

    /**
     * 获取方法名称
     */
    private static String getMethodName(String invokeTarget) {
        String str = StrUtil.subBefore(invokeTarget, "(", false);
        return StrUtil.subAfter(str, ".", true);
    }

    /**
     * 获取方法参数列表
     * 支持字符串、数字、布尔值（简单实现）
     */
    private static List<Object[]> getMethodParams(String invokeTarget) {
        String paramsStr = StrUtil.subBetween(invokeTarget, "(", ")");
        if (StrUtil.isBlank(paramsStr)) {
            return null;
        }

        String[] params = paramsStr.split(",");
        List<Object[]> results = new ArrayList<>();

        for (String param : params) {
            String p = param.trim();
            // 1. 处理字符串 (带有单引号或双引号)
            if ((p.startsWith("'") && p.endsWith("'")) || (p.startsWith("\"") && p.endsWith("\""))) {
                results.add(new Object[]{p.substring(1, p.length() - 1), String.class});
            }
            // 2. 处理布尔值
            else if ("true".equalsIgnoreCase(p) || "false".equalsIgnoreCase(p)) {
                results.add(new Object[]{Boolean.valueOf(p), Boolean.class});
            }
            // 3. 处理数字 (这里暂统一处理为 Integer，可根据需要扩展)
            else {
                results.add(new Object[]{Integer.valueOf(p), Integer.class});
            }
        }
        return results;
    }
}
