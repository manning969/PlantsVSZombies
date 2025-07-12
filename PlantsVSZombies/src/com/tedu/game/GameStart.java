package com.tedu.game;

import com.tedu.controller.GameListener;
import com.tedu.controller.GameThread;
import com.tedu.show.GameJFrame;
import com.tedu.show.GameMainJPanel;
import com.tedu.utils.GameConfig;

/**
 * 植物大战僵尸游戏启动类
 */
public class GameStart {
    
    //程序唯一入口
    public static void main(String[] args) {
        // 设置字体渲染属性，改善中文显示
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("file.encoding", "UTF-8");
        
        // 设置Look and Feel以获得更好的中文支持 - 修复方法调用
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("无法设置系统外观，使用默认外观: " + e.getMessage());
        }
        
        System.out.println("=== 植物大战僵尸 启动中... ===");
        
        try {
            // 初始化字体系统
            com.tedu.utils.FontHelper.initialize();
            
            // 测试字体显示效果
            com.tedu.utils.FontHelper.testFontDisplay();
            
            // 创建游戏窗口
            GameJFrame gameFrame = new GameJFrame();
            gameFrame.setSize(GameConfig.GAME_WIDTH, GameConfig.GAME_HEIGHT);
            gameFrame.setTitle("植物大战僵尸 - Plants vs Zombies");
            
            // 创建游戏主面板
            GameMainJPanel gamePanel = new GameMainJPanel();
            
            // 创建游戏主线程
            GameThread gameThread = new GameThread();
            
            // 创建游戏监听器 - 传入游戏面板引用
            GameListener gameListener = new GameListener(gameThread, gamePanel);
            
            // 设置组件关系
            gameFrame.setjPanel(gamePanel);
            gameFrame.setThread(gameThread);
            gameFrame.setKeylistener(gameListener);
            
            // 重要：为面板添加监听器
            gamePanel.addMouseListener(gameListener);
            gamePanel.addKeyListener(gameListener);
            
            // 启动游戏
            gameFrame.start();
            
            // 额外的焦点确保措施
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gamePanel.requestFocus();
                    gamePanel.requestFocusInWindow();
                    System.out.println("最终焦点设置: " + gamePanel.hasFocus());
                }
            });
            
            // 显示控制说明
            printControlInstructions();
            
            System.out.println("=== 游戏启动成功！===");
            
        } catch (Exception e) {
            System.err.println("游戏启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印游戏控制说明
     */
    private static void printControlInstructions() {
        System.out.println("\n==================== 游戏操作说明 ====================");
        System.out.println("键盘操作:");
        System.out.println("  数字键 1 - 选择豌豆射手 (消耗100阳光)");
        System.out.println("  数字键 2 - 选择向日葵   (消耗50阳光)");
        System.out.println("  数字键 3 - 选择坚果墙   (消耗50阳光)");
        System.out.println("  空格键   - 暂停/继续游戏");
        System.out.println("  R键      - 重新开始游戏");
        System.out.println("  G键      - 切换网格显示");
        System.out.println("  D键      - 切换调试信息");
        System.out.println("  ESC键    - 退出游戏");
        System.out.println();
        System.out.println("鼠标操作:");
        System.out.println("  左键点击 - 种植选中的植物 / 收集阳光");
        System.out.println();
        System.out.println("游戏目标:");
        System.out.println("  - 使用植物抵御僵尸入侵");
        System.out.println("  - 收集阳光来种植更多植物");
        System.out.println("  - 不要让僵尸到达你的房子!");
        System.out.println("  - 完成所有波次即可获胜");
        System.out.println();
        System.out.println("提示：点击游戏窗口确保能接收键盘输入");
        System.out.println("=====================================================\n");
    }
}