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
 * 新增：dying状态下啃食时保持dying动画
 */
public class ConeheadZombie extends Zombie {
    private long lastAttackTime = 0;
    private long lastMoveTime = 0;
    private static final int ATTACK_INTERVAL = 100;

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
              GameConfig.CONEHEAD_ZOMBIE_HP,
              GameConfig.ZOMBIE_SPEED,
              GameConfig.CONEHEAD_ZOMBIE_DAMAGE,
              GameLoad.imgMap.get("conehead_walk"));
        this.lastAttackTime = 0;
        this.setLastMoveTime(0);
        
        // *** 修改：DYING状态表示重伤，不需要自动切换持续时间 ***
        this.dyingAnimationDuration = Long.MAX_VALUE; // 永不自动切换，只有死亡才切换
        this.dieAnimationDuration = DEFAULT_DIE_ANIMATION_DURATION + 200; // 路障僵尸死亡动画稍长
        
        // 设置死亡动画的额外宽度 - 路障僵尸死亡动画更宽一些
        this.dieAnimationExtraWidth = 50;
        this.dieAnimationXOffset = -25;
        
        // *** 新增：设置重伤动画的宽度和偏移 ***
        this.dyingAnimationExtraWidth = 70; // 重伤状态的额外宽度
        this.dyingAnimationXOffset = -25;   // 重伤状态的X偏移
        
        System.out.println("✅ 路障僵尸创建 - 血量: " + GameConfig.CONEHEAD_ZOMBIE_HP + 
                         ", 半血阈值: " + (GameConfig.CONEHEAD_ZOMBIE_HP * 0.5) + 
                         ", 死亡动画宽度: " + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth));
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
        // *** 修改：只有真正死亡时才不检查植物，重伤状态下仍能检查 ***
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
                    System.out.println("🧟‍♂️ 路障僵尸开始啃食植物！行" + rowIndex + stateInfo);
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
                iconKey = "conehead_walk";
                newIcon = GameLoad.imgMap.get(iconKey);
                break;
            case EAT:
                iconKey = "conehead_eat";
                newIcon = GameLoad.imgMap.get(iconKey);
                break;
            case DYING:
                // *** 新增：dying状态下始终使用dying动画，即使在啃食 ***
                iconKey = "conehead_dying";
                newIcon = GameLoad.imgMap.get(iconKey);
                
                // 如果正在啃食，添加调试信息
                if (isEating) {
                    System.out.println("💔🍽️ 路障僵尸重伤状态下啃食，保持dying动画: " + iconKey);
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
            // *** 新增：备用图片逻辑 ***
            String fallbackKey = null;
            if (iconKey.equals("conehead_dying")) {
                fallbackKey = "conehead_walk"; // 如果没有重伤动画，使用行走动画
            }
            
            if (fallbackKey != null) {
                ImageIcon fallbackIcon = GameLoad.imgMap.get(fallbackKey);
                if (fallbackIcon != null) {
                    this.setIcon(fallbackIcon);
                    System.out.println("🔄 路障僵尸使用备用图片: " + fallbackKey + " 代替 " + iconKey);
                }
            }
        }
    }
  
    @Override
    protected boolean canAttack(long gameTime) {
        // *** 修改：只有真正死亡时才不能攻击，重伤状态下仍能攻击 ***
        if (currentAnimationState == ZombieAnimationState.DIE) {
            return false; // 只有死亡状态才不能攻击
        }

        if (isEating) {
            if (gameTime - lastAttackTime >= ATTACK_INTERVAL) {
                lastAttackTime = gameTime;
                
                // *** 新增：重伤状态下攻击频率稍慢 ***
                if (currentAnimationState == ZombieAnimationState.DYING) {
                    // 重伤状态下攻击间隔增加20%
                    if (gameTime - lastAttackTime >= ATTACK_INTERVAL * 1.2) {
                        return true;
                    }
                    return false;
                }
                
                return true;
            }
        }
        return false;
    }

    
    @Override
    protected String getZombieType() {
        return "conehead";
    }

    @Override
    public void die() {
        super.die();
        System.out.println("💀 路障僵尸在行" + rowIndex + "被击败。");
    }

    @Override
    public String toString() {
        double healthPercentage = (double) hp / maxHp;
        String healthStatus = "";
        
        if (healthPercentage <= 0) healthStatus = "死亡";
        else if (healthPercentage <= 0.25) healthStatus = "濒死";
        else if (healthPercentage <= 0.5) healthStatus = "重伤";
        else if (healthPercentage <= 0.75) healthStatus = "轻伤";
        else healthStatus = "健康";
        
        return "ConeheadZombie at (" + getX() + "," + getY() + ") row:" + rowIndex +
               " HP:" + hp + "/" + maxHp + " (" + String.format("%.1f%%", healthPercentage * 100) + ")" +
               " State:" + currentAnimationState + " HealthStatus:" + healthStatus +
               " Dying:" + isDying() + " HasHead:" + hasHead() + " DieWidth:" + (GameConfig.ZOMBIE_WIDTH + dieAnimationExtraWidth);
    }

	public long getLastMoveTime() {
		return lastMoveTime;
	}

	public void setLastMoveTime(long lastMoveTime) {
		this.lastMoveTime = lastMoveTime;
	}
	
	/**
	 * 获取路障僵尸的重伤阈值
	 * 路障僵尸血量较高，可以设置不同的重伤阈值
	 */
	public double getInjuryThreshold() {
	    // 路障僵尸可能有更高的容错率，比如40%血量才进入重伤状态
	    return 0.4; // 或者使用 0.5 与普通僵尸保持一致
	}

	/**
	 * 检查是否应该进入重伤状态
	 */
	public boolean shouldEnterDyingState() {
	    return (double) hp / maxHp <= getInjuryThreshold() && hp > 0;
	}
	
	/**
	 * *** 修改：受到伤害 - 增强调试版本，移除了自定义逻辑，使用父类的统一逻辑 ***
	 */
	@Override
	public void takeDamage(int damage) {
	    System.out.println("\n=== 路障僵尸takeDamage调试开始 ===");
	    System.out.println("🔧 方法被调用: ConeheadZombie.takeDamage(" + damage + ")");
	    System.out.println("🔧 当前状态: " + currentAnimationState);
	    System.out.println("🔧 当前血量: " + hp + "/" + maxHp);
	    System.out.println("🔧 isDying(): " + isDying());
	    System.out.println("🔧 重伤阈值: " + getInjuryThreshold());
	    
	    // *** 新增：调用父类的takeDamage方法，使用统一的逻辑 ***
	    super.takeDamage(damage);
	    
	    System.out.println("🔧 处理后状态: " + currentAnimationState);
	    System.out.println("🔧 处理后血量: " + hp + "/" + maxHp);
	    System.out.println("=== 路障僵尸takeDamage调试结束 ===\n");
	}
}