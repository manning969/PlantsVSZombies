package com.tedu.element.plants;

import javax.swing.ImageIcon;
import com.tedu.element.Plant;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SunManager;
import com.tedu.utils.GameConfig;

/**
 * 向日葵 - 生产阳光的植物
 */
public class Sunflower extends Plant {
    
    public Sunflower() {
        super();
    }
    
    public Sunflower(int gridX, int gridY) {
        super(gridX, gridY, 450, GameConfig.SUNFLOWER_COST, 
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
        // 从SunManager获取生产间隔，并转换为int类型以匹配canPerformAction方法
        int sunProduceInterval = (int) SunManager.getSunflowerProduceInterval();
        
        // 检查是否可以生产阳光
        if (canPerformAction(gameTime, sunProduceInterval)) {
            produceSun();
            lastActionTime = gameTime;
            
            // 调试信息
            System.out.println("🌻 向日葵生产阳光！位置: (" + gridX + "," + gridY + 
                             ") 间隔: " + (sunProduceInterval/1000) + "秒");
        }
    }
    
    /**
     * 生产阳光 - 修改为使用SunManager
     */
    private void produceSun() {
        // 使用SunManager来生成阳光，而不是直接创建
        SunManager sunManager = SunManager.getInstance();
        
        // 计算向日葵的像素坐标
        int pixelX = GameConfig.GRID_START_X + gridX * GameConfig.GRID_WIDTH;
        int pixelY = GameConfig.GRID_START_Y + gridY * GameConfig.GRID_HEIGHT;
        
        // 调用SunManager的方法生成向日葵阳光
        sunManager.generateSunflowerSun(pixelX, pixelY);
        
        System.out.println("🌻 向日葵在网格(" + gridX + "," + gridY + ") 像素坐标(" + 
                         pixelX + "," + pixelY + ") 生产了阳光！");
    }
    
    @Override
    public String toString() {
        return "Sunflower at grid(" + gridX + "," + gridY + ") HP:" + hp + 
               " 下次生产阳光: " + (SunManager.getSunflowerProduceInterval() - 
               (System.currentTimeMillis() - lastActionTime)) + "ms";
    }
}