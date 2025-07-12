package com.tedu.element.items;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.element.ElementObj;
import com.tedu.manager.GameLoad; // 确保导入 GameLoad
import com.tedu.utils.GameConfig; // 确保导入 GameConfig

/**
 * 阳光道具类
 * 支持物理掉落效果：重力加速度、弹跳、减速等
 */
public class Sun extends ElementObj {
    private int value;          // 阳光价值
    private long lifeTime;      // 存在时间 (帧数)
    private long maxLifeTime;   // 最大存在时间 (帧数)
    private boolean isCollected; // 是否被收集

    // 物理参数
    private double velocityY;   // Y方向速度 (像素/帧)
    private double velocityX;   // X方向速度 (像素/帧)
    private double gravity;     // 重力加速度 (像素/帧²)
    private double groundY;     // 地面Y坐标
    private boolean isDropping; // 是否正在下落
    private int bounceCount;    // 已弹跳次数
    private int maxBounces;     // 最大弹跳次数
    private double bounceDamping; // 弹跳阻尼系数 (0-1, 1表示无阻尼)
    private double friction;    // 地面摩擦力 (0-1, 1表示无摩擦)

    // 阳光类型
    public enum SunType {
        NATURAL,    // 天空掉落的自然阳光
        SUNFLOWER   // 向日葵产生的阳光
    }
    private SunType sunType;

    // 动画参数 (静止后的悬浮效果)
    private double floatAmplitude = 2.0; // 悬浮振幅
    private double floatSpeed = 0.1;     // 悬浮速度
    private long animationTime = 0;      // 动画时间计数器 (用于sin函数)

    // 默认构造函数，通常用于反射创建，后续需要通过createElement设置属性
    public Sun() {
        super();
        initializeDefaultValues();
        this.sunType = SunType.NATURAL; // 默认类型
    }

    // 自然阳光构造函数：从天空掉落，无初始水平速度
    public Sun(int x, int startY, int groundY) {
        super(x, startY, GameConfig.SUN_WIDTH, GameConfig.SUN_HEIGHT,
              GameLoad.imgMap.get("sun_normal"));
        initializeDefaultValues();
        this.groundY = groundY;
        this.sunType = SunType.NATURAL;
        setupNaturalSunPhysics();
    }

    // 向日葵阳光构造函数：有初始抛物线轨迹
    public Sun(int x, int startY, int groundY, double initialVelocityX, double initialVelocityY) {
        super(x, startY, GameConfig.SUN_WIDTH, GameConfig.SUN_HEIGHT,
              GameLoad.imgMap.get("sun_normal"));
        initializeDefaultValues();
        this.groundY = groundY;
        this.velocityX = initialVelocityX;
        this.velocityY = initialVelocityY;
        this.sunType = SunType.SUNFLOWER;
        setupSunflowerSunPhysics();
    }

    // 初始化所有阳光通用的默认物理和生命周期值
    private void initializeDefaultValues() {
        this.value = GameConfig.SUN_VALUE;
        this.lifeTime = 0;
        // maxLifeTime 以帧数计算，假设GameThread的sleep是10ms，则100帧/秒
        // GameConfig.SUN_LIFETIME (毫秒) / 10 (毫秒/帧)
        this.maxLifeTime = GameConfig.SUN_LIFETIME / 10;
        this.isCollected = false;
        this.isDropping = true; // 默认开始时处于掉落状态
        this.bounceCount = 0;
        this.maxBounces = 1; // 自然阳光通常不弹跳或只轻微弹跳一次
        this.bounceDamping = 0.6; // 每次弹跳损失40%的速度
        this.friction = 0.9; // 地面摩擦力
        this.animationTime = (long)(Math.random() * 100); // 随机初始动画相位，使多个阳光的悬浮不同步
    }

    // 设置自然阳光的物理特性
    private void setupNaturalSunPhysics() {
        this.velocityY = 0.2; // 初始下落速度较小，缓慢下降
        this.velocityX = 0;   // 无初始水平速度
        this.gravity = 0.08;  // 较小的重力加速度，模拟飘落
        this.maxBounces = 0; // 自然阳光通常不弹跳
    }

    // 设置向日葵阳光的物理特性
    private void setupSunflowerSunPhysics() {
        // 向日葵阳光的初始速度已通过构造函数设置
        this.gravity = 0.2;   // 稍大的重力加速度，模拟抛物线
        this.maxBounces = 1; // 向日葵阳光可能轻微弹跳一次
        this.bounceDamping = 0.5; // 弹跳阻尼
        this.friction = 0.8; // 摩擦力
    }

    @Override
    public ElementObj createElement(String str) {
        // 解析字符串格式: "x,y,groundY" (自然阳光) 或 "x,y,groundY,vx,vy" (向日葵阳光)
        String[] parts = str.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int groundY = Integer.parseInt(parts[2]);

        if (parts.length == 5) {
            // 向日葵阳光带初始速度
            double vx = Double.parseDouble(parts[3]);
            double vy = Double.parseDouble(parts[4]);
            return new Sun(x, y, groundY, vx, vy);
        } else {
            // 自然阳光
            return new Sun(x, y, groundY);
        }
    }

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null && !isCollected) {
            // 如果阳光已经静止，添加悬浮动画
            int drawY = getY();
            if (!isDropping) {
                // 基于动画时间计算Y轴偏移
                drawY += (int)(Math.sin(animationTime * floatSpeed) * floatAmplitude);
            }

            g.drawImage(this.getIcon().getImage(),
                       this.getX(), drawY,
                       this.getW(), this.getH(), null);
        }
    }

    @Override
    public final void model(long gameTime) {
        if (!this.isLive()) {
            return;
        }

        if (isDropping) {
            updatePhysics();
        } else {
            // 静止状态的悬浮动画计时
            animationTime++;
        }

        // 增加存在时间
        lifeTime++;

        // 检查是否超时
        if (lifeTime >= maxLifeTime) {
            this.setLive(false);
            System.out.println("阳光消失了");
        }
    }

    // 更新阳光的物理状态 (位置，速度，重力，弹跳)
    private void updatePhysics() {
        // 更新位置
        setX((int)(getX() + velocityX));
        setY((int)(getY() + velocityY));

        // 应用重力
        velocityY += gravity;

        // 应用水平摩擦力（空气阻力），使其逐渐减速
        velocityX *= 0.99; // 略微的空气阻力

        // 检查是否到达地面
        if (getY() >= groundY) {
            setY((int)groundY); // 确保Y坐标停留在地面

            // 处理弹跳
            if (bounceCount < maxBounces && Math.abs(velocityY) > 0.5) { // 速度过小则不弹跳
                velocityY = -velocityY * bounceDamping; // 反向并减速
                velocityX *= friction; // 水平速度受地面摩擦力影响
                bounceCount++;

                // 向日葵阳光落地后添加一点随机水平速度使弹跳更自然
                if (sunType == SunType.SUNFLOWER && bounceCount == 1) {
                    velocityX += (Math.random() - 0.5) * 0.5; // 轻微随机左右弹开
                }
            } else {
                // 停止弹跳，进入静止悬浮状态
                velocityY = 0;
                velocityX = 0;
                isDropping = false;
                // animationTime在initializeDefaultValues已随机初始化
                System.out.println("阳光落地并静止");
            }
        }

        // 边界检查 - 防止阳光飞出屏幕
        if (getX() < 0) {
            setX(0);
            velocityX = Math.abs(velocityX) * 0.5; // 反弹并减速
        } else if (getX() > GameConfig.GAME_WIDTH - getW()) {
            setX(GameConfig.GAME_WIDTH - getW());
            velocityX = -Math.abs(velocityX) * 0.5; // 反弹并减速
        }
    }

    /**
     * 收集阳光
     */
    public void collect() {
        if (!isCollected) {
            isCollected = true;
            this.setLive(false); // 设置为非活动状态，ElementManager 会移除它
            System.out.println("收集了 " + value + " 点阳光");
        }
    }

    /**
     * 检查点击是否在阳光范围内
     */
    public boolean isClickedAt(int mouseX, int mouseY) {
        int checkY = getY();
        if (!isDropping) { // 如果已经静止，使用动画Y来检测点击
            checkY += (int)(Math.sin(animationTime * floatSpeed) * floatAmplitude);
        }

        return mouseX >= getX() && mouseX <= getX() + getW() &&
               mouseY >= checkY && mouseY <= checkY + getH();
    }

    // Getter方法
    public int getValue() {
        return value;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public boolean isDropping() {
        return isDropping;
    }

    public SunType getSunType() {
        return sunType;
    }

    public double getGroundY() {
        return groundY;
    }

    public void setGroundY(double groundY) {
        this.groundY = groundY;
    }

    /**
     * 创建自然阳光的静态工厂方法
     * @param x 初始X坐标
     * @param startY 初始Y坐标 (通常为屏幕上方)
     * @param groundY 最终落地Y坐标
     * @return Natural Sun object
     */
    public static Sun createNaturalSun(int x, int startY, int groundY) {
        return new Sun(x, startY, groundY);
    }

    /**
     * 创建向日葵阳光的静态工厂方法
     * @param sunflowerX 向日葵的X坐标
     * @param sunflowerY 向日葵的Y坐标
     * @return Sunflower Sun object
     */
    public static Sun createSunflowerSun(int sunflowerX, int sunflowerY) {
        // 向日葵阳光生成位置在向日葵上方中心
        int startX = sunflowerX + (GameConfig.PLANT_WIDTH / 2) - (GameConfig.SUN_WIDTH / 2);
        int startY = sunflowerY - GameConfig.SUN_HEIGHT; // 稍高于向日葵

        // 向日葵阳光的目标地面位置在向日葵所在的格子里或其下方
        int groundY = sunflowerY + GameConfig.GRID_HEIGHT - (GameConfig.SUN_HEIGHT / 2) + 5; // 落在格子底部

        // 随机的初始抛物线轨迹速度
        double initialVelocityX = (Math.random() - 0.5) * 4.0; // -2.0 到 2.0
        double initialVelocityY = -(Math.random() * 2.0 + 3.0); // -3.0 到 -5.0 (向上抛)

        return new Sun(startX, startY, groundY, initialVelocityX, initialVelocityY);
    }

    @Override
    public String toString() {
        return "Sun at (" + getX() + "," + getY() + ") value:" + value +
               " life:" + lifeTime + "/" + maxLifeTime +
               " type:" + sunType + " dropping:" + isDropping +
               " vY:" + String.format("%.2f", velocityY) + " vX:" + String.format("%.2f", velocityX);
    }
}