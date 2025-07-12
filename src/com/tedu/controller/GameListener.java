package com.tedu.controller;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;
import com.tedu.utils.GameConfig;
import com.tedu.utils.GridHelper;
import com.tedu.manager.ShovelManager;

/**
 * 植物大战僵尸游戏监听器 - 增强铲子功能版本
 * 处理键盘和鼠标输入，支持铲子模式
 */
public class GameListener implements KeyListener, MouseListener, MouseMotionListener {
    
    private Set<Integer> pressedKeys = new HashSet<>();
    private GameThread gameThread; // 游戏线程引用
    private String selectedPlant = "peashooter"; // 当前选中的植物
    
    // 铲子管理器引用
    private ShovelManager shovelManager;
    
    // 新增：引用游戏面板以控制显示选项
    private com.tedu.show.GameMainJPanel gamePanel;
    
    public GameListener() {
        this.shovelManager = ShovelManager.getInstance();
    }
    
    public GameListener(GameThread gameThread) {
        this.gameThread = gameThread;
        this.shovelManager = ShovelManager.getInstance();
    }
    
    // 新增构造函数
    public GameListener(GameThread gameThread, com.tedu.show.GameMainJPanel gamePanel) {
        this.gameThread = gameThread;
        this.gamePanel = gamePanel;
        this.shovelManager = ShovelManager.getInstance();
        
        // 设置铲子管理器的游戏面板引用
        this.shovelManager.setGamePanel(gamePanel);
    }
    
    // ==================== 键盘事件处理 ====================
    
    @Override
    public void keyTyped(KeyEvent e) {
        // 不处理keyTyped事件
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // 调试信息
        System.out.println("键盘事件检测到！键码: " + keyCode + " 字符: " + KeyEvent.getKeyText(keyCode));
        
        // 防止重复按键
        if (pressedKeys.contains(keyCode)) {
            System.out.println("重复按键，忽略");
            return;
        }
        pressedKeys.add(keyCode);
        
        // 处理特殊按键
        handleSpecialKeys(keyCode);
        
        // 传递给游戏线程处理（用于铲子快捷键）
        if (gameThread != null) {
            gameThread.handleKeyPress(keyCode);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        pressedKeys.remove(keyCode);
        System.out.println("释放键: " + keyCode);
    }
    
    /**
     * 处理特殊按键
     */
    private void handleSpecialKeys(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_1:
                selectedPlant = "peashooter";
                shovelManager.deactivateShovel(); // 取消铲子模式
                System.out.println("选择植物: 豌豆射手 (消耗100阳光)");
                break;
            case KeyEvent.VK_2:
                selectedPlant = "sunflower";
                shovelManager.deactivateShovel(); // 取消铲子模式
                System.out.println("选择植物: 向日葵 (消耗50阳光)");
                break;
            case KeyEvent.VK_3:
                selectedPlant = "wallnut";
                shovelManager.deactivateShovel(); // 取消铲子模式
                System.out.println("选择植物: 坚果墙 (消耗50阳光)");
                break;
            case KeyEvent.VK_S:
                // S键切换铲子模式
                shovelManager.toggleShovel();
                System.out.println("铲子模式: " + (shovelManager.isShovelActive() ? "激活" : "取消"));
                break;
            case KeyEvent.VK_SPACE:
                System.out.println("暂停/继续游戏");
                pauseGame();
                break;
            case KeyEvent.VK_R:
                System.out.println("重新开始游戏");
                restartGame();
                break;
            case KeyEvent.VK_ESCAPE:
                // ESC键取消铲子模式或退出游戏
                if (shovelManager.isShovelActive()) {
                    shovelManager.deactivateShovel();
                    System.out.println("取消铲子模式");
                } else {
                    System.out.println("退出游戏");
                    exitGame();
                }
                break;
            case KeyEvent.VK_G:
                System.out.println("切换网格显示");
                toggleGridDisplay();
                break;
            case KeyEvent.VK_D:
                System.out.println("切换调试信息显示");
                toggleDebugInfo();
                break;
        }
    }
    
    /**
     * 暂停游戏
     */
    private void pauseGame() {
        if (gameThread != null) {
            gameThread.togglePause();
        }
    }
    
    /**
     * 重启游戏
     */
    private void restartGame() {
        if (gameThread != null) {
            gameThread.restartGame();
        }
    }
    
    /**
     * 退出游戏
     */
    private void exitGame() {
        if (gameThread != null) {
            gameThread.stopGame();
        }
        System.exit(0);
    }
    
    // ==================== 鼠标事件处理 ====================
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        
        System.out.println("鼠标点击: (" + mouseX + ", " + mouseY + ")");
        
        // 首先检查是否点击了铲子按钮
        if (shovelManager.isShovelButtonClicked(mouseX, mouseY)) {
            shovelManager.toggleShovel();
            System.out.println("点击铲子按钮，铲子模式: " + (shovelManager.isShovelActive() ? "激活" : "取消"));
            return;
        }
        
        if (e.getButton() == MouseEvent.BUTTON1) { // 左键
            handleLeftClick(mouseX, mouseY);
        } else if (e.getButton() == MouseEvent.BUTTON3) { // 右键
            handleRightClick(mouseX, mouseY);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        // 可以在这里处理鼠标按下事件
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        // 可以在这里处理鼠标释放事件
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // 鼠标进入组件
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
        // 鼠标离开组件
    }
    
    // ==================== 鼠标移动事件处理 ====================
    
    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();
        
        // 处理铲子按钮悬停效果
        shovelManager.handleShovelButtonHover(mouseX, mouseY);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        // 鼠标拖拽事件
    }
    
    /**
     * 处理左键点击 - 种植植物或收集阳光
     */
    private void handleLeftClick(int mouseX, int mouseY) {
        // 如果铲子模式激活，左键也可以铲除植物
        if (shovelManager.isShovelActive()) {
            handleShovelAction(mouseX, mouseY);
            return;
        }
        
        // 首先尝试收集阳光 - 优先处理阳光收集
        boolean sunCollected = false;
        if (gameThread != null) {
            // 检查是否点击了阳光
            sunCollected = gameThread.collectSun(mouseX, mouseY);
        }
        
        // 如果没有收集到阳光，再尝试种植植物
        if (!sunCollected) {
            // 检查是否在草坪区域 - 使用GridHelper
            if (GridHelper.isInLawnArea(mouseX, mouseY)) {
                // 转换为网格坐标 - 使用GridHelper
                Point gridPoint = GridHelper.pixelToGrid(mouseX, mouseY);
                
                if (gridPoint != null) {
                    int gridX = gridPoint.x;
                    int gridY = gridPoint.y;
                    
                    System.out.println("尝试种植 " + selectedPlant + " 在网格 " + 
                                     GridHelper.gridToString(gridX, gridY));
                    
                    // 种植植物
                    if (gameThread != null) {
                        boolean success = gameThread.plantPlant(selectedPlant, gridX, gridY);
                        if (success) {
                            System.out.println("成功种植 " + selectedPlant + " 在 " + 
                                             GridHelper.gridToString(gridX, gridY));
                        }
                    }
                } else {
                    System.out.println("无效的网格位置");
                }
            } else {
                System.out.println("点击位置不在草坪区域内");
            }
        }
    }
    
    /**
     * 处理右键点击 - 铲除植物或激活铲子模式
     */
    private void handleRightClick(int mouseX, int mouseY) {
        System.out.println("右键点击 - 铲子功能");
        
        // 方案1：右键直接铲除（无需激活铲子模式）
        handleShovelAction(mouseX, mouseY);
        
        // 方案2：右键激活铲子模式（可选）
        // if (!shovelManager.isShovelActive()) {
        //     shovelManager.activateShovel();
        //     System.out.println("右键激活铲子模式");
        // } else {
        //     handleShovelAction(mouseX, mouseY);
        // }
    }
    
    /**
     * 处理铲子操作
     */
    private void handleShovelAction(int mouseX, int mouseY) {
        // 检查是否在草坪区域
        if (GridHelper.isInLawnArea(mouseX, mouseY)) {
            Point gridPoint = GridHelper.pixelToGrid(mouseX, mouseY);
            
            if (gridPoint != null) {
                int gridX = gridPoint.x;
                int gridY = gridPoint.y;
                
                System.out.println("🔧 使用铲子在网格 " + 
                                 GridHelper.gridToString(gridX, gridY));
                
                // 实现铲除植物的逻辑
                if (gameThread != null) {
                    boolean removed = gameThread.removePlant(gridX, gridY);
                    if (removed) {
                        System.out.println("🔧 成功铲除植物在 " + 
                                         GridHelper.gridToString(gridX, gridY));
                        
                        // 铲除成功后可以选择取消铲子模式
                        // shovelManager.deactivateShovel();
                    } else {
                        System.out.println("⚠️ 该位置没有植物可以铲除");
                    }
                }
            }
        } else {
            System.out.println("铲子操作位置不在草坪区域内");
        }
    }
    
    /**
     * 获取网格坐标 - 使用GridHelper
     */
    public int[] getGridCoordinates(int mouseX, int mouseY) {
        Point gridPoint = GridHelper.pixelToGrid(mouseX, mouseY);
        if (gridPoint != null) {
            return new int[]{gridPoint.x, gridPoint.y};
        }
        return null;
    }
    
    /**
     * 检查点击位置是否在草坪区域内 - 使用GridHelper
     */
    public boolean isInLawnArea(int x, int y) {
        return GridHelper.isInLawnArea(x, y);
    }
    
    /**
     * 切换网格显示
     */
    private void toggleGridDisplay() {
        if (gamePanel != null) {
            gamePanel.toggleGridDisplay();
        } else {
            System.out.println("游戏面板引用为空，无法切换网格显示");
        }
    }
    
    /**
     * 切换调试信息显示
     */
    private void toggleDebugInfo() {
        if (gamePanel != null) {
            gamePanel.toggleDebugInfo();
        } else {
            System.out.println("游戏面板引用为空，无法切换调试信息显示");
        }
    }
    
    // 新增setter方法
    public void setGamePanel(com.tedu.show.GameMainJPanel gamePanel) {
        this.gamePanel = gamePanel;
        if (shovelManager != null) {
            shovelManager.setGamePanel(gamePanel);
        }
    }
    
    /**
     * 获取植物信息
     */
    private String getPlantInfo(String plantType) {
        switch (plantType) {
            case "peashooter":
                return "豌豆射手 - 消耗100阳光，发射豌豆攻击僵尸";
            case "sunflower":
                return "向日葵 - 消耗50阳光，定期生产阳光";
            case "wallnut":
                return "坚果墙 - 消耗50阳光，阻挡僵尸前进";
            default:
                return "未知植物";
        }
    }
    
    /**
     * 循环切换植物选择
     */
    public void cycleSelectedPlant() {
        // 切换植物时自动取消铲子模式
        shovelManager.deactivateShovel();
        
        switch (selectedPlant) {
            case "peashooter":
                selectedPlant = "sunflower";
                break;
            case "sunflower":
                selectedPlant = "wallnut";
                break;
            case "wallnut":
                selectedPlant = "peashooter";
                break;
            default:
                selectedPlant = "peashooter";
                break;
        }
        System.out.println("切换到植物: " + selectedPlant);
        showSelectedPlantInfo();
    }
    
    /**
     * 显示当前选中植物的信息
     */
    public void showSelectedPlantInfo() {
        String info = getPlantInfo(selectedPlant);
        System.out.println("当前选中: " + info);
    }
    
    // ==================== Getter和Setter方法 ====================
    
    public String getSelectedPlant() {
        return selectedPlant;
    }
    
    public void setSelectedPlant(String selectedPlant) {
        this.selectedPlant = selectedPlant;
        // 切换植物时自动取消铲子模式
        shovelManager.deactivateShovel();
        System.out.println("切换选择植物: " + selectedPlant);
        showSelectedPlantInfo();
    }
    
    public void setGameThread(GameThread gameThread) {
        this.gameThread = gameThread;
    }
    
    /**
     * 检查某个键是否被按下
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    /**
     * 获取当前按下的所有键
     */
    public Set<Integer> getPressedKeys() {
        return new HashSet<>(pressedKeys);
    }
    
    /**
     * 检查是否按下了移动键
     */
    public boolean isMovementKeyPressed() {
        return pressedKeys.contains(KeyEvent.VK_W) ||
               pressedKeys.contains(KeyEvent.VK_A) ||
               pressedKeys.contains(KeyEvent.VK_S) ||
               pressedKeys.contains(KeyEvent.VK_D) ||
               pressedKeys.contains(KeyEvent.VK_UP) ||
               pressedKeys.contains(KeyEvent.VK_LEFT) ||
               pressedKeys.contains(KeyEvent.VK_DOWN) ||
               pressedKeys.contains(KeyEvent.VK_RIGHT);
    }
    
    /**
     * 打印控制说明
     */
    public static void printControlHelp() {
        System.out.println("=== 游戏控制说明 ===");
        System.out.println("数字键 1-3: 选择植物");
        System.out.println("S键: 切换铲子模式");
        System.out.println("鼠标左键: 种植植物/收集阳光/铲除(铲子模式)");
        System.out.println("鼠标右键: 铲除植物");
        System.out.println("空格键: 暂停/继续");
        System.out.println("R键: 重新开始");
        System.out.println("G键: 切换网格显示");
        System.out.println("D键: 切换调试信息");
        System.out.println("ESC键: 取消铲子模式/退出游戏");
        System.out.println("===================");
    }
}