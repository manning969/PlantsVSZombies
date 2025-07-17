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
    private boolean hasHit = false; // 新增：防止重复伤害的标志
    
    public Pea() {
        super();
        // 应用GameConfig中的伤害配置
        this.damage = GameConfig.PEA_DAMAGE; // 需要在GameConfig中添加 PEA_DAMAGE = 20
        this.speed = GameConfig.PROJECTILE_SPEED;
        this.hasHit = false;
    }
    
    public Pea(int x, int y, int rowIndex) {
        super(x, y, GameConfig.PROJECTILE_WIDTH, GameConfig.PROJECTILE_HEIGHT, 
              GameLoad.imgMap.get("pea"));
        // 使用GameConfig中的配置
        this.damage = GameConfig.PEA_DAMAGE; // 从配置文件获取伤害值
        this.speed = GameConfig.PROJECTILE_SPEED;
        this.rowIndex = rowIndex;
        this.hasHit = false;
        
        System.out.println("🌱 创建豌豆子弹，伤害: " + this.damage + ", 速度: " + this.speed);
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
        if (this.getIcon() != null && !hasHit) { // 只有未击中时才显示
            g.drawImage(this.getIcon().getImage(), 
                       this.getX(), this.getY(), 
                       this.getW(), this.getH(), null);
        }
    }
    
    @Override
    protected void move() {
        // 如果已经击中，不再移动
        if (hasHit) {
            return;
        }
        
        // 向右移动 - 应用速度倍数
        int actualSpeed = speed * GameConfig.currentSpeed;
        this.setX(this.getX() + actualSpeed);
        
        // 检查是否超出屏幕边界
        if (this.getX() > GameConfig.GAME_WIDTH) {
            this.setLive(false);
        }
    }
    
    /**
     * 击中目标时调用 - 修复版，防止重复伤害
     */
    public void hit() {
        if (!hasHit) { // 只有未击中过才执行
            hasHit = true;
            this.setLive(false);
            System.out.println("💥 豌豆击中目标，造成 " + damage + " 点伤害");
        }
    }
    
    /**
     * 检查是否已经击中目标
     */
    public boolean hasHit() {
        return hasHit;
    }
    
    /**
     * 造成伤害 - 新增方法，确保只造成一次伤害
     */
    public boolean dealDamage() {
        if (!hasHit) {
            hasHit = true;
            this.setLive(false);
            System.out.println("💥 豌豆击中目标，造成 " + damage + " 点伤害");
            return true; // 返回true表示成功造成伤害
        }
        return false; // 返回false表示已经造成过伤害
    }
    
    // Getter方法
    public int getDamage() {
        return damage;
    }
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    /**
     * 设置伤害值（用于特殊情况的动态调整）
     */
    public void setDamage(int damage) {
        this.damage = damage;
        System.out.println("🎯 豌豆伤害调整为: " + damage);
    }
    
    @Override
    public String toString() {
        return "Pea at (" + getX() + "," + getY() + ") row:" + rowIndex + 
               " damage:" + damage + " speed:" + speed + " hit:" + hasHit;
    }
}