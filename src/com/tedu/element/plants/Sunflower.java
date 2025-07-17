package com.tedu.element.plants;

import com.tedu.element.Plant;
import com.tedu.element.ElementObj;

import com.tedu.manager.GameLoad;
import com.tedu.manager.SunManager;
import com.tedu.utils.GameConfig;

/**
 * 向日葵 - 生产阳光的植物（整合冷却系统版本）
 */
public class Sunflower extends Plant {
    private int sunProductionCooldown = 0;
    private static final int SUN_INTERVAL = 300; // 生产间隔（帧数）

    public Sunflower() {
        super();
    }
    
    public Sunflower(int gridX, int gridY) {
        super(gridX, gridY, 450, GameConfig.SUNFLOWER_COST, 
              GameLoad.imgMap.get("sunflower_idle"));
    }
    
    @Override
    public ElementObj createElement(String str) {
        String[] parts = str.split(",");
        int gridX = Integer.parseInt(parts[0]);
        int gridY = Integer.parseInt(parts[1]);
        return new Sunflower(gridX, gridY);
    }
    
    @Override
    protected void performAction(long gameTime) {
        // 保持原有SunManager集成
        int sunProduceInterval = (int) SunManager.getSunflowerProduceInterval();
        if (canPerformAction(gameTime, sunProduceInterval)) {
            attack(1); // 调用新attack方法
            lastActionTime = gameTime;
        }
    }

    /**
     * 新增attack方法（整合冷却系统）
     */
    @Override // 现在父类Plant有attack方法，可以正确覆盖
    public void attack(int speedMultiplier) {
        if (sunProductionCooldown > 0) {
            sunProductionCooldown -= speedMultiplier;
            return;
        }
        produceSun();
        sunProductionCooldown = SUN_INTERVAL;
    }

    /**
     * 保持原有SunManager集成
     */
    private void produceSun() {
        SunManager sunManager = SunManager.getInstance();
        int pixelX = GameConfig.GRID_START_X + gridX * GameConfig.GRID_WIDTH;
        int pixelY = GameConfig.GRID_START_Y + gridY * GameConfig.GRID_HEIGHT;
        sunManager.generateSunflowerSun(pixelX, pixelY);
    }
    
    @Override
    public String toString() {
        return "Sunflower at grid(" + gridX + "," + gridY + ") HP:" + hp + 
               " 剩余冷却: " + sunProductionCooldown + "帧";
    }

	@Override
	protected void attack() {
		// TODO Auto-generated method stub
		
	}
}