package com.github.jootnet.m2.core.net.messages;

import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

public class LoginResp implements Message {
	
	/**
	 * 角色
	 */
	public static class Role {
		public int type; // 0:战士 1:法师 2:道士 3:刺客
		public int level; // 等级 1-1000
		public String name; // 昵称
		public String mapNo; // 挂机地图
		public short x; // 挂机x坐标
		public short y; // 挂机x坐标
	}

	@Override
	public MessageType type() {
		return MessageType.LOGIN_RESP;
	}
	/**
	 * 登陆相应错误码
	 * <br>
	 * 0 登陆成功
	 * 1 用户名或密码错误
	 * 2 用户不存在
	 */
	public int code;
	/** 服务端提示信息 */
	public String serverTip;
	/** 账号已有角色列表 */
	public Role[] roles;
	/** 上次进入游戏的昵称 */
	public String lastName;
	
	public LoginResp(int code, String serverTip, Role[] roles, String lastName) {
		this.code = code;
		this.serverTip = serverTip;
		this.roles = roles;
		this.lastName = lastName;
	}
}
