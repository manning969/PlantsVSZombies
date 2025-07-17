package com.tedu.manager;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频管理器 - 管理背景音乐和音效
 */
public class AudioManager {
    private static AudioManager instance = null;
    
    // 音频剪辑存储
    private Map<String, Clip> audioClips = new HashMap<>();
    
    // 当前播放的背景音乐
    private Clip currentBgMusic = null;
    
    // 音量控制
    private float musicVolume = 0.8f;  // 背景音乐音量 (0.0 - 1.0)
    private float sfxVolume = 0.7f;    // 音效音量 (0.0 - 1.0)
    
    // 音乐状态
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    
    private AudioManager() {
        // 私有构造函数
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * 加载音频文件
     * @param name 音频名称（用于后续播放）
     * @param filePath 音频文件路径
     * @return 是否加载成功
     */
    public boolean loadAudio(String name, String filePath) {
        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("❌ 音频文件不存在: " + filePath);
                return false;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            audioClips.put(name, clip);
            System.out.println("✅ 音频加载成功: " + name + " -> " + filePath);
            return true;
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("❌ 不支持的音频格式: " + filePath);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("❌ 音频文件读取错误: " + filePath);
            e.printStackTrace();
            return false;
        } catch (LineUnavailableException e) {
            System.err.println("❌ 音频线路不可用: " + filePath);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 播放背景音乐（循环播放）
     * @param name 音频名称
     */
    public void playBackgroundMusic(String name) {
        if (!musicEnabled) return;
        
        // 停止当前背景音乐
        stopBackgroundMusic();
        
        Clip clip = audioClips.get(name);
        if (clip != null) {
            try {
                clip.setFramePosition(0); // 重置到开始位置
                setVolume(clip, musicVolume);
                clip.loop(Clip.LOOP_CONTINUOUSLY); // 循环播放
                currentBgMusic = clip;
                System.out.println("🎵 开始播放背景音乐: " + name);
            } catch (Exception e) {
                System.err.println("❌ 播放背景音乐失败: " + name);
                e.printStackTrace();
            }
        } else {
            System.err.println("❌ 找不到音频: " + name);
        }
    }
    
    /**
     * 播放音效（单次播放）
     * @param name 音效名称
     */
    public void playSound(String name) {
        if (!sfxEnabled) return;
        
        Clip clip = audioClips.get(name);
        if (clip != null) {
            try {
                // 创建新的音效实例以支持同时播放多个音效
                Clip soundClip = AudioSystem.getClip();
                soundClip.open(AudioSystem.getAudioInputStream(
                    new File(getAudioPath(name))));
                
                setVolume(soundClip, sfxVolume);
                soundClip.start();
                
                // 播放完成后自动关闭
                soundClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        soundClip.close();
                    }
                });
                
                System.out.println("🔊 播放音效: " + name);
            } catch (Exception e) {
                System.err.println("❌ 播放音效失败: " + name);
                e.printStackTrace();
            }
        } else {
            System.err.println("❌ 找不到音效: " + name);
        }
    }
    
    /**
     * 停止背景音乐
     */
    public void stopBackgroundMusic() {
        if (currentBgMusic != null && currentBgMusic.isRunning()) {
            currentBgMusic.stop();
            currentBgMusic = null;
            System.out.println("⏹️ 停止背景音乐");
        }
    }
    
    /**
     * 暂停背景音乐
     */
    public void pauseBackgroundMusic() {
        if (currentBgMusic != null && currentBgMusic.isRunning()) {
            currentBgMusic.stop();
            System.out.println("⏸️ 暂停背景音乐");
        }
    }
    
    /**
     * 恢复背景音乐
     */
    public void resumeBackgroundMusic() {
        if (currentBgMusic != null && !currentBgMusic.isRunning()) {
            currentBgMusic.start();
            System.out.println("▶️ 恢复背景音乐");
        }
    }
    
    /**
     * 设置音量
     * @param clip 音频剪辑
     * @param volume 音量 (0.0 - 1.0)
     */
    private void setVolume(Clip clip, float volume) {
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volume) + gainControl.getMinimum();
            gainControl.setValue(gain);
        } catch (Exception e) {
            System.err.println("⚠️ 音量设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置背景音乐音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentBgMusic != null) {
            setVolume(currentBgMusic, musicVolume);
        }
        System.out.println("🔊 背景音乐音量设置为: " + (int)(musicVolume * 100) + "%");
    }
    
    /**
     * 设置音效音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 音效音量设置为: " + (int)(sfxVolume * 100) + "%");
    }
    
    /**
     * 开启/关闭背景音乐
     */
    public void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (!musicEnabled) {
            stopBackgroundMusic();
        }
        System.out.println("🎵 背景音乐: " + (musicEnabled ? "开启" : "关闭"));
    }
    
    /**
     * 开启/关闭音效
     */
    public void toggleSfx() {
        sfxEnabled = !sfxEnabled;
        System.out.println("🔊 音效: " + (sfxEnabled ? "开启" : "关闭"));
    }
    
    /**
     * 获取音频文件路径 - 修复版本
     */
    private String getAudioPath(String name) {
        // 🔧 根据音效名称返回正确的文件路径
        switch (name) {
            // 僵尸相关音效
            case "zombie_eating":
                return "resources/sounds/sfx/zombie_eating.wav";
            case "zombie_groan":
                return "resources/sounds/sfx/zombie_groan.wav";
            case "zombie_death":
                return "resources/sounds/sfx/zombie_death.wav";
            
            // 植物相关音效
            case "plant_place":
                return "resources/sounds/sfx/plant_place.wav";
            case "plant_shoot":
                return "resources/sounds/sfx/plant_shoot.wav";
            case "shovel_dig":
                return "resources/sounds/sfx/shovel_dig.wav";
            
            // 阳光和道具音效
            case "sun_collect":
                return "resources/sounds/sfx/sun_collect.wav";
            case "sun_drop":
                return "resources/sounds/sfx/sun_drop.wav";
            
            // 小推车音效
            case "lawnmower_start":
                return "resources/sounds/sfx/lawnmower_start.wav";
            case "lawnmower_running":
                return "resources/sounds/sfx/lawnmower_running.wav";
            
            // UI音效
            case "button_click":
                return "resources/sounds/sfx/button_click.wav";
            case "card_select":
                return "resources/sounds/sfx/card_select.wav";
            
            // 游戏状态音效
            case "wave_start":
                return "resources/sounds/sfx/wave_start.wav";
            case "wave_complete":
                return "resources/sounds/sfx/wave_complete.wav";
            case "game_over":
                return "resources/sounds/sfx/game_over.wav";
            case "game_restart":
                return "resources/sounds/sfx/game_restart.wav";
            
            // 背景音乐
            case "menu_music":
                return "resources/sounds/music/menu_background.wav";
            case "game_music":
                return "resources/sounds/music/game_background.wav";
            case "day_music":
                return "resources/sounds/music/day_stage.wav";
            case "night_music":
                return "resources/sounds/music/night_stage.wav";
            case "final_wave_music":
                return "resources/sounds/music/final_wave.wav";
            case "victory_music":
                return "resources/sounds/music/victory.wav";
            case "defeat_music":
                return "resources/sounds/music/defeat.wav";
            
            // 默认情况：尝试在sfx目录中查找
            default:
                System.err.println("⚠️ 未知音效名称: " + name + "，使用默认sfx路径");
                return "resources/sounds/sfx/" + name + ".wav";
        }
    }
    
    /**
     * 清理所有音频资源
     */
    public void cleanup() {
        stopBackgroundMusic();
        
        for (Clip clip : audioClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        audioClips.clear();
        
        System.out.println("🧹 音频资源清理完成");
    }
    
    // Getter方法
    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSfxEnabled() { return sfxEnabled; }
    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume() { return sfxVolume; }
    public boolean isBackgroundMusicPlaying() { 
        return currentBgMusic != null && currentBgMusic.isRunning(); 
    }
}