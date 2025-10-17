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
            System.out.println("[CLIENT] Initiating connection to " + serverAddress + ":" + serverPort);
            System.out.println("[TCP] SYN → SYN-ACK → ACK");
            
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "SOCKET_INIT", 
                    "Khoi tao ket noi socket TCP den " + serverAddress + ":" + serverPort));
                    
            socket = new Socket(serverAddress, serverPort);
            
            System.out.println("[CLIENT] Connection established");
            System.out.println("[SOCKET] Local port: " + socket.getLocalPort());
            System.out.println("[SOCKET] Socket options: SO_KEEPALIVE=true, TCP_NODELAY=true");
            
            // Initialize input/output streams
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "CONNECT", 
                    "Khoi tao luong I/O - ObjectOutputStream/ObjectInputStream"));
                    
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            
            // Start message handling thread
            running = true;
            new Thread(this).start();
            
            System.out.println("[NET-THREAD] Network handler thread started");
            System.out.println("[NET-THREAD] Listening for server messages...");
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "THREAD", 
                    "Thread doc du lieu song song da khoi dong - Asynchronous network communication"));
            
            // Send connection message to server
            Message connectMessage = new Message(MessageType.CONNECT, playerName, "");
            sendMessage(connectMessage);
            
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "HANDSHAKE", 
                    "TCP handshake hoan tat, gui goi tin CONNECT dau tien"));
            
            return true;
        } catch (IOException e) {
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "CONNECTION_ERROR", 
                    "TCP connection failure: " + e.getMessage()));
            clientListener.onConnectionError("Khong the ket noi den server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (running) {
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "DISCONNECT", 
                    "Bat dau qua trinh dong ket noi - Connection teardown initiated"));
                    
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
                    System.out.println(StringUtils.formatNetworkLog("CLIENT", "SOCKET_CLOSE", 
                            "Socket da dong hoan tat - TCP connection terminated"));
                }
            } catch (IOException e) {
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "CLEANUP_ERROR", 
                        "Loi khi dong resources: " + e.getMessage()));
            }
            
            clientListener.onDisconnected();
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "DISCONNECT_COMPLETE", 
                    "Qua trinh dong ket noi da hoan tat - Network resources released"));
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
                long sendTime = System.currentTimeMillis();
                System.out.println("[SEND] Serializing to Object: " + message.toString().length() + " bytes");
                System.out.println("[SEND] Message Type: " + message.getType() + " | From: " + clientId);
                System.out.println("[TCP] Packet sent via TCP stream");
                System.out.println("[PERF] Message sent at: " + sendTime);
                
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "SEND_MESSAGE", 
                        "Gui du lieu qua TCP stream: " + message.getType() + " - Data serialization"));
                
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.out.println("[ERROR] IOException: " + e.getMessage());
                System.out.println("[ERROR] Attempting reconnection...");
                
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "SEND_ERROR", 
                        "Loi khi truyen du lieu qua network: " + e.getMessage() + " - TCP transmission failure"));
                if (running) {
                    running = false;
                    clientListener.onConnectionError("Mat ket noi den server: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Thread xu ly tin nhan tu server
     */
    @Override
    public void run() {
        try {
            System.out.println("[NET-THREAD] Listening for server messages...");
            while (running) {
                Message message = (Message) input.readObject();
                messageQueue.offer(message);
                
                System.out.println("[NETWORK] Message received (" + message.toString().length() + " bytes)");
                System.out.println("[PROTOCOL] Parsed: " + message.getType());
                System.out.println("[EDT] Scheduling GUI update on EDT");
                        
                handleMessage(message);
                System.out.println("[EDT] GUI updated");
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                running = false;
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "CONNECTION_LOST", 
                        "Socket read error: " + e.getMessage() + " - Connection failure detection"));
                clientListener.onConnectionError("Mat ket noi voi server: " + e.getMessage());
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
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "GAME_DATA", 
                        "Nhan object class: " + (message.getData() != null ? message.getData().getClass().getName() : "null") + " - Object type detection"));
                
                if (message.getData() instanceof GameRoom.GameState) {
                    GameRoom.GameState state = (GameRoom.GameState) message.getData();
                    System.out.println(StringUtils.formatNetworkLog("CLIENT", "STATE_VALIDATION", 
                        "GameState currentPlayerId = " + state.getCurrentPlayerId() + ", local clientId = " + clientId));
                    System.out.println(StringUtils.formatNetworkLog("CLIENT", "STATE_COMPARISON", 
                        "ID comparison result: " + (state.getCurrentPlayerId() != null && 
                                                  clientId != null && 
                                                  state.getCurrentPlayerId().equals(clientId)) + 
                        " - State synchronization verification"));
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
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "UNSUPPORTED_MESSAGE", 
                        "Loai tin nhan khong duoc ho tro: " + message.getType() + " - Protocol violation"));
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