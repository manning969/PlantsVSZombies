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
        super(GameConfig.GAME_WIDTH,
              GameConfig.GRID_START_Y + rowIndex * GameConfig.GRID_HEIGHT + GameConfig.ZOMBIE_OFFSET_Y,
              rowIndex,
              CONEHEAD_ZOMBIE_HP,
              GameConfig.ZOMBIE_SPEED,
              CONEHEAD_ZOMBIE_DAMAGE,
              GameLoad.imgMap.get("conehead_walk"));
        this.lastAttackTime = 0;
        this.lastMoveTime = 0;
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION + 200;
        
        // 设置死亡动画的额外宽度 - 路障僵尸死亡动画更宽一些
        this.dieAnimationExtraWidth = 50; // 比普通僵尸更宽
        // 可选：设置X轴偏移，让动画居中显示
        this.dieAnimationXOffset = -25; // 向左偏移25像素来居中
        
        System.out.println("✅ 路障僵尸创建，死亡动画宽度设置为: " + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth));
    }

    @Override
    public ElementObj createElement(String str) {
        int rowIndex = Integer.parseInt(str.trim());
        return new ConeheadZombie(rowIndex);
    }
    
    /**
     * 创建独立的死亡动画图标 - 通过字节数组确保完全独立
     */
    @Override
    protected ImageIcon createIndependentDeathIcon() {
        try {
            java.io.InputStream inputStream = null;
            byte[] imageBytes = null;
            
            // 注意：路障僵尸可能使用相同的死亡动画或不同的动画
            String deathGifPath = "resources/images/zombies/conehead/conehead_die.gif";
            java.io.File gifFile = new java.io.File(deathGifPath);
            
            if (gifFile.exists()) {
                inputStream = new java.io.FileInputStream(gifFile);
            } else {
                // 从类路径读取
                inputStream = getClass().getClassLoader().getResourceAsStream("images/zombies/conehead/conehead_die.gif");
            }
            
            // 如果路障僵尸没有专门的死亡动画，使用普通僵尸的
            if (inputStream == null) {
                System.out.println("⚠️ 路障僵尸死亡动画不存在，使用普通僵尸死亡动画");
                deathGifPath = "resources/images/zombies/normal/normal_die.gif";
                gifFile = new java.io.File(deathGifPath);
                if (gifFile.exists()) {
                    inputStream = new java.io.FileInputStream(gifFile);
                } else {
                    inputStream = getClass().getClassLoader().getResourceAsStream("images/zombies/normal/normal_die.gif");
                }
            }
            
            if (inputStream != null) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                imageBytes = baos.toByteArray();
                inputStream.close();
                baos.close();
                
                ImageIcon independentIcon = new ImageIcon(imageBytes);
                System.out.println("✅ 路障僵尸：通过字节数组创建独立死亡动画图标，大小: " + imageBytes.length + " 字节");
                return independentIcon;
            }
            
        } catch (Exception e) {
            System.err.println("❌ 创建路障僵尸独立死亡动画图标失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 备用方案
        try {
            java.net.URL gifURL = getClass().getClassLoader().getResource("images/zombies/conehead/conehead_die.gif");
            if (gifURL == null) {
                gifURL = getClass().getClassLoader().getResource("images/zombies/normal/normal_die.gif");
            }
            
            if (gifURL != null) {
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
                System.out.println("✅ 路障僵尸：通过URL字节数组创建独立死亡动画图标");
                return independentIcon;
            }
        } catch (Exception e) {
            System.err.println("❌ 通过URL创建路障僵尸独立死亡动画图标失败: " + e.getMessage());
        }
        
        // 最后备用方案
        System.out.println("⚠️ 路障僵尸使用缓存死亡动画图标作为最后备用");
        ImageIcon fallback = GameLoad.imgMap.get("conehead_die");
        if (fallback == null) {
            fallback = GameLoad.imgMap.get("normal_die");
        }
        return fallback;
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
        String iconKey = "";

        switch (currentAnimationState) {
            case WALK:
                iconKey = "normal_walk"; // 注意：这里可能需要改为 "conehead_walk"
                newIcon = GameLoad.imgMap.get(iconKey);
                break;
            case EAT:
                iconKey = "normal_eat"; // 注意：这里可能需要改为 "conehead_eat"
                newIcon = GameLoad.imgMap.get(iconKey);
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