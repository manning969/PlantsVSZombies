package com.tedu.element.effects;

import com.tedu.element.ElementObj;
import com.tedu.manager.GameLoad;
import com.tedu.utils.GameConfig;
import javax.swing.ImageIcon;
import java.awt.Graphics;

/**
 * 僵尸头掉落动画特效
 */
public class HeadDropEffect extends ElementObj {
    private ImageIcon headGif;
    private int frameCount = 8; // 动画持续帧数
    private int currentFrame = 0;
    private int startX, startY;
    private int fallDistance = 40; // 掉落总距离

    public HeadDropEffect(int x, int y, int w, int h) {
        // 放大系数
        double scale = 1.5;
        int bigW = (int)(GameConfig.ZOMBIE_WIDTH * scale);
        int bigH = (int)(GameConfig.ZOMBIE_HEIGHT * scale);
        // 让中心点和原僵尸一致
        int bigX = x - (bigW - GameConfig.ZOMBIE_WIDTH) / 2;
        int bigY = y - (bigH - GameConfig.ZOMBIE_HEIGHT) / 2;
        super.setX(bigX);
        super.setY(bigY);
        super.setW(bigW);
        super.setH(bigH);
        this.startX = bigX;
        this.startY = bigY;
        this.headGif = GameLoad.imgMap.get("normal_head");
    }

    @Override
    public void showElement(Graphics g) {
        if (headGif != null) {
            // 让头部有一个下落的动画效果
            int offsetY = (int) ((double) currentFrame / frameCount * fallDistance);
            g.drawImage(headGif.getImage(), startX, startY + offsetY, getW(), getH(), null);
        }
        currentFrame++;
        if (currentFrame >= frameCount) {
            this.setLive(false); // 动画播放完毕自动消失
        }
    }

    @Override
    public ElementObj createElement(String str) {
        // 格式: x,y,w,h
        String[] params = str.split(",");
        if (params.length >= 4) {
            int x = Integer.parseInt(params[0]);
            int y = Integer.parseInt(params[1]);
            int w = Integer.parseInt(params[2]);
            int h = Integer.parseInt(params[3]);
            return new HeadDropEffect(x, y, w, h);
        }
        return null;
    }
} 