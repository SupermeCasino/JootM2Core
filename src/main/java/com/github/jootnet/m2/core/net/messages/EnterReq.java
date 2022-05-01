package com.github.jootnet.m2.core.net.messages;

import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

/**
 * 进入游戏
 */
public class EnterReq implements Message {

	@Override
	public MessageType type() {
		return MessageType.ENTER_REQ;
	}

	/** 选择进入游戏的角色昵称 */
	public String chrName;
	
	public EnterReq(String chrName) {
		this.chrName = chrName;
	}
}
