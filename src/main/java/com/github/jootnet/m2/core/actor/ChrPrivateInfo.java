package com.github.jootnet.m2.core.actor;


/**
 * 人物私有属性
 * <br>
 * 这些信息自能自己知晓
 * 
 * @author linxing
 *
 */
public final class ChrPrivateInfo {

	/** 当前经验值 */
	public int exp;
	/** 最大经验值 */
	public int levelUpExp;
	/** 背包重量 */
	public int bagWeight;
	/** 最大背包负重 */
	public int maxBagWeight;
	/** 穿戴重量 */
	public int wearWeight;
	/** 最大可穿戴重量 */
	public int maxWearWeight;
	/** 腕力 */
	public int handWeight;
	/** 最大腕力 */
	public int maxHandWeight;
	
	public ChrPrivateInfo(int exp, int levelUpExp, int bagWeight, int maxBagWeight, int wearWeight, int maxWearWeight,
			int handWeight, int maxHandWeight) {
		this.exp = exp;
		this.levelUpExp = levelUpExp;
		this.bagWeight = bagWeight;
		this.maxBagWeight = maxBagWeight;
		this.wearWeight = wearWeight;
		this.maxWearWeight = maxWearWeight;
		this.handWeight = handWeight;
		this.maxHandWeight = maxHandWeight;
	}
}
