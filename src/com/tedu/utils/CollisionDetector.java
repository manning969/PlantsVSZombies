package com.tedu.utils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import com.tedu.element.ElementObj;
import com.tedu.element.Plant;
import com.tedu.element.Zombie;
import com.tedu.element.projectiles.Pea;

/**
 * 碰撞检测工具类
 * 提供各种碰撞检测的实用方法
 */
public class CollisionDetector {
    
    /**
     * 检测两个元素是否发生碰撞
     * @param obj1 第一个元素
     * @param obj2 第二个元素
     * @return 是否发生碰撞
     */
    public static boolean isColliding(ElementObj obj1, ElementObj obj2) {
        if (obj1 == null || obj2 == null || !obj1.isLive() || !obj2.isLive()) {
            return false;
        }
        return obj1.pk(obj2);
    }
    
    /**
     * 检测点是否在元素范围内
     * @param obj 元素对象
     * @param x 点的X坐标
     * @param y 点的Y坐标
     * @return 是否在范围内
     */
    public static boolean isPointInElement(ElementObj obj, int x, int y) {
        if (obj == null || !obj.isLive()) {
            return false;
        }
        Rectangle rect = obj.getRectangle();
        return rect.contains(x, y);
    }
    
    /**
     * 检测子弹与僵尸的碰撞
     * @param projectiles 子弹列表
     * @param zombies 僵尸列表
     * @return 碰撞对列表，每个元素包含[子弹, 僵尸]
     */
    public static List<ElementObj[]> detectProjectileVsZombie(List<ElementObj> projectiles, List<ElementObj> zombies) {
        List<ElementObj[]> collisions = new ArrayList<>();
        
        for (ElementObj projObj : projectiles) {
            if (!projObj.isLive() || !(projObj instanceof Pea)) {
                continue;
            }
            
            Pea pea = (Pea) projObj;
            
            for (ElementObj zombieObj : zombies) {
                if (!zombieObj.isLive() || !(zombieObj instanceof Zombie)) {
                    continue;
                }
                
                Zombie zombie = (Zombie) zombieObj;
                
                // 检查是否在同一行且发生碰撞
                if (pea.getRowIndex() == zombie.getRowIndex() && isColliding(pea, zombie)) {
                    collisions.add(new ElementObj[]{pea, zombie});
                    break; // 一个子弹只能击中一个僵尸
                }
            }
        }
        
        return collisions;
    }
    
    /**
     * 检测僵尸与植物的碰撞
     * @param zombies 僵尸列表
     * @param plants 植物列表
     * @return 碰撞对列表，每个元素包含[僵尸, 植物]
     */
    public static List<ElementObj[]> detectZombieVsPlant(List<ElementObj> zombies, List<ElementObj> plants) {
        List<ElementObj[]> collisions = new ArrayList<>();
        
        for (ElementObj zombieObj : zombies) {
            if (!zombieObj.isLive() || !(zombieObj instanceof Zombie)) {
                continue;
            }
            
            Zombie zombie = (Zombie) zombieObj;
            
            for (ElementObj plantObj : plants) {
                if (!plantObj.isLive() || !(plantObj instanceof Plant)) {
                    continue;
                }
                
                Plant plant = (Plant) plantObj;
                
                // 检查是否在同一行且发生碰撞
                if (zombie.getRowIndex() == plant.getGridY() && isColliding(zombie, plant)) {
                    collisions.add(new ElementObj[]{zombie, plant});
                    break; // 一个僵尸一次只能攻击一个植物
                }
            }
        }
        
        return collisions;
    }
    
    /**
     * 检测鼠标点击与阳光的碰撞
     * @param suns 阳光列表
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @return 被点击的阳光对象，如果没有返回null
     */
    public static ElementObj detectMouseVsSun(List<ElementObj> suns, int mouseX, int mouseY) {
        for (ElementObj sunObj : suns) {
            if (!sunObj.isLive()) {
                continue;
            }
            
            // 检查鼠标点击是否在阳光范围内
            if (mouseX >= sunObj.getX() && mouseX <= sunObj.getX() + sunObj.getW() &&
                mouseY >= sunObj.getY() && mouseY <= sunObj.getY() + sunObj.getH()) {
                return sunObj;
            }
        }
        return null;
    }
    
    /**
     * 检测矩形区域内的所有元素
     * @param elements 元素列表
     * @param area 矩形区域
     * @return 在区域内的元素列表
     */
    public static List<ElementObj> getElementsInArea(List<ElementObj> elements, Rectangle area) {
        List<ElementObj> result = new ArrayList<>();
        
        for (ElementObj obj : elements) {
            if (!obj.isLive()) {
                continue;
            }
            
            Rectangle objRect = obj.getRectangle();
            if (area.intersects(objRect)) {
                result.add(obj);
            }
        }
        
        return result;
    }
    
    /**
     * 检测圆形区域内的所有元素
     * @param elements 元素列表
     * @param centerX 圆心X坐标
     * @param centerY 圆心Y坐标
     * @param radius 半径
     * @return 在区域内的元素列表
     */
    public static List<ElementObj> getElementsInCircle(List<ElementObj> elements, int centerX, int centerY, int radius) {
        List<ElementObj> result = new ArrayList<>();
        
        for (ElementObj obj : elements) {
            if (!obj.isLive()) {
                continue;
            }
            
            // 计算元素中心点到圆心的距离
            int objCenterX = obj.getX() + obj.getW() / 2;
            int objCenterY = obj.getY() + obj.getH() / 2;
            
            double distance = Math.sqrt(Math.pow(objCenterX - centerX, 2) + Math.pow(objCenterY - centerY, 2));
            
            if (distance <= radius) {
                result.add(obj);
            }
        }
        
        return result;
    }
    
    /**
     * 检测指定行内的所有元素
     * @param elements 元素列表
     * @param rowIndex 行索引
     * @return 该行内的元素列表
     */
    public static List<ElementObj> getElementsInRow(List<ElementObj> elements, int rowIndex) {
        List<ElementObj> result = new ArrayList<>();
        
        int rowStartY = GameConfig.GRID_START_Y + rowIndex * GameConfig.GRID_HEIGHT;
        int rowEndY = rowStartY + GameConfig.GRID_HEIGHT;
        
        for (ElementObj obj : elements) {
            if (!obj.isLive()) {
                continue;
            }
            
            int objCenterY = obj.getY() + obj.getH() / 2;
            if (objCenterY >= rowStartY && objCenterY < rowEndY) {
                result.add(obj);
            }
        }
        
        return result;
    }
    
    /**
     * 检测指定列内的所有元素
     * @param elements 元素列表
     * @param colIndex 列索引
     * @return 该列内的元素列表
     */
    public static List<ElementObj> getElementsInColumn(List<ElementObj> elements, int colIndex) {
        List<ElementObj> result = new ArrayList<>();
        
        int colStartX = GameConfig.GRID_START_X + colIndex * GameConfig.GRID_WIDTH;
        int colEndX = colStartX + GameConfig.GRID_WIDTH;
        
        for (ElementObj obj : elements) {
            if (!obj.isLive()) {
                continue;
            }
            
            int objCenterX = obj.getX() + obj.getW() / 2;
            if (objCenterX >= colStartX && objCenterX < colEndX) {
                result.add(obj);
            }
        }
        
        return result;
    }
    
    /**
     * 计算两个元素之间的距离
     * @param obj1 第一个元素
     * @param obj2 第二个元素
     * @return 距离值
     */
    public static double getDistance(ElementObj obj1, ElementObj obj2) {
        if (obj1 == null || obj2 == null) {
            return Double.MAX_VALUE;
        }
        
        int x1 = obj1.getX() + obj1.getW() / 2;
        int y1 = obj1.getY() + obj1.getH() / 2;
        int x2 = obj2.getX() + obj2.getW() / 2;
        int y2 = obj2.getY() + obj2.getH() / 2;
        
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    /**
     * 找到离指定位置最近的元素
     * @param elements 元素列表
     * @param x 指定位置X坐标
     * @param y 指定位置Y坐标
     * @return 最近的元素，如果列表为空返回null
     */
    public static ElementObj findClosestElement(List<ElementObj> elements, int x, int y) {
        ElementObj closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (ElementObj obj : elements) {
            if (!obj.isLive()) {
                continue;
            }
            
            int objCenterX = obj.getX() + obj.getW() / 2;
            int objCenterY = obj.getY() + obj.getH() / 2;
            
            double distance = Math.sqrt(Math.pow(objCenterX - x, 2) + Math.pow(objCenterY - y, 2));
            
            if (distance < minDistance) {
                minDistance = distance;
                closest = obj;
            }
        }
        
        return closest;
    }
    
    /**
     * 检测元素是否在屏幕边界内
     * @param obj 元素对象
     * @return 是否在边界内
     */
    public static boolean isInBounds(ElementObj obj) {
        if (obj == null) {
            return false;
        }
        
        return obj.getX() >= 0 && 
               obj.getY() >= 0 && 
               obj.getX() + obj.getW() <= GameConfig.GAME_WIDTH && 
               obj.getY() + obj.getH() <= GameConfig.GAME_HEIGHT;
    }
    
    /**
     * 检测元素是否超出屏幕边界
     * @param obj 元素对象
     * @return 是否超出边界
     */
    public static boolean isOutOfBounds(ElementObj obj) {
        return !isInBounds(obj);
    }
}