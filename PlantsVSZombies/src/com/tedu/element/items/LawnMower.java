package com.tedu.element.items;

import java.awt.Graphics;
import javax.swing.ImageIcon;
import com.tedu.element.ElementObj;
import com.tedu.element.Zombie;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameElement;
import com.tedu.manager.ElementManager;
import com.tedu.utils.CollisionDetector;
import com.tedu.utils.GameConfig;
import java.util.List;
import java.util.ArrayList; // 新增导入
import java.util.Random; // 新增导入 Random
import java.awt.Rectangle; // 新增导入 Rectangle

/**
 * 小推车（Lawn Mower）类
 * 在每行最左侧，当僵尸碰到时会被激活并向前移动，清除路径上的所有僵尸。
 */
public class LawnMower extends ElementObj {

    private boolean isActive;     // 小推车是否已激活并正在移动
    private int moveSpeed;        // 小推车的移动速度
    private boolean hasFinished;  // 小推车是否已完成任务（达到屏幕边缘）
    private int rowIndex;         // 小推车所在的行索引 (0-4)

    private boolean currentIconIsActive = false; // 用于判断当前显示的GIF是否是激活状态

    // 抖动效果相关
    private Random random = new Random();
    private int shakeXOffset = 0;
    private int shakeYOffset = 0;
    private int shakeMagnitude = 2; // 抖动强度（像素）
    private long lastShakeTime = 0;
    private static final int SHAKE_INTERVAL = 50; // 抖动更新间隔（毫秒）

    public LawnMower() {
        super();
        this.isActive = false;
        this.hasFinished = false;
        this.moveSpeed = GameConfig.LAWN_MOWER_SPEED;
        this.rowIndex = -1;
        ImageIcon idleIcon = GameLoad.imgMap.get("lawn_mower_idle");
        if (idleIcon == null) {
            System.err.println("错误：未找到 lawn_mower_idle 图片！");
        }
        this.setIcon(idleIcon);
        this.currentIconIsActive = false;
        setW(GameConfig.LAWN_MOWER_WIDTH);
        setH(GameConfig.LAWN_MOWER_HEIGHT);
    }

    public LawnMower(int x, int y, int rowIndex) {
        super(x, y, GameConfig.LAWN_MOWER_WIDTH, GameConfig.LAWN_MOWER_HEIGHT,
              GameLoad.imgMap.get("lawn_mower_idle"));
        this.isActive = false;
        this.hasFinished = false;
        this.moveSpeed = GameConfig.LAWN_MOWER_SPEED;
        this.rowIndex = rowIndex;
        this.currentIconIsActive = false;
    }


    @Override
    public ElementObj createElement(String str) {
        String[] parts = str.split(",");
        if (parts.length == 1) {
            int rowIndex = Integer.parseInt(parts[0]);
            int x = GameConfig.LAWN_MOWER_START_X;
            int y = GameConfig.GRID_START_Y + rowIndex * GameConfig.GRID_HEIGHT +
                     (GameConfig.GRID_HEIGHT - GameConfig.LAWN_MOWER_HEIGHT) / 2;
            return new LawnMower(x, y, rowIndex);
        } else if (parts.length == 3) {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int rowIndex = Integer.parseInt(parts[2]);
            return new LawnMower(x, y, rowIndex);
        }
        return null;
    }

    @Override
    public void showElement(Graphics g) {
        if (this.isLive()) {
            ImageIcon currentIconToDraw = this.getIcon();

            if (isActive) {
                if (!currentIconIsActive) {
                    ImageIcon activeIcon = GameLoad.imgMap.get("lawn_mower_active");
                    if (activeIcon != null) {
                        this.setIcon(activeIcon);
                        currentIconIsActive = true;
                        currentIconToDraw = activeIcon;
                    } else {
                        System.err.println("警告: 未找到 lawn_mower_active 图片，小推车动画可能不正确。");
                    }
                }
            } else {
                if (currentIconIsActive) {
                    ImageIcon idleIcon = GameLoad.imgMap.get("lawn_mower_idle");
                    if (idleIcon != null) {
                        this.setIcon(idleIcon);
                        currentIconIsActive = false;
                        currentIconToDraw = idleIcon;
                    } else {
                        System.err.println("警告: 未找到 lawn_mower_idle 图片，小推车动画可能不正确。");
                    }
                } else if (currentIconToDraw == null) { // 首次加载时确保有图标
                    ImageIcon idleIcon = GameLoad.imgMap.get("lawn_mower_idle");
                    if (idleIcon != null) {
                        this.setIcon(idleIcon);
                        currentIconToDraw = idleIcon;
                    } else {
                        System.err.println("错误：首次绘制时未找到 lawn_mower_idle 图片！");
                    }
                }
            }

            if (currentIconToDraw != null) {
                if (isActive) {
                    g.drawImage(currentIconToDraw.getImage(),
                                getX() + shakeXOffset, getY() + shakeYOffset,
                                getW(), getH(), null);
                } else {
                    g.drawImage(currentIconToDraw.getImage(), getX(), getY(), getW(), getH(), null);
                }
            } else {
                System.err.println("错误: 小推车图片为 null，无法绘制！");
            }
        }
    }


    @Override
    public final void model(long gameTime) {
        if (!this.isLive() || hasFinished) {
            return;
        }

        if (isActive) {
            if (System.currentTimeMillis() - lastShakeTime >= SHAKE_INTERVAL) {
                shakeXOffset = random.nextInt(shakeMagnitude * 2 + 1) - shakeMagnitude;
                shakeYOffset = random.nextInt(shakeMagnitude * 2 + 1) - shakeMagnitude;
                lastShakeTime = System.currentTimeMillis();
            }

            reportMowedArea();

            clearZombiesInPath();
            move();

            if (getX() >= GameConfig.GAME_WIDTH) {
                hasFinished = true;
                this.setLive(false);
                System.out.println("小推车在行 " + rowIndex + " 完成任务并离开");
            }
        }
    }


    @Override
    protected void move() {
        setX(getX() + moveSpeed);
    }

    public void activate() {
        if (!isActive) {
            this.isActive = true;
            System.out.println("小推车在行 " + rowIndex + " 被激活！");
        }
    }

    /**
     * 清除路径上的所有僵尸
     */
    private void clearZombiesInPath() {
        ElementManager em = ElementManager.getManager();
        List<ElementObj> currentZombies = new ArrayList<>(em.getElementsByKey(GameElement.ZOMBIES));

        for (ElementObj obj : currentZombies) {
            if (obj instanceof Zombie) {
                Zombie zombie = (Zombie) obj;
                // 只有活着的且不在死亡动画状态的僵尸才会被碾压
                if (zombie.isLive() && zombie.getCurrentAnimationState() != Zombie.ZombieAnimationState.DIE &&
                    zombie.getRowIndex() == this.rowIndex &&
                    CollisionDetector.isColliding(this, zombie)) {
                    System.out.println("小推车在行 " + rowIndex + " 碾压僵尸: " + zombie.getClass().getSimpleName());
                    zombie.die(); // 调用僵尸的die方法，僵尸将进入死亡动画状态
                }
            }
        }
    }

    /**
     * 报告当前位置的草坪已被割
     */
    private void reportMowedArea() {
        ElementManager.getManager().addMowedArea(
            new Rectangle(this.getX(), this.getY(), this.getW(), this.getH())
        );
    }


    @Override
    public void die() {
        System.out.println("小推车在行 " + rowIndex + " 被销毁");
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean hasFinished() {
        return hasFinished;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}