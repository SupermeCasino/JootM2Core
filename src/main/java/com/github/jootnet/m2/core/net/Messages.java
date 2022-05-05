package com.github.jootnet.m2.core.net;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.jootnet.m2.core.actor.Action;
import com.github.jootnet.m2.core.actor.ChrBasicInfo;
import com.github.jootnet.m2.core.net.messages.EnterReq;
import com.github.jootnet.m2.core.net.messages.HumActionChange;
import com.github.jootnet.m2.core.net.messages.LoginReq;

/**
 * 消息工具类
 */
public final class Messages {
	
	/**
	 * 将消息打包到数据缓冲区
	 * 
	 * @param message 消息
	 * @param buffer 缓冲区
	 * @throws IOException 
	 */
	public static byte[] pack(Message message) throws IOException {
		return message.pack();
	}
	
	/**
	 * 解析数据包
	 * 
	 * @param buffer 数据
	 * @return 解析的数据包或null
	 */
	public static Message unpack(ByteBuffer buffer) {
		return Message.unpack(buffer);
	}
    
    /**
     * 为人物新动作创建消息
     * 
     * @param hum 人物
     * @return 人物动作更新消息
     */
    public static Message humActionChange(ChrBasicInfo hum) {
        var step = 1;
		if (hum.action.act == Action.Run) step++;
        var nx = hum.x;
        var ny = hum.y;
        switch (hum.action.dir) {
            case North:
                ny -= step;
                break;
            case NorthEast:
                ny -= step;
                nx += step;
                break;
            case East:
                nx += step;
                break;
            case SouthEast:
                ny += step;
                nx += step;
                break;
            case South:
                ny += step;
                break;
            case SouthWest:
                ny += step;
                nx -= step;
                break;
            case West:
                nx -= step;
                break;
            case NorthWest:
                ny -= step;
                nx -= step;
                break;

            default:
                break;
        }
        return new HumActionChange(hum.name, hum.x, hum.y, nx, ny, hum.action);
    }
    
    /**
     * 为玩家登录创建数据封包
     * 
     * @param una 用户名
     * @param psw 密码
     * @return 数据封包
     */
    public static Message loginReq(String una, String psw) {
    	return new LoginReq(una, psw);
    }
    
    /**
     * 为选择角色创建数据封包
     * 
     * @param chrName 角色昵称
     * @return 数据封包
     */
    public static Message selectChr(String chrName) {
    	return new EnterReq(chrName);
    }
}
