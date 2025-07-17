package com.tedu.controller;

import java.util.List;
import java.util.Map;
import com.tedu.element.ElementObj;
import com.tedu.element.Plant;
import com.tedu.element.Zombie;
import com.tedu.element.projectiles.Pea;
import com.tedu.element.items.Sun;
import com.tedu.element.items.LawnMower;
import com.tedu.element.effects.ShovelDirtEffect;
import com.tedu.manager.AudioManager;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ShovelManager;
import com.tedu.manager.SunManager;
import com.tedu.manager.WaveManager;
import com.tedu.utils.GameConfig;
import com.tedu.utils.CollisionDetector;
import com.tedu.utils.GridHelper;

/**
 * 植物大战僵尸游戏主线程 - 真正的倍速功能实现
 */
public class GameThread extends Thread {
    private ElementManager em;
    private SunManager sunManager;
    private WaveManager waveManager;
    private ShovelManager shovelManager;
    private AudioManager audioManager;

    private boolean gameRunning = true;
    private boolean gamePaused = false;
    private long gameTime = 0;
    
    private boolean running = true;
    private static final long FRAME_PERIOD = 16; // 约60FPS
    private int speedMultiplier = 1;
    
    // 时间管理
    private long lastUpdateTime = System.currentTimeMillis();
    private long accumulatedTime = 0; // 累积的游戏时间

    // 植物冷却管理
    private Map<String, Long> plantCooldowns;

    public GameThread() {
        em = ElementManager.getManager();
        sunManager = SunManager.getInstance();
        waveManager = WaveManager.getInstance();
        shovelManager = ShovelManager.getInstance();
        audioManager = AudioManager.getInstance(); // 初始化音频管理器
        plantCooldowns = new java.util.HashMap<>();
    }

    @Override
    public void run() {
        gameLoad();
        gameRunLoop();
        gameOver();
    }

    /**
     * 游戏主循环 - 修复倍速功能
     */
    private void gameRunLoop() {
	    System.out.println("游戏开始运行...");
	    long lastFrameTime = System.currentTimeMillis();
	
	    while (gameRunning) {
            try {
                if (gamePaused) {
                    audioManager.pauseBackgroundMusic();
                    Thread.sleep(100);
                    continue;
                } else {
                    audioManager.resumeBackgroundMusic();
                }
	
	            long currentTime = System.currentTimeMillis();
	            long deltaTime = currentTime - lastFrameTime;
	            lastFrameTime = currentTime;
	
	            // 应用速度倍数 - 关键修复
	            long scaledDeltaTime = deltaTime * speedMultiplier;
	            
	            // 使用缩放后的时间更新游戏时间
	            gameTime += scaledDeltaTime / 10; // 转换为游戏帧单位
	
	            // *** 核心：所有元素逻辑更新、碰撞检测和清理都集中在 ElementManager 中处理 ***
	            em.gameLogicAndCollisionDetection(gameTime);
	            
	            // 更新波次管理器 - 传递缩放后的时间
	            waveManager.update(gameTime);
	
	            // 更新阳光管理器 - 传递缩放后的时间增量
	            sunManager.update(scaledDeltaTime);
	
	            // 更新背景音乐
                updateBackgroundMusic();
                
	            // 检查游戏胜负条件
	            checkGameState();
	
	            // 控制渲染帧率，不影响游戏逻辑速度
	            Thread.sleep(10); // 保持约100FPS渲染
	
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	            gameRunning = false;
	        }
	    }
	}
    
    /**
     * 游戏更新 - 传递正确的时间增量
     */
    private void gameUpdate(long scaledDeltaTime) {
        // 更新ElementManager，传递缩放后的时间
        em.gameLogicAndCollisionDetection(gameTime);
        
        // 更新管理器，传递缩放后的时间增量
        sunManager.update(scaledDeltaTime);
        waveManager.update(gameTime);
        
        // 更新所有元素的model方法
        updateAllElements(scaledDeltaTime);
    }

    /**
     * 更新所有元素 - 传递时间增量
     */
    private void updateAllElements(long scaledDeltaTime) {
        Map<GameElement, List<ElementObj>> allElements = em.getGameElements();
        
        for (Map.Entry<GameElement, List<ElementObj>> entry : allElements.entrySet()) {
            List<ElementObj> elements = entry.getValue();
            // 创建副本避免并发修改异常
            List<ElementObj> elementsCopy = new java.util.ArrayList<>(elements);
            
            for (ElementObj element : elementsCopy) {
                if (element.isLive()) {
                    // 这里需要修改ElementObj的model方法来接受时间增量
                    // 或者使用当前的累积游戏时间
                    element.model(gameTime);
                }
            }
        }
    }

    /**
     * 设置游戏速度 - 改进版本
     */
    public void setGameSpeed(int multiplier) {
        this.speedMultiplier = Math.max(1, Math.min(4, multiplier));
        
        // 同步更新GameConfig中的速度
        GameConfig.currentSpeed = this.speedMultiplier;
        
        System.out.println("游戏速度设置为: " + speedMultiplier + "倍");
        System.out.println("实际效果: 游戏逻辑将以" + speedMultiplier + "倍速度运行");
    }

    /**
     * 获取当前游戏速度
     */
    public int getCurrentSpeed() {
        return speedMultiplier;
    }

    /**
     * 检查植物冷却 - 使用累积时间
     */
    private boolean isPlantOnCooldown(String plantType) {
        Long cooldownEndTime = plantCooldowns.get(plantType);
        return cooldownEndTime != null && gameTime < cooldownEndTime;
    }

    /**
     * 设置植物冷却 - 使用累积时间
     */
    private void setPlantCooldown(String plantType) {
        int baseCooldown = getPlantCooldown(plantType);
        // 倍速时冷却时间相应缩短
        long scaledCooldown = baseCooldown / GameConfig.currentSpeed;
        plantCooldowns.put(plantType, gameTime + scaledCooldown);
    }

    // 保持原有的其他方法不变...
    private void gameLoad() {
        System.out.println("开始加载植物大战僵尸资源...");

        GameLoad.loadImg(); // 这里会同时加载音频
        GameLoad.loadLawn();
        GameLoad.initializeGame();
        initializeLawnMowers();
        
        // 开始播放背景音乐
        startBackgroundMusic();

        System.out.println("资源加载完成！");
    }
    
    /**
     * 开始播放背景音乐
     */
    private void startBackgroundMusic() {
        // 播放游戏背景音乐
        audioManager.playBackgroundMusic("game_music");
    }
    
    /**
     * 根据游戏状态切换背景音乐
     */
    private void updateBackgroundMusic() {
        WaveManager waveManager = WaveManager.getInstance();
        
        // 最终波次播放紧张音乐
        if (waveManager.getCurrentWave() == waveManager.getTotalWaves()) {
            if (!audioManager.isBackgroundMusicPlaying() || 
                !isPlayingMusic("final_wave_music")) {
                audioManager.playBackgroundMusic("final_wave_music");
            }
        } else {
            // 普通游戏音乐
            if (!audioManager.isBackgroundMusicPlaying() || 
                !isPlayingMusic("game_music")) {
                audioManager.playBackgroundMusic("game_music");
            }
        }
    }

    /**
     * 检查是否正在播放指定音乐
     */
    private boolean isPlayingMusic(String musicName) {
        // 这里需要根据AudioManager的实现来判断
        // 简化实现：假设当前只有一个背景音乐在播放
        return audioManager.isBackgroundMusicPlaying();
    }
    
    private void initializeLawnMowers() {
        em.getElementsByKey(GameElement.LAWN_MOWERS).clear();
        for (int i = 0; i < GameConfig.GRID_ROWS; i++) {
            LawnMower mower = GameLoad.createLawnMower(i);
            if (mower != null) {
                em.addElement(mower, GameElement.LAWN_MOWERS);
                System.out.println("创建小推车在行: " + i);
            }
        }
    }

    private void checkGameState() {
        // 检查失败条件
        List<ElementObj> zombies = em.getElementsByKey(GameElement.ZOMBIES);
        for (ElementObj zombieObj : zombies) {
            Zombie zombie = (Zombie) zombieObj;
            if (zombie.getX() < GameConfig.LAWN_MOWER_START_X) {
                int zombieRow = (zombie.getY() - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
                boolean mowerActiveInRow = false;
                for (ElementObj mowerObj : em.getElementsByKey(GameElement.LAWN_MOWERS)) {
                    LawnMower mower = (LawnMower) mowerObj;
                    if (mower.getRowIndex() == zombieRow && mower.isActive()) {
                        mowerActiveInRow = true;
                        break;
                    }
                }

                if (!mowerActiveInRow) {
                    System.out.println("💀 游戏失败！僵尸吃掉了你的脑子！");
                    
                    // 播放失败音效和音乐
                    audioManager.playSound("game_over");
                    audioManager.playBackgroundMusic("defeat_music");
                    
                    gameRunning = false;
                    return;
                }
            }
        }

        // 检查胜利条件
        if (waveManager.isGameWon()) {
            System.out.println("🎉 恭喜！你成功保卫了花园！");
            
            // 播放胜利音乐
            audioManager.playBackgroundMusic("victory_music");
            
            gameRunning = false;
        }
    }

    /**
     * 种植植物 - 添加音效
     */
    public boolean plantPlant(String plantType, int gridX, int gridY) {
        if (shovelManager.isShovelActive()) {
            System.out.println("铲子模式激活中，无法种植植物。请先取消铲子模式。");
            return false;
        }
        
        if (!isGridEmpty(gridX, gridY)) {
            System.out.println("这里已经有植物了！");
            return false;
        }

        int cost = getPlantCost(plantType);
        if (!sunManager.hasEnoughSun(cost)) {
            return false;
        }

        if (isPlantOnCooldown(plantType)) {
            System.out.println(plantType + " 还在冷却中");
            return false;
        }

        ElementObj plant = GameLoad.createPlant(plantType, gridX, gridY);
        if (plant != null) {
            sunManager.spendSun(cost);
            em.addElement(plant, GameElement.PLANTS);
            setPlantCooldown(plantType);
            
            // 播放种植音效
            audioManager.playSound("plant_place");
            
            System.out.println("种植了 " + plantType + " 在 (" + gridX + "," + gridY + ")");
            return true;
        }

        return false;
    }

    /**
     * 收集阳光 - 添加音效
     */
    public boolean collectSun(int mouseX, int mouseY) {
        if (shovelManager.isShovelActive()) {
            return false;
        }
        
        List<ElementObj> suns = em.getElementsByKey(GameElement.SUNS);
        ElementObj clickedSun = CollisionDetector.detectMouseVsSun(suns, mouseX, mouseY);

        if (clickedSun != null && clickedSun instanceof Sun) {
            Sun sun = (Sun) clickedSun;
            if (!sun.isCollected()) {
                boolean success = sunManager.addSun(sun.getValue(), "收集阳光");
                
                if (success) {
                    sun.collect();
                    
                    // 播放阳光收集音效
                    audioManager.playSound("sun_collect");
                    
                    System.out.println("收集阳光成功！");
                    return true;
                } else {
                    System.out.println("⚠️ 阳光收集太快，请稍后再试！");
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isGridEmpty(int gridX, int gridY) {
        List<ElementObj> plants = em.getElementsByKey(GameElement.PLANTS);
        for (ElementObj obj : plants) {
            if (obj instanceof Plant) {
                Plant plant = (Plant) obj;
                if (plant.getGridX() == gridX && plant.getGridY() == gridY) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getPlantCost(String plantType) {
        switch (plantType) {
            case "peashooter": return GameConfig.PEASHOOTER_COST;
            case "sunflower": return GameConfig.SUNFLOWER_COST;
            case "wallnut": return GameConfig.WALLNUT_COST;
            default: return 999;
        }
    }

    private int getPlantCooldown(String plantType) {
        switch (plantType) {
            case "peashooter": return 150;
            case "sunflower": return 150;
            case "wallnut": return 300;
            default: return 150;
        }
    }

    /**
     * 游戏结束处理 - 清理音频资源
     */
    private void gameOver() {
        audioManager.cleanup();
    }

    public void togglePause() {
        gamePaused = !gamePaused;
        System.out.println(gamePaused ? "游戏已暂停" : "游戏继续");
    }

    /**
     * 重新开始游戏 - 完整版本
     */
    public void restartGame() {
        System.out.println("重新开始游戏...");
        
        // === 音频重置 ===
        if (audioManager != null) {
            audioManager.stopBackgroundMusic();
            System.out.println("🎵 停止当前背景音乐");
        }
        
        // === 管理器重置 ===
        // 重置阳光管理器
        sunManager.reset();
        
        // 重置波次管理器
        waveManager.reset();
        
        // 重置铲子管理器
        shovelManager.deactivateShovel();
        
        // === 游戏元素清理 ===
        // 清空所有游戏元素
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> elements = em.getElementsByKey(ge);
            if (elements != null) {
                elements.clear();
            }
        }
        
        // === 游戏状态重置 ===
        // 重置游戏时间
        gameTime = 0;
        
        // 重置游戏状态
        gamePaused = false;
        gameRunning = true;
        
        // 重置速度设置
        speedMultiplier = 1;
        GameConfig.currentSpeed = 1;
        
        // 重置时间管理
        lastUpdateTime = System.currentTimeMillis();
        
        // === 植物冷却重置 ===
        // 清空植物冷却记录
        plantCooldowns.clear();
        
        // === 重新初始化游戏元素 ===
        // 重新初始化小推车
        initializeLawnMowers();
        
        // === 音频重新开始 ===
        if (audioManager != null) {
            // 重新开始背景音乐
            startBackgroundMusic();
            System.out.println("🎵 重新开始背景音乐");
        }
        
        // === 调试信息 ===
        System.out.println("✅ 游戏重置完成");
        System.out.println("   - 阳光重置为: " + sunManager.getCurrentSun());
        System.out.println("   - 波次重置为: " + waveManager.getCurrentWave() + "/" + waveManager.getTotalWaves());
        System.out.println("   - 铲子状态: " + shovelManager.getCurrentState());
        System.out.println("   - 游戏速度: " + GameConfig.currentSpeed + "倍");
        System.out.println("   - 小推车数量: " + em.getElementsByKey(GameElement.LAWN_MOWERS).size());
        
        // === 可选：显示重新开始提示 ===
        if (audioManager != null) {
            // 播放游戏重新开始音效（如果有的话）
            audioManager.playSound("game_restart");
        }
        
        // === 确保界面更新 ===
        // 如果有游戏面板引用，可以触发重绘
        // gamePanel.repaint(); // 如果需要的话
    }
    
    public void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_S:
                shovelManager.toggleShovel();
                break;
            case java.awt.event.KeyEvent.VK_ESCAPE:
                shovelManager.deactivateShovel();
                break;
            case java.awt.event.KeyEvent.VK_M: // M键切换背景音乐
                audioManager.toggleMusic();
                break;
            case java.awt.event.KeyEvent.VK_N: // N键切换音效
                audioManager.toggleSfx();
                break;
            case java.awt.event.KeyEvent.VK_MINUS: // -键降低音量
                audioManager.setMusicVolume(audioManager.getMusicVolume() - 0.1f);
                break;
            case java.awt.event.KeyEvent.VK_PLUS: // +键提高音量
            case java.awt.event.KeyEvent.VK_EQUALS: // =键也可以提高音量
                audioManager.setMusicVolume(audioManager.getMusicVolume() + 0.1f);
            case java.awt.event.KeyEvent.VK_1:
                shovelManager.deactivateShovel();
                break;
            case java.awt.event.KeyEvent.VK_2:
                shovelManager.deactivateShovel();
                break;
            case java.awt.event.KeyEvent.VK_3:
                shovelManager.deactivateShovel();
                break;
        }
    }

    public boolean removePlant(int gridX, int gridY) {
        List<ElementObj> plants = em.getElementsByKey(GameElement.PLANTS);

        for (int i = plants.size() - 1; i >= 0; i--) {
            ElementObj obj = plants.get(i);
            if (obj instanceof Plant) {
                Plant plant = (Plant) obj;
                if (plant.getGridX() == gridX && plant.getGridY() == gridY) {
                    
                	// 播放铲除音效
                    audioManager.playSound("shovel_dig");
                    
                    int refundAmount = calculatePlantRefund(plant);
                    if (refundAmount > 0) {
                        String plantDisplayName = getPlantDisplayName(plant);
                        int sunBefore = sunManager.getCurrentSun();
                        sunManager.addSunSafely(refundAmount);
                        int sunAfter = sunManager.getCurrentSun();
                        
                        System.out.println("💰 铲除返还: " + plantDisplayName + " +" + refundAmount + " 阳光");
                        System.out.println("   阳光变化: " + sunBefore + " -> " + sunAfter + " (+" + (sunAfter - sunBefore) + ")");
                    } else {
                        System.out.println("💸 该植物无返还阳光");
                    }
                    
                    plant.setLive(false);
                    plants.remove(i);
                    createShovelEffect(gridX, gridY);
                    
                    System.out.println("🔧 铲除了植物: " + plant.getClass().getSimpleName() +
                                     " 在网格 (" + gridX + "," + gridY + ")");
                    return true;
                }
            }
        }
        
        System.out.println("⚠️ 网格 (" + gridX + "," + gridY + ") 没有植物可以铲除");
        return false;
    }

    private int calculatePlantRefund(Plant plant) {
        if (!GameConfig.ENABLE_PLANT_REFUND) {
            return 0;
        }
        
        String plantType = identifyPlantType(plant);
        int refundAmount = com.tedu.manager.PlantManager.calculateRefundAmount(plantType);
        
        System.out.println("🔍 返还计算调试:");
        System.out.println("  - 植物类: " + plant.getClass().getSimpleName());
        System.out.println("  - 识别类型: " + plantType);
        System.out.println("  - 原始成本: " + com.tedu.manager.PlantManager.getPlantCost(plantType));
        System.out.println("  - 返还金额: " + refundAmount);
        
        return refundAmount;
    }

    private String identifyPlantType(Plant plant) {
        String className = plant.getClass().getSimpleName().toLowerCase();
        
        if (className.contains("peashooter")) {
            return "peashooter";
        } else if (className.contains("sunflower")) {
            return "sunflower";
        } else if (className.contains("wallnut") || className.contains("nut")) {
            return "wallnut";
        }
        
        System.out.println("⚠️ 未能识别植物类型: " + className);
        return "unknown";
    }

    private String getPlantDisplayName(Plant plant) {
        String plantType = identifyPlantType(plant);
        return com.tedu.manager.PlantManager.getPlantDisplayName(plantType);
    }
    
    private void createShovelEffect(int gridX, int gridY) {
        try {
            int[] centerPos = GridHelper.gridToCenterPixel(gridX, gridY);
            int effectX = centerPos[0] - 15;
            int effectY = centerPos[1] - 15;
            
            ShovelDirtEffect effect = new ShovelDirtEffect(effectX, effectY, 30, 30);
            if (effect != null) {
                em.addElement(effect, GameElement.EFFECTS);
                System.out.println("🌱 创建铲除特效在 (" + effectX + "," + effectY + ")");
            }
        } catch (Exception e) {
            System.err.println("创建铲除特效失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter方法
    public int getCurrentSun() {
        return sunManager.getCurrentSun();
    }

    public int getCurrentWave() {
        return waveManager.getCurrentWave();
    }

    public int getTotalWaves() {
        return waveManager.getTotalWaves();
    }

    /**
     * 停止游戏 - 停止音频
     */
    public void stopGame() {
        gameRunning = false;
        audioManager.stopBackgroundMusic();
        System.out.println("游戏已停止");
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }
}