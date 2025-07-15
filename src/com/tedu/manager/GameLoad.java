package com.tedu.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.ImageIcon;
import com.tedu.element.ElementObj;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import com.tedu.element.items.Sun;
import com.tedu.element.items.Sun.SunType;
import com.tedu.element.items.LawnMower;
import com.tedu.utils.GameConfig;

/**
 * 植物大战僵尸游戏资源加载器 - 修复死亡动画图片配置
 */
public class GameLoad {
    private static ElementManager em = ElementManager.getManager();

    // 图片集合
    public static Map<String, ImageIcon> imgMap = new HashMap<>();

    // 用于读取文件的类
    private static Properties pro = new Properties();

    /**
     * 加载图片资源
     */
    public static void loadImg() {
        // 首先尝试加载配置文件
        File file = new File("src/com/tedu/text/GameData.pro");

        if (!file.exists()) {
            System.out.println("配置文件不存在，创建默认配置");
            createDefaultGameDataConfig();
        }

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            pro.clear();
            pro.load(isr);

            Set<Object> set = pro.keySet();
            for (Object o : set) {
                String url = pro.getProperty(o.toString());
                File imgFile = new File(url);
                if (imgFile.exists()) {
                    ImageIcon loadedIcon = new ImageIcon(url);
                    imgMap.put(o.toString(), loadedIcon);
                    System.out.println("✅ 成功加载图片: " + o.toString() + " -> " + url);
                } else {
                    System.err.println("❌ 图片文件不存在: " + url + " (键: " + o.toString() + ")");
                    // 为缺失的死亡动画创建占位图片
                    if (o.toString().contains("_die")) {
                        createPlaceholderDeathImage(o.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 验证关键的死亡动画图片是否加载成功
        validateDeathAnimations();
    }

    /**
     * 验证死亡动画图片是否正确加载
     */
    private static void validateDeathAnimations() {
        String[] deathAnimations = {"normal_die", "conehead_die"};
        
        for (String animKey : deathAnimations) {
            if (!imgMap.containsKey(animKey) || imgMap.get(animKey) == null) {
                System.err.println("⚠️  警告: 缺少死亡动画图片: " + animKey);
                createPlaceholderDeathImage(animKey);
            } else {
                System.out.println("✅ 死亡动画图片验证通过: " + animKey);
            }
        }
    }

    /**
     * 为缺失的死亡动画创建占位图片（使用对应的行走图片）
     */
    private static void createPlaceholderDeathImage(String deathKey) {
        try {
            String walkKey = deathKey.replace("_die", "_walk");
            ImageIcon walkIcon = imgMap.get(walkKey);
            
            if (walkIcon != null) {
                // 使用行走图片作为临时的死亡图片
                imgMap.put(deathKey, walkIcon);
                System.out.println("🔄 使用占位图片: " + deathKey + " -> " + walkKey);
            } else {
                // 创建一个空的ImageIcon作为最后的备选
                imgMap.put(deathKey, new ImageIcon());
                System.out.println("⚠️  创建空占位图片: " + deathKey);
            }
        } catch (Exception e) {
            System.err.println("创建占位图片失败: " + deathKey);
            e.printStackTrace();
        }
    }

    /**
     * 创建默认的游戏数据配置文件 - 修复死亡动画路径
     */
    private static void createDefaultGameDataConfig() {
        try {
            File file = new File("src/com/tedu/text/GameData.pro");
            file.getParentFile().mkdirs();

            String defaultContent =
                "# 植物图片\n" +
                "peashooter_idle=resources/images/plants/peashooter/peashooter_idle.gif\n" +
                "peashooter_attack=resources/images/plants/peashooter/peashooter_attack.gif\n" +
                "sunflower_idle=resources/images/plants/sunflower/sunflower_idle.gif\n" +
                "wallnut_full=resources/images/plants/wallnut/wallnut_full.gif\n" +
                "wallnut_cracked1=resources/images/plants/wallnut/wallnut_cracked1.gif\n" +
                "wallnut_cracked2=resources/images/plants/wallnut/wallnut_cracked2.gif\n" +
                "\n# 僵尸图片\n" +
                "normal_walk=resources/images/zombies/normal/normal_walk.gif\n" +
                "normal_eat=resources/images/zombies/normal/normal_eat.gif\n" +
                "normal_die=resources/images/zombies/normal/normal_die.gif\n" +
                "conehead_walk=resources/images/zombies/conehead/conehead_walk1.png\n" +
                "conehead_eat=resources/images/zombies/conehead/conehead_eat.gif\n" +
                "conehead_die=resources/images/zombies/conehead/conehead_die.gif\n" +
                "\n# 子弹图片\n" +
                "pea=resources/images/projectiles/pea.png\n" +
                "\n# 道具图片\n" +
                "sun_normal=resources/images/items/sun/sun_normal.png\n" +
                "\n# 背景图片\n" +
                "background_day=resources/images/backgrounds/day/background_day.png\n" +
                "\n# 小推车图片\n" +
                "lawn_mower_idle=resources/images/items/lawnmower/lawn_mower_idle.png\n" +
                "lawn_mower_active=resources/images/items/lawnmower/lawn_mower_active.png\n" +
                "\n# UI图片\n" +
                "shovel_idle=resources/images/ui/shovel/shovel_idle.png\n" +
                "shovel_hover=resources/images/ui/shovel/shovel_hover.png\n" +
                "shovel_active=resources/images/ui/shovel/shovel_active.png\n" +
                "\n# 特效图片\n" +
                "zombie_crush_effect_1=resources/images/effects/zombie_crush_1.png\n" +
                "zombie_crush_effect_2=resources/images/effects/zombie_crush_2.png\n";

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(defaultContent);
            }

            System.out.println("✅ 已创建默认配置文件（包含铲子图片）: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载草坪网格
     */
    public static void loadLawn() {
        System.out.println("✅ 草坪已加载");
    }

    /**
     * 初始化游戏
     */
    public static void initializeGame() {
        loadObj();
        System.out.println("✅ 游戏初始化完成");
    }

    /**
     * 创建游戏对象的工厂方法
     */
    public static ElementObj getObj(String str) {
        try {
            Class<?> class1 = objMap.get(str);
            if (class1 != null) {
                Object newInstance = class1.newInstance();
                if (newInstance instanceof ElementObj) {
                    return (ElementObj) newInstance;
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对象类映射
     */
    private static Map<String, Class<?>> objMap = new HashMap<>();

    /**
     * 加载对象类配置
     */
    public static void loadObj() {
        File file = new File("src/com/tedu/text/obj.pro");

        if (!file.exists()) {
            createDefaultObjConfig();
        }

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            pro.clear();
            pro.load(isr);

            Set<Object> set = pro.keySet();
            for (Object o : set) {
                String classUrl = pro.getProperty(o.toString());
                try {
                    Class<?> forName = Class.forName(classUrl);
                    objMap.put(o.toString(), forName);
                    System.out.println("✅ 加载类: " + o.toString() + " -> " + classUrl);
                } catch (ClassNotFoundException e) {
                    System.err.println("❌ 类不存在: " + classUrl);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建默认的对象配置文件
     */
    private static void createDefaultObjConfig() {
        try {
            File file = new File("src/com/tedu/text/obj.pro");
            file.getParentFile().mkdirs();

            String defaultContent =
                "# 植物类\n" +
                "peashooter=com.tedu.element.plants.Peashooter\n" +
                "sunflower=com.tedu.element.plants.Sunflower\n" +
                "wallnut=com.tedu.element.plants.WallNut\n" +
                "\n# 僵尸类\n" +
                "normal_zombie=com.tedu.element.zombies.NormalZombie\n" +
                "conehead_zombie=com.tedu.element.zombies.ConeheadZombie\n" +
                "\n# 子弹类\n" +
                "pea=com.tedu.element.projectiles.Pea\n" +
                "\n# 道具类\n" +
                "sun=com.tedu.element.items.Sun\n" +
                "lawn_mower=com.tedu.element.items.LawnMower\n" +
                "crushed_effect=com.tedu.element.effects.CrushedEffect\n";

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(defaultContent);
            }

            System.out.println("✅ 已创建默认对象配置文件: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 其他创建方法保持不变...
    public static ElementObj createPlant(String plantType, int gridX, int gridY) {
        ElementObj obj = getObj(plantType);
        if (obj != null) {
            return obj.createElement(gridX + "," + gridY);
        }
        return null;
    }

    public static ElementObj createZombie(String zombieType, int rowIndex) {
        ElementObj obj = getObj(zombieType);
        if (obj != null) {
            return obj.createElement(String.valueOf(rowIndex));
        }
        return null;
    }

    public static LawnMower createLawnMower(int rowIndex) {
        ElementObj obj = getObj("lawn_mower");
        if (obj != null && obj instanceof LawnMower) {
            return (LawnMower) obj.createElement(String.valueOf(rowIndex));
        }
        return null;
    }

    @Deprecated
    public static ElementObj createSun(int x, int y) {
        return Sun.createNaturalSun(x, y, y);
    }

    public static ElementObj createSun(int startX, int startY, int groundY) {
        return Sun.createNaturalSun(startX, startY, groundY);
    }

    public static ElementObj createSun(int startX, int startY, int groundY, double initialVelocityX, double initialVelocityY) {
        ElementObj obj = getObj("sun");
        if (obj != null && obj instanceof Sun) {
            return ((Sun)obj).createElement(startX + "," + startY + "," + groundY + "," + initialVelocityX + "," + initialVelocityY + "," + SunType.SUNFLOWER.name());
        }
        return null;
    }

    public static ElementObj createEffect(String effectType, int x, int y, int w, int h) {
        ElementObj obj = getObj(effectType);
        if (obj != null) {
            return obj.createElement(x + "," + y + "," + w + "," + h);
        }
        return null;
    }

    /**
     * 调试方法：打印所有已加载的图片
     */
    public static void debugPrintLoadedImages() {
        System.out.println("\n=== 已加载的图片资源 ===");
        for (Map.Entry<String, ImageIcon> entry : imgMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + (entry.getValue() != null ? "✅" : "❌"));
        }
        System.out.println("========================\n");
    }

    public static void main(String[] args) {
        System.out.println("测试资源加载...");
        loadImg();
        loadObj();
        debugPrintLoadedImages();

        // 测试创建对象
        ElementObj peashooter = createPlant("peashooter", 2, 1);
        if (peashooter != null) {
            System.out.println("✅ 成功创建豌豆射手: " + peashooter);
        }

        ElementObj zombie = createZombie("normal_zombie", 1);
        if (zombie != null) {
            System.out.println("✅ 成功创建普通僵尸: " + zombie);
        }

        LawnMower mower = createLawnMower(0);
        if (mower != null) {
            System.out.println("✅ 成功创建小推车: " + mower);
        }
    }
}