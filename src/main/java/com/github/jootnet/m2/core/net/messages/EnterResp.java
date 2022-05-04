package com.github.jootnet.m2.core.net.messages;

import com.github.jootnet.m2.core.actor.ChrBasicInfo;
import com.github.jootnet.m2.core.actor.ChrPrivateInfo;
import com.github.jootnet.m2.core.actor.ChrPublicInfo;
import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

/**
 * 进入游戏响应
 * <br>
 * 这个数据包会发送给当前客户端以及地图内其他玩家
 * <br>
 * 当前客户端如果收到forbidTip，则证明角色不可用，可能被封禁
 * <br>
 * 	其他玩家收到的forbidTip永远为null
 */
public class EnterResp implements Message {

	@Override
	public MessageType type() {
		return MessageType.ENTER_RESP;
	}

	/** 角色禁用原因 */
	public String forbidTip;
	/** 角色基础信息 */
	public ChrBasicInfo cBasic;
	/** 角色可探查信息 */
	public ChrPublicInfo cPublic;
	/** 角色私有信息 */
	public ChrPrivateInfo cPri;
	
	public EnterResp(String forbidTip, ChrBasicInfo cBasic, ChrPublicInfo cPublic, ChrPrivateInfo cPri) {
		this.forbidTip = forbidTip;
		this.cBasic = cBasic;
		this.cPublic = cPublic;
		this.cPri = cPri;
	}
	
	
}
