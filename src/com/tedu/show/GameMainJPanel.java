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
import com.tedu.manager.GameLoad;
import javax.swing.ImageIcon;
import com.tedu.element.items.Sun;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

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

    // 新增：记录当前选中的植物卡片索引
    private int selectedPlantIndex = -1;

    // 新增：记录鼠标悬停的草坪格子坐标
    private int hoverGridX = -1;
    private int hoverGridY = -1;

    
    public GameMainJPanel() {
        init();
        // 新增：支持键盘切换植物卡片
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();
                if (key == java.awt.event.KeyEvent.VK_1) {
                    selectedPlantIndex = 0;
                    repaint();
                } else if (key == java.awt.event.KeyEvent.VK_2) {
                    selectedPlantIndex = 1;
                    repaint();
                } else if (key == java.awt.event.KeyEvent.VK_3) {
                    selectedPlantIndex = 2;
                    repaint();
                }
            }
        });
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
                int startX = 130;
                int startY = 10;
                int cardWidth = 60;
                int cardHeight = 60; // 拉长卡片高度
                int spacing = 5;
                int mx = e.getX();
                int my = e.getY();
                for (int i = 0; i < 3; i++) {
                    int x = startX + i * (cardWidth + spacing);
                    if (mx >= x && mx <= x + cardWidth && my >= startY && my <= startY + cardHeight) {
                        selectedPlantIndex = i;
                        repaint();
                        break;
                    }
                }
                // 判断是否点击在草坪区域
                if (mx > GameConfig.GRID_START_X && my > GameConfig.GRID_START_Y) {
                    int gridX = (mx - GameConfig.GRID_START_X) / GameConfig.GRID_WIDTH;
                    int gridY = (my - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
                    // 优先处理铲子模式
                    if (shovelManager.isShovelActive()) {
                        boolean used = shovelManager.useShovel(gridX, gridY);
                        if (used) {
                            // 移除该格子的植物
                            List<ElementObj> plants = em.getElementsByKey(GameElement.PLANTS);
                            ElementObj toRemove = null;
                            for (ElementObj plant : plants) {
                                // 判断植物是否在该格子
                                int px = (plant.getX() - GameConfig.GRID_START_X) / GameConfig.GRID_WIDTH;
                                int py = (plant.getY() - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
                                if (px == gridX && py == gridY) {
                                    toRemove = plant;
                                    break;
                                }
                            }
                            if (toRemove != null) {
                                plants.remove(toRemove);
                                // 可选：播放铲除特效
                            }
                            repaint();
                            return;
                        }
                    }
                    // 铲子未激活时才进行种植
                    if (selectedPlantIndex != -1) {
                        String[] plantTypes = {"peashooter", "sunflower", "wallnut"};
                        if (sunManager.hasEnoughSun(Integer.parseInt(new String[]{"100","50","50"}[selectedPlantIndex]))) {
                            em.addElement(
                                com.tedu.manager.GameLoad.createPlant(plantTypes[selectedPlantIndex], gridX, gridY),
                                GameElement.PLANTS
                            );
                            sunManager.spendSun(Integer.parseInt(new String[]{"100","50","50"}[selectedPlantIndex]));
                            repaint();
                        }
                    }
                }
                System.out.println("面板通过鼠标点击获得焦点");
            }
        });

        // 鼠标悬停收集阳光
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                // 阳光收集逻辑
                List<ElementObj> suns = em.getElementsByKey(GameElement.SUNS);
                for (ElementObj obj : suns) {
                    if (obj instanceof Sun) {
                        Sun sun = (Sun) obj;
                        if (!sun.isCollected() && mx >= sun.getX() && mx <= sun.getX() + sun.getW()
                                && my >= sun.getY() && my <= sun.getY() + sun.getH()) {
                            sun.collect();
                            sunManager.addSun(sun.getValue());
                        }
                    }
                }
                // 草坪高亮逻辑
                if (mx > GameConfig.GRID_START_X && my > GameConfig.GRID_START_Y) {
                    hoverGridX = (mx - GameConfig.GRID_START_X) / GameConfig.GRID_WIDTH;
                    hoverGridY = (my - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
                } else {
                    hoverGridX = -1;
                    hoverGridY = -1;
                }
                repaint();
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

        // 新增：高亮鼠标悬停的草坪格子
        if (hoverGridX >= 0 && hoverGridY >= 0 && hoverGridX < GameConfig.GRID_COLS && hoverGridY < GameConfig.GRID_ROWS) {
            int hx = GameConfig.GRID_START_X + hoverGridX * GameConfig.GRID_WIDTH;
            int hy = GameConfig.GRID_START_Y + hoverGridY * GameConfig.GRID_HEIGHT;
            g2d.setColor(new Color(255, 255, 0, 80)); // 半透明黄色
            g2d.fillRect(hx, hy, GameConfig.GRID_WIDTH, GameConfig.GRID_HEIGHT);
            g2d.setColor(new Color(255, 200, 0, 180));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(hx, hy, GameConfig.GRID_WIDTH, GameConfig.GRID_HEIGHT);
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
        int cardHeight = 60; // 拉长卡片高度
        int spacing = 5;

        String[] plants = {"豌豆射手", "向日葵", "坚果墙"};
        String[] plantTypes = {"peashooter", "sunflower", "wallnut"};
        String[] costs = {"100", "50", "50"};
        String[] keys = {"1", "2", "3"};
        String[] cardImgKeys = {"peashooter_idle_card", "sunflower_idle_card", "wallnut_full_card"};

        for (int i = 0; i < plants.length; i++) {
            int x = startX + i * (cardWidth + spacing);
            boolean canAfford = sunManager.hasEnoughSun(Integer.parseInt(costs[i]));

            // 卡片背景
            if (selectedPlantIndex == i) {
                g2d.setColor(new Color(255, 215, 0, 220)); // 选中高亮
            } else if (canAfford) {
                g2d.setColor(new Color(139, 69, 19, 200));
            } else {
                g2d.setColor(new Color(100, 100, 100, 200));
            }
            g2d.fillRoundRect(x, startY, cardWidth, cardHeight, 5, 5);

            // 卡片边框
            g2d.setColor(canAfford ? Color.BLACK : Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x, startY, cardWidth, cardHeight, 5, 5);

            // 卡片图片
            ImageIcon cardIcon = GameLoad.imgMap.get(cardImgKeys[i]);
            if (cardIcon != null) {
                int imgW = 32, imgH = 32;
                int imgX = x + (cardWidth - imgW) / 2;
                int imgY = startY + 6;
                g2d.drawImage(cardIcon.getImage(), imgX, imgY, imgW, imgH, null);
            }

            // 植物名称显示在图片下方，且在卡片背景内
            g2d.setFont(FontHelper.getChineseFont(Font.PLAIN, 9)); // 字体再小一点
            g2d.setColor(canAfford ? Color.WHITE : Color.LIGHT_GRAY);
            int nameY = startY + 6 + 32 + 16; // 图片下方留16像素
            int nameX = x + (cardWidth - g2d.getFontMetrics().stringWidth(plants[i])) / 2 + 12; // 再右移6像素
            g2d.drawString(plants[i], nameX, nameY);

            // 快捷键提示
            g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 10));
            g2d.drawString(keys[i], x + 5, startY + 16);

            // 花费
            g2d.setColor(canAfford ? Color.YELLOW : Color.GRAY);
            g2d.setFont(FontHelper.getChineseFont(Font.PLAIN, 10));
            g2d.drawString(costs[i], x + 5, startY + cardHeight - 8);
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
            // 绘制铲子模式激活提示
            g2d.setColor(new Color(255, 255, 0, 180));
            g2d.fillRoundRect(10, 90, 200, 30, 10, 10);
            
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(10, 90, 200, 30, 10, 10);
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(FontHelper.getChineseFont(Font.BOLD, 14));
            g2d.drawString("🔧 铲子模式激活 - 点击铲除植物", 15, 110);
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
            g2d.drawString("�� 获胜！", getWidth() - 150, 50);
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
        int progressX = 520; // 再右侧一点，用户指定
        int progressY = 10;
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