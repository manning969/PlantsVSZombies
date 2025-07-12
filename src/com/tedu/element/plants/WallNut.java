package com.tedu.element.plants;

import javax.swing.ImageIcon;
import com.tedu.element.Plant;
import com.tedu.element.ElementObj;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;

/**
 * 坚果墙 - 防御型植物，血量高，用于阻挡僵尸
 * 完善版本：增强防御机制和血量显示
 */
public class WallNut extends Plant {
    
    private int maxHp; // 记录最大血量，用于显示损坏状态
    private String currentState; // 当前状态：full, cracked1, cracked2
    
    public WallNut() {
        super();
    }
    
    public WallNut(int gridX, int gridY) {
        super(gridX, gridY, 200, GameConfig.WALLNUT_COST, // 增加血量到4000，更耐打
              GameLoad.imgMap.get("wallnut_full"));
        this.maxHp = 200;
        this.currentState = "full";
    }
    
    @Override
    public ElementObj createElement(String str) {
        // 解析字符串格式: "gridX,gridY"
        String[] parts = str.split(",");
        int gridX = Integer.parseInt(parts[0]);
        int gridY = Integer.parseInt(parts[1]);
        
        return new WallNut(gridX, gridY);
    }
    
    @Override
    protected void performAction(long gameTime) {
        // 坚果墙没有主动技能，只是被动防御
        // 检查血量并更换图片
        updateDamageAppearance();
        
        // 可以添加一些防御特效
        if (gameTime % 300 == 0) { // 每3秒检查一次状态
            checkDefenseStatus();
        }
    }
    
    /**
     * 根据血量更新外观
     */
    private void updateDamageAppearance() {
        double hpPercentage = (double) hp / maxHp;
        String newState = currentState;
        
        if (hpPercentage > 0.66) {
            newState = "full";
        } else if (hpPercentage > 0.33) {
            newState = "cracked1";
        } else {
            newState = "cracked2";
        }
        
        // 只有状态改变时才更换图片，避免频繁操作
        if (!newState.equals(currentState)) {
            currentState = newState;
            ImageIcon newIcon = null;
            
            switch (currentState) {
                case "full":
                    newIcon = GameLoad.imgMap.get("wallnut_full");
                    break;
                case "cracked1":
                    newIcon = GameLoad.imgMap.get("wallnut_cracked1");
                    break;
                case "cracked2":
                    newIcon = GameLoad.imgMap.get("wallnut_cracked2");
                    break;
            }
            
            if (newIcon != null) {
                this.setIcon(newIcon);
                System.out.println("坚果墙状态变更为: " + currentState + " (血量: " + hp + "/" + maxHp + ")");
            } else {
                // 如果图片不存在，保持当前图片
                System.out.println("警告: 坚果墙图片 wallnut_" + currentState + " 不存在，保持当前外观");
            }
        }
    }
    
    /**
     * 检查防御状态
     */
    private void checkDefenseStatus() {
        double hpPercentage = (double) hp / maxHp;
        
        if (hpPercentage <= 0.1) {
            System.out.println("警告: 坚果墙即将被摧毁！血量仅剩 " + hp);
        } else if (hpPercentage <= 0.25) {
            System.out.println("坚果墙严重受损，建议更换！");
        }
    }
    
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        
        // 受伤时立即更新外观
        updateDamageAppearance();
        
        System.out.println("坚果墙受到 " + damage + " 点伤害！剩余血量: " + hp + "/" + maxHp + 
                         " (" + String.format("%.1f", (double)hp/maxHp*100) + "%)");
    }
    
    /**
     * 重写死亡方法，添加坚果墙特有的死亡效果
     */
    @Override
    public void die() {
        System.out.println("坚果墙在网格(" + gridX + "," + gridY + ")被摧毁！");
        // 这里可以添加坚果墙被摧毁的特效
        super.die();
    }
    
    /**
     * 获取防御效率（血量百分比）
     */
    public double getDefenseEfficiency() {
        return (double) hp / maxHp;
    }
    
    /**
     * 获取当前状态
     */
    public String getCurrentState() {
        return currentState;
    }
    
    @Override
    public String toString() {
        return "WallNut at grid(" + gridX + "," + gridY + ") HP:" + hp + "/" + maxHp + 
               " State:" + currentState + " Defense:" + String.format("%.1f", getDefenseEfficiency()*100) + "%";
    }
}