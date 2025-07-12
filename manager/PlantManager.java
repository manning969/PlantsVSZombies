package com.tedu.manager;

import com.tedu.utils.GameConfig;

public class PlantManager {

	private static final int PEASHOOTER_COST = GameConfig.PEASHOOTER_COST;
	private static final int SUNFLOWER_COST = GameConfig.SUNFLOWER_COST;
	private static final int WALLNUT_COST = GameConfig.WALLNUT_COST;
	private static final boolean ENABLE_PLANT_REFUND = GameConfig.ENABLE_PLANT_REFUND;
	public static final double PLANT_REFUND_RATE = GameConfig.PLANT_REFUND_RATE;
	public static final int MIN_REFUND_AMOUNT = GameConfig.MIN_REFUND_AMOUNT;
	public static final int MAX_REFUND_AMOUNT = GameConfig.MAX_REFUND_AMOUNT;
	
	/**
     * 根据植物类型获取植物价格
     */
    public static int getPlantCost(String plantType) {
        switch (plantType.toLowerCase()) {
            case "peashooter":
                return PEASHOOTER_COST;
            case "sunflower":
                return SUNFLOWER_COST;
            case "wallnut":
            case "wallnut_full":
            case "wallnut_cracked1":
            case "wallnut_cracked2":
                return WALLNUT_COST;
            default:
                return 0;
        }
    }
    
    /**
     * 计算植物铲除后的返还金额
     */
    public static int calculateRefundAmount(String plantType) {
        if (!ENABLE_PLANT_REFUND) {
            return 0;
        }
        
        int originalCost = getPlantCost(plantType);
        if (originalCost <= 0) {
            return 0;
        }
        
        // 计算返还金额
        int refundAmount = (int) (originalCost * PLANT_REFUND_RATE);
        
        // 应用最小和最大返还限制
        refundAmount = Math.max(refundAmount, MIN_REFUND_AMOUNT);
        refundAmount = Math.min(refundAmount, MAX_REFUND_AMOUNT);
        
        return refundAmount;
    }
    
    /**
     * 获取植物的显示名称（用于提示信息）
     */
    public static String getPlantDisplayName(String plantType) {
        switch (plantType.toLowerCase()) {
            case "peashooter":
                return "豌豆射手";
            case "sunflower":
                return "向日葵";
            case "wallnut":
            case "wallnut_full":
            case "wallnut_cracked1":
            case "wallnut_cracked2":
                return "坚果墙";
            default:
                return "未知植物";
        }
    }
}
