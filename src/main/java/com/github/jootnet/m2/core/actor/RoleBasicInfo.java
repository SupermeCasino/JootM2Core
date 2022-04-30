package com.github.jootnet.m2.core.actor;

/**
 * 角色基础信息
 */
public final class RoleBasicInfo {

	/** 昵称 */
	public String name;
	/** 职业 */
	public Occupation occupation;
	/** 等级 */
	public int level;
	/** 血量 */
	public int hp;
	/** 蓝量 */
	public int mp;
	
	/** 衣服文件索引 */
	public int humFileIdx;
	/** 衣服文件内编号 */
	public int humIdx;
	/** 翅膀文件索引 */
	public int humEffectFileIdx;
	/** 翅膀文件内编号 */
	public int humEffectIdx;
	/** 武器文件索引 */
	public int weaponFileIdx;
	/** 武器文件内编号 */
	public int weaponIdx;
	/** 武器特效文件索引 */
	public int weaponEffectFileIdx;
	/** 武器特效文件内编号 */
	public int weaponEffectIdx;
	
	/** 挂机地图 */
	public String mapNo;
	/** 身处地图x坐标 */
	public int x;
	/** 身处地图y坐标 */
	public int y;
	
	/** 当前动作 */
	public HumActionInfo action;
	/** 动作开始时间 */
	public long actionStartTime;
	/** 动作帧号 */
	public short actionTick;
	/** 动作帧开始时间 */
	public long actionFrameStartTime;
	/** 动作造成的像素偏移x */
	public int shiftX;
	/** 动作造成的像素偏移y */
	public int shiftY;
	/** 动作完成后应该更新的地图坐标x */
	public int nextX;
	/** 动作完成后应该更新的地图坐标y */
	public int nextY;
}
