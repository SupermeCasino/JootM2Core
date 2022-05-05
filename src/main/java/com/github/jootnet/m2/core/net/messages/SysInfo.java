package com.github.jootnet.m2.core.net.messages;

import java.io.DataOutput;
import java.io.IOException;

import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

public final class SysInfo extends Message {
	
	static {
		deSerializers.put(MessageType.SYS_INFO, buffer -> {
			if (!buffer.hasRemaining())
				return null;
			var time = buffer.getLong();
			var mapCount = buffer.getInt();
			var mapNos = new String[mapCount];
			var mapNames = new String[mapCount];
			var mapMMaps = new int[mapCount];
			for (var i = 0; i < mapCount; ++i) {
				mapNos[i] = unpackString(buffer);
			}
			for (var i = 0; i < mapCount; ++i) {
				mapNames[i] = unpackString(buffer);
			}
			for (var i = 0; i < mapCount; ++i) {
				mapMMaps[i] = buffer.getInt();
			}
			return new SysInfo(time, mapCount, mapNos, mapNames, mapMMaps);
		});
	}

	@Override
	public MessageType type() {
		return MessageType.SYS_INFO;
	}

	/** unix秒数 */
	public long time;
	/** 地图数 */
	public int mapCount;
	/** 地图编号 */
	public String[] mapNos;
	/** 地图名称 */
	public String[] mapNames;
	/** 小地图图片编号 */
	public int[] mapMMaps;
	
	public SysInfo(long time, int mapCount, String[] mapNos, String[] mapNames, int[] mapMMaps) {
		this.time = time;
		this.mapCount = mapCount;
		this.mapNos = mapNos;
		this.mapNames = mapNames;
		this.mapMMaps = mapMMaps;
	}

	@Override
	protected void packCore(DataOutput buffer) throws IOException {
		buffer.writeLong(time);
		buffer.writeInt(mapCount);
		for (var i = 0; i < mapCount; ++i) {
			packString(mapNos[i], buffer);
		}
		for (var i = 0; i < mapCount; ++i) {
			packString(mapNames[i], buffer);
		}
		for (var i = 0; i < mapCount; ++i) {
			buffer.writeInt(mapMMaps[i]);
		}
	}
	
	
}
