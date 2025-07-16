package com.tedu.element.zombies;

import com.tedu.element.ElementObj;
import com.tedu.element.Plant;
import com.tedu.element.Zombie;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;
import com.tedu.utils.ConfigLoader;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * 路障僵尸类 - 修复死亡动画切换问题，增加死亡动画宽度调整
 */
public class ConeheadZombie extends Zombie {
    private long lastAttackTime = 0;
    private long lastMoveTime = 0;
    private static final int ATTACK_INTERVAL = 100;

    private static final int CONEHEAD_ZOMBIE_HP = 700;
    private static final int CONEHEAD_ZOMBIE_DAMAGE = 15;

    public ConeheadZombie() {
        super();
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION + 200; // 路障僵尸死亡动画稍长
        // 设置死亡动画的额外宽度 - 路障僵尸死亡动画更宽一些
        this.dieAnimationExtraWidth = 70; // 比普通僵尸更宽
        // 可选：设置X轴偏移，让动画居中显示
        this.dieAnimationXOffset = -25; // 向左偏移35像素来居中
    }

    public ConeheadZombie(int rowIndex) {
        super(
            GameConfig.GAME_WIDTH,
            GameConfig.GRID_START_Y + rowIndex * GameConfig.GRID_HEIGHT + GameConfig.ZOMBIE_OFFSET_Y,
            rowIndex,
            ConfigLoader.getZombieInt("conehead.hp", 700),
            ConfigLoader.getZombieInt("conehead.speed", GameConfig.ZOMBIE_SPEED),
            ConfigLoader.getZombieInt("conehead.damage", 15),
            GameLoad.imgMap.getOrDefault(
                "conehead_walk",
                new ImageIcon(ConfigLoader.getZombieProperty("conehead.img_walk"))
            )
        );
        this.lastAttackTime = 0;
        this.lastMoveTime = 0;
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION + 200;
        this.dieAnimationExtraWidth = 70;
        this.dieAnimationXOffset = -25;
        System.out.println("✅ 路障僵尸创建，死亡动画宽度设置为: " + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth));
    }

    @Override
    public ElementObj createElement(String str) {
        int rowIndex = Integer.parseInt(str.trim());
        return new ConeheadZombie(rowIndex);
    }

    @Override
    protected void checkForPlants() {
        // 死亡状态下不检查植物
        if (currentAnimationState == ZombieAnimationState.DIE || isDying()) {
            return;
        }

        if (isEating) {
            if (target == null || !target.isLive()) {
                stopEating();
            }
            return;
        }

        ElementManager em = ElementManager.getManager();
        List<ElementObj> plants = em.getElementsByKey(GameElement.PLANTS);

        for (ElementObj obj : plants) {
            if (obj instanceof Plant) {
                Plant plant = (Plant) obj;
                if (plant.getGridY() == this.rowIndex &&
                    this.getX() + this.getW() - 10 >= plant.getX() &&
                    this.getX() < plant.getX() + plant.getW() - 10) {
                    startEating(plant);
                    System.out.println("🧟‍♂️ 路障僵尸开始啃食植物！行" + rowIndex);
                    break;
                }
            }
        }
    }

    @Override
    protected void updateImage() {
        ImageIcon newIcon = null;
        String logMessage = "";
        String iconKey = "";
        switch (currentAnimationState) {
            case WALK:
                iconKey = "conehead_walk";
                newIcon = GameLoad.imgMap.getOrDefault(
                    iconKey,
                    new ImageIcon(ConfigLoader.getZombieProperty("conehead.img_walk"))
                );
                logMessage = "🚶 切换到走路动画";
                break;
            case EAT:
                iconKey = "conehead_eat";
                newIcon = GameLoad.imgMap.getOrDefault(
                    iconKey,
                    new ImageIcon(ConfigLoader.getZombieProperty("conehead.img_eat"))
                );
                logMessage = "🍽️ 切换到吃动画";
                break;
            case DIE:
                iconKey = "conehead_die";
                newIcon = GameLoad.imgMap.getOrDefault(
                    iconKey,
                    new ImageIcon(ConfigLoader.getZombieProperty("conehead.img_die"))
                );
                logMessage = "💀 切换到死亡动画 (宽度: " + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth) + ")";
                break;
            default:
                iconKey = "conehead_walk";
                newIcon = GameLoad.imgMap.getOrDefault(
                    iconKey,
                    new ImageIcon(ConfigLoader.getZombieProperty("conehead.img_walk"))
                );
                logMessage = "❓ 未知状态，默认走路动画";
                break;
        }
        if (newIcon == null) {
            System.err.println("❌ 无法加载图片: " + iconKey + " (状态: " + currentAnimationState + ")");
            if (currentAnimationState == ZombieAnimationState.DIE) {
                newIcon = GameLoad.imgMap.getOrDefault(
                    "conehead_walk",
                    new ImageIcon(ConfigLoader.getZombieProperty("conehead.img_walk"))
                );
                if (newIcon != null) {
                    System.out.println("🔄 使用备用图片: conehead_walk 代替 conehead_die");
                }
            }
        }
        if (newIcon != null && this.getIcon() != newIcon) {
            this.setIcon(newIcon);
            System.out.println("🎭 " + this.getClass().getSimpleName() + " " + logMessage + " (图片: " + iconKey + ")");
        } else if (newIcon == null) {
            System.err.println("⚠️ " + this.getClass().getSimpleName() + " 无法设置图片: " + iconKey);
        }
    }

    @Override
    protected boolean canAttack(long gameTime) {
        // 死亡状态下不能攻击
        if (currentAnimationState == ZombieAnimationState.DIE || isDying()) {
            return false;
        }

        if (isEating) {
            if (gameTime - lastAttackTime >= ATTACK_INTERVAL) {
                lastAttackTime = gameTime;
                return true;
            }
        }
        return false;
    }

    @Override
    public void die() {
        super.die();
        System.out.println("💀 路障僵尸在行" + rowIndex + "被击败。");
    }

    @Override
    public String toString() {
        return "ConeheadZombie at (" + getX() + "," + getY() + ") row:" + rowIndex +
               " HP:" + hp + "/" + maxHp + " State:" + currentAnimationState + 
               " Dying:" + isDying() + " DieWidth:" + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth);
    }
}