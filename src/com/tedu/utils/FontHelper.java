package com.tedu.utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字体处理工具类 - 专门解决中文显示问题
 */
public class FontHelper {
    
    private static Font defaultChineseFont = null;
    private static boolean initialized = false;
    
    // 中文字体优先级列表
    private static final String[] CHINESE_FONT_NAMES = {
        "Microsoft YaHei",      // 微软雅黑
        "SimHei",               // 黑体
        "Microsoft JhengHei",   // 微软正黑体
        "PingFang SC",          // 苹方-简
        "Hiragino Sans GB",     // 冬青黑体简体中文
        "Source Han Sans CN",   // 思源黑体
        "Noto Sans CJK SC",     // Noto Sans 中日韩
        "WenQuanYi Micro Hei",  // 文泉驿微米黑
        "SimSun",               // 宋体
        "NSimSun",              // 新宋体
        "FangSong",             // 仿宋
        "KaiTi",                // 楷体
        "Dialog",               // 系统默认
        "SansSerif",            // Sans Serif
        "Serif",                // Serif
        "Monospaced"            // 等宽字体
    };
    
    /**
     * 初始化字体系统
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        System.out.println("正在初始化中文字体支持...");
        
        // 获取系统所有可用字体
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        System.out.println("系统可用字体数量: " + availableFonts.length);
        
        // 查找最佳中文字体
        defaultChineseFont = findBestChineseFont(availableFonts);
        
        if (defaultChineseFont != null) {
            System.out.println("选择的中文字体: " + defaultChineseFont.getFontName());
        } else {
            System.out.println("未找到合适的中文字体，将使用默认字体");
            defaultChineseFont = new Font("Dialog", Font.PLAIN, 12);
        }
        
        initialized = true;
    }
    
    /**
     * 查找最佳的中文字体
     */
    private static Font findBestChineseFont(String[] availableFonts) {
        List<String> availableList = Arrays.asList(availableFonts);
        
        // 按优先级顺序查找字体
        for (String fontName : CHINESE_FONT_NAMES) {
            if (availableList.contains(fontName)) {
                Font testFont = new Font(fontName, Font.PLAIN, 12);
                
                // 测试字体是否能正确显示中文
                if (canDisplayChinese(testFont)) {
                    System.out.println("找到合适的中文字体: " + fontName);
                    return testFont;
                }
            }
        }
        
        // 如果没有找到预定义的字体，尝试查找任何包含"微软"、"黑体"等关键词的字体
        for (String fontName : availableFonts) {
            if (fontName.contains("微软") || fontName.contains("黑体") || 
                fontName.contains("宋体") || fontName.contains("雅黑")) {
                Font testFont = new Font(fontName, Font.PLAIN, 12);
                if (canDisplayChinese(testFont)) {
                    System.out.println("找到中文字体: " + fontName);
                    return testFont;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 测试字体是否能显示中文
     */
    private static boolean canDisplayChinese(Font font) {
        String[] testStrings = {
            "中文测试",
            "植物大战僵尸",
            "豌豆射手",
            "向日葵",
            "坚果墙"
        };
        
        for (String test : testStrings) {
            if (font.canDisplayUpTo(test) != -1) {
                return false; // 如果有字符无法显示，返回false
            }
        }
        return true;
    }
    
    /**
     * 获取中文字体
     */
    public static Font getChineseFont(int style, int size) {
        if (!initialized) {
            initialize();
        }
        
        if (defaultChineseFont != null) {
            return defaultChineseFont.deriveFont(style, size);
        } else {
            return new Font("Dialog", style, size);
        }
    }
    
    /**
     * 获取默认大小的中文字体
     */
    public static Font getChineseFont() {
        return getChineseFont(Font.PLAIN, 12);
    }
    
    /**
     * 获取粗体中文字体
     */
    public static Font getBoldChineseFont(int size) {
        return getChineseFont(Font.BOLD, size);
    }
    
    /**
     * 获取斜体中文字体
     */
    public static Font getItalicChineseFont(int size) {
        return getChineseFont(Font.ITALIC, size);
    }
    
    /**
     * 创建自定义字体，如果不支持中文则回退到默认中文字体
     */
    public static Font createFont(String fontName, int style, int size) {
        Font customFont = new Font(fontName, style, size);
        
        // 测试是否支持中文
        if (canDisplayChinese(customFont)) {
            return customFont;
        } else {
            System.out.println("字体 " + fontName + " 不支持中文，使用默认中文字体");
            return getChineseFont(style, size);
        }
    }
    
    /**
     * 打印系统字体信息（调试用）
     */
    public static void printSystemFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        
        System.out.println("=== 系统可用字体 ===");
        for (String font : fonts) {
            System.out.println(font);
        }
        System.out.println("=== 字体列表结束 ===");
    }
    
    /**
     * 测试字体显示效果
     */
    public static void testFontDisplay() {
        if (!initialized) {
            initialize();
        }
        
        String testText = "植物大战僵尸游戏测试";
        Font testFont = getChineseFont();
        
        System.out.println("测试文本: " + testText);
        System.out.println("使用字体: " + testFont.getFontName());
        System.out.println("字体支持测试: " + (testFont.canDisplayUpTo(testText) == -1 ? "支持" : "不支持"));
    }
}