package com.uno.server;

import com.uno.model.Game;
import com.uno.model.Player;
import com.uno.utils.Message;
import com.uno.utils.MessageType;
import com.uno.utils.StringUtils;

import java.io.*;
import java.net.Socket;

/**
 * Handles a client connection with the server, processing messages and managing game state
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final UnoServer server;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Player player;
    private boolean running;
    
    /**
     * Creates a new client handler for the specified socket
     * 
     * @param socket The client socket connection
     * @param server The Uno server instance
     */
    public ClientHandler(Socket socket, UnoServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.running = true;
        
        System.out.println(StringUtils.formatNetworkLog("SERVER", "NEW_CONNECTION", 
                "Tao handler xu ly client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + 
                " - Connection established"));
    }
    
    @Override
    public void run() {
        try {
            // Initialize input/output streams
            System.out.println(StringUtils.formatNetworkLog("SERVER", "STREAM_SETUP", 
                    "Thiet lap stream I/O cho client " + clientSocket.getInetAddress().getHostAddress() + 
                    " - Initializing ObjectInputStream/ObjectOutputStream"));
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println(StringUtils.formatNetworkLog("SERVER", "READY", 
                    "Kenh truyen du lieu da san sang - Communication channel established"));
            
            // Process messages from client
            while (running) {
                Message message = (Message) input.readObject();
                System.out.println(StringUtils.formatNetworkLog("SERVER", "MESSAGE_RECEIVED", 
                        "Nhan tin nhan tu client: " + message.getType() + " - Object deserialization"));
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(StringUtils.formatNetworkLog("SERVER", "CONNECTION_ERROR", 
                    "Loi ket noi voi client: " + e.getMessage() + " - Socket communication failure"));
        } finally {
            close();
        }
    }
    
    /**
     * Processes messages from the client and routes to appropriate handlers
     * 
     * @param message The message to process
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
                sendMessage(new Message(MessageType.ERROR, "Unsupported message type", server.getServerId()));
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
            sendMessage(new Message(MessageType.ERROR, "Cannot create room", server.getServerId()));
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
            sendMessage(new Message(MessageType.ERROR, "Cannot join room", server.getServerId()));
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
                sendMessage(new Message(MessageType.ERROR, "Cannot start game", server.getServerId()));
            }
        } else {
            sendMessage(new Message(MessageType.ERROR, "You are not the room owner", server.getServerId()));
        }
    }
    
    /**
     * Handles a play card message from the client
     * 
     * @param message The play card message containing card index and color
     */
    private void handlePlayCard(Message message) {
        GameRoom room = server.getRoomByPlayer(player.getId());
        
        if (room != null) {
            // Extract data from message (cardIndex and selected color)
            Object[] data = (Object[]) message.getData();
            int cardIndex = (Integer) data[0];
            String colorName = (String) data[1];
            
            Game game = room.getGame();
            
            if (room.playCard(player.getId(), cardIndex, colorName)) {
                // Update game state for all players
                room.updateGameState();
                
                // Check if game is over
                if (game.isGameOver()) {
                    room.broadcast(new Message(MessageType.GAME_OVER, game.getWinner(), server.getServerId()));
                }
            } else {
                sendMessage(new Message(MessageType.ERROR, "Cannot play card", server.getServerId()));
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
                sendMessage(new Message(MessageType.ERROR, "Cannot draw card", server.getServerId()));
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
                sendMessage(new Message(MessageType.ERROR, "Cannot end turn", server.getServerId()));
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
                sendMessage(new Message(MessageType.ERROR, "Cannot challenge", server.getServerId()));
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
            // Log network message transmission
            System.out.println(StringUtils.formatNetworkLog("SERVER", "SEND_MESSAGE", 
                    "Gui " + message.getType() + " den " + 
                    (player != null ? player.getName() : "client") + " - Object serialization"));
            
            // Send message
            output.writeObject(message);
            
            // Reset ObjectOutputStream to avoid object caching
            // This ensures the latest state of GameState is always sent
            output.reset(); 
            output.flush();
        } catch (IOException e) {
            System.out.println(StringUtils.formatNetworkLog("SERVER", "SEND_ERROR", 
                    "Loi gui du lieu qua network: " + e.getMessage() + " - TCP transmission failure"));
            close();
        }
    }
    
    /**
     * Closes the connection with the client
     */
    public void close() {
        if (!running) {
            // Already closed
            return;
        }
        
        System.out.println(StringUtils.formatNetworkLog("SERVER", "DISCONNECT", 
                "Bat dau qua trinh dong ket noi voi client " + 
                (player != null ? player.getName() : "anonymous") + " - Connection teardown initiated"));
                
        try {
            running = false;
            
            // Leave room if currently in one
            handleLeaveRoom();
            
            // Close all streams and socket
            if (input != null) {
                input.close();
            }
            
            if (output != null) {
                output.close();
            }
            
            if (clientSocket != null) {
                clientSocket.close();
                System.out.println(StringUtils.formatNetworkLog("SERVER", "SOCKET_CLOSE", 
                        "Socket da dong hoan tat: " + clientSocket.getInetAddress().getHostAddress() + 
                        " - TCP connection terminated"));
            }
            
            // Notify server about disconnection
            server.removeClient(this);
        } catch (IOException e) {
            System.out.println(StringUtils.formatNetworkLog("SERVER", "CLEANUP_ERROR", 
                    "Loi khi dong resources: " + e.getMessage() + " - Resource cleanup failure"));
        }
    }
    
    /**
     * Gets the player associated with this connection
     * 
     * @return The player object
     */
    public Player getPlayer() {
        return player;
    }
}