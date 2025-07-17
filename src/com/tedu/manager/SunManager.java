package com.tedu.manager;

import com.tedu.element.ElementObj;
import com.tedu.element.items.Sun; // 确保导入 Sun 类
import com.tedu.element.items.Sun.SunType; // 确保导入 Sun.SunType 枚举
import com.tedu.utils.GameConfig;
import java.util.Random;

/**
 * 阳光管理器 - 单例模式
 * 管理游戏中的阳光数量，控制阳光生成速度
 * 支持物理掉落效果的阳光生成
 */
public class SunManager {

    private static SunManager instance = null;
    private int currentSun;
    private int totalSunCollected; // 统计总共收集的阳光

    // 阳光生成控制参数
    private long lastNaturalSunTime; // 上次自然掉落阳光的时间
    // 自然阳光间隔：参考原版，大约7-12秒随机生成一次阳光
    private static final long NATURAL_SUN_MIN_INTERVAL = GameConfig.NATURAL_SUN_INTERVAL-3; // 自然阳光最小间隔（7秒）
    private static final long NATURAL_SUN_MAX_INTERVAL = GameConfig.NATURAL_SUN_INTERVAL+2; // 自然阳光最大间隔（12秒）
    private long nextNaturalSunInterval; // 下次自然阳光的实际间隔
    private static final int NATURAL_SUN_AMOUNT = GameConfig.NATURAL_SUN_AMOUNT; // 自然阳光数量

    // 向日葵阳光生成控制
    // 向日葵产阳光间隔：原版向日葵产出阳光约为24-25秒左右
    public static final long SUNFLOWER_PRODUCE_INTERVAL = GameConfig.SUNFLOWER_PRODUCE_INTERVAL; // 向日葵产阳光间隔（24秒）
    public static final int SUNFLOWER_SUN_AMOUNT = GameConfig.SUNFLOWER_SUN_AMOUNT; // 向日葵产生的阳光数量

    // 阳光收集控制
    private static final long SUN_COLLECTION_COOLDOWN = 100; // 阳光收集冷却时间（0.1秒）
    private long lastSunCollectionTime;

    private Random random; // 用于生成随机阳光位置和时间间隔

    private SunManager() {
        this.currentSun = GameConfig.INITIAL_SUN;
        this.totalSunCollected = 0;
        this.lastNaturalSunTime = System.currentTimeMillis();
        this.random = new Random();
        this.nextNaturalSunInterval = generateNextNaturalSunInterval();
        this.lastSunCollectionTime = 0;
    }

    /**
     * 获取单例实例
     */
    public static synchronized SunManager getInstance() {
        if (instance == null) {
            instance = new SunManager();
        }
        return instance;
    }

    /**
     * 生成下一个自然阳光的随机间隔
     */
    private long generateNextNaturalSunInterval() {
        return NATURAL_SUN_MIN_INTERVAL + random.nextInt((int)(NATURAL_SUN_MAX_INTERVAL - NATURAL_SUN_MIN_INTERVAL + 1));
    }

    /**
     * 游戏更新时调用，检查是否需要自然生成阳光
     */
    public void update() {
        long currentTime = System.currentTimeMillis();

        // 检查是否需要自然掉落阳光
        if (currentTime - lastNaturalSunTime >= nextNaturalSunInterval) {
            generateNaturalSun();
            lastNaturalSunTime = currentTime;
            nextNaturalSunInterval = generateNextNaturalSunInterval();
        }
    }
    
    /**
     * 支持倍速的更新方法
     * @param deltaTime 时间增量（毫秒）
     */
    public void update(long deltaTime) {
        // 应用速度倍数来缩放时间
        long scaledDeltaTime = deltaTime * GameConfig.currentSpeed;
        
        // 更新自然阳光生成
        updateNaturalSunWithDelta(scaledDeltaTime);
        
        // 更新收集冷却
        updateCollectionCooldown(scaledDeltaTime);
    }
    
    /**
     * 更新收集冷却
     */
    private void updateCollectionCooldown(long deltaTime) {
        // 这里可以添加基于deltaTime的冷却逻辑
        // 目前保持现有的基于系统时间的逻辑
    }
    
    /**
     * 使用增量时间更新自然阳光生成
     */
    private void updateNaturalSunWithDelta(long deltaTime) {
        long currentTime = System.currentTimeMillis();
        
        // 检查是否需要自然掉落阳光
        if (currentTime - lastNaturalSunTime >= nextNaturalSunInterval) {
            generateNaturalSun();
            lastNaturalSunTime = currentTime;
            nextNaturalSunInterval = generateNextNaturalSunInterval();
        }
    }

    /**
     * 自然生成阳光（从天空掉落）
     * 使用物理掉落效果
     */
    private void generateNaturalSun() {
        System.out.println("天空掉落阳光！");

        // 在草坪区域随机生成阳光的X坐标，确保不超出屏幕边界
        int randomX = GameConfig.GRID_START_X + random.nextInt(
            Math.max(1, (GameConfig.GRID_COLS * GameConfig.GRID_WIDTH) - GameConfig.SUN_WIDTH)
        );

        // 阳光从屏幕上方开始掉落，加上随机偏移让出现位置更自然
        int startY = -GameConfig.SUN_HEIGHT - random.nextInt(80); // 从屏幕上方随机高度开始

        // 目标地面Y坐标在草坪区域内
        // 确保阳光落地位置不会太靠近顶部或底部边缘
        int minTargetY = GameConfig.GRID_START_Y + GameConfig.SUN_HEIGHT; // 至少在草坪顶部以下一点
        int maxTargetY = GameConfig.GRID_START_Y + (GameConfig.GRID_ROWS * GameConfig.GRID_HEIGHT) - GameConfig.SUN_HEIGHT - 10;
        int targetY = minTargetY + random.nextInt(Math.max(1, maxTargetY - minTargetY));

        // 使用 Sun 的静态工厂方法创建自然阳光
        ElementObj naturalSun = Sun.createNaturalSun(randomX, startY, targetY);

        if (naturalSun != null) {
            ElementManager.getManager().addElement(naturalSun, GameElement.SUNS);
            System.out.println("天空掉落阳光在 (" + randomX + "," + startY + ") 目标地面: " + targetY);
        }
    }

    /**
     * 向日葵生成阳光
     * 使用物理掉落效果，包含抛物线轨迹
     * @param plantX 向日葵的X坐标
     * @param plantY 向日葵的Y坐标
     */
    public void generateSunflowerSun(int plantX, int plantY) {
        System.out.println("向日葵产生阳光！");

        // 检查向日葵是否在底部行
        int plantRow = (plantY - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
        if (plantRow >= GameConfig.GRID_ROWS - 1) {
            System.out.println("⚠️ 检测到底部行向日葵，使用特殊阳光生成逻辑");
        }
        
        // 使用 Sun 的静态工厂方法创建向日葵阳光
        ElementObj sunflowerSun = Sun.createSunflowerSun(plantX, plantY);

        if (sunflowerSun != null) {
            ElementManager.getManager().addElement(sunflowerSun, GameElement.SUNS);
            System.out.println("向日葵在 (" + plantX + "," + plantY + ") 产生阳光。");
        }
    }

    /**
     * 增加阳光数量并显示来源信息 (重载方法)
     */
    public boolean addSun(int amount, String source) {
        long currentTime = System.currentTimeMillis();

        // 防止过快收集阳光
        if (currentTime - lastSunCollectionTime < SUN_COLLECTION_COOLDOWN) {
            return false;
        }

        currentSun += amount;
        totalSunCollected += amount;
        lastSunCollectionTime = currentTime;

        System.out.println("💰 获得阳光 +" + amount + " 来源: " + source + " (当前: " + currentSun + ")");
        return true;
    }
    
    /**
     * 安全增加阳光（防止溢出，无冷却）
     */
    public void addSunSafely(int amount) {
        if (amount > 0) {
            // 防止整数溢出
            if (Integer.MAX_VALUE - this.currentSun > amount) {
                this.currentSun += amount;
            } else {
                this.currentSun = Integer.MAX_VALUE;
            }
            this.totalSunCollected += amount;
            System.out.println("💰 安全增加阳光: +" + amount + " (当前: " + this.currentSun + ")");
        }
    }

    /**
     * 消费阳光
     */
    public boolean spendSun(int amount) {
        if (currentSun >= amount) {
            currentSun -= amount;
            System.out.println("消费阳光 -" + amount + "，剩余阳光: " + currentSun);
            return true;
        } else {
            System.out.println("阳光不足！需要 " + amount + "，当前只有 " + currentSun);
            return false;
        }
    }

    /**
     * 检查是否有足够的阳光
     */
    public boolean hasEnoughSun(int amount) {
        return currentSun >= amount;
    }

    /**
     * 重置阳光（新游戏时使用）
     */
    public void reset() {
        currentSun = GameConfig.INITIAL_SUN;
        totalSunCollected = 0;
        lastNaturalSunTime = System.currentTimeMillis();
        nextNaturalSunInterval = generateNextNaturalSunInterval();
        this.lastSunCollectionTime = 0;
    }

    /**
     * 获取阳光收集冷却状态
     */
    public boolean canCollectSun() {
        return System.currentTimeMillis() - lastSunCollectionTime >= SUN_COLLECTION_COOLDOWN;
    }

    /**
     * 获取距离下次自然阳光生成的时间（秒）
     */
    public int getTimeToNextNaturalSun() {
        long timeRemaining = nextNaturalSunInterval - (System.currentTimeMillis() - lastNaturalSunTime);
        return Math.max(0, (int)(timeRemaining / 1000));
    }

    // Getter方法
    public int getCurrentSun() {
        return currentSun;
    }

    public int getTotalSunCollected() {
        return totalSunCollected;
    }

    /**
     * 获取阳光显示字符串
     */
    public String getSunDisplayText() {
        return String.valueOf(currentSun);
    }

    // 获取常量值的方法，供其他类使用
    public static long getSunflowerProduceInterval() {
        return SUNFLOWER_PRODUCE_INTERVAL;
    }

    public static int getSunflowerSunAmount() {
        return SUNFLOWER_SUN_AMOUNT;
    }

    public static int getNaturalSunAmount() {
        return NATURAL_SUN_AMOUNT;
    }
}