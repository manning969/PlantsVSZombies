package com.tedu.manager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.tedu.element.items.Sun;
import com.tedu.element.items.Sun.SunType;
import com.tedu.utils.GameConfig;
import com.tedu.element.items.LawnMower;

/**
 * 植物大战僵尸游戏资源加载器 - 修复死亡动画图片配置并添加加速按钮图片加载
 */
public class GameLoad {
    private static ElementManager em = ElementManager.getManager();

    // 图片集合（保留原有的ImageIcon类型）
    public static Map<String, ImageIcon> imgMap = new HashMap<>();
    // 新增的Image类型图片集合（用于加速按钮等需要Image类型的资源）
    public static Map<String, Image> imageMap = new HashMap<>();

    // 用于读取文件的类
    private static Properties pro = new Properties();

    /**
     * 加载图片资源
     */
    public static void loadImg() {
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
                    if (o.toString().contains("_die")) {
                        createPlaceholderDeathImage(o.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        validateDeathAnimations();
        
        // 加载加速按钮图片（新增）
        loadSpeedButtonImages();
        
     // 调试输出
        debugPrintLoadedImages();
    }

    /**
     * 加载加速按钮图片（新增方法）
     */
    private static void loadSpeedButtonImages() {
        // 使用 imgMap 而不是 imageMap
        ImageIcon normalIcon = imgMap.get(GameConfig.SPEED_BUTTON_NORMAL);
        ImageIcon doubleIcon = imgMap.get(GameConfig.SPEED_BUTTON_DOUBLE);
        
        if (normalIcon != null) {
            imageMap.put(GameConfig.SPEED_BUTTON_NORMAL, normalIcon.getImage());
        } else {
            System.err.println("❌ 未找到速度按钮普通图标: " + GameConfig.SPEED_BUTTON_NORMAL);
            createPlaceholderImage(GameConfig.SPEED_BUTTON_NORMAL);
        }
        
        if (doubleIcon != null) {
            imageMap.put(GameConfig.SPEED_BUTTON_DOUBLE, doubleIcon.getImage());
        } else {
            System.err.println("❌ 未找到速度按钮加速图标: " + GameConfig.SPEED_BUTTON_DOUBLE);
            createPlaceholderImage(GameConfig.SPEED_BUTTON_DOUBLE);
        }
        
        // 调试输出
        System.out.println("速度按钮图片加载状态:");
        System.out.println("  " + GameConfig.SPEED_BUTTON_NORMAL + ": " + 
                          (imageMap.get(GameConfig.SPEED_BUTTON_NORMAL) != null ? "✅" : "❌"));
        System.out.println("  " + GameConfig.SPEED_BUTTON_DOUBLE + ": " + 
                          (imageMap.get(GameConfig.SPEED_BUTTON_DOUBLE) != null ? "✅" : "❌"));
    }

    private static void createPlaceholderImage(String key) {
        // 创建简单的占位图片
        BufferedImage placeholder = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 50, 50);
        g2d.setColor(Color.WHITE);
        g2d.drawString(key, 5, 25);
        g2d.dispose();
        
        imageMap.put(key, placeholder);
        System.out.println("🟧 创建占位图片: " + key);
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
                imgMap.put(deathKey, walkIcon);
                System.out.println("🔄 使用占位图片: " + deathKey + " -> " + walkKey);
            } else {
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
                "conehead_walk=resources/images/zombies/conehead/conehead_walk.gif\n" +
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
                "shovel_active=resources/images/ui/shovel/shovel_active.png\n";

            // 使用 OutputStreamWriter 替代 FileWriter(File, Charset) 以兼容 JDK 8
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
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
            // 使用 Class.forName 获取 Class 对象，然后调用 newInstance()
            // 注意：newInstance() 在 JDK 9+ 中已被废弃，但在 JDK 8 中仍是常用方式
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
                "lawn_mower=com.tedu.element.items.LawnMower\n" ;

            // 使用 OutputStreamWriter 替代 FileWriter(File, Charset) 以兼容 JDK 8
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
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
        System.out.println("\n=== 已加载的ImageIcon图片资源 ===");
        for (Map.Entry<String, ImageIcon> entry : imgMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + (entry.getValue() != null ? "✅" : "❌"));
        }
        
        System.out.println("\n=== 已加载的Image图片资源 ===");
        for (Map.Entry<String, Image> entry : imageMap.entrySet()) {
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
    
    public static void validateResources() {
        // 检查速度按钮图片是否存在
        checkResourceExists("resources/images/ui/speed_1x.png");
        checkResourceExists("resources/images/ui/speed_2x.png");
    }

    private static void checkResourceExists(String path) {
        try {
            java.net.URL resUrl = GameLoad.class.getClassLoader().getResource(path);
            if (resUrl == null) {
                System.err.println("❌ 资源文件不存在: " + path);
                
                // 尝试在项目目录中查找
                File file = new File(path);
                System.out.println("    绝对路径: " + file.getAbsolutePath());
                System.out.println("    文件存在: " + file.exists());
            } else {
                System.out.println("✅ 资源验证通过: " + path);
            }
        } catch (Exception e) {
            System.err.println("资源验证异常: " + path);
            e.printStackTrace();
        }
    }
}