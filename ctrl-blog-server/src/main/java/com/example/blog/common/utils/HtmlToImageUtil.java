package com.example.blog.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 纯 Java 原生实现的高颜值封面生成器 (零第三方依赖)
 * 完美替代 Playwright，极低内存消耗，毫秒级响应
 */
public class HtmlToImageUtil {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 630;
    private static final int PADDING = 80;

    // ============================== 1. 准备多套高颜值渐变配色方案 ==============================
    private static final String[][] GRADIENT_COLORS = {
            {"#667eea", "#764ba2"}, // 经典紫蓝
            {"#84fab0", "#8fd3f4"}, // 清新极光绿
            {"#fa709a", "#fee140"}, // 日落甜橙
            {"#30cfd0", "#330867"}, // 深海幽蓝
            {"#a8edea", "#fed6e3"}, // 糖果粉嫩
            {"#8e9eab", "#eef2f3"}, // 极简灰白
            {"#4facfe", "#00f2fe"}, // 科技亮蓝
            {"#43e97b", "#38f9d7"}  // 原谅色系
    };

    // ============================== 2. 核心生成逻辑 ==============================

    /**
     * 生成封面图并直接返回字节数组 (完全在内存中绘制，无本地磁盘 I/O)
     *
     * @param title  文章标题
     * @param author 作者名
     * @return 图片的 byte 数组
     */
    public static byte[] generateCoverBytes(String title, String author) {
        try {
            // 1. 创建画布
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 开启抗锯齿，使文字和几何图形极度平滑
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 2. 随机选择一套配色
            ThreadLocalRandom random = ThreadLocalRandom.current();
            String[] colors = GRADIENT_COLORS[random.nextInt(GRADIENT_COLORS.length)];
            Color colorStart = Color.decode(colors[0]);
            Color colorEnd = Color.decode(colors[1]);

            // 3. 绘制背景 (50%概率纯渐变，50%概率带底纹的半透明渐变)
            boolean drawPattern = random.nextBoolean();
            if (drawPattern) {
                drawPatternBackground(g2d, colorStart, colorEnd, random.nextInt(3));
            } else {
                drawGradientBackground(g2d, colorStart, colorEnd);
            }

            // 4. 准备字体
            // 优先使用系统自带的中文字体，如果没有则使用默认的 SansSerif
            Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 64);
            Font authorFont = new Font("Microsoft YaHei", Font.PLAIN, 32);

            // 5. 绘制文字 (自动换行与垂直居中算法)
            drawCenteredText(g2d, title, author, titleFont, authorFont);

            // 6. 释放画笔并输出为字节数组
            g2d.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("原生封面生成失败", e);
        }
    }

    // ============================== 3. 内部绘图算法实现 ==============================

    /**
     * 绘制纯渐变背景
     */
    private static void drawGradientBackground(Graphics2D g2d, Color start, Color end) {
        // 对角线渐变
        GradientPaint gradient = new GradientPaint(0, 0, start, WIDTH, HEIGHT, end);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /**
     * 绘制带有几何纹理的渐变背景 (高度模拟 CSS 纹理效果)
     */
    private static void drawPatternBackground(Graphics2D g2d, Color start, Color end, int patternType) {
        // 先铺一层浅色底
        g2d.setColor(new Color(245, 245, 250));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        g2d.setColor(new Color(200, 200, 220, 100)); // 纹理颜色
        g2d.setStroke(new BasicStroke(1.5f));

        // 绘制不同底纹
        int step = 30;
        switch (patternType) {
            case 0: // 网格纹理
                for (int i = 0; i < WIDTH; i += step) g2d.drawLine(i, 0, i, HEIGHT);
                for (int i = 0; i < HEIGHT; i += step) g2d.drawLine(0, i, WIDTH, i);
                break;
            case 1: // 波点纹理
                for (int x = 0; x < WIDTH; x += step) {
                    for (int y = 0; y < HEIGHT; y += step) {
                        g2d.fillOval(x - 2, y - 2, 4, 4);
                    }
                }
                break;
            case 2: // 斜线纹理
                for (int i = -HEIGHT; i < WIDTH; i += step) {
                    g2d.drawLine(i, 0, i + HEIGHT, HEIGHT);
                }
                break;
        }

        // 覆盖一层 85% 不透明度的渐变色，营造高级遮罩感
        Color overlayStart = new Color(start.getRed(), start.getGreen(), start.getBlue(), 215);
        Color overlayEnd = new Color(end.getRed(), end.getGreen(), end.getBlue(), 215);
        LinearGradientPaint overlay = new LinearGradientPaint(
                new Point2D.Float(0, 0), new Point2D.Float(WIDTH, HEIGHT),
                new float[]{0.0f, 1.0f}, new Color[]{overlayStart, overlayEnd}
        );
        g2d.setPaint(overlay);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /**
     * 绘制居中且支持自动换行的文字 (自带高级文本阴影)
     */
    private static void drawCenteredText(Graphics2D g2d, String title, String author, Font titleFont, Font authorFont) {
        FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
        FontMetrics authorMetrics = g2d.getFontMetrics(authorFont);

        int maxTextWidth = WIDTH - (PADDING * 2);
        List<String> titleLines = new ArrayList<>();

        // 1. 标题自动换行算法
        StringBuilder currentLine = new StringBuilder();
        for (char c : title.toCharArray()) {
            if (titleMetrics.stringWidth(currentLine.toString() + c) > maxTextWidth) {
                titleLines.add(currentLine.toString());
                currentLine = new StringBuilder(String.valueOf(c));
            } else {
                currentLine.append(c);
            }
        }
        titleLines.add(currentLine.toString());

        // 2. 计算整体文本块的高度，用于绝对垂直居中
        int titleLineHeight = titleMetrics.getHeight();
        int authorMarginTop = 40;
        int totalHeight = (titleLines.size() * titleLineHeight) + authorMarginTop + authorMetrics.getHeight();

        int startY = (HEIGHT - totalHeight) / 2 + titleMetrics.getAscent();

        // 3. 开始逐行绘制标题
        int currentY = startY;
        for (String line : titleLines) {
            int x = (WIDTH - titleMetrics.stringWidth(line)) / 2;
            drawTextWithShadow(g2d, line, x, currentY, titleFont);
            currentY += titleLineHeight;
        }

        // 4. 绘制作者 (叠加在标题下方)
        currentY += authorMarginTop;
        String authorText = "By " + author;
        int authorX = (WIDTH - authorMetrics.stringWidth(authorText)) / 2;
        drawTextWithShadow(g2d, authorText, authorX, currentY, authorFont);
    }

    /**
     * 绘制带阴影的文字 (还原 CSS text-shadow)
     */
    private static void drawTextWithShadow(Graphics2D g2d, String text, int x, int y, Font font) {
        g2d.setFont(font);
        // 先画阴影 (右下角偏移 2px，颜色为半透明黑)
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawString(text, x + 2, y + 2);
        // 再画主体 (纯白色)
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x, y);
    }
}