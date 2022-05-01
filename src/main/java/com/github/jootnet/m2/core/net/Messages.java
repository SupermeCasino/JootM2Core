package com.github.jootnet.m2.core.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.github.jootnet.m2.core.actor.Action;
import com.github.jootnet.m2.core.actor.Direction;
import com.github.jootnet.m2.core.actor.HumActionInfo;
import com.github.jootnet.m2.core.actor.Occupation;
import com.github.jootnet.m2.core.actor.ChrBasicInfo;
import com.github.jootnet.m2.core.net.messages.EnterReq;
import com.github.jootnet.m2.core.net.messages.EnterResp;
import com.github.jootnet.m2.core.net.messages.HumActionChange;
import com.github.jootnet.m2.core.net.messages.LoginReq;
import com.github.jootnet.m2.core.net.messages.LoginResp;

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
		var stream = new ByteArrayOutputStream();
		var buffer = new DataOutputStream(stream);
		// 0.类型
		buffer.writeInt(message.type().id());
		
		switch (message.type()) {
		
		case HUM_ACTION_CHANGE: {
			var humActionChange = (HumActionChange) message;
			// 1.人物姓名
			pack(humActionChange.name, buffer);
			// 2.当前坐标以及动作完成后的坐标
			buffer.writeShort((short) humActionChange.x);
			buffer.writeShort((short) humActionChange.y);
			buffer.writeShort((short) humActionChange.nextX);
			buffer.writeShort((short) humActionChange.nextY);
			// 3.动作
			pack(humActionChange.action, buffer);
			break;
		}
		
		case LOGIN_REQ: {
			var loginReq = (LoginReq) message;
			// 1.用户名
			pack(loginReq.una, buffer);
			// 2.密码
			pack(loginReq.psw, buffer);
			break;
		}
		
		case LOGIN_RESP: {
			var loginResp = (LoginResp) message;
			// 1.错误码
			buffer.writeInt(loginResp.code);
			// 2.服务端消息
			pack(loginResp.serverTip, buffer);
			// 3.角色列表
			if (loginResp.roles != null) {
				buffer.writeByte((byte) loginResp.roles.length);
				for (var r : loginResp.roles) {
					buffer.writeInt(r.type);
					buffer.writeInt(r.level);
					pack(r.name, buffer);
					pack(r.mapNo, buffer);
					buffer.writeShort(r.x);
					buffer.writeShort(r.y);
				}
			} else {
				buffer.writeByte(0);
			}
			// 4.上次选择的昵称
			pack(loginResp.lastName, buffer);
			break;
		}
		case ENTER_REQ: {
			var enterReq = (EnterReq) message;
			pack(enterReq.chrName, buffer);
			break;
		}
		case ENTER_RESP: {
			var enterResp = (EnterResp) message;
			pack(enterResp.forbidTip, buffer);
			pack(enterResp.cbi, buffer);
			break;
		}
		
		
		default:
			return null;
		}

		buffer.flush();
		return stream.toByteArray();
	}
	
	/**
	 * 解析数据包
	 * 
	 * @param bytes 数据
	 * @return 解析的数据包或null
	 */
	public static Message unpack(byte[] bytes) {
		var buffer = ByteBuffer.wrap(bytes);
		MessageType type = null;
		
		var typeId = buffer.getInt();
		for (var msgType : MessageType.values()) {
			if (msgType.id() == typeId) {
				type = msgType;
				break;
			}
		}
		
		if (type == null) return null;
		
		switch (type) {
		
		case HUM_ACTION_CHANGE: {
			String name = unpack(buffer);
			var x = buffer.getShort();
			var y = buffer.getShort();
			var nx = buffer.getShort();
			var ny = buffer.getShort();
			var humActionInfo = new HumActionInfo();
			unpack(humActionInfo, buffer);
			return new HumActionChange(name, x, y, nx, ny, humActionInfo);
		}
		
		case LOGIN_REQ: {
			String una = unpack(buffer);
			String psw = unpack(buffer);
			return new LoginReq(una, psw);
		}
		
		case LOGIN_RESP: {
			var code = buffer.getInt();
			String serverTip = unpack(buffer);
			int roleCount = buffer.get();
			var roles = new LoginResp.Role[roleCount];
			for (var i = 0; i < roleCount; ++i) {
				roles[i] = new LoginResp.Role();
				roles[i].type = buffer.getInt();
				roles[i].level = buffer.getInt();
				roles[i].name = unpack(buffer);
				roles[i].mapNo = unpack(buffer);
				roles[i].x = buffer.getShort();
				roles[i].y = buffer.getShort();
			}
			String lastName = unpack(buffer);
			return new LoginResp(code, serverTip, roles, lastName);
		}
		
		case ENTER_REQ: {
			return new EnterReq(unpack(buffer));
		}
		
		case ENTER_RESP: {
			var forbidTip = unpack(buffer);
			if (forbidTip != null) {
				return new EnterResp(forbidTip, null);
			}
			return new EnterResp(null, unpackChrBasicInfo(buffer));
		}
		
		default:
			break;
		}
		
		return null;
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
    
    private static void pack(String str, DataOutput buffer) throws IOException {
    	if (str == null) {
    		buffer.writeByte(0);
    		return;
    	}
    	var bytes = str.getBytes(StandardCharsets.UTF_8);
    	buffer.writeByte((byte) bytes.length);
    	buffer.write(bytes);
    }
    private static String unpack(ByteBuffer buffer) {
    	var bytesLen = buffer.get();
		String str = null;
		if (bytesLen > 0) {
			var bytes = new byte[bytesLen];
			buffer.get(bytes);
			str = new String(bytes, StandardCharsets.UTF_8);
		}
		return str;
    }
    private static void pack(HumActionInfo info, DataOutput buffer) throws IOException {
    	buffer.writeByte((byte) info.act.ordinal());
    	buffer.writeByte((byte) info.dir.ordinal());
    	buffer.writeShort(info.frameIdx);
    	buffer.writeShort(info.frameCount);
    	buffer.writeShort(info.duration);
    }
    private static void unpack(HumActionInfo info, ByteBuffer buffer) {
    	byte actOrdinal = buffer.get();
    	for (var act : Action.values()) {
    		if (act.ordinal() == actOrdinal) {
    			info.act = act;
    			break;
    		}
    	}
    	byte dirOrdinal = buffer.get();
    	for (var dir : Direction.values()) {
    		if (dir.ordinal() == dirOrdinal) {
    			info.dir = dir;
    			break;
    		}
    	}
    	info.frameIdx = buffer.getShort();
    	info.frameCount = buffer.getShort();
    	info.duration = buffer.getShort();
    }
    private static void pack(ChrBasicInfo info, DataOutput buffer) throws IOException {
    	pack(info.name, buffer);
    	buffer.writeInt(info.occupation.ordinal());
    	buffer.writeInt(info.level);
    	buffer.writeInt(info.hp);
    	buffer.writeInt(info.mp);
    	buffer.writeInt(info.humFileIdx);
    	buffer.writeInt(info.humIdx);
    	buffer.writeInt(info.humEffectFileIdx);
    	buffer.writeInt(info.humEffectIdx);
    	buffer.writeInt(info.weaponFileIdx);
    	buffer.writeInt(info.weaponIdx);
    	buffer.writeInt(info.weaponEffectFileIdx);
    	buffer.writeInt(info.weaponEffectIdx);
    	pack(info.mapNo, buffer);
    	buffer.writeInt(info.x);
    	buffer.writeInt(info.y);
    }
    private static ChrBasicInfo unpackChrBasicInfo(ByteBuffer buffer) {
    	var name = unpack(buffer);
    	var oi = buffer.getInt();
    	Occupation occ = null;
    	for (var item : Occupation.values()) {
    		if (item.ordinal() == oi) {
    			occ = item;
    			break;
    		}
    	}
    	var level = buffer.getInt();
    	var hp = buffer.getInt();
    	var mp = buffer.getInt();
    	var humFileIdx = buffer.getInt();
    	var humIdx = buffer.getInt();
    	var humEffectFileIdx = buffer.getInt();
    	var humEffectIdx = buffer.getInt();
    	var weaponFileIdx = buffer.getInt();
    	var weaponIdx = buffer.getInt();
    	var weaponEffectFileIdx = buffer.getInt();
    	var weaponEffectIdx = buffer.getInt();
    	var mapNo = unpack(buffer);
    	var x = buffer.getInt();
    	var y = buffer.getInt();
    	var ret = new ChrBasicInfo(name, occ, level, hp, mp, mapNo, x, y);
    	ret.humFileIdx = humFileIdx;
    	ret.humIdx = humIdx;
    	ret.humEffectFileIdx = humEffectFileIdx;
    	ret.humEffectIdx = humEffectIdx;
    	ret.weaponFileIdx = weaponFileIdx;
    	ret.weaponIdx = weaponIdx;
    	ret.weaponEffectFileIdx = weaponEffectFileIdx;
    	ret.weaponEffectIdx = weaponEffectIdx;
    	return ret;
    }
}
