package com.uno.client;

import com.uno.model.CardColor;
import com.uno.server.GameRoom;
import com.uno.utils.Message;
import com.uno.utils.MessageType;
import com.uno.utils.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Client network communication manager class
 */
public class UnoClient implements Runnable {
    private static final int DEFAULT_PORT = 5000;
    
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String clientId;
    private final BlockingQueue<Message> messageQueue;
    private final ClientListener clientListener;
    private volatile boolean running;
    
    public UnoClient(String serverAddress, ClientListener clientListener) {
        this(serverAddress, DEFAULT_PORT, clientListener);
    }
    
    public UnoClient(String serverAddress, int serverPort, ClientListener clientListener) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientListener = clientListener;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = false;
    }
    
    /**
     * Connect to the game server
     * 
     * @param playerName Name of the player
     * @return true if connection was successful, false otherwise
     */
    public boolean connect(String playerName) {
        try {
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "ket_noi", 
                    "Dang ket noi den server " + serverAddress + ":" + serverPort));
                    
            socket = new Socket(serverAddress, serverPort);
            
            // Initialize input/output streams
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "kết_nối", 
                    "Thiết lập luồng I/O"));
                    
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // Start message handling thread
            running = true;
            new Thread(this).start();
            
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "kết_nối", 
                    "Thread xử lý tin nhắn đã khởi động"));
            
            // Send connection message to server
            Message connectMessage = new Message(MessageType.CONNECT, playerName, "");
            sendMessage(connectMessage);
            
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "kết_nối", 
                    "Kết nối thành công, gửi tin CONNECT"));
            
            return true;
        } catch (IOException e) {
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "kết_nối", 
                    "Lỗi kết nối: " + e.getMessage()));
            clientListener.onConnectionError("Không thể kết nối đến server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (running) {
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "ngắt_kết_nối", 
                    "Đang ngắt kết nối khỏi server"));
                    
            running = false;
            
            // Send disconnect message to server
            sendMessage(new Message(MessageType.DISCONNECT, null, clientId));
            
            // Close streams and socket
            try {
                if (output != null) {
                    output.close();
                }
                
                if (input != null) {
                    input.close();
                }
                
                if (socket != null) {
                    socket.close();
                    System.out.println(StringUtils.formatNetworkLog("UnoClient", "đóng_socket", 
                            "Socket đã đóng"));
                }
            } catch (IOException e) {
                System.out.println(StringUtils.formatNetworkLog("UnoClient", "ngắt_kết_nối", 
                        "Lỗi đóng kết nối: " + e.getMessage()));
            }
            
            clientListener.onDisconnected();
            System.out.println(StringUtils.formatNetworkLog("UnoClient", "ngắt_kết_nối", 
                    "Ngắt kết nối hoàn tất"));
        }
    }
    
    /**
     * Send a message to the server
     * 
     * @param message Message to send
     */
    public void sendMessage(Message message) {
        if (output != null) {
            try {
                System.out.println(StringUtils.formatNetworkLog("UnoClient", "gửi_tin", 
                        "Gửi tin nhắn: " + message.getType()));
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.out.println(StringUtils.formatNetworkLog("UnoClient", "gửi_tin", 
                        "Lỗi gửi tin nhắn: " + e.getMessage()));
                if (running) {
                    running = false;
                    clientListener.onConnectionError("Mất kết nối đến server: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Thread xử lý tin nhắn từ server
     */
    @Override
    public void run() {
        try {
            while (running) {
                Message message = (Message) input.readObject();
                messageQueue.offer(message);
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                running = false;
                System.out.println("Lỗi khi đọc tin nhắn từ server: " + e.getMessage());
                clientListener.onConnectionError("Mất kết nối với server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Xử lý tin nhắn từ server
     * 
     * @param message Tin nhắn cần xử lý
     */
    private void handleMessage(Message message) {
        switch (message.getType()) {
            case CONNECT_ACCEPT:
                this.clientId = (String) message.getData();
                clientListener.onConnected(clientId);
                break;
                
            case CONNECT_REJECT:
                clientListener.onConnectionRejected((String) message.getData());
                disconnect();
                break;
                
            case ROOM_LIST:
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> roomList = (List<Map<String, Object>>) message.getData();
                clientListener.onRoomListReceived(roomList);
                break;
                
            case ROOM_UPDATE:
                clientListener.onRoomUpdated(message.getData());
                break;
                
            case START_GAME:
                clientListener.onGameStarted(message.getData());
                break;
                
            case GAME_UPDATE:
                System.out.println("UnoClient: Nhận GAME_UPDATE, data class: " + (message.getData() != null ? message.getData().getClass().getName() : "null"));
                
                if (message.getData() instanceof GameRoom.GameState) {
                    GameRoom.GameState state = (GameRoom.GameState) message.getData();
                    System.out.println("UnoClient: GameState nhận được có currentPlayerId = " + state.getCurrentPlayerId());
                    System.out.println("UnoClient: clientId hiện tại = " + clientId);
                    System.out.println("UnoClient: Giống nhau? " + 
                                     (state.getCurrentPlayerId() != null && 
                                      clientId != null && 
                                      state.getCurrentPlayerId().equals(clientId)));
                }
                
                clientListener.onGameUpdated(message.getData());
                break;
                
            case GAME_OVER:
                clientListener.onGameOver(message.getData());
                break;
                
            case CHAT_MESSAGE:
                clientListener.onChatMessageReceived((String) message.getData(), message.getSenderId());
                break;
                
            case ERROR:
                clientListener.onErrorReceived((String) message.getData());
                break;
                
            case INFO:
                clientListener.onInfoReceived((String) message.getData());
                break;
                
            default:
                System.out.println("Loại tin nhắn không được hỗ trợ: " + message.getType());
                break;
        }
    }
    
    /**
     * Tạo phòng mới
     * 
     * @param roomName Tên phòng
     */
    public void createRoom(String roomName) {
        sendMessage(new Message(MessageType.CREATE_ROOM, roomName, clientId));
    }
    
    /**
     * Tham gia phòng
     * 
     * @param roomId ID của phòng
     */
    public void joinRoom(String roomId) {
        sendMessage(new Message(MessageType.JOIN_ROOM, roomId, clientId));
    }
    
    /**
     * Rời khỏi phòng
     */
    public void leaveRoom() {
        sendMessage(new Message(MessageType.LEAVE_ROOM, null, clientId));
    }
    
    /**
     * Bắt đầu game
     */
    public void startGame() {
        sendMessage(new Message(MessageType.START_GAME, null, clientId));
    }
    
    /**
     * Đánh lá bài
     * 
     * @param cardIndex Vị trí lá bài trong tay
     * @param declaredColor Màu được chọn nếu là lá Wild
     */
    public void playCard(int cardIndex, CardColor declaredColor) {
        Object[] data = {cardIndex, declaredColor.name()};
        sendMessage(new Message(MessageType.PLAY_CARD, data, clientId));
    }
    
    /**
     * Rút lá bài
     */
    public void drawCard() {
        sendMessage(new Message(MessageType.DRAW_CARD, null, clientId));
    }
    
    /**
     * Kết thúc lượt
     */
    public void endTurn() {
        sendMessage(new Message(MessageType.END_TURN, null, clientId));
    }
    
    /**
     * Hô Uno
     */
    public void callUno() {
        sendMessage(new Message(MessageType.CALL_UNO, null, clientId));
    }
    
    /**
     * Thách thức Wild Draw Four
     * 
     * @param challengedPlayerId ID của người chơi bị thách thức
     */
    public void challenge(String challengedPlayerId) {
        sendMessage(new Message(MessageType.CHALLENGE, challengedPlayerId, clientId));
    }
    
    /**
     * Gửi tin nhắn chat
     * 
     * @param chatMessage Nội dung tin nhắn
     */
    public void sendChatMessage(String chatMessage) {
        sendMessage(new Message(MessageType.CHAT_MESSAGE, chatMessage, clientId));
    }
    
    /**
     * Interface cho client để nhận các sự kiện từ server
     */
    public interface ClientListener {
        void onConnected(String clientId);
        void onDisconnected();
        void onConnectionRejected(String reason);
        void onConnectionError(String message);
        void onRoomListReceived(List<Map<String, Object>> roomList);
        void onRoomUpdated(Object roomData);
        void onGameStarted(Object gameData);
        void onGameUpdated(Object gameState);
        void onGameOver(Object winner);
        void onChatMessageReceived(String message, String senderId);
        void onErrorReceived(String errorMessage);
        void onInfoReceived(String infoMessage);
    }
}