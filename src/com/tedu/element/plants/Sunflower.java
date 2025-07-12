package com.tedu.element.plants;

import javax.swing.ImageIcon;
import com.tedu.element.Plant;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;

/**
 * 向日葵 - 生产阳光的植物
 */
public class Sunflower extends Plant {
    
    private static final int SUN_PRODUCE_INTERVAL = 2000; // 生产阳光的间隔（20秒）
    
    public Sunflower() {
        super();
    }
    
    public Sunflower(int gridX, int gridY) {
        super(gridX, gridY, 80, GameConfig.SUNFLOWER_COST, 
              GameLoad.imgMap.get("sunflower_idle"));
    }
    
    @Override
    public ElementObj createElement(String str) {
        // 解析字符串格式: "gridX,gridY"
        String[] parts = str.split(",");
        int gridX = Integer.parseInt(parts[0]);
        int gridY = Integer.parseInt(parts[1]);
        
        return new Sunflower(gridX, gridY);
    }
    
    @Override
    protected void performAction(long gameTime) {
        // 检查是否可以生产阳光
        if (canPerformAction(gameTime, SUN_PRODUCE_INTERVAL)) {
            produceSun();
            lastActionTime = gameTime;
        }
    }
    
    /**
     * 生产阳光
     */
    private void produceSun() {
        ElementManager em = ElementManager.getManager();
        
        // 在向日葵附近生成阳光
        int sunX = this.getX() + (int)(Math.random() * 40) - 20; // 随机偏移
        int sunY = this.getY() + (int)(Math.random() * 40) - 20;
        
        // 确保阳光不会生成在屏幕外
        sunX = Math.max(50, Math.min(sunX, GameConfig.GAME_WIDTH - 50));
        sunY = Math.max(50, Math.min(sunY, GameConfig.GAME_HEIGHT - 50));
        
        ElementObj sun = GameLoad.createSun(sunX, sunY);
        if (sun != null) {
            em.addElement(sun, GameElement.SUNS);
            System.out.println("向日葵生产了阳光！位置: (" + gridX + "," + gridY + ")");
        }
    }
    
    @Override
    public String toString() {
        return "Sunflower at grid(" + gridX + "," + gridY + ") HP:" + hp;
    }
}