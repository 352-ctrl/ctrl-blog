package com.example.blog.modules.monitor.service.impl;

import cn.hutool.core.util.StrUtil;
import com.example.blog.modules.monitor.model.vo.ServerMonitorVO;
import com.example.blog.modules.monitor.service.ServerMonitorService;
import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServerMonitorServiceImpl implements ServerMonitorService {

    @Resource
    private MeterRegistry meterRegistry;

    @Override
    public ServerMonitorVO getServerInfo() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // 1. CPU 使用率信息
        Map<String, Object> cpu = new HashMap<>();
        double cpuUsage = getMetricValue("system.cpu.usage");
        cpu.put("cpuNum", Runtime.getRuntime().availableProcessors());
        cpu.put("sysUsage", String.format("%.2f%%", cpuUsage * 100)); // 系统总使用率
        cpu.put("userUsage", String.format("%.2f%%", getMetricValue("process.cpu.usage") * 100)); // 用户进程使用率
        cpu.put("freeUsage", String.format("%.2f%%", (1 - cpuUsage) * 100)); // 空闲率

        // 2. 物理内存信息 (非JVM内存)
        Map<String, Object> memory = new HashMap<>();
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        memory.put("total", formatByteSize(totalMemory));
        memory.put("used", formatByteSize(usedMemory));
        memory.put("free", formatByteSize(freeMemory));
        memory.put("usage", String.format("%.2f%%", ((double) usedMemory / totalMemory) * 100));

        // 3. JVM 内存信息
        Map<String, Object> jvm = new HashMap<>();
        double jvmUsed = getMetricValue("jvm.memory.used");
        double jvmMax = getMetricValue("jvm.memory.max");
        jvm.put("used", formatByteSize((long) jvmUsed));
        jvm.put("max", formatByteSize((long) jvmMax));
        jvm.put("usage", String.format("%.2f%%", (jvmUsed / jvmMax) * 100));
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("home", System.getProperty("java.home"));
        jvm.put("uptime", formatTime((long) getMetricValue("process.uptime")));

        // 4. 服务器系统信息
        Map<String, Object> sys = new HashMap<>();
        sys.put("osName", osBean.getName());
        sys.put("osArch", osBean.getArch());

        // 5. 磁盘状态 (根目录)
        Map<String, Object> disk = new HashMap<>();
        File root = new File("/");
        long diskTotal = root.getTotalSpace();
        long diskFree = root.getFreeSpace();
        long diskUsed = diskTotal - diskFree;
        disk.put("total", formatByteSize(diskTotal));
        disk.put("free", formatByteSize(diskFree));
        disk.put("used", formatByteSize(diskUsed));
        disk.put("usage", String.format("%.2f%%", diskTotal > 0 ? ((double) diskUsed / diskTotal) * 100 : 0));

        return ServerMonitorVO.builder()
                .cpu(cpu)
                .memory(memory)
                .jvm(jvm)
                .sys(sys)
                .disk(disk)
                .build();
    }

    private double getMetricValue(String metricName) {
        Gauge gauge = meterRegistry.find(metricName).gauge();
        return gauge != null ? gauge.value() : 0.0;
    }

    private String formatByteSize(long size) {
        if (size <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int index = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, index), units[index]);
    }

    private String formatTime(long seconds) {
        long day = seconds / (24 * 3600);
        long hour = (seconds % (24 * 3600)) / 3600;
        long minute = (seconds % 3600) / 60;
        long second = seconds % 60;
        return (day > 0 ? day + "天 " : StrUtil.EMPTY) + hour + "小时 " + minute + "分 " + second + "秒";
    }
}