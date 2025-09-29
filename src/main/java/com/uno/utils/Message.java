package com.uno.utils;

import java.io.Serializable;

/**
 * Class đại diện cho một tin nhắn được gửi giữa client và server
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final MessageType type;
    private final Object data;
    private final String senderId;
    
    public Message(MessageType type, Object data, String senderId) {
        this.type = type;
        this.data = data;
        this.senderId = senderId;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    @Override
    public String toString() {
        return "Message{type=" + type + ", senderId=" + senderId + "}";
    }
}