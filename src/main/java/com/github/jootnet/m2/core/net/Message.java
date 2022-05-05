package com.github.jootnet.m2.core.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class Message {
	@FunctionalInterface
	public interface MessageDeSerializer {
		Message unpack(ByteBuffer buffer);
	}
	protected static Map<MessageType, MessageDeSerializer> deSerializers = new HashMap<>();
	
	static {
		getClassName("com.github.jootnet.m2.core.net.messages", true).parallelStream().forEach(t -> {
			try {
				Class.forName(t);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
	}
	
    /**
     * 获取消息类型
     * 
     * @return 消息类型
     * @see MessageType
     */
    public abstract MessageType type();
	
	/**
	 * 将当前对象打包为字节数组
	 * 
	 * @return 序列化后得数据
	 * @throws IOException 
	 */
	public byte[] pack() throws IOException {
		var stream = new ByteArrayOutputStream();
		var buffer = new DataOutputStream(stream);
		// 0.类型
		buffer.writeInt(type().id());
		packCore(buffer);
		buffer.flush();
		return stream.toByteArray();
	}
	protected abstract void packCore(DataOutput buffer) throws IOException;
	
	/**
	 * 从ByteBuffer中反序列化数据包
	 * 
	 * @param buffer 数据缓冲区
	 * @return 解析出得数据包或null
	 */
	public static Message unpack(ByteBuffer buffer) {
		var type = (MessageType) null;
		
		var typeId = buffer.getInt();
		for (var msgType : MessageType.values()) {
			if (msgType.id() == typeId) {
				type = msgType;
				break;
			}
		}
		
		if (type == null) return null;
		
		if (!deSerializers.containsKey(type)) return null;
		
		return deSerializers.get(type).unpack(buffer);
	}

    protected static void packString(String str, DataOutput buffer) throws IOException {
    	if (str == null) {
    		buffer.writeByte(0);
    		return;
    	}
    	var bytes = str.getBytes(StandardCharsets.UTF_8);
    	buffer.writeByte((byte) bytes.length);
    	buffer.write(bytes);
    }
    protected static String unpackString(ByteBuffer buffer) {
    	var bytesLen = buffer.get();
		String str = null;
		if (bytesLen > 0) {
			var bytes = new byte[bytesLen];
			buffer.get(bytes);
			str = new String(bytes, StandardCharsets.UTF_8);
		}
		return str;
    }
    
    // 以下代码来自于网络
    /**
     * 获取某包下所有类
     *
     * @param packageName 包名
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassName(String packageName, boolean isRecursion) {
        Set<String> classNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");

        URL url = loader.getResource(packagePath);
        if (url != null) {
            String protocol = url.getProtocol();
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (jarFile != null) {
                    getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            /*从所有的jar包中查找包名*/
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }

        return classNames;
    }

    /**
     * 从项目文件获取某包下有类
     *
     * @param filePath    文件路径
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion) {
        Set<String> className = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        for (File childFile : files) {

            if (childFile.isDirectory()) {
                if (isRecursion) {
                    className.addAll(getClassNameFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion));
                }
            } else {
                String fileName = childFile.getName();
                //endsWith() 方法用于测试字符串是否以指定的后??结束??  !fileName.contains("$") 文件名中不包? '$'
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    className.add(packageName + "." + fileName.replace(".class", ""));
                }
            }
        }

        return className;
    }


    /**
     * @param jarEntries
     * @param packageName
     * @param isRecursion
     * @return
     */
    private static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName,
                                                   boolean isRecursion) {
        Set<String> classNames = new HashSet<>();

        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory()) {
                /*
                 * 这里是为了方便，先把"/" 转成 "." 再判?? ".class" 的做法可能会有bug
                 * (FIXME: 先把"/" 转成 "." 再判?? ".class" 的做法可能会有bug)
                 */
                String entryName = jarEntry.getName().replace("/", ".");
                if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
                    entryName = entryName.replace(".class", "");
                    if (isRecursion) {
                        classNames.add(entryName);
                    } else if (!entryName.replace(packageName + ".", "").contains(".")) {
                        classNames.add(entryName);
                    }
                }
            }
        }

        return classNames;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下??有类
     *
     * @param urls        URL集合
     * @param packageName
     * @param isRecursion 是否遍历子包
     * @return 类的完整名称
     */
    private static Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<>();

        for (int i = 0; i < urls.length; i++) {
            String classPath = urls[i].getPath();
            //不必搜索classes文件??
            if (classPath.endsWith("classes/")) {
                continue;
            }

            JarFile jarFile = null;
            try {
                jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (jarFile != null) {
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }

        return classNames;
    }
}
