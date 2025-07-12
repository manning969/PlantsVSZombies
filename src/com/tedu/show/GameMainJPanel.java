package com.tedu.show;
import com.tedu.utils.FontHelper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.ShovelManager;
import com.tedu.manager.SunManager;
import com.tedu.manager.WaveManager;
import com.tedu.utils.GameConfig;

/**
 * 植物大战僵尸游戏主面板 - 完整铲子功能版本 (新增小推车绘制)
 */
public class GameMainJPanel extends JPanel implements Runnable {

    private ElementManager em;
    private SunManager sunManager;      // 阳光管理器
    private WaveManager waveManager;    // 波次管理器
    private ShovelManager shovelManager; // 铲子管理器
    
    private boolean showGrid = true;
    private boolean showDebugInfo = true;

    // 游戏线程引用（用于调用种植和铲除方法）
    private com.tedu.controller.GameThread gameThread;

    
    public GameMainJPanel() {
        init();
    }

    public void init() {
        em = ElementManager.getManager();
        sunManager = SunManager.getInstance();
        waveManager = WaveManager.getInstance();
        shovelManager = ShovelManager.getInstance(); // 初始化铲子管理器
        
        // 初始化字体支持
        FontHelper.initialize();

        setBackground(new Color(34, 139, 34));

        // 重要：焦点设置
        setFocusable(true);
        setRequestFocusEnabled(true);

        // 设置铲子管理器的游戏面板引用
        shovelManager.setGamePanel(this);
        
        // 添加鼠标点击监听器来获取焦点
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
                System.out.println("面板通过鼠标点击获得焦点");
            }
        });

        System.out.println("GameMainJPanel 初始化完成，可获得焦点: " + isFocusable());
    }

    /**
     * 设置游戏线程引用
     */
    public void setGameThread(com.tedu.controller.GameThread gameThread) {
        this.gameThread = gameThread;
    }
    
    /**
     * 重写paint方法进行游戏画面绘制
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        // 绘制背景
        drawBackground(g2d);

        // 绘制网格
        if (showGrid) {
            drawGrid(g2d);
        }

        // 绘制所有游戏元素
        drawGameElements(g2d);

        // 绘制游戏界面UI
        drawGameUI(g2d);

        // 绘制调试信息
        if (showDebugInfo) {
            drawDebugInfo(g2d);
        }
    }
    
    

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g2d) {
        // 绘制天空
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, getWidth(), GameConfig.GRID_START_Y);

        // 绘制草坪
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, GameConfig.GRID_START_Y, getWidth(),
                    GameConfig.GRID_ROWS * GameConfig.GRID_HEIGHT);

        // 绘制房子区域
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(0, GameConfig.GRID_START_Y, GameConfig.GRID_START_X,
                    GameConfig.GRID_ROWS * GameConfig.GRID_HEIGHT);
    }

    /**
     * 绘制网格线
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(0, 100, 0, 100));
        g2d.setStroke(new BasicStroke(1));

        // 绘制垂直线
        for (int i = 0; i <= GameConfig.GRID_COLS; i++) {
            int x = GameConfig.GRID_START_X + i * GameConfig.GRID_WIDTH;
            g2d.drawLine(x, GameConfig.GRID_START_Y,
                        x, GameConfig.GRID_START_Y + GameConfig.GRID_ROWS * GameConfig.GRID_HEIGHT);
        }

        // 绘制水平线
        for (int i = 0; i <= GameConfig.GRID_ROWS; i++) {
            int y = GameConfig.GRID_START_Y + i * GameConfig.GRID_HEIGHT;
            g2d.drawLine(GameConfig.GRID_START_X, y,
                        GameConfig.GRID_START_X + GameConfig.GRID_COLS * GameConfig.GRID_WIDTH, y);
        }
    }

    /**
     * 绘制所有游戏元素 (调整绘制顺序以确保小推车在僵尸下方)
     */
    private void drawGameElements(Graphics2D g2d) {
        Map<GameElement, List<ElementObj>> all = em.getGameElements();

        // 调整绘制顺序，确保小推车在僵尸下面，背景在最下面
        GameElement[] drawOrder = {
            GameElement.BACKGROUND,
            GameElement.LAWN,
            GameElement.PLANTS,
            GameElement.LAWN_MOWERS, // 新增：在植物和僵尸之间绘制小推车，确保层次感
            GameElement.ZOMBIES,
            GameElement.PROJECTILES,
            GameElement.EFFECTS,
            GameElement.SUNS,
            GameElement.UI_ELEMENTS
        };

        for (GameElement ge : drawOrder) {
            List<ElementObj> list = all.get(ge);
            if (list != null) {
                for (ElementObj obj : list) {
                    if (obj.isLive()) {
                        obj.showElement(g2d);
                    }
                }
            }
        }
    }

    /**
     * 绘制游戏UI界面
     */
    private void drawGameUI(Graphics2D g2d) {
        // 绘制阳光计数器
        drawSunCounter(g2d);

        // 绘制植物选择栏
        drawPlantSelector(g2d);

        // 绘制铲子按钮
        drawShovelButton(g2d);

        // 绘制游戏状态信息
        drawGameStatus(g2d);

        // 绘制波次进度
        drawWaveProgress(g2d);
        
        // 绘制铲子模式提示
        drawShovelModeIndicator(g2d);
    }

    /**
     * 绘制阳光计数器 - 显示真实数据
     */
    private void drawSunCounter(Graphics2D g2d) {
        // 背景
        g2d.setColor(new Color(255, 255, 0, 200));
        g2d.fillRoundRect(10, 10, 100, 40, 10, 10);

        // 边框
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(10, 10, 100, 40, 10, 10);

        // 文字 - 显示真实阳光数量
        g2d.setColor(Color.BLACK);
        g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 16));
        String sunText = "阳光: " + sunManager.getCurrentSun();
        g2d.drawString(sunText, 20, 35);
    }

    /**
     * 绘制植物选择栏 - 使用FontHelper修复中文显示
     */
    private void drawPlantSelector(Graphics2D g2d) {
        int startX = 130;
        int startY = 10;
        int cardWidth = 60;
        int cardHeight = 40;
        int spacing = 5;

        String[] plants = {"豌豆射手", "向日葵", "坚果墙"};
        String[] plantTypes = {"peashooter", "sunflower", "wallnut"};
        String[] costs = {"100", "50", "50"};
        String[] keys = {"1", "2", "3"};

        for (int i = 0; i < plants.length; i++) {
            int x = startX + i * (cardWidth + spacing);
            int cost = Integer.parseInt(costs[i]);
            boolean canAfford = sunManager.hasEnoughSun(cost);

            // 卡片背景
            if (canAfford) {
                g2d.setColor(new Color(139, 69, 19, 200));
            } else {
                g2d.setColor(new Color(100, 100, 100, 200));
            }
            g2d.fillRoundRect(x, startY, cardWidth, cardHeight, 5, 5);

            // 卡片边框
            g2d.setColor(canAfford ? Color.BLACK : Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x, startY, cardWidth, cardHeight, 5, 5);

            // 快捷键提示
            g2d.setColor(canAfford ? Color.WHITE : Color.LIGHT_GRAY);
            g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 10));
            g2d.drawString(keys[i], x + 5, startY + 12);

            // 植物名称 - 使用FontHelper
            g2d.setFont(FontHelper.getChineseFont(Font.PLAIN, 8));
            g2d.drawString(plants[i], x + 5, startY + 25);

            // 花费
            g2d.setColor(canAfford ? Color.YELLOW : Color.GRAY);
            g2d.setFont(FontHelper.getChineseFont(Font.PLAIN, 8));
            g2d.drawString(costs[i], x + 5, startY + 37);
        }
    }

    /**
     * 绘制铲子按钮
     */
    private void drawShovelButton(Graphics2D g2d) {
        if (shovelManager != null) {
            shovelManager.drawShovelButton(g2d);
        }
    }

    /**
     * 绘制铲子模式指示器
     */
    private void drawShovelModeIndicator(Graphics2D g2d) {
        if (shovelManager != null && shovelManager.isShovelActive()) {
            // 计算文字宽度以确定合适的框大小
            String indicatorText = "🔧 铲子模式激活 - 点击铲除植物";
            g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 14));
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(indicatorText);
            int textHeight = fm.getHeight();
            
            // 设置框的尺寸，确保文字能完全显示
            int boxWidth = Math.max(textWidth + 20, 280); // 至少280像素宽，或根据文字宽度调整
            int boxHeight = textHeight + 10; // 根据文字高度调整
            int boxX = 10;
            int boxY = 90;
            
            // 绘制铲子模式激活提示背景
            g2d.setColor(new Color(255, 255, 0, 180));
            g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
            
            // 绘制边框
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
            
            // 绘制文字 - 使用FontHelper确保中文正确显示
            g2d.setColor(Color.BLACK);
            g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 14));
            
            // 计算文字居中位置
            int textX = boxX + (boxWidth - textWidth) / 2;
            int textY = boxY + (boxHeight + fm.getAscent()) / 2 - 2; // 稍微调整垂直位置
            
            g2d.drawString(indicatorText, textX, textY);
        }
    }

    /**
     * 绘制游戏状态信息 - 使用FontHelper修复中文显示
     */
    private void drawGameStatus(Graphics2D g2d) {
        // 使用FontHelper获取中文字体
        g2d.setFont(FontHelper.getBoldChineseFont(14));
        g2d.setColor(Color.WHITE);

        // 显示波次信息
        String waveText = "波次: " + waveManager.getCurrentWave() + "/" + waveManager.getTotalWaves();
        g2d.drawString(waveText, getWidth() - 150, 25);

        // 显示游戏状态
        if (waveManager.isGameWon()) {
            g2d.setColor(Color.GREEN);
            g2d.setFont(FontHelper.getBoldChineseFont(24));
            g2d.drawString("🎉 获胜！", getWidth() - 150, 50);
        } else if (waveManager.isWaveInProgress()) {
            g2d.setColor(Color.RED);
            g2d.drawString("波次进行中...", getWidth() - 150, 50);
        } else {
            g2d.setColor(Color.CYAN);
            g2d.drawString("准备下一波", getWidth() - 150, 50);
        }
    }

    /**
     * 绘制波次进度条 - 修复中文显示
     */
    private void drawWaveProgress(Graphics2D g2d) {
        int progressX = 10;
        int progressY = 60;
        int progressWidth = 200;
        int progressHeight = 20;

        // 进度条背景
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.fillRoundRect(progressX, progressY, progressWidth, progressHeight, 10, 10);

        // 进度条前景
        double progress = (double) waveManager.getCurrentWave() / waveManager.getTotalWaves();
        int fillWidth = (int) (progressWidth * progress);

        g2d.setColor(new Color(0, 255, 0, 200));
        g2d.fillRoundRect(progressX, progressY, fillWidth, progressHeight, 10, 10);

        // 进度条边框
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(progressX, progressY, progressWidth, progressHeight, 10, 10);

        // 进度文字 - 使用支持中文的字体
        g2d.setColor(Color.WHITE);
        g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 12)); // 统一使用FontHelper
        String progressText = "波次进度: " + (int)(progress * 100) + "%";
        g2d.drawString(progressText, progressX + 5, progressY + 15);
    }

    /**
     * 绘制调试信息 - 显示真实数据
     */
    private void drawDebugInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier", Font.PLAIN, 10));

        int y = getHeight() - 140; // 调整位置以避免与铲子模式指示器重叠

        // 显示管理器状态
        g2d.drawString("=== 调试信息 ===", 10, y);
        y += 15;

        g2d.drawString("阳光管理器: " + sunManager.getCurrentSun() + "/" + sunManager.getTotalSunCollected(), 10, y);
        y += 12;

        g2d.drawString("波次管理器: 第" + waveManager.getCurrentWave() + "波 " +
                      (waveManager.isWaveInProgress() ? "进行中" : "等待中"), 10, y);
        y += 12;

        // 显示铲子状态
        g2d.drawString("铲子状态: " + shovelManager.getCurrentState() + 
                      " (激活: " + shovelManager.isShovelActive() + ")", 10, y);
        y += 12;

        // 显示各类元素数量
        Map<GameElement, List<ElementObj>> all = em.getGameElements();
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            if (list != null && list.size() > 0) {
                g2d.drawString(ge.name() + ": " + list.size(), 10, y);
                y += 12;
            }
        }
    }

    /**
     * 多线程刷新界面
     */
    @Override
    public void run() {
        while (true) {
            try {
                this.repaint();
                Thread.sleep(16); // 约60FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Getter和Setter方法
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        System.out.println("网格显示: " + (showGrid ? "开启" : "关闭"));
    }

    public void setShowDebugInfo(boolean showDebugInfo) {
        this.showDebugInfo = showDebugInfo;
        System.out.println("调试信息显示: " + (showDebugInfo ? "开启" : "关闭"));
    }

    // 新增：切换方法
    public void toggleGridDisplay() {
        this.showGrid = !this.showGrid;
        System.out.println("网格显示已" + (showGrid ? "开启" : "关闭"));
    }

    public void toggleDebugInfo() {
        this.showDebugInfo = !this.showDebugInfo;
        System.out.println("调试信息显示已" + (showDebugInfo ? "开启" : "关闭"));
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public boolean isShowDebugInfo() {
        return showDebugInfo;
    }
}