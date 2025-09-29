package com.uno.utils;

/**
 * Enum đại diện cho các loại tin nhắn trao đổi giữa client và server
 */
public enum MessageType {
    // Tin nhắn liên quan đến kết nối
    CONNECT,        // Client kết nối đến server
    CONNECT_ACCEPT, // Server chấp nhận kết nối
    CONNECT_REJECT, // Server từ chối kết nối
    DISCONNECT,     // Client ngắt kết nối
    
    // Tin nhắn liên quan đến phòng chơi
    CREATE_ROOM,    // Client yêu cầu tạo phòng
    JOIN_ROOM,      // Client yêu cầu tham gia phòng
    LEAVE_ROOM,     // Client rời khỏi phòng
    ROOM_LIST,      // Danh sách phòng chơi
    ROOM_UPDATE,    // Cập nhật thông tin phòng
    
    // Tin nhắn liên quan đến game
    START_GAME,     // Bắt đầu game
    GAME_UPDATE,    // Cập nhật trạng thái game
    PLAY_CARD,      // Đánh lá bài
    DRAW_CARD,      // Rút lá bài
    END_TURN,       // Kết thúc lượt
    CALL_UNO,       // Hô Uno
    CHALLENGE,      // Thách thức (cho Wild Draw Four)
    GAME_OVER,      // Game kết thúc
    
    // Tin nhắn chat
    CHAT_MESSAGE,   // Tin nhắn chat
    
    // Tin nhắn lỗi và thông báo khác
    ERROR,          // Thông báo lỗi
    INFO            // Thông tin khác
}