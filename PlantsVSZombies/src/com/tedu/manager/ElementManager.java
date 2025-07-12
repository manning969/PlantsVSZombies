package com.tedu.manager;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tedu.element.items.LawnMower; // 导入小推车类
import com.tedu.element.projectiles.Pea;
import com.tedu.element.zombies.NormalZombie;
import com.tedu.element.Zombie; // 导入僵尸类
import com.tedu.utils.CollisionDetector; // 导入碰撞检测工具类
import java.util.Iterator; // 用于安全移除元素
import com.tedu.element.ElementObj;
import com.tedu.element.Plant;

//元素管理器
public class ElementManager {
//	private List<Object> listMap;
//	private List<Object> listPlay;
	
	//枚举类型当作Map的key，用于获取不同类型的资源
	//List中的元素的泛型是元素基类
	//所有元素都可存放到Map集合中，显示模块只需要获取到Map即可
	//调用元素基类的showElement()方法
	private Map<GameElement, List<ElementObj>> gameElements;
	
	private List<Rectangle> mowedAreas;
	
	public Map<GameElement, List<ElementObj>> getGameElements() {
		return gameElements;
		
	}
	
	//添加元素方法，一般由加载器调用
	public void addElement(ElementObj obj, GameElement ge) {
//		List<ElementObj> list = gameElements.get(ge);
//		list.add(obj);
		gameElements.get(ge).add(obj);//添加元素对象到集合，按key值存储
	}
	//依据key返回list集合，取出某一类元素
	public List<ElementObj> getElementsByKey(GameElement ge){
		return gameElements.get(ge);
	}

	//单例模式：内存中有且只有一个实例
	//单例模式包括饿汉模式和饱汉模式
	//饿汉模式：启动就自动加载实例
	//饱汉模式：需要使用的时候才加载实例
	
	//编写方式：
	//1. 需要一个静态属性（定义一个常量）单例的引用
	private static ElementManager EM = null;
	//2. 提供一个静态方法（返回此实例）return单例的引用
	//synchronized线程锁：保证本方法执行中只有一个线程
	public static synchronized ElementManager getManager() {
		if(EM == null) {//空值判定
			EM = new ElementManager();
		}
		return EM;
	}
	//3. 为防止其他人自己的使用（类可以实例化），会私有化构造方法
	//Element em = new ElementManager();
	private ElementManager() {//私有化构造方法
		init();
	}
	
//	static {//饿汉实例化对象，静态语句块是在类被加载的时候直接执行
//		EM = new ElementManager();//只会执行一次
//		
//	}
    //init方法的作用：为了重写构造方法，功能扩展
	public void init() {//此处完成实例化
		//hashMap hash散列
		gameElements = new HashMap<GameElement, List<ElementObj>>();
		//将每种元素集合都放入到map中
//		gameElements.put(GameElement.PLAY, new ArrayList<ElementObj>());
//		gameElements.put(GameElement.MAPS, new ArrayList<ElementObj>());
//		gameElements.put(GameElement.ENEMY, new ArrayList<ElementObj>());
//		gameElements.put(GameElement.BOSS, new ArrayList<ElementObj>());
		for(GameElement ge:GameElement.values()) {
			gameElements.put(ge, new ArrayList<ElementObj>());
		}
		mowedAreas = new ArrayList<>();
	    //还有道具、子弹、爆炸效果、死亡效果等等
	}
	
	 /**
     * 移除元素方法
     * @param obj 要移除的元素对象
     * @param ge 元素所属的类型 (GameElement)
     */
    public void removeElement(ElementObj obj, GameElement ge) { // <-- 添加这个方法
        List<ElementObj> list = gameElements.get(ge);
        if (list != null) {
            list.remove(obj);
        }
    }

    /**
     * 游戏主循环中调用的所有元素的逻辑处理和碰撞检测
     */
    public void gameLogicAndCollisionDetection(long gameTime) {
        // 1. 让所有元素执行自己的model逻辑 (遍历副本以避免ConcurrentModificationException)
        Map<GameElement, List<ElementObj>> currentElements = new HashMap<>();
        for (Map.Entry<GameElement, List<ElementObj>> entry : gameElements.entrySet()) {
            currentElements.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        for (Map.Entry<GameElement, List<ElementObj>> entry : currentElements.entrySet()) {
            List<ElementObj> elementsInType = entry.getValue();
            for (ElementObj obj : elementsInType) {
                // 即使对象已经被标记为不Live，如果它在播放死亡动画，也需要继续更新其model来完成动画
                obj.model(gameTime);
            }
        }

        // 2. 执行碰撞检测
        List<ElementObj> lawnMowers = getElementsByKey(GameElement.LAWN_MOWERS);
        List<ElementObj> zombies = getElementsByKey(GameElement.ZOMBIES);
        List<ElementObj> projectiles = getElementsByKey(GameElement.PROJECTILES);
        List<ElementObj> plants = getElementsByKey(GameElement.PLANTS);

        // 小推车与僵尸的碰撞检测
        List<LawnMower> currentLawnMowers = new ArrayList<>();
        for(ElementObj objMower : lawnMowers) {
            if(objMower instanceof LawnMower) {
                currentLawnMowers.add((LawnMower) objMower);
            }
        }

        List<Zombie> currentZombiesForMowerCollision = new ArrayList<>();
        for(ElementObj objZombie : zombies) {
            if(objZombie instanceof Zombie) {
                currentZombiesForMowerCollision.add((Zombie) objZombie);
            }
        }

        for (LawnMower mower : currentLawnMowers) {
            if (!mower.isActive() && !mower.hasFinished()) {
                for (Zombie zombie : currentZombiesForMowerCollision) {
                    if (zombie.isLive() && zombie.getCurrentAnimationState() != Zombie.ZombieAnimationState.DIE &&
                        zombie.getRowIndex() == mower.getRowIndex() &&
                        CollisionDetector.isColliding(mower, zombie)) {
                        mower.activate();
                        break;
                    }
                }
            }
        }

        // 豌豆与僵尸的碰撞检测 - 修复版
        checkPeaZombieCollisions(projectiles, zombies);

        // 僵尸与植物的碰撞检测
        List<ElementObj[]> zombiePlantCollisions = CollisionDetector.detectZombieVsPlant(zombies, plants);
        for (ElementObj[] collision : zombiePlantCollisions) {
            Zombie zombie = (Zombie) collision[0];
            Plant plant = (Plant) collision[1];
            if (zombie.isLive() && zombie.getCurrentAnimationState() != Zombie.ZombieAnimationState.DIE) {
                 zombie.startEating(plant);
            }
        }

        // 3. 清理死亡或完成任务的元素
        cleanupElements();
    }
    
    private void checkPeaZombieCollisions(List<ElementObj> projectiles, List<ElementObj> zombies) {
        Iterator<ElementObj> projIterator = projectiles.iterator();
        
        while (projIterator.hasNext()) {
            ElementObj projObj = projIterator.next();
            
            if (!(projObj instanceof Pea) || !projObj.isLive()) {
                continue;
            }
            
            Pea pea = (Pea) projObj;
            
            // 检查豌豆是否已经击中过目标
            if (pea.hasHit()) {
                continue; // 跳过已经击中的豌豆
            }
            
            // 检查与所有僵尸的碰撞
            boolean hitDetected = false;
            for (ElementObj zombieObj : zombies) {
                if (!(zombieObj instanceof Zombie) || !zombieObj.isLive()) {
                    continue;
                }
                
                Zombie zombie = (Zombie) zombieObj;
                
                // 确保僵尸还活着且不在死亡状态
                if (zombie.getCurrentAnimationState() == Zombie.ZombieAnimationState.DIE || zombie.isDying()) {
                    continue;
                }
                
                // 检查是否在同一行且发生碰撞
                if (pea.getRowIndex() == zombie.getRowIndex() && 
                    CollisionDetector.isColliding(pea, zombie)) {
                    
                    // 确保只造成一次伤害
                    if (pea.dealDamage()) { // 使用 dealDamage() 而不是 die()
                        zombie.takeDamage(pea.getDamage());
                        System.out.println("🎯 豌豆击中僵尸！造成 " + pea.getDamage() + " 点伤害");
                        hitDetected = true;
                        break; // 击中一个僵尸后跳出循环
                    }
                }
            }
            
            // 如果豌豆击中了目标，在下一次清理时会被移除
            // 不需要在这里手动移除，避免ConcurrentModificationException
        }
    }

    public void cleanupElements() {
        for (Map.Entry<GameElement, List<ElementObj>> entry : gameElements.entrySet()) {
            List<ElementObj> elements = entry.getValue();
            Iterator<ElementObj> iterator = elements.iterator();
            while (iterator.hasNext()) {
                ElementObj obj = iterator.next();
                
                if (obj instanceof Zombie) {
                    Zombie zombie = (Zombie) obj;
                    if (!zombie.isLive() && zombie.isDyingAnimationFinished()) {
                        System.out.println("清理僵尸: " + zombie.getClass().getSimpleName() + " (死亡动画结束)");
                        iterator.remove();
                    }
                }
                else if (obj instanceof LawnMower) {
                    LawnMower mower = (LawnMower) obj;
                    if (!mower.isLive() || mower.hasFinished()) {
                        System.out.println("清理小推车: " + mower.getRowIndex() + " (完成任务)");
                        iterator.remove();
                    }
                }
                else if (!obj.isLive()) {
                    System.out.println("清理元素: " + obj.getClass().getSimpleName() + " (已死亡/失效)");
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * 添加被割过的草坪区域
     */
    public void addMowedArea(Rectangle area) {
        if (this.mowedAreas == null) { // 额外的安全检查
            this.mowedAreas = new ArrayList<>();
        }
        this.mowedAreas.add(area);
    }

    public List<Rectangle> getMowedAreas() {
        return mowedAreas;
    }
    
    // Test method
    public static void main(String[] args) {
        System.out.println("ElementManager test...");
        ElementManager em = ElementManager.getManager();
    }
}
