package com.tedu.utils;

/**
 * 游戏配置常量类 - 修复版本 (新增小推车配置)
 */
public class GameConfig {
    // 游戏窗口设置
    public static final int GAME_WIDTH = 900;
    public static final int GAME_HEIGHT = 600;

    // 网格设置
    public static final int GRID_ROWS = 5;          // 5行
    public static final int GRID_COLS = 9;          // 9列
    public static final int GRID_WIDTH = 80;        // 每格宽度
    public static final int GRID_HEIGHT = 100;      // 每格高度
    public static final int GRID_START_X = 220;     // 网格起始X坐标
    public static final int GRID_START_Y = 80;      // 网格起始Y坐标

    // 植物设置
    public static final int PLANT_WIDTH = 60;
    public static final int PLANT_HEIGHT = 70;      // 从80降低到70，降低植物高度
    public static final int PLANT_OFFSET_Y = 15;    // 新增：植物在网格中的Y偏移量

    // 僵尸设置
    public static final int ZOMBIE_WIDTH = 80;
    public static final int ZOMBIE_HEIGHT = 100;
    public static final int ZOMBIE_SPEED = 1;       // 僵尸移动速度 - 保持1像素/帧
    public static final int ZOMBIE_OFFSET_Y = 0;   // 僵尸在网格中的Y偏移量 (可调整以居中)
    
    // 僵尸伤害
    public static final int NORMAL_ZOMBIE_DAMAGE = 10; // 普通僵尸伤害
    public static final int CONEHEAD_ZOMBIE_DAMAGE = 15;
    
    // 子弹设置
    public static final int PROJECTILE_WIDTH = 20;
    public static final int PROJECTILE_HEIGHT = 20;
    public static final int PROJECTILE_SPEED = 2;
    
    // 伤害配置
    public static final int PEA_DAMAGE = 20;        // 豌豆射手的豌豆伤害

    // 阳光设置
    public static final int SUN_WIDTH = 40;
    public static final int SUN_HEIGHT = 40;
    public static final int SUN_VALUE = 25;         // 每个阳光的价值
    public static final int INITIAL_SUN = 150;       // 初始阳光数量

    // 阳光相关配置
    public static final long NATURAL_SUN_INTERVAL = 10000; // 自然阳光间隔（10秒）
    public static final int NATURAL_SUN_AMOUNT = 25; // 自然阳光数量
    public static final long SUNFLOWER_PRODUCE_INTERVAL = 24000; // 向日葵产阳光间隔（20秒）
    public static final int SUNFLOWER_SUN_AMOUNT = 50; // 向日葵产生的阳光数量
    public static final long SUN_COLLECTION_COOLDOWN = 100; // 阳光收集冷却时间（0.1秒）

    // 阳光掉落和收集参数
    public static final int SUN_FALL_SPEED = 2; // 阳光掉落速度
    public static final int SUN_LIFETIME = 15000; // 阳光存在时间（15秒后消失）
    public static final int SUN_COLLECTION_RADIUS = 30; // 阳光收集半径

    // 植物价格
    public static final int PEASHOOTER_COST = 100;
    public static final int SUNFLOWER_COST = 50;
    public static final int WALLNUT_COST = 50;

    // 植物铲除返还配置
    public static final double PLANT_REFUND_RATE = 0.5; // 返还50%的原价
    public static final int MIN_REFUND_AMOUNT = 10;     // 最小返还金额
    public static final int MAX_REFUND_AMOUNT = 100;     // 最大返还金额
    public static final boolean ENABLE_PLANT_REFUND = true; // 是否启用返还系统
    
    // 游戏机制
    public static final int SHOOT_INTERVAL = 300;   // 射击间隔(游戏帧)
    public static final int SUN_PRODUCE_INTERVAL = 2000; // 阳光生产间隔

    // 僵尸血量
    public static final int NORMAL_ZOMBIE_HP = 120;
    public static final int CONEHEAD_ZOMBIE_HP = 150;
    public static final int BUCKETHEAD_ZOMBIE_HP = 180;

    // 僵尸移动频率控制
    public static final int ZOMBIE_MOVE_INTERVAL = 5;  // 每8帧移动1次，降低移动频率
    public static final boolean ENABLE_ZOMBIE_KILL_REWARD = true; // 是否启用击杀奖励
    public static final int ZOMBIE_KILL_BASE_REWARD = 10;         // 基础击杀奖励
    public static final int NORMAL_ZOMBIE_REWARD = 10;           // 普通僵尸奖励
    public static final int CONEHEAD_ZOMBIE_REWARD = 15;         // 路障僵尸奖励  
    public static final int BUCKETHEAD_ZOMBIE_REWARD = 25;       // 铁桶僵尸奖励
    
    // 小推车配置
    public static final int LAWN_MOWER_WIDTH = 70;  // 小推车宽度 (根据图片调整)
    public static final int LAWN_MOWER_HEIGHT = 70; // 小推车高度 (根据图片调整)
    public static final int LAWN_MOWER_SPEED = 5;   // 小推车移动速度
    // 小推车起始X坐标，通常在房子区域的最右侧
    public static final int LAWN_MOWER_START_X = 100; // 或者 100 左右，确保在房子区域可见
	
    
    public static final int GAME_FPS_MS = 0;
    
    public static final int DEFAULT_DIE_ANIMATION_DURATION = 300;

    // 游戏速度配置
    public static final int NORMAL_SPEED = 1;
    public static final int FAST_SPEED = 2;
    public static int currentSpeed = NORMAL_SPEED;
    public static float TIME_SCALE = 1.0f; // 可动态调整的游戏速度（支持小数）

    // 时间缩放因子（基于long类型）
    public static long getTimeScaleFactor() {
        return currentSpeed * 1000L; // 转换为毫秒因子
    }
    
    // 加速按钮位置和尺寸
    public static final int SPEED_BUTTON_X = 335; // 加速按钮X坐标
    public static final int SPEED_BUTTON_Y = 12;  // 加速按钮Y坐标
    public static final int SPEED_BUTTON_SIZE = 40; // 按钮大小
    
    public static final String SPEED_BUTTON_NORMAL = "speed_button_normal";
    public static final String SPEED_BUTTON_DOUBLE = "speed_button_double";
}