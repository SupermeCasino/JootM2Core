package com.github.jootnet.m2.core.image.html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.github.jootnet.m2.core.SDK;
import com.github.jootnet.m2.core.image.Texture;

/**
 * WZL文件解析类
 * <br>
 * wzl文件由wzx和wzl组成
 * <br>
 * wzx是索引文件，前44字节是文件固定头（可能有版权/修改时间等信息）
 * <br>  然后是一个int，表示图集中纹理总数（记为n），紧接着是n个int，为wzl文件中每个图片的起始数据偏移
 * <br>
 * wzl文件前64字节是描述数据，但0x2C处是一个int，也是纹理总数（n），0x28处可能是修改时间
 * <br>  然后是根据wzx中的偏移来解析每一张纹理，如果偏移是0，则表示纹理为空
 * <br>  每个纹理数据由16个固定头部和变长色彩数据组成
 * <br>  色彩数据可能使用zlib压缩过
 * 
 * @author LinXing
 *
 */
public final class WZL extends Thread {
	/** 库内图片总数 */
	private int imageCount;
	/** 纹理数据起始偏移 */
	private int[] offsetList;
	/** 纹理加载标志 */
	private volatile boolean[] loadedFlag;
	/** 纹理消费者 */
	private TextureConsumer textureConsumer;
	/** 库加载完毕事件 */
	private LoadCompletedEventHandler loadCompletedEventHandler;
	/** 是否被取消 */
	private volatile boolean cancel;
	/** wzx网络路径 */
	private String wzxUrl;
	/** wzl网络路径 */
	private String wzlUrl;
	/** 优先加载的纹理编号 */
	private Queue<Integer> seizes;
	/** 后台加载线程是否启动 */
	private boolean started;
	/** 文件名 */
	private String fno;
	/** 加载信号量 */
	private Semaphore loadSemaphore;
	/** 自动加载间隔 */
	private int autoLoadDelyInMilli = 5 * 1000; // 默认5秒
	/** 单次加载最大数据量（从磁盘或网络下载） */
	private int maxLoadSizePer = 256 * 1024; // 默认256K

	/**
	 * 微端模式，从网络下载
	 * 
	 * @param wzlName     纹理库名称
	 * @param wdBaseUrl 微端基址
	 */
	public WZL(String wzlName, String wdBaseUrl) {
		setDaemon(true);
		setName("WZL-" + hashCode());
		seizes = new ConcurrentLinkedDeque<>();
		loadSemaphore = new Semaphore(0);

		if (!wdBaseUrl.endsWith("/")) wdBaseUrl += "/";
		fno = wzlName.toLowerCase();
		wzxUrl = wdBaseUrl + "data/"
				+ fno + ".wzx";
		wzlUrl = wdBaseUrl + "data/"
				+ fno + ".wzl";
	}

	/**
	 * 设置纹理加载完成回调
	 * 
	 * @param consumer 事件处理函数
	 * @return 当前对象
	 */
	public WZL onTextureLoaded(TextureConsumer consumer) {
		textureConsumer = consumer;
		return this;
	}

	/**
	 * 当前图集所有纹理都加载完毕后回调 <br>
	 * 对从网络加载也有效 <br>
	 * 如果是从网络加载(即微端模式)，则此时应该从临时文件把wzl拷贝到微端目录了
	 * 
	 * @param eventHandler 事件处理函数
	 * @return 当前对象
	 */
	public WZL onAllTextureLoaded(LoadCompletedEventHandler eventHandler) {
		loadCompletedEventHandler = eventHandler;
		return this;
	}

	/**
	 * 停止加载 <br>
	 * 用于中止加载，退出内部线程
	 */
	public void cancelLoad() {
		cancel = true;
		loadSemaphore.release();
	}

	/**
	 * 加载特定编号纹理 <br>
	 * 当这些编号纹理加载完毕之后，仍会在后台继续加载库内其他纹理，并通过{@link TextureConsumer#recv(Texture)}向外告知
	 * <br>
	 * 此函数可多次调用，以打断后台顺序加载其他纹理 <br>
	 * 库内所有纹理加载完毕后会触发{@link LoadCompleted#op()}向外告知 <br>
	 * 如果wzl文件来自微端，则会复制到wzx同级目录
	 * 
	 * @param seizes 需要优先加载的纹理编号
	 * @return 当前对象
	 */
	public WZL load(int... seizes) {
		for (var i : seizes) {
			this.seizes.offer(i);
		}
		if (!started) {
			start(); // 启动后台线程进行顺序加载
			started = true;
		} else {
			loadSemaphore.drainPermits();
			loadSemaphore.release();
		}
		return this;
	}

	@Override
	public void run() {
		runHttp();
	}

	private void runHttp() {
		while (!cancel) {
			try {
				var url = new URL(wzxUrl);
				var conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(1000);
				conn.setReadTimeout(3000);
				conn.setRequestMethod("GET");
				conn.connect();
	
				var respCode = conn.getResponseCode();
				if (respCode < 200 || respCode >= 300) {
					conn.disconnect();
					continue;
				}
				var byteBuffer = (ByteBuffer) null;
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
					byteBuffer = ByteBuffer.wrap(dData).order(ByteOrder.LITTLE_ENDIAN);
				}
				byteBuffer.position(44);
				imageCount = byteBuffer.getInt();
				offsetList = new int[imageCount + 1];
				loadedFlag = new boolean[imageCount];
				for (var i = 0; i < imageCount; ++i) {
					offsetList[i] = byteBuffer.getInt();// UnsignedInt
					if (offsetList[i] < 64) offsetList[i] = 0;
				}
	
				conn.disconnect();
				break;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		var fLen = 0l;
		try {
			var url = new URL(wzlUrl);
			var conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(3000);
			conn.setRequestMethod("HEAD");
			conn.connect();
			fLen = conn.getContentLength(); // 如果是nginx可能没有这个属性，需要设置！
			offsetList[imageCount] = (int) (fLen - 1);
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (!cancel) {
			// 是否已完成所有纹理加载
			var loadedCompleted = true;
			for (var i = 0; i < imageCount; ++i) {
				if (!loadedFlag[i]) {
					loadedCompleted = false;
					break;
				}
			}
			if (loadedCompleted) {
				if (loadCompletedEventHandler != null)
					loadCompletedEventHandler.loadCompleted(fno);
				break;
			}

			// 本次开始加载的纹理编号
			var startNo = 0;
			// 支持抢占式优先级
			var seize = seizes.poll();
			if (seize == null || loadedFlag[seize]) {
				try {
					if (loadSemaphore.tryAcquire(autoLoadDelyInMilli, TimeUnit.MILLISECONDS)) {
						if (cancel) return;
						seize = seizes.poll();
						loadSemaphore.drainPermits();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (seize != null && !loadedFlag[seize]) {
				startNo = seize;
			} else {
				for (var i = 0; i < imageCount; ++i) {
					if (!loadedFlag[i]) {
						startNo = i;
						break;
					}
				}
			}
			// 如果是空图片
			if (offsetList[startNo] == 0) {
				loadedFlag[startNo] = true;
				if (textureConsumer != null)
					textureConsumer.recv(fno, startNo, EMPTY);
				continue;
			}

			var startOffset = offsetList[startNo];
			var texDataLen = 0;
			for (var i = startNo + 1; i < imageCount + 1; ++i) {
				if (offsetList[i] != 0) {
					texDataLen = offsetList[i] - offsetList[startNo];
					break;
				}
			}
			var rangeEnd = Math.min(startOffset + Math.max(maxLoadSizePer, texDataLen), fLen - 1); // 微端模式下，每次下载的数据马上解析并存储到显存，同时写入临时文件，这样给用户的体验上看显示要慢一些
			try {
				var url = new URL(wzlUrl);
				var conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(1000);
				conn.setReadTimeout(3000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Range", "bytes=" + startOffset + "-" + rangeEnd);
				conn.connect();

				var respCode = conn.getResponseCode();
				if (respCode < 200 || respCode >= 300) {
					conn.disconnect();
					continue;
				}
				var buffer = (ByteBuffer) null;
				try (var bos = new ByteArrayOutputStream()) {
					try (var is = conn.getInputStream()) {
						var readLen = 0;
						var buf = new byte[4096];

						while ((readLen = is.read(buf)) > 0) {
							bos.write(buf, 0, readLen);
						}
					}
					var dData = bos.toByteArray();
					buffer = ByteBuffer.wrap(dData).order(ByteOrder.LITTLE_ENDIAN);
				}
				conn.disconnect();
				
				unpackTextures(buffer, startNo);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void unpackTextures(ByteBuffer byteBuffer, int startNo) throws IOException {
		for (var no = startNo; no < imageCount; ++no) {
			if (cancel) break;
			if (offsetList[no] == 0) {
				if (!loadedFlag[no]) {
					loadedFlag[no] = true;
					if (textureConsumer != null)
						textureConsumer.recv(fno, no, EMPTY);
				}
				continue;
			}
			if (byteBuffer.remaining() < 16)
				break;
			var colorBit = byteBuffer.get();
			var compressFlag = byteBuffer.get() != 0;
			byteBuffer.position(byteBuffer.position() + 2); // 2字节未知数据
			var width = byteBuffer.getShort();
			var height = byteBuffer.getShort();
			var offsetX = byteBuffer.getShort();
			var offsetY = byteBuffer.getShort();
			var dataLen = byteBuffer.getInt();
			if (dataLen == 0) {
				// 这里可能是一个bug，或者是其他引擎作者没有说清楚
				// 本来一直以为是compressFlag作为是否zlib压缩的标志位
				// 后来发现如果这里的长度是0，则表示没有压缩，后面是图片尺寸的裸数据
				dataLen = width * height;
				if (colorBit == 5) dataLen *= 2;
				compressFlag = false;
			}
			if (byteBuffer.remaining() < dataLen)
				break;
			if (loadedFlag[no]) {
				byteBuffer.position(byteBuffer.position() + dataLen);
				continue;
			}
			var pixels = new byte[dataLen];
			byteBuffer.get(pixels);
			if (compressFlag) {
				pixels = SDK.unzip(pixels);
			}
			byte[] sRGBA = new byte[width * height * 4];
			if (colorBit != 5) { // 8位
				int p_index = 0;
				for (int h = height - 1; h >= 0; --h)
					for (int w = 0; w < width; ++w) {
						// 跳过填充字节
						if (w == 0)
							p_index += SDK.skipBytes(8, width);
						byte[] pallete = SDK.palletes[pixels[p_index++] & 0xff];
						int _idx = (w + h * width) * 4;
						sRGBA[_idx] = pallete[1];
						sRGBA[_idx + 1] = pallete[2];
						sRGBA[_idx + 2] = pallete[3];
						sRGBA[_idx + 3] = pallete[0];
					}
			} else { // 16位
				ByteBuffer bb = ByteBuffer.wrap(pixels).order(ByteOrder.LITTLE_ENDIAN);
				int p_index = 0;
				for (int h = height - 1; h >= 0; --h)
					for (int w = 0; w < width; ++w, p_index += 2) {
						// 跳过填充字节
						if (w == 0)
							p_index += SDK.skipBytes(16, width);
						short pdata = bb.getShort(p_index);
						byte r = (byte) ((pdata & 0xf800) >> 8);// 由于是与16位做与操作，所以多出了后面8位
						byte g = (byte) ((pdata & 0x7e0) >> 3);// 多出了3位，在强转时前8位会自动丢失
						byte b = (byte) ((pdata & 0x1f) << 3);// 少了3位
						int _idx = (w + h * width) * 4;
						sRGBA[_idx] = r;
						sRGBA[_idx + 1] = g;
						sRGBA[_idx + 2] = b;
						if (r == 0 && g == 0 && b == 0) {
							sRGBA[_idx + 3] = 0;
						} else {
							sRGBA[_idx + 3] = -1;
						}
					}
			}
			loadedFlag[no] = true;
			if (textureConsumer != null) {
				textureConsumer.recv(fno, no, new Texture(false, width, height, offsetX, offsetY, sRGBA));
			}
		}
	}

	@FunctionalInterface
	public interface TextureConsumer {
		/**
		 * 单个纹理加载完毕时触发
		 * 
		 * @param fno 文件编号
		 * @param no  纹理编号，从0开始
		 * @param tex 纹理对象
		 */
		void recv(String fno, int no, Texture tex);
	}

	@FunctionalInterface
	public interface LoadCompletedEventHandler {
		/**
		 * 所有纹理加载完毕后触发
		 * 
		 * @param fno 文件编号
		 */
		void loadCompleted(String fno);
	}

	/** 空图片 */
	private static Texture EMPTY;
	
	static {
		EMPTY = new Texture(true, 1, 1, 0, 0, new byte[] { SDK.palletes[0][1], SDK.palletes[0][2], SDK.palletes[0][3], SDK.palletes[0][0] });
	}
}
