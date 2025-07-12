package com.tedu.manager;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import com.tedu.utils.GameConfig;

/**
 * 铲子管理器 - 处理铲子模式的切换和显示 (修复版)
 */
public class ShovelManager {
    
    private static ShovelManager instance = null;
    
    // 铲子状态
    public enum ShovelState {
        INACTIVE,   // 未激活
        ACTIVE,     // 激活状态
        HOVER       // 悬停状态
    }
    
    private ShovelState currentState = ShovelState.INACTIVE;
    private ImageIcon shovelIdleIcon;
    private ImageIcon shovelHoverIcon;
    private ImageIcon shovelActiveIcon;
    
    private Cursor normalCursor;      // 正常鼠标光标
    private Cursor shovelCursor;      // 铲子光标
    private JComponent gamePanel;     // 游戏面板引用
    
    // 铲子按钮位置（在UI中的位置）
    private int shovelButtonX = 400;
    private int shovelButtonY = 10;
    private int shovelButtonWidth = 50;
    private int shovelButtonHeight = 50;
    
    private ShovelManager() {
        loadShovelImages();
        createShovelCursor();
    }
    
    public static synchronized ShovelManager getInstance() {
        if (instance == null) {
            instance = new ShovelManager();
        }
        return instance;
    }
    
    /**
     * 加载铲子图片资源 - 修复版
     */
    private void loadShovelImages() {
        try {
            // 尝试从GameLoad加载图片（如果GameLoad可用）
            // shovelIdleIcon = GameLoad.imgMap.get("shovel_idle");
            // shovelHoverIcon = GameLoad.imgMap.get("shovel_hover");
            // shovelActiveIcon = GameLoad.imgMap.get("shovel_active");
            
            // 如果图片不存在或GameLoad不可用，直接创建默认图片
            System.out.println("⚠️ 创建默认铲子图片");
            createDefaultShovelImages();
            
        } catch (Exception e) {
            System.err.println("加载铲子图片失败: " + e.getMessage());
            createDefaultShovelImages();
        }
    }
    
    /**
     * 创建默认的铲子图片
     */
    private void createDefaultShovelImages() {
        try {
            // 创建空闲状态铲子图标
            BufferedImage idleImage = createShovelImage(java.awt.Color.ORANGE, java.awt.Color.GRAY);
            shovelIdleIcon = new ImageIcon(idleImage);
            
            // 创建悬停状态铲子图标（稍微亮一些）
            BufferedImage hoverImage = createShovelImage(java.awt.Color.YELLOW, java.awt.Color.LIGHT_GRAY);
            shovelHoverIcon = new ImageIcon(hoverImage);
            
            // 创建激活状态铲子图标（高亮）
            BufferedImage activeImage = createShovelImage(java.awt.Color.RED, java.awt.Color.WHITE);
            shovelActiveIcon = new ImageIcon(activeImage);
            
            System.out.println("✅ 创建了默认铲子图片");
            
        } catch (Exception e) {
            System.err.println("创建默认铲子图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建铲子图像
     */
    private BufferedImage createShovelImage(java.awt.Color handleColor, java.awt.Color bladeColor) {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // 启用抗锯齿
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制铲子柄
        g2d.setColor(handleColor);
        g2d.fillRect(14, 5, 4, 20);
        
        // 绘制铲子头
        g2d.setColor(bladeColor);
        g2d.fillOval(8, 20, 16, 10);
        
        // 绘制边框
        g2d.setColor(java.awt.Color.BLACK);
        g2d.setStroke(new java.awt.BasicStroke(1));
        g2d.drawRect(14, 5, 4, 20);  // 铲子柄边框
        g2d.drawOval(8, 20, 16, 10); // 铲子头边框
        
        g2d.dispose();
        return image;
    }
    
    /**
     * 创建铲子光标
     */
    private void createShovelCursor() {
        try {
            normalCursor = Cursor.getDefaultCursor();
            
            if (shovelIdleIcon != null) {
                // 使用铲子图片创建自定义光标
                BufferedImage cursorImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = cursorImage.createGraphics();
                
                // 启用抗锯齿
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.drawImage(shovelIdleIcon.getImage(), 0, 0, 24, 24, null);
                
                // 添加阴影效果
                g2d.setColor(new java.awt.Color(0, 0, 0, 50));
                g2d.fillOval(1, 25, 22, 5);
                
                g2d.dispose();
                
                shovelCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    cursorImage, new Point(12, 12), "Shovel Cursor"
                );
                
                System.out.println("✅ 铲子光标创建成功");
            } else {
                // 如果没有图片，使用系统预定义的光标
                shovelCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                System.out.println("⚠️ 使用默认十字光标作为铲子光标");
            }
            
        } catch (Exception e) {
            System.err.println("创建铲子光标失败: " + e.getMessage());
            shovelCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
    }
    
    /**
     * 设置游戏面板引用
     */
    public void setGamePanel(JComponent panel) {
        this.gamePanel = panel;
    }
    
    /**
     * 激活铲子模式
     */
    public void activateShovel() {
        if (currentState != ShovelState.ACTIVE) {
            currentState = ShovelState.ACTIVE;
            
            if (gamePanel != null) {
                gamePanel.setCursor(shovelCursor);
            }
            
            System.out.println("🔧 铲子模式已激活！点击植物来铲除");
        }
    }
    
    /**
     * 取消激活铲子模式
     */
    public void deactivateShovel() {
        if (currentState != ShovelState.INACTIVE) {
            currentState = ShovelState.INACTIVE;
            
            if (gamePanel != null) {
                gamePanel.setCursor(normalCursor);
            }
            
            System.out.println("🔧 铲子模式已取消");
        }
    }
    
    /**
     * 切换铲子状态
     */
    public void toggleShovel() {
        if (currentState == ShovelState.ACTIVE) {
            deactivateShovel();
        } else {
            activateShovel();
        }
    }
    
    /**
     * 检查是否点击了铲子按钮
     */
    public boolean isShovelButtonClicked(int mouseX, int mouseY) {
        return mouseX >= shovelButtonX && mouseX <= shovelButtonX + shovelButtonWidth &&
               mouseY >= shovelButtonY && mouseY <= shovelButtonY + shovelButtonHeight;
    }
    
    /**
     * 处理鼠标悬停在铲子按钮上
     */
    public void handleShovelButtonHover(int mouseX, int mouseY) {
        boolean isHovering = isShovelButtonClicked(mouseX, mouseY);
        
        if (isHovering && currentState == ShovelState.INACTIVE) {
            currentState = ShovelState.HOVER;
        } else if (!isHovering && currentState == ShovelState.HOVER) {
            currentState = ShovelState.INACTIVE;
        }
    }
    
    /**
     * 绘制铲子按钮
     */
    public void drawShovelButton(Graphics2D g2d) {
        // 根据状态选择图片
        ImageIcon iconToDraw;
        switch (currentState) {
            case ACTIVE:
                iconToDraw = shovelActiveIcon != null ? shovelActiveIcon : shovelIdleIcon;
                break;
            case HOVER:
                iconToDraw = shovelHoverIcon != null ? shovelHoverIcon : shovelIdleIcon;
                break;
            default:
                iconToDraw = shovelIdleIcon;
                break;
        }
        
        // 绘制按钮背景
        if (currentState == ShovelState.ACTIVE) {
            g2d.setColor(new java.awt.Color(255, 255, 0, 200)); // 黄色高亮
        } else if (currentState == ShovelState.HOVER) {
            g2d.setColor(new java.awt.Color(200, 200, 200, 150)); // 灰色悬停
        } else {
            g2d.setColor(new java.awt.Color(139, 69, 19, 150)); // 棕色背景
        }
        
        g2d.fillRoundRect(shovelButtonX, shovelButtonY, shovelButtonWidth, shovelButtonHeight, 8, 8);
        
        // 绘制边框
        g2d.setColor(currentState == ShovelState.ACTIVE ? java.awt.Color.YELLOW : java.awt.Color.BLACK);
        g2d.setStroke(new java.awt.BasicStroke(currentState == ShovelState.ACTIVE ? 3 : 1));
        g2d.drawRoundRect(shovelButtonX, shovelButtonY, shovelButtonWidth, shovelButtonHeight, 8, 8);
        
        // 绘制铲子图标
        if (iconToDraw != null) {
            g2d.drawImage(iconToDraw.getImage(), 
                         shovelButtonX + 9, shovelButtonY + 9,
                         shovelButtonWidth - 18, shovelButtonHeight - 18, null);
        } else {
            // 如果没有图标，绘制文字
            g2d.setColor(currentState == ShovelState.ACTIVE ? java.awt.Color.BLACK : java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            g2d.drawString("铲", shovelButtonX + 15, shovelButtonY + 30);
        }
        
        // 绘制快捷键提示
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 8));
        g2d.drawString("S", shovelButtonX + 3, shovelButtonY + 12);
        
        // 绘制状态文字
        if (currentState == ShovelState.ACTIVE) {
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 8));
            g2d.drawString("激活", shovelButtonX + 2, shovelButtonY + 47);
        }
    }
    
    /**
     * 使用铲子铲除植物
     */
    public boolean useShovel(int gridX, int gridY) {
        if (currentState != ShovelState.ACTIVE) {
            return false; // 铲子未激活
        }
        
        System.out.println("🔧 使用铲子铲除植物在网格 (" + gridX + "," + gridY + ")");
        
        // 铲除成功后可以选择自动取消激活铲子
        // deactivateShovel();
        
        return true;
    }
    
    // Getter方法
    public boolean isShovelActive() {
        return currentState == ShovelState.ACTIVE;
    }
    
    public ShovelState getCurrentState() {
        return currentState;
    }
    
    public int getShovelButtonX() { return shovelButtonX; }
    public int getShovelButtonY() { return shovelButtonY; }
    public int getShovelButtonWidth() { return shovelButtonWidth; }
    public int getShovelButtonHeight() { return shovelButtonHeight; }
    
    // Setter方法（用于自定义按钮位置）
    public void setShovelButtonPosition(int x, int y) {
        this.shovelButtonX = x;
        this.shovelButtonY = y;
    }
    
    public void setShovelButtonSize(int width, int height) {
        this.shovelButtonWidth = width;
        this.shovelButtonHeight = height;
    }
}