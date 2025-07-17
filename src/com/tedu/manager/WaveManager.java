package com.tedu.manager;

import java.util.ArrayList;
import java.util.List;
import com.tedu.element.ElementObj;
import com.tedu.utils.GameConfig;

/**
 * 波次管理器 - 增强版，模仿真实植物大战僵尸
 * 特点：
 * 1. 游戏开始有准备时间
 * 2. 波次之间有间隔
 * 3. 最后一波是大波攻击
 * 4. 难度逐渐递增
 */
public class WaveManager {
    
    private static WaveManager instance = null;
    
    // 游戏阶段
    private enum GamePhase {
        PREPARATION,    // 准备阶段
        WAVE_ACTIVE,    // 波次进行中
        WAVE_INTERVAL,  // 波次间隔
        FINAL_WAVE,     // 最终波次
        GAME_COMPLETE   // 游戏完成
    }
    
    private GamePhase currentPhase = GamePhase.PREPARATION;
    private int currentWave = 0;
    private int totalWaves = 10;
    private long phaseStartTime = 0;
    private long nextZombieSpawnTime = 0;
    private boolean gameWon = false;
    
    // 时间控制常量（以游戏帧为单位，1秒≈100帧）
    private static final long PREPARATION_TIME = 800;      // 8秒准备时间
    private static final long WAVE_INTERVAL_TIME = 400;     // 4秒波次间隔
    private static final long FINAL_WAVE_WARNING_TIME = 500; // 5秒最终波次警告
    
    // 波次配置
    private List<WaveConfig> waveConfigs;
    
    private WaveManager() {
        initializeWaves();
    }
    
    public static synchronized WaveManager getInstance() {
        if (instance == null) {
            instance = new WaveManager();
        }
        return instance;
    }
    
    /**
     * 初始化波次配置 - 模仿真实PVZ难度曲线
     */
    private void initializeWaves() {
        waveConfigs = new ArrayList<>();
        
        // 第1波：简单开始，只有少量普通僵尸
        waveConfigs.add(new WaveConfig(1, 3, 0, 0, 800, "新手指导"));
        
        // 第2-3波：逐渐增加普通僵尸数量
        waveConfigs.add(new WaveConfig(2, 5, 0, 0, 700, "熟悉节奏"));
        waveConfigs.add(new WaveConfig(3, 6, 0, 0, 650, "轻松应对"));
        
        // 第4波：引入路障僵尸
        waveConfigs.add(new WaveConfig(4, 4, 2, 0, 600, "新的挑战"));
        
        // 第5波：中期难度
        waveConfigs.add(new WaveConfig(5, 6, 3, 0, 550, "稳步前进"));
        
        // 第6-7波：增加难度
        waveConfigs.add(new WaveConfig(6, 7, 4, 1, 500, "难度提升"));
        waveConfigs.add(new WaveConfig(7, 8, 5, 1, 450, "保持专注"));
        
        // 第8-9波：高难度
        waveConfigs.add(new WaveConfig(8, 10, 6, 2, 400, "严峻考验"));
        waveConfigs.add(new WaveConfig(9, 12, 7, 2, 350, "最后冲刺"));
        
        // 第10波：最终大波攻击
        waveConfigs.add(new WaveConfig(10, 15, 10, 5, 300, "最终决战"));
    }
    
    /**
     * 更新波次状态 - 增强版状态机
     */
    public void update(long gameTime) {
        if (gameWon) return;
        
        switch (currentPhase) {
            case PREPARATION:
                updatePreparationPhase(gameTime);
                break;
            case WAVE_ACTIVE:
                updateActiveWave(gameTime);
                break;
            case WAVE_INTERVAL:
                updateWaveInterval(gameTime);
                break;
            case FINAL_WAVE:
                updateFinalWave(gameTime);
                break;
            case GAME_COMPLETE:
                // 游戏已完成，不做任何操作
                break;
        }
    }
    
    /**
     * 更新准备阶段
     */
    private void updatePreparationPhase(long gameTime) {
        if (phaseStartTime == 0) {
            phaseStartTime = gameTime;
            System.out.println("🌱 游戏开始！你有8秒时间准备防御...");
            System.out.println("💡 提示：种植向日葵收集阳光，用豌豆射手攻击僵尸，用坚果墙阻挡僵尸！");
        }
        
        long elapsedTime = gameTime - phaseStartTime;
        
        // 倒计时提醒
        if (elapsedTime == PREPARATION_TIME - 500) {
            System.out.println("⚠️  僵尸将在5秒后到达！");
        } else if (elapsedTime == PREPARATION_TIME - 300) {
            System.out.println("⚠️  僵尸将在3秒后到达！");
        } else if (elapsedTime == PREPARATION_TIME - 100) {
            System.out.println("⚠️  僵尸来了！");
        }
        
        if (elapsedTime >= PREPARATION_TIME) {
            startFirstWave(gameTime);
        }
    }
    
    /**
     * 开始第一波
     */
    private void startFirstWave(long gameTime) {
        currentWave = 1;
        currentPhase = GamePhase.WAVE_ACTIVE;
        phaseStartTime = gameTime;
        nextZombieSpawnTime = gameTime + 200; // 第一只僵尸延迟2秒出现
        
        WaveConfig config = getCurrentWaveConfig();
        System.out.println("🌊 第 " + currentWave + " 波开始！" + config.description);
        System.out.println("   普通僵尸: " + config.normalZombies + 
                          " 路障僵尸: " + config.coneheadZombies + 
                          " 铁桶僵尸: " + config.bucketheadZombies);
    }
    
    /**
     * 更新活跃波次
     */
    private void updateActiveWave(long gameTime) {
        WaveConfig config = getCurrentWaveConfig();
        
        // 生成僵尸
        if (gameTime >= nextZombieSpawnTime && config.hasZombiesLeft()) {
            spawnNextZombie();
            nextZombieSpawnTime = gameTime + config.spawnInterval;
        }
        
        // 检查波次是否完成
        if (config.isWaveComplete() && !hasZombiesOnField()) {
            completeCurrentWave(gameTime);
        }
    }
    
    /**
     * 完成当前波次
     */
    private void completeCurrentWave(long gameTime) {
        System.out.println("✅ 第 " + currentWave + " 波完成！");
        
        // 检查是否是最后一波
        if (currentWave >= totalWaves) {
            gameWon = true;
            currentPhase = GamePhase.GAME_COMPLETE;
            return;
        }
        
        // 检查是否即将进入最终波次
        if (currentWave == totalWaves - 1) {
            currentPhase = GamePhase.FINAL_WAVE;
            phaseStartTime = gameTime;
            System.out.println("🔥 警告：最终波次即将到来！准备迎接最后的挑战！");
        } else {
            currentPhase = GamePhase.WAVE_INTERVAL;
            phaseStartTime = gameTime;
            
            // 波次完成奖励（额外阳光）
            SunManager sunManager = SunManager.getInstance();
            int bonusSun = 25 + currentWave * 5; // 奖励阳光随波次递增
            
            // 使用addSunSafely方法，因为这是系统奖励，不需要冷却时间限制
            sunManager.addSunSafely(bonusSun);
            System.out.println("🌞 波次完成奖励: +" + bonusSun + " 阳光！");
        }
    }
    
    /**
     * 更新波次间隔
     */
    private void updateWaveInterval(long gameTime) {
        long elapsedTime = gameTime - phaseStartTime;
        
        // 间隔时间倒计时
        if (elapsedTime == WAVE_INTERVAL_TIME - 300) {
            System.out.println("⏰ 下一波将在3秒后开始！");
        } else if (elapsedTime == WAVE_INTERVAL_TIME - 100) {
            System.out.println("⏰ 下一波即将开始！");
        }
        
        if (elapsedTime >= WAVE_INTERVAL_TIME) {
            startNextWave(gameTime);
        }
    }
    
    /**
     * 开始下一波
     */
    private void startNextWave(long gameTime) {
        currentWave++;
        currentPhase = GamePhase.WAVE_ACTIVE;
        phaseStartTime = gameTime;
        nextZombieSpawnTime = gameTime + 100;
        
        WaveConfig config = getCurrentWaveConfig();
        System.out.println("🌊 第 " + currentWave + " 波开始！" + config.description);
        
        // 显示波次详情
        System.out.println("   僵尸构成 - 普通:" + config.normalZombies + 
                          " 路障:" + config.coneheadZombies + 
                          " 铁桶:" + config.bucketheadZombies);
        System.out.println("   生成间隔: " + config.spawnInterval + " 帧");
    }
    
    /**
     * 更新最终波次
     */
    private void updateFinalWave(long gameTime) {
        long elapsedTime = gameTime - phaseStartTime;
        
        if (elapsedTime >= FINAL_WAVE_WARNING_TIME) {
            startNextWave(gameTime);
            System.out.println("💀 最终波次开始！这是最后的战斗！");
        }
    }
    
    /**
     * 生成下一只僵尸
     */
    private void spawnNextZombie() {
        WaveConfig config = getCurrentWaveConfig();
        ElementManager em = ElementManager.getManager();
        
        String zombieType = config.getNextZombieType();
        int randomRow = (int) (Math.random() * GameConfig.GRID_ROWS);
        
        ElementObj zombie = GameLoad.createZombie(zombieType, randomRow);
        if (zombie != null) {
            em.addElement(zombie, GameElement.ZOMBIES);
            System.out.println("🧟 生成 " + getZombieTypeName(zombieType) + " 在第 " + (randomRow + 1) + " 行");
        }
    }
    
    /**
     * 获取僵尸类型中文名称
     */
    private String getZombieTypeName(String zombieType) {
        switch (zombieType) {
            case "normal_zombie": return "普通僵尸";
            case "conehead_zombie": return "路障僵尸";
            case "buckethead_zombie": return "铁桶僵尸";
            default: return "未知僵尸";
        }
    }
    
    /**
     * 检查场上是否还有僵尸
     */
    private boolean hasZombiesOnField() {
        ElementManager em = ElementManager.getManager();
        return !em.getElementsByKey(GameElement.ZOMBIES).isEmpty();
    }
    
    /**
     * 获取当前波次配置
     */
    private WaveConfig getCurrentWaveConfig() {
        if (currentWave <= 0 || currentWave > waveConfigs.size()) {
            return new WaveConfig(1, 3, 0, 0, 800, "默认波次");
        }
        return waveConfigs.get(currentWave - 1);
    }
    
    /**
     * 获取游戏进度描述
     */
    public String getProgressDescription() {
        switch (currentPhase) {
            case PREPARATION:
                long prepTime = PREPARATION_TIME - (System.currentTimeMillis() % PREPARATION_TIME);
                return "准备阶段 (剩余 " + (prepTime / 100) + " 秒)";
            case WAVE_ACTIVE:
                return "第 " + currentWave + " 波进行中";
            case WAVE_INTERVAL:
                return "波次间隔";
            case FINAL_WAVE:
                return "最终波次警告";
            case GAME_COMPLETE:
                return "游戏完成";
            default:
                return "未知状态";
        }
    }
    
    // Getter方法
    public int getCurrentWave() { return currentWave; }
    public int getTotalWaves() { return totalWaves; }
    public boolean isGameWon() { return gameWon; }
    public boolean isWaveInProgress() { return currentPhase == GamePhase.WAVE_ACTIVE; }
    public boolean isPreparationPhase() { return currentPhase == GamePhase.PREPARATION; }
    
    /**
     * 重置波次管理器
     */
    public void reset() {
        currentWave = 0;
        currentPhase = GamePhase.PREPARATION;
        phaseStartTime = 0;
        nextZombieSpawnTime = 0;
        gameWon = false;
        
        // 重新初始化所有波次配置
        for (WaveConfig config : waveConfigs) {
            config.reset();
        }
        
        System.out.println("波次管理器已重置");
    }
    
    /**
     * 波次配置内部类 - 增强版
     */
    private static class WaveConfig {
        int waveNumber;
        int normalZombies;
        int coneheadZombies;
        int bucketheadZombies;
        int spawnInterval;
        String description;
        
        // 已生成数量计数器
        int normalSpawned = 0;
        int coneheadSpawned = 0;
        int bucketheadSpawned = 0;
        
        public WaveConfig(int waveNumber, int normal, int conehead, int buckethead, 
                         int interval, String description) {
            this.waveNumber = waveNumber;
            this.normalZombies = normal;
            this.coneheadZombies = conehead;
            this.bucketheadZombies = buckethead;
            this.spawnInterval = interval;
            this.description = description;
        }
        
        public boolean hasZombiesLeft() {
            return normalSpawned < normalZombies || 
                   coneheadSpawned < coneheadZombies || 
                   bucketheadSpawned < bucketheadZombies;
        }
        
        public boolean isWaveComplete() {
            return !hasZombiesLeft();
        }
        
        public String getNextZombieType() {
            // 优先生成高级僵尸，营造压迫感
            if (bucketheadSpawned < bucketheadZombies) {
                bucketheadSpawned++;
                return "buckethead_zombie";
            } else if (coneheadSpawned < coneheadZombies) {
                coneheadSpawned++;
                return "conehead_zombie";
            } else if (normalSpawned < normalZombies) {
                normalSpawned++;
                return "normal_zombie";
            }
            return "normal_zombie";
        }
        
        public void reset() {
            normalSpawned = 0;
            coneheadSpawned = 0;
            bucketheadSpawned = 0;
        }
        
        public int getTotalZombies() {
            return normalZombies + coneheadZombies + bucketheadZombies;
        }
        
        public int getSpawnedZombies() {
            return normalSpawned + coneheadSpawned + bucketheadSpawned;
        }
    }
}