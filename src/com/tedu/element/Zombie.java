
package com.tedu.element;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.utils.GameConfig;
import com.tedu.manager.GameLoad;

/**
 * 僵尸基类 - 修复死亡动画处理，确保僵尸死亡后立即停止移动，添加移动间隔控制
 */
public abstract class Zombie extends ElementObj {
    protected int hp;           // 生命值
    protected int maxHp;        // 最大生命值
    protected int speed;        // 移动速度
    protected int damage;       // 攻击力
    protected int rowIndex;     // 所在行索引
    protected boolean isEating; // 是否正在啃食
    protected ElementObj target; // 当前攻击目标
    
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
    
    protected ImageIcon independentDeathIcon = null;
    protected boolean deathIconCreated = false;

    // 僵尸动画状态枚举
    public enum ZombieAnimationState {
        WALK,
        EAT,
        DIE
    }

    protected ZombieAnimationState currentAnimationState; // 当前动画状态
    protected long animationStateStartTime;               // 当前动画状态开始时间
    protected ZombieAnimationState previousState;        // 上一个状态，用于调试

    // 死亡动画持续时间（毫秒）
    protected static final long DEFAULT_DIE_ANIMATION_DURATION = 1500; // 1.5秒
    protected long dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION;

    // 死亡标记 - 用于更严格的状态控制
    private boolean isDying = false;

    public Zombie() {
        super();
        this.currentAnimationState = ZombieAnimationState.WALK;
        this.animationStateStartTime = System.currentTimeMillis();
        this.isDying = false;
        this.frameCounter = 0;
        this.lastMoveFrame = 0;
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
        // 增加帧计数器
        frameCounter++;
        
        // *** 核心修复：死亡状态下只处理动画，不执行任何其他逻辑 ***
        if (currentAnimationState == ZombieAnimationState.DIE) {
            handleDeathAnimation(gameTime);
            return; // 直接返回，不执行任何移动或攻击逻辑
        }

        // *** 额外保护：如果僵尸已标记为死亡，但状态还未切换，强制切换 ***
        if (isDying && currentAnimationState != ZombieAnimationState.DIE) {
            System.out.println("⚠️  强制切换僵尸到死亡状态: " + this.getClass().getSimpleName());
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
            return; // 兜底检查
        }

        // 检查前方是否有植物
        checkForPlants();

        // 攻击逻辑
        if (isEating && canAttack(gameTime)) {
            attackPlant();
        }

        // 移动逻辑 - 只有在不啃食时才移动，并且使用间隔控制
        if (!isEating) {
            // 检查是否到了移动时间
            if (frameCounter - lastMoveFrame >= GameConfig.ZOMBIE_MOVE_INTERVAL) {
                move();
                lastMoveFrame = frameCounter; // 更新上次移动的帧数
                
                // 调试信息 - 每移动10次输出一次
                if ((frameCounter / GameConfig.ZOMBIE_MOVE_INTERVAL) % 10 == 0) {
                    System.out.println("🚶 " + this.getClass().getSimpleName() + 
                                     " 移动 - 帧数: " + frameCounter + 
                                     ", 位置: (" + this.getX() + "," + this.getY() + ")" +
                                     ", 移动间隔: " + GameConfig.ZOMBIE_MOVE_INTERVAL);
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
        if (currentAnimationState == ZombieAnimationState.DIE || isDying) {
            return;
        }
        this.setX(this.getX() - speed);
    }

    /**
     * 受到伤害 - 改进的伤害处理
     */
    public void takeDamage(int damage) {
        if (isDying || currentAnimationState == ZombieAnimationState.DIE) {
            return; // 死亡状态下不再受伤
        }

        this.hp -= damage;
        System.out.println("🗡️  " + this.getClass().getSimpleName() + " 受到 " + damage + " 点伤害，剩余血量: " + this.hp);
        
        if (this.hp <= 0) {
            this.hp = 0;
            die(); // 血量归零时立即死亡
        }
    }

    /**
     * 开始啃食植物
     */
    public void startEating(ElementObj plant) {
        if (isDying || currentAnimationState == ZombieAnimationState.DIE) {
            return; // 死亡状态下不能啃食
        }

        if (!isEating) {
            this.isEating = true;
            this.target = plant;
            setAnimationState(ZombieAnimationState.EAT);
            System.out.println("🍽️  " + this.getClass().getSimpleName() + " 开始啃食植物！");
        }
    }

    /**
     * 停止啃食
     */
    public void stopEating() {
        if (isDying || currentAnimationState == ZombieAnimationState.DIE) {
            return; // 死亡状态下不需要停止啃食
        }

        if (isEating) {
            this.isEating = false;
            this.target = null;
            setAnimationState(ZombieAnimationState.WALK);
            System.out.println("🚶 " + this.getClass().getSimpleName() + " 停止啃食，继续行走！");
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
            plant.takeDamage(damage);

            if (!plant.isLive()) {
                stopEating();
            }
        }
    }

    /**
     * 僵尸死亡方法 - 改进的死亡处理
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
        
        setAnimationState(ZombieAnimationState.DIE);
        
        System.out.println("💀 " + this.getClass().getSimpleName() + " 在行" + rowIndex + " 开始死亡动画！");
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
               + " 状态:" + currentAnimationState + " 死亡标记:" + isDying + "]";
    }
}