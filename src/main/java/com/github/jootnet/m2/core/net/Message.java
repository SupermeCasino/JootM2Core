package com.github.jootnet.m2.core.net;

public interface Message {
    /**
     * 获取消息类型
     * 
     * @return 消息类型
     * @see MessageType
     */
    public MessageType type();
}
