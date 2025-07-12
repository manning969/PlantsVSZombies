package com.tedu.manager;

import com.tedu.element.Zombie;
import com.tedu.element.zombies.NormalZombie;
import com.tedu.utils.GameConfig;
import com.tedu.element.zombies.ConeheadZombie;
import java.util.Random;

/**
 * 僵尸工厂类 - 用于创建不同类型的僵尸
 */
public class ZombieFactory {
	
	public static final boolean ENABLE_ZOMBIE_KILL_REWARD = GameConfig.ENABLE_ZOMBIE_KILL_REWARD; // 是否启用击杀奖励
	public static final int ZOMBIE_KILL_BASE_REWARD = GameConfig.ZOMBIE_KILL_BASE_REWARD;         // 基础击杀奖励
	public static final int NORMAL_ZOMBIE_REWARD = GameConfig.NORMAL_ZOMBIE_REWARD;           // 普通僵尸奖励
	public static final int CONEHEAD_ZOMBIE_REWARD = GameConfig.CONEHEAD_ZOMBIE_REWARD;         // 路障僵尸奖励  
	public static final int BUCKETHEAD_ZOMBIE_REWARD = GameConfig.BUCKETHEAD_ZOMBIE_REWARD;       // 铁桶僵尸奖励
    
    public enum ZombieType {
        NORMAL,
        CONEHEAD,
        BUCKETHEAD  // 预留给铁桶僵尸
    }
    
    private static final Random random = new Random();
    
    /**
     * 创建指定类型的僵尸
     * @param type 僵尸类型
     * @param rowIndex 行索引
     * @return 僵尸对象
     */
    public static Zombie createZombie(ZombieType type, int rowIndex) {
        switch (type) {
            case NORMAL:
                return new NormalZombie(rowIndex);
            case CONEHEAD:
                return new ConeheadZombie(rowIndex);
            case BUCKETHEAD:
                // 待实现铁桶僵尸
                // return new BucketheadZombie(rowIndex);
                return new ConeheadZombie(rowIndex); // 临时返回路障僵尸
            default:
                return new NormalZombie(rowIndex);
        }
    }
    
    /**
     * 根据游戏进度随机创建僵尸
     * @param rowIndex 行索引
     * @param gameLevel 游戏关卡（影响僵尸类型概率）
     * @return 僵尸对象
     */
    public static Zombie createRandomZombie(int rowIndex, int gameLevel) {
        // 根据游戏关卡调整僵尸类型概率
        int rand = random.nextInt(100);
        
        if (gameLevel <= 1) {
            // 第1关：只有普通僵尸
            return createZombie(ZombieType.NORMAL, rowIndex);
        } else if (gameLevel <= 3) {
            // 第2-3关：70%普通僵尸，30%路障僵尸
            if (rand < 70) {
                return createZombie(ZombieType.NORMAL, rowIndex);
            } else {
                return createZombie(ZombieType.CONEHEAD, rowIndex);
            }
        } else {
            // 第4关及以上：50%普通僵尸，40%路障僵尸，10%铁桶僵尸
            if (rand < 50) {
                return createZombie(ZombieType.NORMAL, rowIndex);
            } else if (rand < 90) {
                return createZombie(ZombieType.CONEHEAD, rowIndex);
            } else {
                return createZombie(ZombieType.BUCKETHEAD, rowIndex);
            }
        }
    }
    
    /**
     * 获取僵尸类型的基础分数（用于击杀奖励）
     * @param type 僵尸类型
     * @return 分数
     */
    public static int getZombieScore(ZombieType type) {
        switch (type) {
            case NORMAL:
                return 10;
            case CONEHEAD:
                return 20;
            case BUCKETHEAD:
                return 50;
            default:
                return 10;
        }
    }
    
    /**
     * 获取僵尸类型的威胁等级（用于AI决策）
     * @param type 僵尸类型
     * @return 威胁等级（1-5）
     */
    public static int getThreatLevel(ZombieType type) {
        switch (type) {
            case NORMAL:
                return 1;
            case CONEHEAD:
                return 2;
            case BUCKETHEAD:
                return 4;
            default:
                return 1;
        }
    }
    
    /**
     * 根据僵尸对象获取其类型
     * @param zombie 僵尸对象
     * @return 僵尸类型
     */
    public static ZombieType getZombieType(Zombie zombie) {
        if (zombie instanceof NormalZombie) {
            return ZombieType.NORMAL;
        } else if (zombie instanceof ConeheadZombie) {
            return ZombieType.CONEHEAD;
        } else {
            return ZombieType.NORMAL; // 默认类型
        }
    }
    /**
     * 根据僵尸类型获取击杀奖励
     */
    public static int getZombieKillReward(String zombieType) {
        if (!ENABLE_ZOMBIE_KILL_REWARD) {
            return 0;
        }
        
        switch (zombieType.toLowerCase()) {
            case "normal":
            case "normal_zombie":
                return NORMAL_ZOMBIE_REWARD;
            case "conehead":
            case "conehead_zombie":
                return CONEHEAD_ZOMBIE_REWARD;
            case "buckethead":
            case "buckethead_zombie":
                return BUCKETHEAD_ZOMBIE_REWARD;
            default:
                return ZOMBIE_KILL_BASE_REWARD; // 默认奖励
        }
    }

    /**
     * 获取僵尸类型的显示名称
     */
    public static String getZombieDisplayName(String zombieType) {
        switch (zombieType.toLowerCase()) {
            case "normal":
            case "normal_zombie":
                return "普通僵尸";
            case "conehead":
            case "conehead_zombie":
                return "路障僵尸";
            case "buckethead":
            case "buckethead_zombie":
                return "铁桶僵尸";
            default:
                return "僵尸";
        }
    }
}