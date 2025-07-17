package com.tedu.show;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 游戏窗体
 */
public class GameJFrame extends JFrame{
    // 扩大窗体尺寸，特别是高度，给底部阳光留出足够空间
    public static int GameX = 900;  // 宽度从800增加到900
    public static int GameY = 700;  // 高度从600增加到700，重点解决底部空间不足问题
    
    //初始化对象
    //1. 定义正在显示的面板
    private JPanel jPanel = null;
    //2. 定义键盘监听
    private KeyListener keyListener = null;
    //3. 鼠标监听
    private MouseMotionListener mouseMotionListener = null;
    private MouseListener mouseListener = null;
    //4. 定义游戏主进程
    private Thread thread = null;
    
    //构造方法
    public GameJFrame() {
        init();
    }
    
    public void init() {
        this.setSize(GameX, GameY);//设置窗体大小
        this.setTitle("植物大战僵尸 - Plants vs Zombies");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//设置退出并且关闭
        this.setLocationRelativeTo(null);//屏幕居中显示
        this.setResizable(false); // 禁止调整窗口大小
        
        System.out.println("📏 窗体尺寸为: " + GameX + "x" + GameY);
    }

    //1. 面板注入
    public void setjPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }
    
    //2. 键盘监听注入
    public void setKeylistener(KeyListener keylistener) {
        this.keyListener = keylistener;
    }
    
    //3. 鼠标监听注入
    public void setMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.mouseMotionListener = mouseMotionListener;
    }
    
    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }
    
    //4. 游戏主进程注入
    public void setThread(Thread thread) {
        this.thread = thread;
    }
    
    //启动方法
    public void start() {
        // 添加面板
        if(jPanel != null) {
            this.add(jPanel);
            
            // 重要：设置面板焦点属性
            jPanel.setFocusable(true);
            jPanel.requestFocusInWindow();
        }
        
        // 添加键盘监听器 - 同时添加到窗口和面板
        if(keyListener != null) {
            this.addKeyListener(keyListener);  // 添加到窗口
            if(jPanel != null) {
                jPanel.addKeyListener(keyListener);  // 添加到面板
            }
        }
        
        // 添加鼠标监听器
        if(mouseListener != null && jPanel != null) {
            jPanel.addMouseListener(mouseListener);
        }
        
        // 启动游戏线程
        if(thread != null) {
            thread.start();
        }
        
        // 显示界面
        this.setVisible(true);
        
        // 确保面板获得焦点 - 在显示后再次请求焦点
        if(jPanel != null) {
            jPanel.requestFocus();
            jPanel.requestFocusInWindow();
            
            System.out.println("面板焦点状态: " + jPanel.hasFocus());
            System.out.println("面板可获得焦点: " + jPanel.isFocusable());
        }
        
        //如果面板是runnable的子类实体对象，启动面板线程
        if(this.jPanel instanceof Runnable) {
            Runnable run = (Runnable)this.jPanel;
            Thread th = new Thread(run);
            th.start();
        }
        
        // 延迟确保焦点设置
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(jPanel != null) {
                    jPanel.requestFocus();
                    System.out.println("延迟设置焦点完成");
                }
            }
        });
    }
}