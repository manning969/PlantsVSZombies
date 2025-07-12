package com.tedu.element.projectiles;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.element.ElementObj;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;

/**
 * 豌豆子弹类
 */
public class Pea extends ElementObj {
    private int damage;     // 伤害值
    private int speed;      // 移动速度
    private int rowIndex;   // 所在行索引
    
    public Pea() {
        super();
    }
    
    public Pea(int x, int y, int rowIndex) {
        super(x, y, GameConfig.PROJECTILE_WIDTH, GameConfig.PROJECTILE_HEIGHT, 
              GameLoad.imgMap.get("pea"));
        this.damage = 20;
        this.speed = GameConfig.PROJECTILE_SPEED;
        this.rowIndex = rowIndex;
    }
    
    @Override
    public ElementObj createElement(String str) {
        // 解析字符串格式: "x,y,rowIndex"
        String[] parts = str.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int rowIndex = Integer.parseInt(parts[2]);
        
        return new Pea(x, y, rowIndex);
    }
    
    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), 
                       this.getX(), this.getY(), 
                       this.getW(), this.getH(), null);
        }
    }
    
    @Override
    protected void move() {
        // 向右移动
        this.setX(this.getX() + speed);
        
        // 检查是否超出屏幕边界
        if (this.getX() > GameConfig.GAME_WIDTH) {
            this.setLive(false);
        }
    }
    
    /**
     * 击中目标时调用
     */
    public void hit() {
        this.setLive(false);
    }
    
    // Getter方法
    public int getDamage() {
        return damage;
    }
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    @Override
    public String toString() {
        return "Pea at (" + getX() + "," + getY() + ") row:" + rowIndex + " damage:" + damage;
    }
}