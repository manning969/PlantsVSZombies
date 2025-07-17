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
 * 铲子管理器 - 使用真实图片资源版本
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
     * 加载铲子图片资源 - 修复版，使用真实图片
     */
    private void loadShovelImages() {
        try {
            // 优先从GameLoad的imgMap加载
            if (GameLoad.imgMap != null) {
                // 尝试多种可能的键名
                String[] possibleIdleKeys = {"shovel_idle", "shovel_idle_png", "ui_shovel_idle"};
                String[] possibleHoverKeys = {"shovel_hover", "shovel_hover_png", "ui_shovel_hover"};
                String[] possibleActiveKeys = {"shovel_active", "shovel_activate", "shovel_active_png", "ui_shovel_active"};
                
                // 查找空闲状态图片
                for (String key : possibleIdleKeys) {
                    if (GameLoad.imgMap.containsKey(key)) {
                        shovelIdleIcon = GameLoad.imgMap.get(key);
                        System.out.println("✅ 找到空闲铲子图片，键: " + key);
                        break;
                    }
                }
                
                // 查找悬停状态图片
                for (String key : possibleHoverKeys) {
                    if (GameLoad.imgMap.containsKey(key)) {
                        shovelHoverIcon = GameLoad.imgMap.get(key);
                        System.out.println("✅ 找到悬停铲子图片，键: " + key);
                        break;
                    }
                }
                
                // 查找激活状态图片
                for (String key : possibleActiveKeys) {
                    if (GameLoad.imgMap.containsKey(key)) {
                        shovelActiveIcon = GameLoad.imgMap.get(key);
                        System.out.println("✅ 找到激活铲子图片，键: " + key);
                        break;
                    }
                }
                
                // 检查加载结果
                if (shovelIdleIcon != null && shovelHoverIcon != null && shovelActiveIcon != null) {
                    System.out.println("✅ 从GameLoad.imgMap加载所有铲子图片成功");
                    return;
                } else {
                    // 如果只找到部分图片，尝试用已找到的图片替代缺失的
                    if (shovelIdleIcon != null) {
                        if (shovelHoverIcon == null) {
                            shovelHoverIcon = shovelIdleIcon;
                            System.out.println("🔄 使用idle图片替代hover图片");
                        }
                        if (shovelActiveIcon == null) {
                            shovelActiveIcon = shovelIdleIcon;
                            System.out.println("🔄 使用idle图片替代active图片");
                        }
                        System.out.println("✅ 使用替代方案完成铲子图片加载");
                        return;
                    }
                }
            }
            
            // 备用方案：直接从resources路径加载
            shovelIdleIcon = loadImageFromResources("ui/shovel/shovel_idle.png");
            shovelHoverIcon = loadImageFromResources("ui/shovel/shovel_hover.png");
            shovelActiveIcon = loadImageFromResources("ui/shovel/shovel_active.png");
            
            if (shovelIdleIcon != null || shovelHoverIcon != null || shovelActiveIcon != null) {
                // 用成功加载的图片替代失败的
                if (shovelIdleIcon != null) {
                    if (shovelHoverIcon == null) shovelHoverIcon = shovelIdleIcon;
                    if (shovelActiveIcon == null) shovelActiveIcon = shovelIdleIcon;
                }
                return;
            }
            
            // 最后的备用方案：创建默认图片
            System.out.println("⚠️ 无法加载任何铲子图片，使用默认绘制图片");
            createDefaultShovelImages();
            
        } catch (Exception e) {
            System.err.println("加载铲子图片失败: " + e.getMessage());
            createDefaultShovelImages();
        }
    }
    
    /**
     * 从resources路径加载图片
     */
    private ImageIcon loadImageFromResources(String imagePath) {
        try {
            // 尝试多种路径格式
            String[] possiblePaths = {
                "resources/images/" + imagePath,
                "/resources/images/" + imagePath,
                "images/" + imagePath,
                "/" + imagePath,
                imagePath
            };
            
            for (String path : possiblePaths) {
                try {
                    java.net.URL imageURL = getClass().getClassLoader().getResource(path);
                    if (imageURL != null) {
                        ImageIcon icon = new ImageIcon(imageURL);
                        if (icon.getIconWidth() > 0) { // 确保图片有效
                            System.out.println("✅ 成功加载图片: " + path);
                            return icon;
                        }
                    }
                } catch (Exception e) {
                    // 继续尝试下一个路径
                }
            }
            
            // 尝试从文件系统加载
            try {
                String filePath = "resources/images/" + imagePath;
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) {
                    ImageIcon icon = new ImageIcon(filePath);
                    if (icon.getIconWidth() > 0) {
                        return icon;
                    }
                }
            } catch (Exception e) {
                // 忽略文件系统加载失败
            }
            
        } catch (Exception e) {
            System.err.println("加载图片失败 " + imagePath + ": " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 创建默认的铲子图片（仅在无法加载真实图片时使用）
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
     * 绘制铲子按钮 - 使用真实图片
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
        
        // 绘制按钮背景（可选，根据设计需求）
        if (currentState == ShovelState.ACTIVE) {
            g2d.setColor(new java.awt.Color(255, 255, 0, 100)); // 半透明黄色高亮
            g2d.fillRoundRect(shovelButtonX - 2, shovelButtonY - 2, 
                             shovelButtonWidth + 4, shovelButtonHeight + 4, 8, 8);
        } else if (currentState == ShovelState.HOVER) {
            g2d.setColor(new java.awt.Color(200, 200, 200, 80)); // 半透明灰色悬停
            g2d.fillRoundRect(shovelButtonX - 1, shovelButtonY - 1, 
                             shovelButtonWidth + 2, shovelButtonHeight + 2, 6, 6);
        }
        
        // 绘制铲子图标 - 主要使用真实图片
        if (iconToDraw != null) {
            g2d.drawImage(iconToDraw.getImage(), 
                         shovelButtonX, shovelButtonY,
                         shovelButtonWidth, shovelButtonHeight, null);
        } else {
            // 备用方案：如果图片完全加载失败，绘制简单文字
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRoundRect(shovelButtonX, shovelButtonY, shovelButtonWidth, shovelButtonHeight, 8, 8);
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setStroke(new java.awt.BasicStroke(2));
            g2d.drawRoundRect(shovelButtonX, shovelButtonY, shovelButtonWidth, shovelButtonHeight, 8, 8);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            g2d.drawString("铲", shovelButtonX + 17, shovelButtonY + 30);
        }
        
        // 绘制激活状态边框
        if (currentState == ShovelState.ACTIVE) {
            g2d.setColor(java.awt.Color.YELLOW);
            g2d.setStroke(new java.awt.BasicStroke(3));
            g2d.drawRoundRect(shovelButtonX - 2, shovelButtonY - 2, 
                             shovelButtonWidth + 4, shovelButtonHeight + 4, 8, 8);
        }
        
        // 绘制快捷键提示
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 8));
        g2d.fillRect(shovelButtonX + 2, shovelButtonY + 2, 12, 10);
        g2d.setColor(java.awt.Color.BLACK);
        g2d.drawString("S", shovelButtonX + 4, shovelButtonY + 10);
    }
    
    /**
     * 使用铲子铲除植物
     */
    public boolean useShovel(int gridX, int gridY) {
	    if (currentState != ShovelState.ACTIVE) {
	        return false; // 铲子未激活
	    }
	    
	    // 查找指定网格位置的植物
	    com.tedu.element.Plant plant = findPlantAtGrid(gridX, gridY);
	    if (plant != null) {
	        return useShovelWithRefund(plant);
	    } else {
	        System.out.println("⚠️ 网格 (" + gridX + "," + gridY + ") 没有找到植物");
	        return false;
	    }
	}
    
    /**
	 * 使用铲子铲除植物并返还金币
	 */
    private boolean useShovelWithRefund(com.tedu.element.Plant plant) {
        System.out.println("=== 铲除植物调试开始 ===");
        
        if (plant == null) {
            System.out.println("❌ 调试：植物对象为null");
            return false;
        }
        
        if (!plant.isLive()) {
            System.out.println("❌ 调试：植物已死亡，isLive=" + plant.isLive());
            return false;
        }
        
        // 获取植物信息
        String className = plant.getClass().getSimpleName();
        String plantType = getPlantType(plant);
        String plantDisplayName = PlantManager.getPlantDisplayName(plantType);
        int plantX = plant.getX();
        int plantY = plant.getY();
        
        System.out.println("🔍 调试信息：");
        System.out.println("  - 植物类名: " + className);
        System.out.println("  - 识别类型: " + plantType);
        System.out.println("  - 显示名称: " + plantDisplayName);
        System.out.println("  - 位置: (" + plantX + "," + plantY + ")");
        
        // 检查配置
        System.out.println("🔧 配置检查：");
        System.out.println("  - 返还功能启用: " + GameConfig.ENABLE_PLANT_REFUND);
        System.out.println("  - 返还比例: " + (GameConfig.PLANT_REFUND_RATE * 100) + "%");
        System.out.println("  - 最小返还: " + GameConfig.MIN_REFUND_AMOUNT);
        System.out.println("  - 最大返还: " + GameConfig.MAX_REFUND_AMOUNT);
        
        // 计算返还金额
        int plantCost = PlantManager.getPlantCost(plantType);
        int refundAmount = PlantManager.calculateRefundAmount(plantType);
        
        System.out.println("💰 金额计算：");
        System.out.println("  - 植物原价: " + plantCost);
        System.out.println("  - 计算返还: " + refundAmount);
        
        System.out.println("🔧 正在铲除植物: " + plantDisplayName + " (类型: " + plantType + ")");
        
        // 执行铲除
        plant.die();
        System.out.println("✅ 植物已标记为死亡");
        
        // 返还金币
        if (refundAmount > 0) {
            SunManager sunManager = SunManager.getInstance();
            
            // 记录铲除前的阳光数量
            int sunBefore = sunManager.getCurrentSun();
            System.out.println("💰 铲除前阳光数量: " + sunBefore);
            
            // 使用addSunSafely方法，避免冷却时间限制
            sunManager.addSunSafely(refundAmount);
            
            // 记录铲除后的阳光数量
            int sunAfter = sunManager.getCurrentSun();
            System.out.println("💰 铲除后阳光数量: " + sunAfter);
            System.out.println("💰 实际增加: " + (sunAfter - sunBefore));
            
            System.out.println("✅ 铲除 " + plantDisplayName + " 返还 " + refundAmount + " 阳光");
            
            // 创建返还特效
            createRefundEffect(plantX, plantY, refundAmount);
            
        } else {
            System.out.println("💸 铲除 " + plantDisplayName + " 无返还");
            System.out.println("  原因分析：");
            if (!GameConfig.ENABLE_PLANT_REFUND) {
                System.out.println("  - 返还功能被禁用");
            } else if (plantCost <= 0) {
                System.out.println("  - 植物类型未知或无价值");
            } else {
                System.out.println("  - 计算出的返还金额为0");
            }
        }
        
        System.out.println("=== 铲除植物调试结束 ===");
        return true;
    }
    
	/**
	 * 根据植物对象获取植物类型
	 */
    private String getPlantType(com.tedu.element.Plant plant) {
        String className = plant.getClass().getSimpleName().toLowerCase();
        String packageName = plant.getClass().getPackage().getName();
        
        System.out.println("🔍 植物类型识别：");
        System.out.println("  - 完整类名: " + plant.getClass().getName());
        System.out.println("  - 简单类名: " + className);
        System.out.println("  - 包名: " + packageName);
        
        String plantType = "unknown";
        
        if (className.contains("peashooter")) {
            plantType = "peashooter";
            System.out.println("  ✅ 识别为豌豆射手");
        } else if (className.contains("sunflower")) {
            plantType = "sunflower";
            System.out.println("  ✅ 识别为向日葵");
        } else if (className.contains("wallnut") || className.contains("nut")) {
            plantType = "wallnut";
            System.out.println("  ✅ 识别为坚果墙");
        } else {
            System.out.println("  ⚠️ 未能识别植物类型，将按未知处理");
        }
        
        return plantType;
    }
	
	/**
	 * 在指定网格位置查找植物
	 */
	private com.tedu.element.Plant findPlantAtGrid(int gridX, int gridY) {
	    ElementManager em = ElementManager.getManager();
	    java.util.List<com.tedu.element.ElementObj> plants = em.getElementsByKey(GameElement.PLANTS);
	    
	    for (com.tedu.element.ElementObj obj : plants) {
	        if (obj instanceof com.tedu.element.Plant) {
	            com.tedu.element.Plant plant = (com.tedu.element.Plant) obj;
	            // 通过植物的网格坐标判断
	            if (plant.getGridX() == gridX && plant.getGridY() == gridY && plant.isLive()) {
	                return plant;
	            }
	        }
	    }
	    return null;
	}
	
	/**
	 * 创建返还金币的视觉特效
	 */
	private void createRefundEffect(int x, int y, int amount) {
	    try {
	        System.out.println("🎆===================🎆");
	        System.out.println("💰    返还 +" + amount + " 阳光！    💰");
	        System.out.println("📍  位置: (" + x + "," + y + ")   📍");
	        System.out.println("🎆===================🎆");
	        
	        // 如果有特效系统，可以在这里添加
	        // RefundTextEffect effect = new RefundTextEffect(x, y, amount);
	        // ElementManager.getManager().addElement(effect, GameElement.EFFECTS);
	        
	    } catch (Exception e) {
	        System.err.println("创建返还特效失败: " + e.getMessage());
	    }
	}
	
	/**
	 * 处理鼠标点击事件
	 * @param mouseX 鼠标X坐标
	 * @param mouseY 鼠标Y坐标
	 * @return 是否处理了点击事件
	 */
	public boolean handleClick(int mouseX, int mouseY) {
	    // 检查是否点击了铲子按钮
	    if (isShovelButtonClicked(mouseX, mouseY)) {
	        toggleShovel();
	        System.out.println("🔧 铲子按钮被点击，当前状态: " + currentState);
	        return true;
	    }
	    return false;
	}

	/**
	 * 处理鼠标悬停事件
	 * @param mouseX 鼠标X坐标
	 * @param mouseY 鼠标Y坐标
	 * @return 是否在铲子按钮上悬停
	 */
	public boolean handleHover(int mouseX, int mouseY) {
	    boolean wasHovering = (currentState == ShovelState.HOVER);
	    
	    // 更新悬停状态
	    handleShovelButtonHover(mouseX, mouseY);
	    
	    // 返回悬停状态是否发生变化
	    return wasHovering != (currentState == ShovelState.HOVER);
	}

	/**
	 * 检查指定坐标是否在铲子按钮范围内
	 * @param mouseX 鼠标X坐标
	 * @param mouseY 鼠标Y坐标
	 * @return 是否在铲子按钮范围内
	 */
	public boolean isPointInShovelButton(int mouseX, int mouseY) {
	    return isShovelButtonClicked(mouseX, mouseY);
	}

	/**
	 * 重置铲子状态（用于游戏重新开始等场景）
	 */
	public void reset() {
	    deactivateShovel();
	    System.out.println("🔧 铲子管理器已重置");
	}

	/**
	 * 获取铲子按钮的矩形区域
	 * @return 铲子按钮的矩形区域
	 */
	public java.awt.Rectangle getShovelButtonBounds() {
	    return new java.awt.Rectangle(shovelButtonX, shovelButtonY, 
	                                 shovelButtonWidth, shovelButtonHeight);
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