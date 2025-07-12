package com.tedu.element.effects;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import javax.swing.ImageIcon;
import com.tedu.element.ElementObj;

/**
 * 铲子挖掘特效类 - 显示土块飞溅效果
 */
public class ShovelDirtEffect extends ElementObj {
    
    private long startTime;
    private long duration = 800; // 特效持续800毫秒
    private int particleCount = 8; // 土块粒子数量
    private Particle[] particles;
    
    public ShovelDirtEffect() {
        super();
        this.startTime = System.currentTimeMillis();
        initializeParticles();
    }
    
    public ShovelDirtEffect(int x, int y, int w, int h) {
        super(x, y, w, h, null);
        this.startTime = System.currentTimeMillis();
        initializeParticles();
    }
    
    /**
     * 初始化粒子
     */
    private void initializeParticles() {
        particles = new Particle[particleCount];
        for (int i = 0; i < particleCount; i++) {
            particles[i] = new Particle(
                getX() + getW() / 2, // 从中心开始
                getY() + getH() / 2,
                (Math.random() - 0.5) * 4, // 随机X速度
                -Math.random() * 3 - 1,    // 向上的Y速度
                new Color(139, 69, 19)      // 棕色土块
            );
        }
    }
    
    @Override
    public void showElement(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > duration) {
            this.setLive(false);
            return;
        }
        
        // 计算透明度（随时间递减）
        float alpha = 1.0f - (float) elapsed / duration;
        
        // 绘制粒子
        for (Particle particle : particles) {
            particle.update();
            particle.draw(g2d, alpha);
        }
        
        // 绘制中心爆炸效果
        drawCenterEffect(g2d, elapsed, alpha);
    }
    
    /**
     * 绘制中心爆炸效果
     */
    private void drawCenterEffect(Graphics2D g2d, long elapsed, float alpha) {
        int centerX = getX() + getW() / 2;
        int centerY = getY() + getH() / 2;
        
        // 创建带透明度的颜色
        Color effectColor = new Color(139, 69, 19, (int)(alpha * 180));
        g2d.setColor(effectColor);
        
        // 绘制扩散的圆圈
        int radius = (int)(elapsed / 20); // 半径随时间增长
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 绘制内部填充
        if (elapsed < duration / 2) {
            Color fillColor = new Color(205, 133, 63, (int)(alpha * 100));
            g2d.setColor(fillColor);
            g2d.fillOval(centerX - radius/2, centerY - radius/2, radius, radius);
        }
    }
    
    @Override
    public ElementObj createElement(String str) {
        String[] params = str.split(",");
        if (params.length >= 4) {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);
            int w = Integer.parseInt(params[2]);
            int h = Integer.parseInt(params[3]);
            return new ShovelDirtEffect(x, y, w, h);
        }
        return new ShovelDirtEffect();
    }
    
    /**
     * 粒子内部类
     */
    private static class Particle {
        private double x, y;
        private double vx, vy; // 速度
        private double gravity = 0.2; // 重力
        private Color color;
        private int size;
        
        public Particle(double x, double y, double vx, double vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = (int)(Math.random() * 4) + 2; // 2-5像素大小
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += gravity; // 应用重力
            
            // 简单的空气阻力
            vx *= 0.98;
            vy *= 0.98;
        }
        
        public void draw(Graphics2D g2d, float alpha) {
            Color drawColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(alpha * 255)
            );
            g2d.setColor(drawColor);
            g2d.fillOval((int)x - size/2, (int)y - size/2, size, size);
        }
    }
}