package com.tedu.element.plants;

import javax.swing.ImageIcon;
import com.tedu.element.Plant;
import com.tedu.element.ElementObj;
import com.tedu.element.projectiles.Pea;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;
import java.util.List;

/**
 * 豌豆射手 - 基础攻击植物
 */
public class Peashooter extends Plant {

    // 新增：用于跟踪当前动画类型，避免重复设置ImageIcon
    private boolean currentIconIsAttacking = false; // 用于判断当前显示的GIF是否是攻击的

    public Peashooter() {
        super();
        // 初始设置为待机动画
        this.setIcon(GameLoad.imgMap.get("peashooter_idle"));
        this.currentIconIsAttacking = false;
    }

    public Peashooter(int gridX, int gridY) {
        super(gridX, gridY, 500, GameConfig.PEASHOOTER_COST,
              GameLoad.imgMap.get("peashooter_idle")); // 初始设置为待机动画
        this.currentIconIsAttacking = false;
    }

    @Override
    public ElementObj createElement(String str) {
        // 解析字符串格式: "gridX,gridY"
        String[] parts = str.split(",");
        int gridX = Integer.parseInt(parts[0]);
        int gridY = Integer.parseInt(parts[1]);

        return new Peashooter(gridX, gridY);
    }

    @Override
    protected void performAction(long gameTime) {
        boolean zombiePresent = hasZombieInRow(); // 检查是否有僵尸

        // 检查是否可以射击
        if (canPerformAction(gameTime, GameConfig.SHOOT_INTERVAL)) {
            // 检查本行是否有僵尸
            if (zombiePresent) {
                shoot();
                lastActionTime = gameTime;
                // 当射击时，立即切换到攻击动画
                if (!currentIconIsAttacking) {
                    this.setIcon(GameLoad.imgMap.get("peashooter_attack"));
                    currentIconIsAttacking = true;
                    System.out.println("豌豆射手切换到攻击动画");
                }
            }
        }
        
        // 如果没有僵尸，或者上次攻击时间已经过去足够久，切换回待机动画
        // 考虑攻击动画播放完成后切换回待机
        // 可以设置一个攻击动画的持续时间，或者在下一个周期没有僵尸时切换
        // 这里简化为：如果当前没有僵尸在行内，就切换回待机
        if (!zombiePresent && currentIconIsAttacking) {
            this.setIcon(GameLoad.imgMap.get("peashooter_idle"));
            currentIconIsAttacking = false;
            System.out.println("豌豆射手切换到待机动画");
        }
        // 如果初始设置的图标为null，确保在启动时设置一个
        else if (this.getIcon() == null) { // 首次加载时确保有图标
             this.setIcon(GameLoad.imgMap.get("peashooter_idle"));
        }
    }

    /**
     * 检查本行是否有僵尸
     */
    private boolean hasZombieInRow() {
        ElementManager em = ElementManager.getManager();
        List<ElementObj> zombies = em.getElementsByKey(GameElement.ZOMBIES);

        for (ElementObj zombie : zombies) {
            // 检查僵尸是否在同一行且在豌豆射手右侧
            if (isInSameRow(zombie) && zombie.getX() > this.getX()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否在同一行
     */
    private boolean isInSameRow(ElementObj obj) {
        int objGridY = (obj.getY() - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
        return objGridY == this.gridY;
    }

    /**
     * 射击豌豆 - 调整发射位置对齐豌豆射手的口
     */
    private void shoot() {
        ElementManager em = ElementManager.getManager();

        // 调整豌豆发射位置，使其从豌豆射手的口部发射
        int peaX = this.getX() + this.getW() - 5; // 从植物右侧稍微内侧发射
        int peaY = this.getY() + this.getH() / 4; // 从植物上1/3位置发射（模拟嘴部位置）

        Pea pea = new Pea(peaX, peaY, this.gridY);
        em.addElement(pea, GameElement.PROJECTILES);

        System.out.println("豌豆射手射击！位置: (" + gridX + "," + gridY + ")");
    }

    @Override
    public String toString() {
        return "Peashooter at grid(" + gridX + "," + gridY + ") HP:" + hp;
    }
}