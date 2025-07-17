package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

//所有元素的基类
//抽象类
public abstract class ElementObj {
	protected int x;
    private int y;
    private int w;
    private int h;
    private ImageIcon icon;//图片对象，可以获取到原始图片的属性
    //各种必要的状态值
    private boolean live = true;//生存状态，true代表存在，false代表死亡
    //生存，死亡，隐身，无敌
    
    //默认构造
    public ElementObj() {//无参构造，继承的时候不报错
    	
    }
    
    //带参数的构造方法，可由子类传入参数到父类
    /**
     * @param x 左上角X坐标
     * @param y 左上角y坐标
     * @param w w宽度
     * @param h h高度
     * @param icon 图片
     */
	public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.icon = icon;
	}
    
	//抽象方法
	/**
	 * @param g 画笔用于绘画
	 */
	public abstract void showElement(Graphics g);

	/**
	 * 1. 使用父类定义接受键盘事件的方法：只有需要实现键盘监听的子类，重写此方法
	 * 2. 使用接口的方式：需要在监听类进行类型转换
	 * 约定 配置
	 * @param bl 点击的类型 true：按下 false：松开
	 * @param key 触发的键盘的code值
	 * @扩展：本方法是否可以分为2个方法？1个接收按下，1个接受松开
	 */
	public void keyClick(boolean bl, int key) {//不是强制需要重写的
		System.out.println("测试使用");
	}
	
	/**
	 * @说明 移动方法; 需要移动的子类，请 重写这个方法
	 */
	protected void move() {	
		
	}
	
	/**
	 * @设计模式 模板模式;在模板模式中定义 对象执行方法的先后顺序,由子类选择性重写方法
	 *        1.移动  2.换装  3.子弹发射
	 * @说明 去掉final，允许子类重写，但提供默认的标准模板实现
	 * @设计理念 平衡安全性与灵活性：
	 *           - 大部分子类使用默认模板（安全）
	 *           - 特殊子类可以完全自定义（灵活）
	 *           - 通过文档和规范引导正确使用
	 */
	public void model(long gameTime) {
		// 生存状态检查 - 这是必须的安全检查
		if (!this.isLive()) {
			return;
		}
		
		// 调用标准模板方法
		executeStandardTemplate(gameTime);
	}
	
	/**
	 * 标准模板执行方法 - 提供给子类调用的标准流程
	 * @param gameTime 游戏时间
	 */
	protected final void executeStandardTemplate(long gameTime) {
		// 先换装
		updateImage();
		// 再移动
		move();
		// 再发射子弹/执行特殊行动
		add(gameTime);
	}
	
	/**
	 * 自定义行为模板方法 - 为需要特殊行为的子类提供的钩子方法
	 * @param gameTime 游戏时间
	 * @说明 子类如果需要完全自定义行为，可以重写model方法，
	 *      但建议在适当的地方调用executeStandardTemplate或其组成部分
	 */
	protected void executeCustomBehavior(long gameTime) {
		// 默认调用标准模板
		executeStandardTemplate(gameTime);
	}
	
	protected void updateImage() {}
	public void update(long deltaTime) {
        // 子类需要重写这个方法
    }
	protected void add(long gameTime){}
	
//	死亡方法  给子类继承的
	public void die() {  //死亡也是一个对象
		
	}
	
    public  ElementObj createElement(String str) {
		
		return null;
	}

    /**
	 * @说明 本方法返回 元素的碰撞矩形对象(实时返回)
	 * @return
	 */
	public Rectangle getRectangle() {
//		可以将这个数据进行处理 
		return new Rectangle(x,y,w,h);
	}
	
	/**
	 * @说明 碰撞方法
	 * 一个是 this对象 一个是传入值 obj
	 * @param obj
	 * @return boolean 返回true 说明有碰撞，返回false说明没有碰撞
	 */
	public boolean pk(ElementObj obj) {	
		return this.getRectangle().intersects(obj.getRectangle());
	}
	
	//只要是VO类，就要为属性生成get和set方法
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
	public boolean isLive() {
		return live;
	}
	
	public void setLive(boolean live) {
		this.live = live;
	}
}