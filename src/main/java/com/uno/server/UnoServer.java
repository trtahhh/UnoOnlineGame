package com.uno.server;

import com.uno.model.Player;
import com.uno.utils.Message;
import com.uno.utils.MessageType;
import com.uno.utils.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server class for Uno game handling client connections and game rooms
 */
public class UnoServer {
    private static final int DEFAULT_PORT = 5000;
    
    private final String serverId;
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService clientThreadPool;
    private final List<ClientHandler> clients;
    private final Map<String, GameRoom> rooms;
    private volatile boolean running;
    
    public UnoServer() {
        this(DEFAULT_PORT);
    }
    
    public UnoServer(int port) {
        this.serverId = UUID.randomUUID().toString();
        this.port = port;
        this.clientThreadPool = Executors.newCachedThreadPool();
        this.clients = new ArrayList<>();
        this.rooms = new HashMap<>();
        this.running = false;
    }
    
    /**
     * Starts the server and begins accepting client connections
     */
    public void start() {
        try {
            System.out.println("[SERVER] Starting UNO Online Server...");
            System.out.println("[TCP] Creating ServerSocket on port " + port);
            
            serverSocket = new ServerSocket(port);
            running = true;
            
            System.out.println("[SERVER] ServerSocket created on port " + port);
            System.out.println("[TCP] Binding to 0.0.0.0:" + port);
            System.out.println("[TCP] Listening for incoming connections...");
            System.out.println("[THREADING] Thread Pool initialized: 100 threads");
            System.out.println("[THREADING] Thread Model: One-Thread-Per-Client");
            
            System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "STARTUP", 
                    "Khoi tao TCP server socket tren port " + port + " - Server socket initialization"));
            
            // Wait for client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    int clientPort = clientSocket.getPort();
                    
                    System.out.println("[TCP] TCP Handshake completed");
                    System.out.println("[SERVER] Client connected from " + clientAddress + ":" + clientPort);
                    System.out.println("[SOCKET] Remote Address: /" + clientAddress + ":" + clientPort);
                    System.out.println("[SOCKET] Local Address: /" + serverSocket.getInetAddress().getHostAddress() + ":" + port);
                    
                    System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "ACCEPT_CONNECTION", 
                            "Ket noi TCP moi tu client: " + clientAddress + ":" + clientPort + 
                            " - Connection accept event"));
                    
                    // Create and start handler for client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clients.add(clientHandler);
                    
                    System.out.println("[THREAD] New thread assigned to client_" + String.format("%03d", clients.size()));
                    System.out.printf("[THREAD-POOL] Active: %d | Idle: %d | Queue: 0%n", 
                                    clientThreadPool instanceof java.util.concurrent.ThreadPoolExecutor ? 
                                    ((java.util.concurrent.ThreadPoolExecutor)clientThreadPool).getActiveCount() : clients.size(),
                                    100 - clients.size());
                    
                    clientThreadPool.execute(clientHandler);
                } catch (IOException e) {
                    if (running) {
                        System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "ACCEPT_ERROR", 
                                "Loi khi xu ly accept() operation: " + e.getMessage() + " - Socket accept failure"));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "STARTUP_ERROR", 
                    "Loi khoi tao server socket: " + e.getMessage() + " - Socket bind failure"));
        } finally {
            stop();
        }
    }
    
    /**
     * Stops the server and closes all connections
     */
    public void stop() {
        running = false;
        
        System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "SHUTDOWN", 
                "Bat dau qua trinh dong server - Server shutdown initiated"));
        
        // Close all client connections
        int closedConnections = 0;
        for (ClientHandler client : clients) {
            client.close();
            closedConnections++;
        }
        System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "CONNECTION_CLEANUP", 
                "Da dong " + closedConnections + " ket noi client - Client socket cleanup"));
        clients.clear();
        
        // Shut down thread pool
        clientThreadPool.shutdown();
        System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "THREAD_CLEANUP", 
                "Thread pool da shutdown - Resource cleanup"));
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "SOCKET_CLOSE", 
                        "Server socket da dong - Main listening socket closed"));
            }
        } catch (IOException e) {
            System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "SOCKET_CLOSE_ERROR", 
                    "Loi dong server socket: " + e.getMessage() + " - Socket shutdown error"));
        }
        
        System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "SHUTDOWN_COMPLETE", 
                "Server da dung hoan toan - All resources released"));
    }
    
    /**
     * Xóa client khỏi danh sách
     * 
     * @param client Client cần xóa
     */
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
    
    /**
     * Tạo phòng mới
     * 
     * @param roomName Tên phòng
     * @param host Người chơi chủ phòng
     * @return Phòng được tạo
     */
    public GameRoom createRoom(String roomName, Player host) {
        for (ClientHandler client : clients) {
            if (client.getPlayer() != null && client.getPlayer().getId().equals(host.getId())) {
                GameRoom room = new GameRoom(roomName, host, client);
                rooms.put(room.getId(), room);
                return room;
            }
        }
        return null;
    }
    
    /**
     * Xóa phòng
     * 
     * @param roomId ID của phòng cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    public boolean removeRoom(String roomId) {
        return rooms.remove(roomId) != null;
    }
    
    /**
     * Lấy phòng theo ID
     * 
     * @param roomId ID của phòng
     * @return Phòng tương ứng nếu tồn tại, ngược lại null
     */
    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    /**
     * Lấy phòng chứa người chơi
     * 
     * @param playerId ID của người chơi
     * @return Phòng chứa người chơi nếu tồn tại, ngược lại null
     */
    public GameRoom getRoomByPlayer(String playerId) {
        for (GameRoom room : rooms.values()) {
            for (Player player : room.getGame().getPlayers()) {
                if (player.getId().equals(playerId)) {
                    return room;
                }
            }
        }
        return null;
    }
    
    /**
     * Lấy danh sách phòng
     * 
     * @return Danh sách phòng
     */
    public List<Map<String, Object>> getRoomList() {
        List<Map<String, Object>> roomList = new ArrayList<>();
        
        for (GameRoom room : rooms.values()) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("id", room.getId());
            roomInfo.put("name", room.getName());
            roomInfo.put("hostName", room.getHost().getName());
            roomInfo.put("playerCount", room.getPlayerCount());
            roomInfo.put("gameStarted", room.getGame().isGameStarted());
            
            roomList.add(roomInfo);
        }
        
        return roomList;
    }
    
    /**
     * Gửi danh sách phòng đến tất cả client
     */
    public void broadcastRoomList() {
        List<Map<String, Object>> roomList = getRoomList();
        
        for (ClientHandler client : clients) {
            client.sendMessage(new Message(MessageType.ROOM_LIST, roomList, serverId));
        }
    }
    
    /**
     * Lấy ID của server
     * 
     * @return ID của server
     */
    public String getServerId() {
        return serverId;
    }
    
    /**
     * Lấy số lượng client đang kết nối
     * 
     * @return Số lượng client đang kết nối
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Lấy số lượng phòng chơi
     * 
     * @return Số lượng phòng chơi
     */
    public int getRoomCount() {
        return rooms.size();
    }
    
    /**
     * Entry point của server
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Kiểm tra xem có cổng được chỉ định qua command line không
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println(StringUtils.formatNetworkLog("SERVER_MAIN", "CONFIG_ERROR", 
                        "Cong khong hop le, su dung cong mac dinh: " + DEFAULT_PORT));
            }
        }
        
        // Khởi tạo và chạy server
        UnoServer server = new UnoServer(port);
        server.start();
    }
}