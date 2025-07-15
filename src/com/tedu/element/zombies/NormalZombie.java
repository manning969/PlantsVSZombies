package com.tedu.element.zombies;

import com.tedu.element.ElementObj;
import com.tedu.element.Plant;
import com.tedu.element.Zombie;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * 普通僵尸类 - 修复死亡动画切换问题，增加死亡动画宽度调整
 */
public class NormalZombie extends Zombie {
    private long lastAttackTime = 0;
    private long lastMoveTime = 0;
    private static final int ATTACK_INTERVAL = 100;

    public NormalZombie() {
        super();
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION;
        // 设置死亡动画的额外宽度 - 比正常宽度多50像素
        this.dieAnimationExtraWidth = 70;
        // 可选：设置X轴偏移，让动画居中显示
        this.dieAnimationXOffset = -25; // 向左偏移25像素来居中
    }

    public NormalZombie(int rowIndex) {
        super(GameConfig.GAME_WIDTH,
              GameConfig.GRID_START_Y + rowIndex * GameConfig.GRID_HEIGHT + GameConfig.ZOMBIE_OFFSET_Y,
              rowIndex,
              GameConfig.NORMAL_ZOMBIE_HP,
              GameConfig.ZOMBIE_SPEED,
              GameConfig.NORMAL_ZOMBIE_DAMAGE,
              GameLoad.imgMap.get("normal_walk"));
        this.lastAttackTime = 0;
        this.lastMoveTime = 0;
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION;
        
        // 设置死亡动画的额外宽度 - 比正常宽度多50像素
        this.dieAnimationExtraWidth = 70;
        // 可选：设置X轴偏移，让动画居中显示
        this.dieAnimationXOffset = -25; // 向左偏移25像素来居中
        
        System.out.println("✅ 普通僵尸创建，死亡动画宽度设置为: " + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth));
    }

    @Override
    public ElementObj createElement(String str) {
        int rowIndex = Integer.parseInt(str.trim());
        return new NormalZombie(rowIndex);
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
                    System.out.println("🧟 普通僵尸开始啃食植物！行" + rowIndex);
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
                iconKey = "normal_walk";
                newIcon = GameLoad.imgMap.get(iconKey);
                logMessage = "🚶 切换到走路动画";
                break;
            case EAT:
                iconKey = "normal_eat";
                newIcon = GameLoad.imgMap.get(iconKey);
                logMessage = "🍽️ 切换到啃食动画";
                break;
            case DYING:
                iconKey = "normal_dying";
                newIcon = GameLoad.imgMap.get(iconKey);
                logMessage = "💥 切换到dying动画";
                break;
            case DIE:
                iconKey = "normal_die";
                newIcon = GameLoad.imgMap.get(iconKey);
                logMessage = "💀 切换到死亡动画 (宽度: " + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth) + ")";
                break;
            default:
                iconKey = "normal_walk";
                newIcon = GameLoad.imgMap.get(iconKey);
                logMessage = "❓ 未知状态，默认走路动画";
                break;
        }

        // 检查图片是否成功加载
        if (newIcon == null) {
            System.err.println("❌ 无法加载图片: " + iconKey + " (状态: " + currentAnimationState + ")");
            // 使用备用图片
            if (currentAnimationState == ZombieAnimationState.DIE) {
                newIcon = GameLoad.imgMap.get("normal_walk"); // 使用走路图片作为备用
                if (newIcon != null) {
                    System.out.println("🔄 使用备用图片: normal_walk 代替 normal_die");
                }
            }
        }

        // 只有当图片不同时才更新
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
        System.out.println("💀 普通僵尸在行" + rowIndex + "被击败。");
    }

    @Override
    public String toString() {
        return "NormalZombie at (" + getX() + "," + getY() + ") row:" + rowIndex +
               " HP:" + hp + "/" + maxHp + " State:" + currentAnimationState + 
               " Dying:" + isDying() + " DieWidth:" + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth);
    }
}