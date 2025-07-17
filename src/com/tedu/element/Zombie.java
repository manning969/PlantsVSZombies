package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.utils.GameConfig;
import com.tedu.manager.AudioManager;
import com.tedu.manager.GameLoad;
import com.tedu.manager.ZombieFactory;

/**
 * 僵尸基类 - 修复死亡动画处理，确保僵尸死亡后立即停止移动，添加移动间隔控制
 * 新增头部状态管理和优化的状态转换逻辑
 */
public abstract class Zombie extends ElementObj {
    protected int hp;           // 生命值
    protected int maxHp;        // 最大生命值
    protected int speed;        // 移动速度
    protected int damage;       // 攻击力
    protected int rowIndex;     // 所在行索引
    protected boolean isEating; // 是否正在啃食
    protected ElementObj target; // 当前攻击目标
    protected AudioManager audioManager; //音效管理器
    
    private long lastEatingSoundTime = 0;
    private static final long EATING_SOUND_INTERVAL = 1000; // 1秒播放一次啃食音效
    
    // *** 新增：头部状态管理 ***
    protected boolean hasHead = true;        // 是否有头部
    protected boolean hasDroppedHead = false; // 是否已经掉过头
    
    // 移动控制相关
    protected long frameCounter = 0;        // 帧计数器
    protected long lastMoveFrame = 0;       // 上次移动的帧数
    
    // 新增：死亡动画的额外宽度（相对于正常宽度）
    protected int dieAnimationExtraWidth = 0;
    // 新增：死亡动画的额外高度（相对于正常高度）
    protected int dieAnimationExtraHeight = 0;
    // 新增：死亡动画时的X偏移（如果死亡动画需要居中或向左/右偏移）
    protected int dieAnimationXOffset = 0;
    // 新增：死亡动画时的Y偏移
    protected int dieAnimationYOffset = 0;
    
    // dying动画的额外宽度和偏移
    protected int dyingAnimationExtraWidth = 70;
    protected int dyingAnimationXOffset = -25;
    protected ImageIcon independentDeathIcon = null;
    protected boolean deathIconCreated = false;

    // 僵尸动画状态枚举
    public enum ZombieAnimationState {
        WALK,
        EAT,
        DYING,
        DIE
    }

    protected ZombieAnimationState currentAnimationState; // 当前动画状态
    protected long animationStateStartTime;               // 当前动画状态开始时间
    protected ZombieAnimationState previousState;        // 上一个状态，用于调试

    // 死亡动画持续时间（毫秒）
    protected static final long DEFAULT_DIE_ANIMATION_DURATION = 1500; // 1.5秒
    protected long dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION;

    // 新增dying动画持续时间（毫秒）
    protected static final long DEFAULT_DYING_ANIMATION_DURATION = 900; // 0.9秒
    protected long dyingAnimationDuration = DEFAULT_DYING_ANIMATION_DURATION;
    
    // 死亡标记 - 用于更严格的状态控制
    private boolean isDying = false;

    public Zombie() {
        super();
        this.currentAnimationState = ZombieAnimationState.WALK;
        this.animationStateStartTime = System.currentTimeMillis();
        this.isDying = false;
        this.frameCounter = 0;
        this.lastMoveFrame = 0;
        this.hasHead = true;
        this.hasDroppedHead = false;
        this.audioManager = AudioManager.getInstance();
    }

    public Zombie(int x, int y, int rowIndex, int hp, int speed, int damage, ImageIcon icon) {
        super(x, y, GameConfig.ZOMBIE_WIDTH, GameConfig.ZOMBIE_HEIGHT, icon);
        this.rowIndex = rowIndex;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
        this.damage = damage;
        this.isEating = false;
        this.target = null;
        this.currentAnimationState = ZombieAnimationState.WALK;
        this.animationStateStartTime = System.currentTimeMillis();
        this.isDying = false;
        this.frameCounter = 0;
        this.lastMoveFrame = 0;
        this.hasHead = true;
        this.hasDroppedHead = false;
        this.audioManager = AudioManager.getInstance();
    }

    @Override
    public void showElement(Graphics g) {
        if (this.getIcon() != null) {
            g.drawImage(this.getIcon().getImage(),
                       this.getX(), this.getY(),
                       this.getW(), this.getH(), null);
        }

        // 只在活着或死亡动画播放中时显示血条
        if (currentAnimationState != ZombieAnimationState.DIE) {
            drawHealthBar(g);
        }
    }

    /**
     * 绘制血条
     */
    private void drawHealthBar(Graphics g) {
        if (hp < maxHp && currentAnimationState != ZombieAnimationState.DIE) {
            int barWidth = 40;
            int barHeight = 4;
            int barX = this.getX() + (this.getW() - barWidth) / 2;
            int barY = this.getY() - 10;

            g.setColor(java.awt.Color.RED);
            g.fillRect(barX, barY, barWidth, barHeight);

            g.setColor(java.awt.Color.GREEN);
            int healthWidth = (int) ((double) hp / maxHp * barWidth);
            g.fillRect(barX, barY, healthWidth, barHeight);
        }
    }

	@Override
	public final void model(long gameTime) {
	    // 增加帧计数器 - 应用速度倍数
	    frameCounter += GameConfig.currentSpeed;
	
	    // *** DYING状态处理 - 重伤状态，僵尸继续移动但动画不同 ***
	    if (currentAnimationState == ZombieAnimationState.DYING) {
	        long timeInDyingState = System.currentTimeMillis() - animationStateStartTime;
	        
	        // dying动画只在刚切换时调整宽度和偏移
	        if (timeInDyingState < 200 && timeInDyingState >= 0) {
	            int targetWidth = GameConfig.ZOMBIE_WIDTH + dyingAnimationExtraWidth;
	            int targetHeight = GameConfig.ZOMBIE_HEIGHT;
	            if (this.getW() != targetWidth || this.getH() != targetHeight) {
	                int originalX = this.getX();
	                int originalY = this.getY();
	                this.setW(targetWidth);
	                this.setH(targetHeight);
	                this.setX(originalX + dyingAnimationXOffset);
	                System.out.println("💔 " + this.getClass().getSimpleName() + " 进入重伤状态，外观改变");
	            }
	        }
	        
	        // *** 重伤状态下继续正常游戏逻辑 ***
	        // 检查前方是否有植物
	        checkForPlants();
	
	        // 攻击逻辑
	        if (isEating && canAttack(gameTime)) {
	            attackPlant();
	        }
	
	        // 移动逻辑 - 重伤状态下也要移动，应用速度倍数
	        if (!isEating) {
	            long moveInterval = GameConfig.ZOMBIE_MOVE_INTERVAL / GameConfig.currentSpeed; // 倍速时移动更频繁
	            if (frameCounter - lastMoveFrame >= moveInterval) {
	                move();
	                lastMoveFrame = frameCounter;
	                
	                // 调试信息
	                if ((frameCounter / moveInterval) % 10 == 0) {
	                    System.out.println("💔 " + this.getClass().getSimpleName() + 
	                        " 重伤状态移动 - 帧数: " + frameCounter + 
	                        ", 位置: (" + this.getX() + "," + this.getY() + ")" + 
	                        ", 血量: " + hp + "/" + maxHp);
	                }
	            }
	        }
	        
	        updateImage();
	        return; // DYING状态下不检查死亡，只有takeDamage会触发die()
	    }
	
	    // *** DIE状态下只处理死亡动画 ***
	    if (currentAnimationState == ZombieAnimationState.DIE) {
	        handleDeathAnimation(gameTime);
	        return;
	    }
	
	    // *** 额外保护：如果僵尸已标记为死亡，强制切换到DIE状态 ***
	    if (isDying && currentAnimationState != ZombieAnimationState.DYING && currentAnimationState != ZombieAnimationState.DIE) {
	        System.out.println("⚠️ 强制切换僵尸到DIE状态: " + this.getClass().getSimpleName());
	        setAnimationState(ZombieAnimationState.DIE);
	        return;
	    }
	
	    // *** 血量检查：如果血量为0但还未标记死亡，立即处理 ***
	    if (hp <= 0 && !isDying) {
	        System.out.println("🩸 检测到血量为0，立即执行死亡: " + this.getClass().getSimpleName());
	        die();
	        return;
	    }
	
	    // 正常状态下的逻辑（WALK, EAT）
	    if (!this.isLive()) {
	        return;
	    }
	
	    // 检查前方是否有植物
	    checkForPlants();
	
	    // 攻击逻辑
	    if (isEating && canAttack(gameTime)) {
	        attackPlant();
	    }
	
	    // 移动逻辑 - 应用速度倍数
	    if (!isEating) {
	        long moveInterval = GameConfig.ZOMBIE_MOVE_INTERVAL / GameConfig.currentSpeed; // 倍速时移动更频繁
	        if (frameCounter - lastMoveFrame >= moveInterval) {
	            move();
	            lastMoveFrame = frameCounter;
	
	            // 调试信息
	            if ((frameCounter / moveInterval) % 10 == 0) {
	                System.out.println("🚶 " + this.getClass().getSimpleName() +
	                    " 移动 - 帧数: " + frameCounter +
	                    ", 位置: (" + this.getX() + "," + this.getY() + ")" +
	                    ", 血量: " + hp + "/" + maxHp);
	            }
	        }
	    }
	
	    // 更新动画图像
	    updateImage();
	}

    /**
     * 处理死亡动画逻辑
     */
    private void handleDeathAnimation(long gameTime) {
        long timeInDeathState = System.currentTimeMillis() - animationStateStartTime;
        
        // 确保死亡动画图标已创建且独立
        if (!deathIconCreated) {
            independentDeathIcon = createIndependentDeathIcon();
            if (independentDeathIcon != null) {
                this.setIcon(independentDeathIcon);
                deathIconCreated = true;
                System.out.println("🎬 " + this.getClass().getSimpleName() + " 创建独立死亡动画图标");
            }
        }
        
        // 在死亡动画开始的前几帧内调整尺寸（只调整一次）
        if (timeInDeathState < 200 && timeInDeathState >= 0) {
            int targetWidth = GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth;
            int targetHeight = GameConfig.ZOMBIE_HEIGHT + dieAnimationExtraHeight;
            
            if (this.getW() != targetWidth || this.getH() != targetHeight) {
                int originalX = this.getX();
                int originalY = this.getY();
                
                this.setW(targetWidth);
                this.setH(targetHeight);
                this.setX(originalX + dieAnimationXOffset);
                this.setY(originalY + dieAnimationYOffset);
                
                System.out.println("💀 " + this.getClass().getSimpleName() + 
                                 " 死亡动画尺寸已调整: " +
                                 "宽度 " + GameConfig.ZOMBIE_WIDTH + " -> " + targetWidth + 
                                 ", 高度 " + GameConfig.ZOMBIE_HEIGHT + " -> " + targetHeight);
            }
        }
        
        // 检查动画是否播放完毕
        if (timeInDeathState >= dieAnimationDuration) {
            if (timeInDeathState >= dieAnimationDuration + 100) {
                super.setLive(false);
                System.out.println("💀 " + this.getClass().getSimpleName() + " 死亡动画播放完毕，标记为不活跃");
            }
        }
    }
    
    /**
     * 创建独立的死亡动画图标 - 抽象方法，由子类实现
     */
    protected abstract ImageIcon createIndependentDeathIcon();

    @Override
    protected void move() {
        // *** 重要：移动前再次检查状态，防止死亡僵尸移动 ***
        if (currentAnimationState == ZombieAnimationState.DIE) {
            return;
        }
        this.setX(this.getX() - speed);
    }
    
    /**
     * 获取重伤阈值 - 子类可以重写以自定义阈值
     */
    public double getInjuryThreshold() {
        return 0.5; // 默认50%
    }

    /**
     * *** 修改：受到伤害 - 第一次进入dying状态时掉头 ***
     */
    public void takeDamage(int damage) {
        if (isDying || currentAnimationState == ZombieAnimationState.DIE) {
            return;
        }

        this.hp -= damage;
        System.out.println("🗡️ " + this.getClass().getSimpleName() + " 受到 " + damage + " 点伤害，剩余血量: " + this.hp + "/" + this.maxHp);
        
        if (this.hp <= 0) {
            this.hp = 0;
            die();
            return;
        }
        
        // *** 修改：检查是否需要进入dying状态 ***
        double healthPercentage = (double) this.hp / this.maxHp;
        if (healthPercentage <= getInjuryThreshold() && currentAnimationState == ZombieAnimationState.WALK) {
            System.out.println("💔 " + this.getClass().getSimpleName() + " 血量低于" + (getInjuryThreshold() * 100) + "%，进入重伤状态！(" + 
                             this.hp + "/" + this.maxHp + " = " + String.format("%.1f%%", healthPercentage * 100) + ")");
            
            // *** 新增：第一次进入dying状态时掉头（如果有头的话） ***
            if (hasHead && !hasDroppedHead) {
                dropHead();
                hasHead = false;
                hasDroppedHead = true;
                System.out.println("🪓 " + this.getClass().getSimpleName() + " 掉头了！");
            }
            
            setAnimationState(ZombieAnimationState.DYING);
        }
    }

    /**
     * *** 新增：掉头特效方法 ***
     */
    private void dropHead() {
        try {
            com.tedu.manager.ElementManager.getManager().addElement(
                new com.tedu.element.effects.HeadDropEffect(
                    this.getX(), this.getY(), this.getW(), this.getH()
                ),
                com.tedu.manager.GameElement.EFFECTS
            );
            System.out.println("🪓 " + this.getClass().getSimpleName() + " 掉头特效已添加");
        } catch (Exception e) {
            System.err.println("❌ 掉头特效添加失败: " + e.getMessage());
        }
    }

    /**
     * *** 修改：开始啃食植物 - dying状态下不切换动画 ***
     */
    public void startEating(ElementObj plant) {
        if (currentAnimationState == ZombieAnimationState.DIE) {
            return; // 只有真正死亡时才不能啃食
        }

        if (!isEating) {
            this.isEating = true;
            this.target = plant;
            
            // 🔊 播放啃食音效
            playEatingSound();
            
            // *** 新增：dying状态下不切换到EAT动画，保持DYING动画 ***
            if (currentAnimationState != ZombieAnimationState.DYING) {
                setAnimationState(ZombieAnimationState.EAT);
                System.out.println("🍽️ " + this.getClass().getSimpleName() + " 开始啃食植物！");
            } else {
                // dying状态下啃食，保持dying动画
                System.out.println("🍽️💔 " + this.getClass().getSimpleName() + " 重伤状态下开始啃食植物！（保持重伤动画）");
            }
        }
    }

    /**
     * *** 修改：停止啃食 - 根据血量和头部状态决定回到哪个状态 ***
     */
    public void stopEating() {
        if (currentAnimationState == ZombieAnimationState.DIE) {
            return; // 死亡状态下不需要停止啃食
        }

        if (isEating) {
            this.isEating = false;
            this.target = null;
            
            // 🔊 重置音效计时器
            this.lastEatingSoundTime = 0;
            
            // *** 根据血量决定状态 ***
            if (hp <= maxHp * getInjuryThreshold()) {
                setAnimationState(ZombieAnimationState.DYING);
                System.out.println("💔 " + this.getClass().getSimpleName() + " 停止啃食，回到重伤状态！");
            } else {
                setAnimationState(ZombieAnimationState.WALK);
                System.out.println("🚶 " + this.getClass().getSimpleName() + " 停止啃食，继续行走！");
            }
        }
    }


    /**
     * 攻击植物
     */
    protected void attackPlant() {
        if (isDying || currentAnimationState == ZombieAnimationState.DIE) {
            return; // 死亡状态下不能攻击
        }

        if (target != null && target instanceof Plant) {
            Plant plant = (Plant) target;
            
            // 🔊 播放啃食音效（带间隔控制）
            playEatingSound();
            
            plant.takeDamage(damage);

            if (!plant.isLive()) {
                stopEating();
            }
        }
    }
    
    private void playEatingSound() {
        long currentTime = System.currentTimeMillis();
        
        // 控制音效播放频率，避免过于频繁
        if (currentTime - lastEatingSoundTime > EATING_SOUND_INTERVAL) {
            if (audioManager != null) {
                audioManager.playSound("zombie_eating");
                lastEatingSoundTime = currentTime;
                System.out.println("🔊 " + this.getClass().getSimpleName() + " 播放啃食音效");
            } else {
                System.err.println("❌ AudioManager未初始化！");
            }
        }
    }
    
    /**
     * *** 修改：小推车击杀方法 - 根据头部状态决定是否掉头 ***
     */
    public void die(boolean skipDying) {
        if (isDying) {
            return; // 防止重复调用
        }
        this.isDying = true;
        this.isEating = false;
        this.target = null;
        
        // *** 小推车击杀逻辑：根据是否有头决定是否掉头 ***
        if (hasHead && !hasDroppedHead) {
            // 有头的情况：掉头 + 死亡
            dropHead();
            hasHead = false;
            hasDroppedHead = true;
            System.out.println("🚗💥 " + this.getClass().getSimpleName() + " 被小推车撞击，掉头并死亡！");
        } else {
            // 没头的情况：直接死亡
            System.out.println("🚗💥 " + this.getClass().getSimpleName() + " 被小推车撞击，直接死亡！（已无头）");
        }
        
        giveKillReward();
        
        if (skipDying) {
            setAnimationState(ZombieAnimationState.DIE);
            System.out.println("💀 " + this.getClass().getSimpleName() + " 跳过dying状态，直接进入die动画");
        } else {
            setAnimationState(ZombieAnimationState.DYING);
            System.out.println("💀 " + this.getClass().getSimpleName() + " 进入dying状态");
        }
    }
    
          
    /**
     * *** 修改：僵尸死亡方法 - 不再重复掉头 ***
     */
    @Override
    public void die() {
        if (isDying) {
            System.out.println("⚠️ " + this.getClass().getSimpleName() + " 重复调用die()方法，已忽略");
            return;
        }

        this.isDying = true;
        this.deathIconCreated = false; // 重置死亡图标创建标记
        this.independentDeathIcon = null; // 清空之前的死亡图标
        
        this.isEating = false;
        this.target = null;
        
        // *** 修改：如果还没掉过头且有头，在这里掉头 ***
        // （通常不会执行到这里，因为正常情况下takeDamage会先掉头再进入dying状态）
        if (hasHead && !hasDroppedHead) {
            dropHead();
            hasHead = false;
            hasDroppedHead = true;
            System.out.println("🪓 " + this.getClass().getSimpleName() + " 在最终死亡时掉头");
        }
        
        giveKillReward();
        
        setAnimationState(ZombieAnimationState.DIE);
        
        System.out.println("💀 " + this.getClass().getSimpleName() + " 在行" + rowIndex + " 开始死亡动画！");
    }
    
    /**
     * 给予击杀奖励
     */
    private void giveKillReward() {
        try {
            if (!GameConfig.ENABLE_ZOMBIE_KILL_REWARD) {
                return;
            }
            
            String zombieType = getZombieType();
            int rewardAmount = ZombieFactory.getZombieKillReward(zombieType);
            
            if (rewardAmount > 0) {
                com.tedu.manager.SunManager sunManager = com.tedu.manager.SunManager.getInstance();
                String zombieDisplayName = ZombieFactory.getZombieDisplayName(zombieType);
                
                // 记录奖励前的阳光（用于调试）
                int sunBefore = sunManager.getCurrentSun();
                
                // 使用addSunSafely避免冷却限制
                sunManager.addSunSafely(rewardAmount);
                
                // 记录奖励后的阳光（用于调试）
                int sunAfter = sunManager.getCurrentSun();
                
                System.out.println("🏆 击杀奖励: 消灭" + zombieDisplayName + " +" + rewardAmount + " 阳光");
                System.out.println("   阳光变化: " + sunBefore + " -> " + sunAfter + " (+" + (sunAfter - sunBefore) + ")");
                
                // 创建击杀奖励特效（可选）
                createKillRewardEffect(rewardAmount);
            }
            
        } catch (Exception e) {
            System.err.println("给予击杀奖励时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取僵尸类型 - 新增方法，子类可以重写
     */
    protected String getZombieType() {
        String className = this.getClass().getSimpleName().toLowerCase();
        
        if (className.contains("normal")) {
            return "normal";
        } else if (className.contains("conehead")) {
            return "conehead";
        } else if (className.contains("buckethead")) {
            return "buckethead";
        }
        
        // 默认根据血量判断（备用方案）
        if (maxHp <= 150) {
            return "normal";
        } else if (maxHp <= 400) {
            return "conehead";
        } else {
            return "buckethead";
        }
    }

    /**
     * 创建击杀奖励特效 - 新增方法
     */
    private void createKillRewardEffect(int rewardAmount) {
        try {
            System.out.println("⭐ === 击杀奖励特效 ===");
            System.out.println("💰    获得 +" + rewardAmount + " 阳光！    💰");
            System.out.println("📍  僵尸位置: (" + this.getX() + "," + this.getY() + ")");
            System.out.println("⭐ ==================");
            
            // 如果有特效系统，可以在这里添加视觉特效
            // KillRewardEffect effect = new KillRewardEffect(this.getX(), this.getY(), rewardAmount);
            // ElementManager.getManager().addElement(effect, GameElement.EFFECTS);
            
        } catch (Exception e) {
            System.err.println("创建击杀奖励特效失败: " + e.getMessage());
        }
    }


    /**
     * 设置僵尸的动画状态
     */
    protected void setAnimationState(ZombieAnimationState newState) {
        if (this.currentAnimationState != newState) {
            this.previousState = this.currentAnimationState;
            this.currentAnimationState = newState;
            this.animationStateStartTime = System.currentTimeMillis();
            
            System.out.println("🎭 " + this.getClass().getSimpleName() + " 状态变化: " 
                             + previousState + " -> " + newState + " (时间戳: " + animationStateStartTime + ")");
            
            // 立即更新图像
            updateImage();
            
            // 如果切换到死亡状态，输出额外信息
            if (newState == ZombieAnimationState.DIE) {
                System.out.println("🎬 开始播放死亡动画，当前图标: " + 
                                 (getIcon() != null ? "已设置" : "未设置"));
            }
        }
    }
    /**
     * 更新僵尸的动画图片 - 子类必须实现
     */
    @Override
    protected abstract void updateImage();

    /**
     * 检查是否可以攻击 - 子类实现
     */
    protected boolean canAttack(long gameTime) { return true; }

    /**
     * 检查前方是否有植物 - 子类实现
     */
    protected void checkForPlants() { }

    // *** 新增：头部状态相关的getter方法 ***
    public boolean hasHead() { return hasHead; }
    public boolean hasDroppedHead() { return hasDroppedHead; }
    public void setHasHead(boolean hasHead) { this.hasHead = hasHead; }

    // Getter方法
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getRowIndex() { return rowIndex; }
    public boolean isEating() { return isEating; }
    public int getDamage() { return damage; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getSpeed() { return speed; }
    public ZombieAnimationState getCurrentAnimationState() { return currentAnimationState; }
    public boolean isDying() { return isDying; }

    /**
     * 判断僵尸死亡动画是否播放完毕
     */
    public boolean isDyingAnimationFinished() {
        return currentAnimationState == ZombieAnimationState.DIE &&
               System.currentTimeMillis() - animationStateStartTime >= dieAnimationDuration;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [行:" + rowIndex + " 血量:" + hp + "/" + maxHp 
               + " 状态:" + currentAnimationState + " 死亡标记:" + isDying + " 有头:" + hasHead + "]";
    }
}