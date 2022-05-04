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
	/** 魔法攻击力 */
	public int magicAttackPoint;
	/** 道术攻击力 */
	public int taositAttackPoint;
	/** 防御力 */
	public int defensePoint;
	/** 魔法防御力 */
	public int magicDefensePoint;
	
	public ChrPublicInfo(int attackPoint, int magicAttackPoint, int taositAttackPoint, int defensePoint,
			int magicDefensePoint) {
		this.attackPoint = attackPoint;
		this.magicAttackPoint = magicAttackPoint;
		this.taositAttackPoint = taositAttackPoint;
		this.defensePoint = defensePoint;
		this.magicDefensePoint = magicDefensePoint;
	}
	
}
