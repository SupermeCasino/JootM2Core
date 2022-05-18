package com.github.jootnet.m2.core.net;

/**
 * 网络消息类型
 */
public enum MessageType {
    /**
     * 更新人物动作
     */
    HUM_ACTION_CHANGE(1001),
    /**
     * 登陆
     * <br>
     * 客户端发送用户名密码到服务端
     */
	LOGIN_REQ(1002),
	/**
	 * 登陆结果
	 */
	LOGIN_RESP(1003),
	/** 进入游戏世界 */
	ENTER_REQ(1004),
	/** 进入游戏世界结果 */
	ENTER_RESP(1005),
	/** 客户端连上时服务器返回一些配置信息 */
	SYS_INFO(1006),
	/** 新用户 */
	NEW_USER_REQ(1007),
	NEW_USER_RESP(1008),
	/** 新角色 */
	NEW_CHR_REQ(1009),
	NEW_CHR_RESP(1010),
	/** 离开游戏世界 */
	OUT_REQ(1011),
	/** 离开游戏世界结果 */
	OUT_RESP(1012),
	/** 被踢出登录 */
	KICKED_OUT(1013),
	;

    private int id_;
    private MessageType(int id) {
        this.id_ = id;
    }
    /**
     * 获取动作的数值编号表示
     * 
     * @return 当前消息类型的数值编号表示
     */
    public int id() {
        return this.id_;
    }
}
