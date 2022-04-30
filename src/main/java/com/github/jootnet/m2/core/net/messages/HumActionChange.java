package com.github.jootnet.m2.core.net.messages;

import com.github.jootnet.m2.core.actor.HumActionInfo;
import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

/**
 * 告知玩家动作更改的消息
 */
public final class HumActionChange implements Message {

    @Override
    public MessageType type() {
        return MessageType.HUM_ACTION_CHANGE;
    }
    
    /** 动作发生的玩家名称*/
    public String name;
    /** 获取动作发生时身处的横坐标 */
    public int x;
    /** 获取动作发生时身处的纵坐标 */
    public int y;
    /** 角色当前动作完成之后应该到达的横坐标 */
    public int nextX;
    /** 角色当前动作完成之后应该到达的纵坐标 */
    public int nextY;
    /** 玩家动作 */
    public HumActionInfo action;

    public HumActionChange(String name, int x, int y, int nextX, int nextY, HumActionInfo action) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.nextX = nextX;
        this.nextY = nextY;
        this.action = action;
    }
}
