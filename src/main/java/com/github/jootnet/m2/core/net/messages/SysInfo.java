package com.github.jootnet.m2.core.net.messages;

import com.github.jootnet.m2.core.net.Message;
import com.github.jootnet.m2.core.net.MessageType;

public final class SysInfo implements Message {

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
	
	
}
