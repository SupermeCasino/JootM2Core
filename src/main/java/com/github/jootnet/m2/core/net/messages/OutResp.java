package com.github.jootnet.m2.core.net.messages;

import java.io.DataOutput;
import java.io.IOException;

import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

/**
 * 离开游戏世界结果
 * 
 * @author LinXing
 *
 */
public class OutResp extends Message {
	
	static {
		deSerializers.put(MessageType.OUT_RESP, buffer -> {
			var code = buffer.getInt();
			var serverTip = unpackString(buffer);
			var chrName = unpackString(buffer);
			return new OutResp(code, serverTip, chrName);
		});
	}
	
	/**
	 * 错误码
	 * <br>
	 * 0 成功 99未知错误
	 */
	public int code;
	/**
	 * 服务端提示信息
	 */
	public String serverTip;
	/**
	 * 角色昵称
	 */
	public String chrName;

	public OutResp(int code, String serverTip, String chrName) {
		this.code = code;
		this.serverTip = serverTip;
		this.chrName = chrName;
	}

	@Override
	public MessageType type() {
		return MessageType.OUT_RESP;
	}

	@Override
	protected void packCore(DataOutput buffer) throws IOException {
		buffer.writeInt(code);
		packString(serverTip, buffer);
		packString(chrName, buffer);
	}

}
