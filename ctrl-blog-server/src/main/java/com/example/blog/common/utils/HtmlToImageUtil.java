package com.example.blog.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 纯 Java 原生实现的高颜值封面生成器 (毛玻璃质感 + 得意黑字体)
 */
public class HtmlToImageUtil {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 630;

    // 静态缓存字体，避免每次生成都去读取 IO 和解析字体，极大提升并发性能
    private static Font CUSTOM_TITLE_FONT;
    private static Font CUSTOM_AUTHOR_FONT;

    static {
        try {
            // 使用 .ttf 文件，并确保路径与你截图的目录结构完全一致
            InputStream fontStream = HtmlToImageUtil.class.getResourceAsStream("/fonts/DeYiHei/SmileySans-Oblique-2.ttf");
            if (fontStream != null) {
                // 加载 TTF 字体
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                // 标题字号 64，作者字号 32
                CUSTOM_TITLE_FONT = baseFont.deriveFont(Font.PLAIN, 64f);
                CUSTOM_AUTHOR_FONT = baseFont.deriveFont(Font.PLAIN, 32f);
            } else {
                System.err.println("警告：未找到字体文件，将使用系统默认字体");
                CUSTOM_TITLE_FONT = new Font("SansSerif", Font.BOLD, 64);
                CUSTOM_AUTHOR_FONT = new Font("SansSerif", Font.PLAIN, 32);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CUSTOM_TITLE_FONT = new Font("SansSerif", Font.BOLD, 64);
            CUSTOM_AUTHOR_FONT = new Font("SansSerif", Font.PLAIN, 32);
        }
    }

    // 多套极客风高颜值渐变配色方案
    private static final String[][] GRADIENT_COLORS = {
            {"#667eea", "#764ba2"}, // 经典紫蓝
            {"#84fab0", "#8fd3f4"}, // 清新极光绿
            {"#fa709a", "#fee140"}, // 日落甜橙
            {"#30cfd0", "#330867"}, // 深海幽蓝
            {"#4facfe", "#00f2fe"}  // 科技亮蓝
    };

    /**
     * 生成封面图并直接返回字节数组
     */
    public static byte[] generateCoverBytes(String title, String author) {
        try {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 开启最高级别的抗锯齿，保证字体和圆角极度平滑
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 随机挑选一套渐变色
            ThreadLocalRandom random = ThreadLocalRandom.current();
            String[] colors = GRADIENT_COLORS[random.nextInt(GRADIENT_COLORS.length)];
            Color colorStart = Color.decode(colors[0]);
            Color colorEnd = Color.decode(colors[1]);

            // 1. 绘制底层渐变与波点纹理
            drawPatternBackground(g2d, colorStart, colorEnd);

            // 2. 绘制毛玻璃半透明内容卡片
            drawGlassCard(g2d);

            // 3. 绘制文字 (自动换行、居中、带阴影)
            drawCenteredText(g2d, title, author, CUSTOM_TITLE_FONT, CUSTOM_AUTHOR_FONT);

            g2d.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("封面生成失败", e);
        }
    }

    /**
     * 绘制带有波点纹理的渐变背景
     */
    private static void drawPatternBackground(Graphics2D g2d, Color start, Color end) {
        // 底色
        g2d.setColor(new Color(245, 245, 250));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // 波点纹理
        g2d.setColor(new Color(255, 255, 255, 60));
        int step = 40;
        for (int x = 0; x < WIDTH; x += step) {
            for (int y = 0; y < HEIGHT; y += step) {
                g2d.fillOval(x - 3, y - 3, 6, 6);
            }
        }

        // 覆盖渐变色，营造滤镜感
        LinearGradientPaint overlay = new LinearGradientPaint(
                new Point2D.Float(0, 0), new Point2D.Float(WIDTH, HEIGHT),
                new float[]{0.0f, 1.0f}, new Color[]{start, end}
        );
        g2d.setPaint(overlay);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /**
     * 绘制毛玻璃质感卡片
     */
    private static void drawGlassCard(Graphics2D g2d) {
        int cardWidth = WIDTH - 200;
        int cardHeight = HEIGHT - 160;
        int cardX = 100;
        int cardY = 80;
        int arc = 40; // 圆角弧度

        // 卡片外层泛光阴影
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(cardX + 10, cardY + 15, cardWidth, cardHeight, arc, arc);

        // 卡片半透明主体 (磨砂白)
        g2d.setColor(new Color(255, 255, 255, 215));
        g2d.fillRoundRect(cardX, cardY, cardWidth, cardHeight, arc, arc);

        // 卡片极细白色边框 (模拟玻璃边缘反光)
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.draw(new RoundRectangle2D.Float(cardX, cardY, cardWidth, cardHeight, arc, arc));
    }

    /**
     * 在卡片内部绘制居中文本
     */
    private static void drawCenteredText(Graphics2D g2d, String title, String author, Font titleFont, Font authorFont) {
        FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
        FontMetrics authorMetrics = g2d.getFontMetrics(authorFont);

        int maxTextWidth = WIDTH - 300; // 限制在卡片内部的宽度
        List<String> titleLines = new ArrayList<>();

        // 标题自动换行逻辑
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

        int titleLineHeight = titleMetrics.getHeight();
        int authorMarginTop = 50;
        // 计算整体文本块高度，用于绝对垂直居中
        int totalHeight = (titleLines.size() * titleLineHeight) + authorMarginTop + authorMetrics.getHeight();
        int startY = (HEIGHT - totalHeight) / 2 + titleMetrics.getAscent();

        // 绘制标题
        int currentY = startY;
        for (String line : titleLines) {
            int x = (WIDTH - titleMetrics.stringWidth(line)) / 2;
            // 绘制文字阴影 (微弱的黑色偏移)
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.setFont(titleFont);
            g2d.drawString(line, x + 2, currentY + 2);
            // 绘制文字主体 (深色深邃字体)
            g2d.setColor(new Color(40, 44, 52));
            g2d.drawString(line, x, currentY);
            currentY += titleLineHeight;
        }

        // 绘制作者签名
        currentY += authorMarginTop;
        String authorText = "——  " + author + "  ——";
        int authorX = (WIDTH - authorMetrics.stringWidth(authorText)) / 2;
        g2d.setFont(authorFont);
        g2d.setColor(new Color(100, 108, 120));
        g2d.drawString(authorText, authorX, currentY);
    }
}