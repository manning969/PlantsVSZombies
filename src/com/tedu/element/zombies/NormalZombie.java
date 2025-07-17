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
 * 新增：dying状态下啃食时保持dying动画
 */
public class NormalZombie extends Zombie {
    private long lastAttackTime = 0;
    private long lastMoveTime = 0;
    private static final int ATTACK_INTERVAL = 100;
    protected int eatAnimationExtraWidth = -20; // 减小20像素宽度
    protected int eatAnimationExtraHeight = 0;  // 高度不变
    protected int eatAnimationXOffset = 10;     // 向右偏移10像素来居中
    protected int eatAnimationYOffset = 0;      // Y轴偏移
    
    // 记录原始尺寸（用于状态切换时恢复）
    protected int originalWidth = GameConfig.ZOMBIE_WIDTH;
    protected int originalHeight = GameConfig.ZOMBIE_HEIGHT;
    protected int originalX;
    protected int originalY;
    
    public NormalZombie() {
        super();
        this.dyingAnimationDuration = Long.MAX_VALUE; // 永不自动切换
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
        this.dyingAnimationDuration = Long.MAX_VALUE; // 永不自动切换
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION;
        
        // 设置死亡动画的额外宽度 - 比正常宽度多50像素
        this.dieAnimationExtraWidth = 70;
        // 可选：设置X轴偏移，让动画居中显示
        this.dieAnimationXOffset = -25; // 向左偏移25像素来居中
    
        this.eatAnimationExtraWidth = -30;  // 减小30像素宽度
        this.eatAnimationExtraHeight = 0;   // 高度不变
        this.eatAnimationXOffset = 15;      // 向右偏移15像素
        this.eatAnimationYOffset = 0;       // Y轴不偏移
    
        // 记录原始位置
        this.originalX = this.getX();
        this.originalY = this.getY();
    }

    @Override
    public ElementObj createElement(String str) {
        int rowIndex = Integer.parseInt(str.trim());
        return new NormalZombie(rowIndex);
    }
    
    /**
     * 获取血量百分比
     */
    public double getHealthPercentage() {
        return (double) hp / maxHp;
    }

    /**
     * 是否处于重伤状态（半血以下）
     */
    public boolean isSeverlyInjured() {
        return getHealthPercentage() <= 0.5 && hp > 0;
    }

    /**
     * 获取血量状态描述
     */
    public String getHealthStatus() {
        double percentage = getHealthPercentage();
        if (percentage <= 0) return "死亡";
        else if (percentage <= 0.25) return "濒死";
        else if (percentage <= 0.5) return "重伤";
        else if (percentage <= 0.75) return "轻伤";
        else return "健康";
    }
    
    /**
     * 创建独立的死亡动画图标 - 通过字节数组确保完全独立
     */
    @Override
    protected ImageIcon createIndependentDeathIcon() {
        try {
            // 方法1：通过读取字节数组创建完全独立的ImageIcon
            java.io.InputStream inputStream = null;
            byte[] imageBytes = null;
            
            // 尝试从文件系统读取
            String deathGifPath = "resources/images/zombies/normal/normal_die.gif";
            java.io.File gifFile = new java.io.File(deathGifPath);
            
            if (gifFile.exists()) {
                inputStream = new java.io.FileInputStream(gifFile);
            } else {
                // 从类路径读取
                inputStream = getClass().getClassLoader().getResourceAsStream("images/zombies/normal/normal_die.gif");
            }
            
            if (inputStream != null) {
                // 读取完整的字节数组
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                imageBytes = baos.toByteArray();
                inputStream.close();
                baos.close();
                
                // 基于字节数组创建ImageIcon，确保每个实例都是独立的
                ImageIcon independentIcon = new ImageIcon(imageBytes);
                System.out.println("✅ 普通僵尸：通过字节数组创建独立死亡动画图标，大小: " + imageBytes.length + " 字节");
                return independentIcon;
            }
            
        } catch (Exception e) {
            System.err.println("❌ 创建普通僵尸独立死亡动画图标失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 备用方案：尝试通过URL创建
        try {
            java.net.URL gifURL = getClass().getClassLoader().getResource("images/zombies/normal/normal_die.gif");
            if (gifURL != null) {
                // 通过URL读取字节数组
                java.io.InputStream urlStream = gifURL.openStream();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = urlStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = baos.toByteArray();
                urlStream.close();
                baos.close();
                
                ImageIcon independentIcon = new ImageIcon(imageBytes);
                System.out.println("✅ 普通僵尸：通过URL字节数组创建独立死亡动画图标");
                return independentIcon;
            }
        } catch (Exception e) {
            System.err.println("❌ 通过URL创建普通僵尸独立死亡动画图标失败: " + e.getMessage());
        }
        
        // 最后备用方案：返回缓存图标（可能仍有共享问题，但至少不会崩溃）
        System.out.println("⚠️ 普通僵尸使用缓存死亡动画图标作为最后备用");
        return GameLoad.imgMap.get("normal_die");
    }
    

    @Override
    protected void checkForPlants() {
        // *** 修正：只有真正死亡时才不检查植物，重伤状态下仍能检查 ***
        if (currentAnimationState == ZombieAnimationState.DIE) {
            return; // 只有死亡状态才不检查植物
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
                    
                    // *** 新增：根据状态显示不同信息 ***
                    String stateInfo = "";
                    if (currentAnimationState == ZombieAnimationState.DYING) {
                        stateInfo = "（重伤状态）";
                    }
                    System.out.println("🧟 普通僵尸开始啃食植物！行" + rowIndex + stateInfo);
                    break;
                }
            }
        }
    }

    /**
     * *** 修改：更新图像 - dying状态下啃食时保持dying动画 ***
     */
    @Override
    protected void updateImage() {
        ImageIcon newIcon = null;
        String iconKey = "";

        switch (currentAnimationState) {
            case WALK:
                iconKey = "normal_walk";
                newIcon = GameLoad.imgMap.get(iconKey);
                break;
            case EAT:
                iconKey = "normal_eat";
                newIcon = GameLoad.imgMap.get(iconKey);
                break;
            case DYING:
                // *** 新增：dying状态下始终使用dying动画，即使在啃食 ***
                iconKey = "normal_dying";
                newIcon = GameLoad.imgMap.get(iconKey);
                
                // 如果正在啃食，添加调试信息
                if (isEating) {
                    System.out.println("💔🍽️ 普通僵尸重伤状态下啃食，保持dying动画: " + iconKey);
                }
                break;
            case DIE:
                // 死亡状态下不在这里更新图标，由 handleDeathAnimation 处理
                return;
            default:
                iconKey = "normal_walk";
                newIcon = GameLoad.imgMap.get(iconKey);
                break;
        }

        if (newIcon != null) {
            this.setIcon(newIcon);
        } else {
            System.err.println("⚠️ " + this.getClass().getSimpleName() + " 无法设置图片: " + iconKey);
        }
    }

    @Override
    protected boolean canAttack(long gameTime) {
        // *** 修正：只有真正死亡时才不能攻击，重伤状态下仍能攻击 ***
        if (currentAnimationState == ZombieAnimationState.DIE) {
            return false; // 只有死亡状态才不能攻击
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
    protected String getZombieType() {
        return "normal";
    }
    
    @Override
    public void die() {
        super.die();
        System.out.println("💀 普通僵尸在行" + rowIndex + "被击败。");
    }

    @Override
    public String toString() {
        double healthPercentage = getHealthPercentage();
        String healthStatus = getHealthStatus();
        
        return "NormalZombie at (" + getX() + "," + getY() + ") row:" + rowIndex +
               " HP:" + hp + "/" + maxHp + " (" + String.format("%.1f%%", healthPercentage * 100) + ")" +
               " State:" + currentAnimationState + " HealthStatus:" + healthStatus +
               " Dying:" + isDying() + " HasHead:" + hasHead() + " DieWidth:" + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth);
    }
}