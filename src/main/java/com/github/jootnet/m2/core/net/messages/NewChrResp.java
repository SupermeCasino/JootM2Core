package com.github.jootnet.m2.core.net.messages;

import java.io.DataOutput;
import java.io.IOException;

import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

/**
 * 创建角色响应
 * 
 * @author linxing
 *
 */
public class NewChrResp extends Message {
	
	static {
		deSerializers.put(MessageType.NEW_CHR_RESP, buffer -> {
			var code = buffer.getInt();
			var serverTip = unpackString(buffer);
			return new NewChrResp(code, serverTip);
		});
	}

	@Override
	public MessageType type() {
		return MessageType.NEW_CHR_RESP;
	}
	
	/**
	 * 错误码
	 * <br>
	 * 0:成功 1:角色已满 2:昵称已存在 3:昵称不合法 99:其他
	 */
	public int code;
	/** 服务端提示信息 */
	public String serverTip;

	public NewChrResp(int code, String serverTip) {
		this.code = code;
		this.serverTip = serverTip;
	}

	@Override
	protected void packCore(DataOutput buffer) throws IOException {
		buffer.writeInt(code);
		packString(serverTip, buffer);
	}

}
