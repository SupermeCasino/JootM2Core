/*
 * Copyright 2017 JOOTNET Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Support: https://github.com/jootnet/mir2.core
 */
package com.github.jootnet.m2.core.map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.jootnet.m2.core.SDK;

/**
 * 地图管理类<br>
 * 地图文件头以Delphi语言描述如下<br>
 * 
 * <pre>
 * TMapHeader = packed record
    wWidth      :Word;                 	//宽度			2
    wHeight     :Word;                 	//高度			2
    sTitle      :String[15]; 			//标题			16
    UpdateDate  :TDateTime;          	//更新日期			8
    VerFlag     :Byte;					//标识(新的格式为02)	1
    Reserved    :array[0..22] of Char;  //保留			23
  end;
 * </pre>
 * 
 * 十周年之后的版本可能出现新版本地图，3KM2中式20110428加入的对新版地图的支持<br>
 * 不过除了问陈天桥之外暂时不能知道新版地图中最后两个字节是干嘛用的
 * 
 * @author 云中双月
 */
public final class Maps {

	/**
	 * 获取一个地图对象
	 *
	 * @param mapPath 地图文件全路径
	 * @param wdBaseUrl 微端基址
	 * @return 解析出来的地图对象
	 */
	public static final Map get(String mapPath, String wdBaseUrl) {
		try {
			var buffer = (ByteBuffer)null;
			if (Files.exists(Paths.get(SDK.repairFileName(mapPath)))) {
				buffer = ByteBuffer.wrap(Files.readAllBytes(Paths.get(SDK.repairFileName(mapPath)))).order(ByteOrder.LITTLE_ENDIAN);
			} else {
				if (!wdBaseUrl.endsWith("/")) wdBaseUrl += "/";
				var mapUrl = wdBaseUrl + "map/"
						+ new File(mapPath).getName().toLowerCase();
				var url = new URL(mapUrl);
				var conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(1000);
				conn.setReadTimeout(3000);
				conn.setRequestMethod("GET");
				conn.connect();
	
				var respCode = conn.getResponseCode();
				if (respCode < 200 || respCode >= 300) {
					conn.disconnect();
					return null;
				}
				try (var bos = new ByteArrayOutputStream()) {
					try (var is = conn.getInputStream()) {
						var readLen = 0;
						var buf = new byte[4096];
	
						while ((readLen = is.read(buf)) > 0) {
							bos.write(buf, 0, readLen);
						}
					}
					var dData = bos.toByteArray();
					try {
						dData = SDK.unzip(dData);
					} catch (IOException ex) {}
					if (!Files.exists(Paths.get(mapPath).getParent())) {
						Files.createDirectories(Paths.get(mapPath).getParent());
					}
					Files.write(Paths.get(mapPath), dData);
					buffer = ByteBuffer.wrap(dData).order(ByteOrder.LITTLE_ENDIAN);
				}
	
				conn.disconnect();
			}
			Map ret = new Map();
			ret.setWidth(buffer.getShort());
			ret.setHeight(buffer.getShort());
			/*
			 * br_map.skipBytes(24); boolean newMapFlag = br_map.readByte() == 2; //
			 * 新版地图每一个Tile占用14个字节，最后的两个字节作用未知 br_map.skipBytes(23);
			 */
			buffer.position(buffer.position() + 48);
			int tileByteSize = buffer.remaining() / ret.getWidth() / ret.getHeight();
			MapTileInfo[][] mapTileInfos = new MapTileInfo[ret.getWidth()][ret.getHeight()];
			for (int width = 0; width < ret.getWidth(); ++width)
				for (int height = 0; height < ret.getHeight(); ++height) {
					MapTileInfo mi = new MapTileInfo();
					// 读取背景
					short bng = buffer.getShort();
					// 读取中间层
					short mid = buffer.getShort();
					// 读取对象层
					short obj = buffer.getShort();
					// 设置背景
					if ((bng & 0x7fff) > 0) {
						mi.setBngImgIdx((short) ((bng & 0x7fff) - 1));
						mi.setHasBng(true);
					}
					// 设置中间层
					if ((mid & 0x7fff) > 0) {
						mi.setMidImgIdx((short) ((mid & 0x7fff) - 1));
						mi.setHasMid(true);
					}
					// 设置对象层
					if ((obj & 0x7fff) > 0) {
						mi.setObjImgIdx((short) ((obj & 0x7fff) - 1));
						mi.setHasObj(true);
					}
					// 设置是否可站立
					mi.setCanWalk((bng & 0x8000) != 0x8000 && (obj & 0x8000) != 0x8000);
					// 设置是否可飞行
					mi.setCanFly((obj & 0x8000) != 0x8000);

					// 读取门索引(第7个byte)
					byte btTmp = buffer.get();
					if ((btTmp & 0x80) == 0x80) {
						mi.setDoorCanOpen(true);
					}
					mi.setDoorIdx((byte) (btTmp & 0x7F));
					// 读取门偏移(第8个byte)
					btTmp = buffer.get();
					if (btTmp != 0) {
						mi.setHasDoor(true);
					}
					mi.setDoorOffset((short) (btTmp & 0xFF));
					// 读取动画帧数(第9个byte)
					btTmp = buffer.get();
					if ((btTmp & 0x7F) > 0) {
						mi.setAniFrame((byte) (btTmp & 0x7F));
						mi.setHasAni(true);
						mi.setHasObj(false);
						mi.setAniBlendMode((btTmp & 0x80) == 0x80);
					}
					// 读取并设置动画跳帧数(第10个byte)
					mi.setAniTick(buffer.get());
					// 读取资源文件索引(第11个byte)
					mi.setObjFileIdx(buffer.get());
					if (mi.getObjFileIdx() != 0)
						mi.setObjFileIdx((byte) (mi.getObjFileIdx() + 1));
					// 读取光照(第12个byte)
					mi.setLight(buffer.get());
					if (tileByteSize == 14) {
						mi.setBngFileIdx(buffer.get());
						if (mi.getBngFileIdx() != 0)
							mi.setBngFileIdx((byte) (mi.getBngFileIdx() + 1));
						mi.setMidFileIdx(buffer.get());
						if (mi.getMidFileIdx() != 0)
							mi.setMidFileIdx((byte) (mi.getMidFileIdx() + 1));
					} else if (tileByteSize > 14) {
						buffer.position(buffer.position() + tileByteSize - 14);
						System.err.println(mapPath + " have unkwon tileByteSize " + tileByteSize);
					}
					if (width % 2 != 0 || height % 2 != 0)
						mi.setHasBng(false);
					mapTileInfos[width][height] = mi;
				}
			ret.setMapTiles(mapTileInfos);
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
