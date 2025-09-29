package com.uno.server;

import com.uno.model.Game;
import com.uno.model.Player;
import com.uno.utils.Message;
import com.uno.utils.MessageType;

import java.io.*;
import java.net.Socket;

/**
 * Class đại diện cho một kết nối client với server
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UnoServer server;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Player player;
    private boolean running;
    
    public ClientHandler(Socket socket, UnoServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            // Khởi tạo luồng input/output
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());
            
            // Xử lý tin nhắn từ client
            while (running) {
                Message message = (Message) input.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Lỗi trong kết nối với client: " + e.getMessage());
        } finally {
            close();
        }
    }
    
    /**
     * Xử lý tin nhắn từ client
     * 
     * @param message Tin nhắn cần xử lý
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CONNECT:
                handleConnect(message);
                break;
            case DISCONNECT:
                handleDisconnect();
                break;
            case CREATE_ROOM:
                handleCreateRoom(message);
                break;
            case JOIN_ROOM:
                handleJoinRoom(message);
                break;
            case LEAVE_ROOM:
                handleLeaveRoom();
                break;
            case START_GAME:
                handleStartGame();
                break;
            case PLAY_CARD:
                handlePlayCard(message);
                break;
            case DRAW_CARD:
                handleDrawCard();
                break;
            case END_TURN:
                handleEndTurn();
                break;
            case CALL_UNO:
                handleCallUno();
                break;
            case CHALLENGE:
                handleChallenge(message);
                break;
            case CHAT_MESSAGE:
                handleChatMessage(message);
                break;
            default:
                sendMessage(new Message(MessageType.ERROR, "Loại tin nhắn không được hỗ trợ", server.getServerId()));
                break;
        }
    }
    
    /**
     * Xử lý tin nhắn kết nối từ client
     * 
     * @param message Tin nhắn kết nối
     */
    private void handleConnect(Message message) {
        String playerName = (String) message.getData();
        player = new Player(playerName);
        
        // In thông tin về ID của player
        System.out.println("ClientHandler: Player mới kết nối - " + playerName + " với ID: '" + player.getId() + "'");
        
        // Thông báo cho client về kết nối thành công
        sendMessage(new Message(MessageType.CONNECT_ACCEPT, player.getId(), server.getServerId()));
        
        // Gửi danh sách phòng hiện có
        sendMessage(new Message(MessageType.ROOM_LIST, server.getRoomList(), server.getServerId()));
    }

    // Xử lý tin nhắn ngắt kết nối từ client
    private void handleDisconnect() {
        running = false;
    }

    /* Xử lý tin nhắn tạo phòng từ client
     *
     * @param message Tin nhắn tạo phòng
     */
    private void handleCreateRoom(Message message) {
        String roomName = (String) message.getData();
        GameRoom room = server.createRoom(roomName, player);
        
        if (room != null) {
            // Thông báo cho client về tạo phòng thành công
            sendMessage(new Message(MessageType.ROOM_UPDATE, room, server.getServerId()));
            
            // Thông báo cho tất cả client về danh sách phòng mới
            server.broadcastRoomList();
        } else {
            sendMessage(new Message(MessageType.ERROR, "Không thể tạo phòng", server.getServerId()));
        }
    }

    /* Xử lý tin nhắn tham gia phòng từ client
     *
     * @param message Tin nhắn tham gia phòng
     */
    private void handleJoinRoom(Message message) {
        String roomId = (String) message.getData();
        GameRoom room = server.getRoom(roomId);
        
        if (room != null && room.addPlayer(player, this)) {
            // Thông báo cho client về tham gia phòng thành công
            sendMessage(new Message(MessageType.ROOM_UPDATE, room, server.getServerId()));
            
            // Thông báo cho tất cả người chơi trong phòng về người chơi mới
            room.broadcast(new Message(MessageType.ROOM_UPDATE, room, server.getServerId()));
            
            // Cập nhật danh sách phòng cho tất cả người chơi
            server.broadcastRoomList();
        } else {
            sendMessage(new Message(MessageType.ERROR, "Không thể tham gia phòng", server.getServerId()));
        }
    }
    
    /**
     * Xử lý tin nhắn rời phòng từ client
     */
    private void handleLeaveRoom() {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            room.removePlayer(player.getId());
            
            // Thông báo cho client về rời phòng thành công
            sendMessage(new Message(MessageType.ROOM_LIST, server.getRoomList(), server.getServerId()));
            
            // Thông báo cho tất cả người chơi trong phòng về người chơi rời đi
            room.broadcast(new Message(MessageType.ROOM_UPDATE, room, server.getServerId()));
            
            // Cập nhật danh sách phòng cho tất cả người chơi
            server.broadcastRoomList();
            
            // Xóa phòng nếu không còn người chơi
            if (room.getPlayerCount() == 0) {
                server.removeRoom(room.getId());
                server.broadcastRoomList();
            }
        }
    }
    
    /**
     * Xử lý tin nhắn bắt đầu game từ client
     */
    private void handleStartGame() {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null && room.getHost().getId().equals(player.getId())) {
            if (room.startGame()) {
                // Thông báo cho tất cả người chơi trong phòng về bắt đầu game
                // Gửi GameState thay vì Game để đảm bảo tính nhất quán
                for (String playerId : room.getPlayerIds()) {
                    ClientHandler handler = room.getClientHandler(playerId);
                    if (handler != null) {
                        handler.sendMessage(new Message(MessageType.START_GAME, room.getPlayerGameState(playerId), server.getServerId()));
                    }
                }
                
                // Cập nhật trạng thái game cho mỗi người chơi
                room.updateGameState();
            } else {
                sendMessage(new Message(MessageType.ERROR, "Không thể bắt đầu game", server.getServerId()));
            }
        } else {
            sendMessage(new Message(MessageType.ERROR, "Bạn không phải là chủ phòng", server.getServerId()));
        }
    }
    
    /**
     * Xử lý tin nhắn đánh bài từ client
     * 
     * @param message Tin nhắn đánh bài
     */
    private void handlePlayCard(Message message) {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            @SuppressWarnings("unchecked")
            Object[] data = (Object[]) message.getData();
            int cardIndex = (Integer) data[0];
            String colorName = (String) data[1];
            
            Game game = room.getGame();
            Player currentPlayer = game.getCurrentPlayer();
            String currentPlayerId = currentPlayer != null ? currentPlayer.getId() : null;
            System.out.println("HandlePlayCard: currentPlayerId = " + currentPlayerId + ", playerId = " + player.getId());
            
            if (room.playCard(player.getId(), cardIndex, colorName)) {
                // Cập nhật trạng thái game cho tất cả người chơi
                room.updateGameState();
                
                // Kiểm tra xem game đã kết thúc chưa
                if (game.isGameOver()) {
                    room.broadcast(new Message(MessageType.GAME_OVER, game.getWinner(), server.getServerId()));
                }
            } else {
                sendMessage(new Message(MessageType.ERROR, "Không thể đánh bài", server.getServerId()));
            }
        }
    }
    
    /**
     * Xử lý tin nhắn rút bài từ client
     */
    private void handleDrawCard() {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            if (room.drawCard(player.getId())) {
                // Cập nhật trạng thái game cho người chơi đã rút bài
                sendMessage(new Message(MessageType.GAME_UPDATE, room.getPlayerGameState(player.getId()), server.getServerId()));
            } else {
                sendMessage(new Message(MessageType.ERROR, "Không thể rút bài", server.getServerId()));
            }
        }
    }
    
    /**
     * Xử lý tin nhắn kết thúc lượt từ client
     */
    private void handleEndTurn() {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            if (room.endTurn(player.getId())) {
                // Cập nhật trạng thái game cho tất cả người chơi
                room.updateGameState();
            } else {
                sendMessage(new Message(MessageType.ERROR, "Không thể kết thúc lượt", server.getServerId()));
            }
        }
    }
    
    /**
     * Xử lý tin nhắn hô Uno từ client
     */
    private void handleCallUno() {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            if (room.callUno(player.getId())) {
                // Thông báo cho tất cả người chơi về hô Uno
                room.broadcast(new Message(MessageType.CALL_UNO, player.getName(), server.getServerId()));
            }
        }
    }
    
    /**
     * Xử lý tin nhắn thách thức từ client
     * 
     * @param message Tin nhắn thách thức
     */
    private void handleChallenge(Message message) {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            String challengedPlayerId = (String) message.getData();
            if (room.challenge(player.getId(), challengedPlayerId)) {
                // Cập nhật trạng thái game cho tất cả người chơi
                room.updateGameState();
            } else {
                sendMessage(new Message(MessageType.ERROR, "Không thể thách thức", server.getServerId()));
            }
        }
    }
    
    /**
     * Xử lý tin nhắn chat từ client
     * 
     * @param message Tin nhắn chat
     */
    private void handleChatMessage(Message message) {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            String chatMessage = player.getName() + ": " + message.getData();
            room.broadcast(new Message(MessageType.CHAT_MESSAGE, chatMessage, player.getId()));
        }
    }
    
    /**
     * Gửi tin nhắn đến client
     * 
     * @param message Tin nhắn cần gửi
     */
    public void sendMessage(Message message) {
        try {
            output.writeObject(message);
            // FIX: Reset ObjectOutputStream để tránh caching đối tượng.
            // Điều này đảm bảo trạng thái mới nhất của GameState luôn được gửi đi.
            output.reset(); 
            output.flush();
        } catch (IOException e) {
            System.out.println("Lỗi khi gửi tin nhắn đến client: " + e.getMessage());
            close();
        }
    }
    
    /**
     * Đóng kết nối với client
     */
    public void close() {
        try {
            running = false;
            
            // Rời khỏi phòng nếu đang ở trong phòng
            handleLeaveRoom();
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
            
            // Thông báo cho server về ngắt kết nối
            server.removeClient(this);
        } catch (IOException e) {
            System.out.println("Lỗi khi đóng kết nối với client: " + e.getMessage());
        }
    }
    
    /**
     * Lấy người chơi liên kết với kết nối này
     * 
     * @return Người chơi
     */
    public Player getPlayer() {
        return player;
    }
}