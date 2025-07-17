package com.tedu.controller;

import java.util.List;
import java.util.Map;
import com.tedu.element.ElementObj;
import com.tedu.element.Plant;
import com.tedu.element.Zombie;
import com.tedu.element.items.Sun;
import com.tedu.element.items.LawnMower;
import com.tedu.element.effects.ShovelDirtEffect;
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
 * 植物大战僵尸游戏主线程 - 集成管理器版本 (新增完整铲子功能)
 */
public class GameThread extends Thread {
    private ElementManager em;
    private SunManager sunManager;      // 阳光管理器
    private WaveManager waveManager;    // 波次管理器
    private ShovelManager shovelManager; // 铲子管理器

    private boolean gameRunning = true;
    private boolean gamePaused = false; // 游戏暂停状态
    private long gameTime = 0;
    private boolean running = true;
    private static final long FRAME_PERIOD = 16; // 约60FPS (1000/60 ≈ 16ms)
    private int speedMultiplier = 1;
    
    // 使用 long 类型记录时间
    private long lastUpdateTime = System.currentTimeMillis();
    
    // 植物冷却管理
    private Map<String, Long> plantCooldowns;

    public GameThread() {
        em = ElementManager.getManager();
        sunManager = SunManager.getInstance();
        waveManager = WaveManager.getInstance();
        shovelManager = ShovelManager.getInstance(); // 初始化铲子管理器
        plantCooldowns = new java.util.HashMap<>();
    }

    @Override
    public void run() {
        // *** 核心修改：将 gameLoad() 移到循环外部，只执行一次 ***
        gameLoad(); // 在线程启动时只加载一次资源

        // 游戏主循环
        gameRunLoop(); // 调用实际的游戏运行循环

        // 游戏结束后的清理
        gameOver();
        while (running) {
            long startTime = System.currentTimeMillis();
            
            // 计算时间增量 (毫秒)
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;
            
            // 应用速度因子 (基于速度倍数)
            long scaledDeltaTime = deltaTime * speedMultiplier; // 使用类字段
            
            // 游戏更新
            gameUpdate(scaledDeltaTime);
            
            // 控制帧率
            long frameTime = System.currentTimeMillis() - startTime;
            if (frameTime < FRAME_PERIOD) {
                try {
                    Thread.sleep(FRAME_PERIOD - frameTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void gameUpdate(long deltaTime) {
        // 所有需要时间控制的更新都使用 long 类型的 deltaTime 参数
        ElementManager em = ElementManager.getInstance();
        em.update(deltaTime);
        
        // 更新管理器
        SunManager.getInstance().update(deltaTime);
        WaveManager.getInstance().update(deltaTime);
        // ... 其他管理器更新
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }
    /**
     * 游戏资源加载
     */
    private void gameLoad() {
        System.out.println("开始加载植物大战僵尸资源...");

        GameLoad.loadImg();
        GameLoad.loadLawn();
        GameLoad.initializeGame(); // 这会加载obj.pro并初始化ElementManager
        initializeLawnMowers(); // 在游戏加载时创建小推车

        System.out.println("资源加载完成！");
    }

    /**
     * 初始化所有行的小推车
     */
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

    /**
     * 游戏主循环 - 协调各个管理器
     */
    private void gameRunLoop() { // 重命名为 gameRunLoop，避免与 run() 方法混淆
        System.out.println("游戏开始运行...");

        while (gameRunning) {
            try {
                if (gamePaused) {
                    Thread.sleep(100);
                    continue;
                }

                // *** 核心：所有元素逻辑更新、碰撞检测和清理都集中在 ElementManager 中处理 ***
                em.gameLogicAndCollisionDetection(System.currentTimeMillis());
                
                // 更新波次管理器
                waveManager.update(gameTime);

                // 更新阳光管理器
                sunManager.update();

                // 检查游戏胜负条件
                checkGameState();

                gameTime++;
                Thread.sleep(10); // 约100FPS

            } catch (InterruptedException e) {
                e.printStackTrace();
                gameRunning = false;
            }
        }
    }

    /**
     * 检查游戏状态 (保持不变)
     */
    private void checkGameState() {
        // 检查失败条件
        List<ElementObj> zombies = em.getElementsByKey(GameElement.ZOMBIES);
        for (ElementObj zombieObj : zombies) {
            Zombie zombie = (Zombie) zombieObj; // Cast to Zombie
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
                    gameRunning = false;
                    return;
                }
            }
        }

        // 检查胜利条件 - 使用波次管理器
        if (waveManager.isGameWon()) {
            System.out.println("🎉 恭喜！你成功保卫了花园！");
            gameRunning = false;
        }
    }

    /**
     * 游戏元素自动化更新方法
     */ 
    @Deprecated
    public void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long gameTime) {
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> list = all.get(ge); // 获取原始列表
            // 使用迭代器安全移除元素，防止ConcurrentModificationException
            java.util.Iterator<ElementObj> iterator = list.iterator(); // 创建迭代器
            while (iterator.hasNext()) {
                ElementObj obj = iterator.next();
                if (!obj.isLive()) {
                    obj.die(); // 执行死亡方法
                    iterator.remove(); // 安全移除
                } else {
                    obj.model(gameTime); // 调用模板方法
                }
            }
        }
    }
    
    /**
     * 种植植物 - 使用阳光管理器
     */
    public boolean plantPlant(String plantType, int gridX, int gridY) {
        // 如果铲子模式激活，不允许种植
        if (shovelManager.isShovelActive()) {
            System.out.println("铲子模式激活中，无法种植植物。请先取消铲子模式。");
            return false;
        }
        
        // 检查网格是否为空
        if (!isGridEmpty(gridX, gridY)) {
            System.out.println("这里已经有植物了！");
            return false;
        }

        // 获取植物花费
        int cost = getPlantCost(plantType);

        // 检查阳光是否足够 - 使用阳光管理器
        if (!sunManager.hasEnoughSun(cost)) {
            return false;
        }

        // 检查植物是否在冷却中
        if (isPlantOnCooldown(plantType)) {
            System.out.println(plantType + " 还在冷却中");
            return false;
        }

        // 创建植物
        ElementObj plant = GameLoad.createPlant(plantType, gridX, gridY);
        if (plant != null) {
            // 消费阳光 - 使用阳光管理器
            sunManager.spendSun(cost);

            // 添加植物到游戏中
            em.addElement(plant, GameElement.PLANTS);

            // 设置植物冷却
            setPlantCooldown(plantType);

            System.out.println("种植了 " + plantType + " 在 (" + gridX + "," + gridY + ")");
            return true;
        }

        return false;
    }

    /**
     * 收集阳光 - 使用CollisionDetector，返回是否成功收集
     */
    public boolean collectSun(int mouseX, int mouseY) {
        // 如果铲子模式激活，不收集阳光
        if (shovelManager.isShovelActive()) {
            return false;
        }
        
        List<ElementObj> suns = em.getElementsByKey(GameElement.SUNS);

        // 使用CollisionDetector检测鼠标点击
        ElementObj clickedSun = CollisionDetector.detectMouseVsSun(suns, mouseX, mouseY);

        if (clickedSun != null && clickedSun instanceof Sun) {
            Sun sun = (Sun) clickedSun;
            if (!sun.isCollected()) {
                // 使用带来源信息的addSun方法
                boolean success = sunManager.addSun(sun.getValue(), "收集阳光");
                
                if (success) {
                    sun.collect();
                    System.out.println("收集阳光成功！");
                    return true;
                } else {
                    // 收集失败，通常是因为冷却时间
                    System.out.println("⚠️ 阳光收集太快，请稍后再试！");
                    return false;
                }
            }
        }
        return false;
    }
    /**
     * 检查网格是否为空
     */
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

    /**
     * 获取植物花费
     */
    private int getPlantCost(String plantType) {
        switch (plantType) {
            case "peashooter": return GameConfig.PEASHOOTER_COST;
            case "sunflower": return GameConfig.SUNFLOWER_COST;
            case "wallnut": return GameConfig.WALLNUT_COST;
            default: return 999;
        }
    }

    /**
     * 植物冷却管理
     */
    private void setPlantCooldown(String plantType) {
        int cooldown = getPlantCooldown(plantType);
        plantCooldowns.put(plantType, gameTime + cooldown);
    }

    private boolean isPlantOnCooldown(String plantType) {
        Long cooldownEndTime = plantCooldowns.get(plantType);
        return cooldownEndTime != null && gameTime < cooldownEndTime;
    }

    private int getPlantCooldown(String plantType) {
        switch (plantType) {
            case "peashooter": return 150; // 1.5秒冷却
            case "sunflower": return 150; //
            case "wallnut": return 300;   // 3秒冷却
            default: return 150; //
        }
    }

    /**
     * 游戏结束处理
     */
    private void gameOver() {
        // 资源清理等
    }

    /**
     * 暂停/继续游戏
     */
    public void togglePause() {
        gamePaused = !gamePaused;
        System.out.println(gamePaused ? "游戏已暂停" : "游戏继续");
    }

    /**
     * 重新开始游戏
     */
    public void restartGame() {
        System.out.println("重新开始游戏...");
        // 重置所有管理器
        sunManager.reset();
        waveManager.reset();
        shovelManager.deactivateShovel(); // 重置铲子状态

        // 清空所有游戏元素
        for (GameElement ge : GameElement.values()) {
            em.getElementsByKey(ge).clear();
        }
        // 重新初始化小推车
        initializeLawnMowers();

        // 重置游戏状态
        gameTime = 0;
        gamePaused = false;
        plantCooldowns.clear();

        System.out.println("游戏重置完成");
    }

    
    /**
     * 处理键盘事件 - 添加铲子快捷键
     */
    public void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_S: // S键激活/取消铲子
                shovelManager.toggleShovel();
                break;
            case java.awt.event.KeyEvent.VK_ESCAPE: // ESC键取消铲子模式
                shovelManager.deactivateShovel();
                break;
            case java.awt.event.KeyEvent.VK_1: // 数字键种植植物
                // 取消铲子模式，切换到种植模式
                shovelManager.deactivateShovel();
                // 设置当前选择的植物为豌豆射手
                break;
            case java.awt.event.KeyEvent.VK_2:
                shovelManager.deactivateShovel();
                // 设置当前选择的植物为向日葵
                break;
            case java.awt.event.KeyEvent.VK_3:
                shovelManager.deactivateShovel();
                // 设置当前选择的植物为坚果墙
                break;
        }
    }

    /**
     * 增强的铲除植物方法 - 支持铲子模式和阳光返还
     */
    public boolean removePlant(int gridX, int gridY) {
        List<ElementObj> plants = em.getElementsByKey(GameElement.PLANTS);

        for (int i = plants.size() - 1; i >= 0; i--) {
            ElementObj obj = plants.get(i);
            if (obj instanceof Plant) {
                Plant plant = (Plant) obj;
                if (plant.getGridX() == gridX && plant.getGridY() == gridY) {
                    
                    // === 新增：计算和返还阳光 ===
                    int refundAmount = calculatePlantRefund(plant);
                    if (refundAmount > 0) {
                        String plantDisplayName = getPlantDisplayName(plant);
                        
                        // 记录返还前的阳光数量（用于调试）
                        int sunBefore = sunManager.getCurrentSun();
                        
                        // 返还阳光
                        sunManager.addSunSafely(refundAmount);
                        
                        // 记录返还后的阳光数量（用于调试）
                        int sunAfter = sunManager.getCurrentSun();
                        
                        System.out.println("💰 铲除返还: " + plantDisplayName + " +" + refundAmount + " 阳光");
                        System.out.println("   阳光变化: " + sunBefore + " -> " + sunAfter + " (+" + (sunAfter - sunBefore) + ")");
                    } else {
                        System.out.println("💸 该植物无返还阳光");
                    }
                    // === 返还逻辑结束 ===
                    
                    // 移除植物
                    plant.setLive(false);
                    plants.remove(i);
                    
                    // 播放铲除音效（如果有的话）
                    // SoundManager.playSound("shovel_dig");
                    
                    // 显示铲除特效
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

    /**
     * 计算植物铲除返还的阳光数量
     * 保持封装性：将逻辑委托给专门的管理器
     */
    private int calculatePlantRefund(Plant plant) {
        if (!GameConfig.ENABLE_PLANT_REFUND) {
            return 0;
        }
        
        String plantType = identifyPlantType(plant);
        
        // 使用现有的PlantManager来计算返还金额（保持封装）
        int refundAmount = com.tedu.manager.PlantManager.calculateRefundAmount(plantType);
        
        System.out.println("🔍 返还计算调试:");
        System.out.println("  - 植物类: " + plant.getClass().getSimpleName());
        System.out.println("  - 识别类型: " + plantType);
        System.out.println("  - 原始成本: " + com.tedu.manager.PlantManager.getPlantCost(plantType));
        System.out.println("  - 返还金额: " + refundAmount);
        
        return refundAmount;
    }

    /**
     * 根据植物对象识别植物类型
     * 保持封装性：只负责类型识别，不处理业务逻辑
     */
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

    /**
     * 获取植物的显示名称
     * 保持封装性：委托给PlantManager
     */
    private String getPlantDisplayName(Plant plant) {
        String plantType = identifyPlantType(plant);
        return com.tedu.manager.PlantManager.getPlantDisplayName(plantType);
    }
    
    /**
     * 创建铲除特效 - 修复版本
     */
    private void createShovelEffect(int gridX, int gridY) {
        try {
            // 计算特效位置
            int[] centerPos = GridHelper.gridToCenterPixel(gridX, gridY);
            int effectX = centerPos[0] - 15; // 特效宽度的一半
            int effectY = centerPos[1] - 15; // 特效高度的一半
            
            // 直接创建土块飞溅特效
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

    // Getter方法 - 通过管理器获取
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
     * 停止游戏
     */
    public void stopGame() {
        gameRunning = false;
        System.out.println("游戏已停止");
    }
 // 新增: 设置游戏速度方法
    public void setGameSpeed(int multiplier) {
        this.speedMultiplier = Math.max(1, Math.min(4, multiplier));
        System.out.println("游戏速度设置为: " + speedMultiplier + "倍");
    }
}