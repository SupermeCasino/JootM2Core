package com.github.jootnet.m2.core.actor;

/**
 * 人物公开属性
 * <br>
 * 这些属性可以被近身玩家通过ctrl+右键方式查看
 * 
 * @author linxing
 *
 */
public final class ChrPublicInfo {

	/** 攻击力 */
	public int attackPoint;
	/** 攻击力上限 */
	public int maxAttackPoint;
	/** 魔法攻击力 */
	public int magicAttackPoint;
	/** 魔法攻击力上限 */
	public int maxMagicAttackPoint;
	/** 道术攻击力 */
	public int taositAttackPoint;
	/** 道术攻击力上限 */
	public int maxTaositAttackPoint;
	/** 防御力 */
	public int defensePoint;
	/** 防御力上限 */
	public int maxDefensePoint;
	/** 魔法防御力 */
	public int magicDefensePoint;
	/** 魔法防御力上限 */
	public int maxMagicDefensePoint;
	
	public ChrPublicInfo(int attackPoint, int maxAttackPoint, int magicAttackPoint, int maxMagicAttackPoint,
			int taositAttackPoint, int maxTaositAttackPoint, int defensePoint, int maxDefensePoint,
			int magicDefensePoint, int maxMagicDefensePoint) {
		this.attackPoint = attackPoint;
		this.maxAttackPoint = maxAttackPoint;
		this.magicAttackPoint = magicAttackPoint;
		this.maxMagicAttackPoint = maxMagicAttackPoint;
		this.taositAttackPoint = taositAttackPoint;
		this.maxTaositAttackPoint = maxTaositAttackPoint;
		this.defensePoint = defensePoint;
		this.maxDefensePoint = maxDefensePoint;
		this.magicDefensePoint = magicDefensePoint;
		this.maxMagicDefensePoint = maxMagicDefensePoint;
	}
}
