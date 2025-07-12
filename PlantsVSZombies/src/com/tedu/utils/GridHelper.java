package com.tedu.utils;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * 网格辅助工具类 - 增强版（新增铲子功能支持）
 * 提供网格坐标转换、验证等功能
 */
public class GridHelper {
    
    /**
     * 将像素坐标转换为网格坐标
     * @param pixelX 像素X坐标
     * @param pixelY 像素Y坐标
     * @return 网格坐标点，如果超出范围返回null
     */
    public static Point pixelToGrid(int pixelX, int pixelY) {
        // 检查是否在草坪区域内
        if (!isInLawnArea(pixelX, pixelY)) {
            return null;
        }
        
        int gridX = (pixelX - GameConfig.GRID_START_X) / GameConfig.GRID_WIDTH;
        int gridY = (pixelY - GameConfig.GRID_START_Y) / GameConfig.GRID_HEIGHT;
        
        // 验证网格坐标是否有效
        if (isValidGridPosition(gridX, gridY)) {
            return new Point(gridX, gridY);
        }
        
        return null;
    }
    
    /**
     * 将网格坐标转换为像素坐标（网格中心点）
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 像素坐标点
     */
    public static Point gridToPixel(int gridX, int gridY) {
        int pixelX = GameConfig.GRID_START_X + gridX * GameConfig.GRID_WIDTH + GameConfig.GRID_WIDTH / 2;
        int pixelY = GameConfig.GRID_START_Y + gridY * GameConfig.GRID_HEIGHT + GameConfig.GRID_HEIGHT / 2;
        return new Point(pixelX, pixelY);
    }
    
    /**
     * 将网格坐标转换为中心像素坐标（返回数组）- 新增方法，用于铲子特效
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 像素坐标数组 [x, y]
     */
    public static int[] gridToCenterPixel(int gridX, int gridY) {
        int pixelX = GameConfig.GRID_START_X + gridX * GameConfig.GRID_WIDTH + GameConfig.GRID_WIDTH / 2;
        int pixelY = GameConfig.GRID_START_Y + gridY * GameConfig.GRID_HEIGHT + GameConfig.GRID_HEIGHT / 2;
        return new int[]{pixelX, pixelY};
    }
    
    /**
     * 将网格坐标转换为像素坐标（左上角）
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 像素坐标点
     */
    public static Point gridToPixelTopLeft(int gridX, int gridY) {
        int pixelX = GameConfig.GRID_START_X + gridX * GameConfig.GRID_WIDTH;
        int pixelY = GameConfig.GRID_START_Y + gridY * GameConfig.GRID_HEIGHT;
        return new Point(pixelX, pixelY);
    }
    
    /**
     * 检查像素坐标是否在草坪区域内
     * @param pixelX 像素X坐标
     * @param pixelY 像素Y坐标
     * @return 是否在草坪区域内
     */
    public static boolean isInLawnArea(int pixelX, int pixelY) {
        return pixelX >= GameConfig.GRID_START_X && 
               pixelX <= GameConfig.GRID_START_X + GameConfig.GRID_COLS * GameConfig.GRID_WIDTH &&
               pixelY >= GameConfig.GRID_START_Y && 
               pixelY <= GameConfig.GRID_START_Y + GameConfig.GRID_ROWS * GameConfig.GRID_HEIGHT;
    }
    
    /**
     * 检查网格坐标是否有效
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 是否有效
     */
    public static boolean isValidGridPosition(int gridX, int gridY) {
        return gridX >= 0 && gridX < GameConfig.GRID_COLS &&
               gridY >= 0 && gridY < GameConfig.GRID_ROWS;
    }
    
    /**
     * 获取网格矩形区域
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 网格矩形
     */
    public static Rectangle getGridRectangle(int gridX, int gridY) {
        Point topLeft = gridToPixelTopLeft(gridX, gridY);
        return new Rectangle(topLeft.x, topLeft.y, GameConfig.GRID_WIDTH, GameConfig.GRID_HEIGHT);
    }
    
    /**
     * 计算两个网格位置之间的距离
     * @param gridX1 第一个位置的X坐标
     * @param gridY1 第一个位置的Y坐标
     * @param gridX2 第二个位置的X坐标
     * @param gridY2 第二个位置的Y坐标
     * @return 距离
     */
    public static double getGridDistance(int gridX1, int gridY1, int gridX2, int gridY2) {
        int deltaX = gridX2 - gridX1;
        int deltaY = gridY2 - gridY1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    /**
     * 检查两个网格位置是否在同一行
     * @param gridY1 第一个位置的Y坐标
     * @param gridY2 第二个位置的Y坐标
     * @return 是否在同一行
     */
    public static boolean isSameRow(int gridY1, int gridY2) {
        return gridY1 == gridY2;
    }
    
    /**
     * 检查两个网格位置是否在同一列
     * @param gridX1 第一个位置的X坐标
     * @param gridX2 第二个位置的X坐标
     * @return 是否在同一列
     */
    public static boolean isSameColumn(int gridX1, int gridX2) {
        return gridX1 == gridX2;
    }
    
    /**
     * 获取指定行的所有网格位置
     * @param gridY 行号
     * @return 该行所有网格位置的数组
     */
    public static Point[] getRowPositions(int gridY) {
        if (gridY < 0 || gridY >= GameConfig.GRID_ROWS) {
            return new Point[0];
        }
        
        Point[] positions = new Point[GameConfig.GRID_COLS];
        for (int x = 0; x < GameConfig.GRID_COLS; x++) {
            positions[x] = new Point(x, gridY);
        }
        return positions;
    }
    
    /**
     * 获取指定列的所有网格位置
     * @param gridX 列号
     * @return 该列所有网格位置的数组
     */
    public static Point[] getColumnPositions(int gridX) {
        if (gridX < 0 || gridX >= GameConfig.GRID_COLS) {
            return new Point[0];
        }
        
        Point[] positions = new Point[GameConfig.GRID_ROWS];
        for (int y = 0; y < GameConfig.GRID_ROWS; y++) {
            positions[y] = new Point(gridX, y);
        }
        return positions;
    }
    
    /**
     * 获取网格位置的字符串表示
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 字符串表示，如"(2,3)"
     */
    public static String gridToString(int gridX, int gridY) {
        return "(" + gridX + "," + gridY + ")";
    }
    
    /**
     * 获取指定范围内的所有网格位置
     * @param centerX 中心X坐标
     * @param centerY 中心Y坐标
     * @param radius 半径（网格单位）
     * @return 范围内的网格位置数组
     */
    public static Point[] getGridsInRange(int centerX, int centerY, int radius) {
        java.util.List<Point> positions = new java.util.ArrayList<>();
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (isValidGridPosition(x, y)) {
                    double distance = getGridDistance(centerX, centerY, x, y);
                    if (distance <= radius) {
                        positions.add(new Point(x, y));
                    }
                }
            }
        }
        
        return positions.toArray(new Point[0]);
    }
    
    /**
     * 检查鼠标点击是否在指定网格内 - 新增方法，用于精确点击检测
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param gridX 目标网格X坐标
     * @param gridY 目标网格Y坐标
     * @return 是否在指定网格内
     */
    public static boolean isMouseInGrid(int mouseX, int mouseY, int gridX, int gridY) {
        Rectangle gridRect = getGridRectangle(gridX, gridY);
        return gridRect.contains(mouseX, mouseY);
    }
    
    /**
     * 获取网格中心的精确像素坐标 - 新增方法，用于特效定位
     * @param gridX 网格X坐标
     * @param gridY 网格Y坐标
     * @return 中心像素坐标Point对象
     */
    public static Point getGridCenter(int gridX, int gridY) {
        return gridToPixel(gridX, gridY);
    }
}