package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.utils.GameConfig;

/**
 * 植物基类
 */
public abstract class Plant extends ElementObj {
    protected int hp;           // 生命值
    protected int cost;         // 种植花费的阳光
    protected int gridX;        // 网格X坐标
    protected int gridY;        // 网格Y坐标
    protected long lastActionTime; // 上次行动时间
    
    public Plant() {
        super();
    }
    
    public Plant(int gridX, int gridY, int hp, int cost, ImageIcon icon) {
        // super()调用必须是第一条语句，将网格坐标转换计算直接放在参数中
        super(GameConfig.GRID_START_X + gridX * GameConfig.GRID_WIDTH,  // pixelX
              GameConfig.GRID_START_Y + gridY * GameConfig.GRID_HEIGHT + GameConfig.PLANT_OFFSET_Y, // pixelY 加上偏移量
              GameConfig.PLANT_WIDTH, 
              GameConfig.PLANT_HEIGHT, 
              icon);
        
        // 在super()调用后设置其他属性
        this.gridX = gridX;
        this.gridY = gridY;
        this.hp = hp;
        this.cost = cost;
        this.lastActionTime = 0;
    }
    
    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(), 
                       this.getX(), this.getY(), 
                       this.getW(), this.getH(), null);
        }
    }
    
    /**
     * 植物的特殊行动（由子类实现）
     * @param gameTime 游戏时间
     */
    protected abstract void performAction(long gameTime);
    
    /**
     * 检查是否可以执行行动
     * @param gameTime 当前游戏时间
     * @param interval 行动间隔
     * @return 是否可以行动
     */
    protected boolean canPerformAction(long gameTime, int interval) {
        // 倍速时间隔会相应缩短
        long scaledInterval = interval / GameConfig.currentSpeed;
        return gameTime - lastActionTime >= scaledInterval;
    }
    
    /**
     * 受到伤害
     * @param damage 伤害值
     */
    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.setLive(false);
        }
    }
    
    /**
     * 模板方法重写 - 植物的行为循环
     */
    @Override
    public final void model(long gameTime) {
        if (!this.isLive()) {
            return;
        }
        
        // 应用速度倍数到游戏时间
        long scaledGameTime = gameTime * GameConfig.currentSpeed;
        
        // 执行植物特有的行动
        performAction(scaledGameTime);
        
        // 更新图像（如果有动画）
        updateImage();
    }
    
    // canPerformAction和Setter方法
    public int getHp() {
        return hp;
    }
    
    public void setHp(int hp) {
        this.hp = hp;
    }
    
    public int getCost() {
        return cost;
    }
    
    public int getGridX() {
        return gridX;
    }
    
    public int getGridY() {
        return gridY;
    }
}