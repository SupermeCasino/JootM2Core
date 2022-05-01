package com.github.jootnet.m2.core.net.messages;

import com.github.jootnet.m2.core.actor.ChrBasicInfo;
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
	/** 角色的状态 */
	public ChrBasicInfo cbi;
	
	public EnterResp(String forbidTip, ChrBasicInfo cbi) {
		this.forbidTip = forbidTip;
		this.cbi = cbi;
	}
}
