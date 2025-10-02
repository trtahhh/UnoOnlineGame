package com.uno.client;

import com.uno.gui.GameGUI;
import com.uno.gui.LobbyGUI;
import com.uno.gui.LoginGUI;
import com.uno.model.CardColor;
import com.uno.server.GameRoom;
import com.uno.server.GameRoom.GameState;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Class chính của client, quản lý giao diện và kết nối mạng
 */
public class UnoClientMain implements UnoClient.ClientListener {
    private UnoClient client;
    private String playerId;
    // private String currentRoomId; // Removed unused field
    
    private LoginGUI loginGUI;
    private LobbyGUI lobbyGUI;
    private GameGUI gameGUI;
    
    /**
     * Khởi tạo client
     */
    public UnoClientMain() {
        // Hiển thị màn hình đăng nhập
        SwingUtilities.invokeLater(() -> {
            loginGUI = new LoginGUI(this);
            loginGUI.setVisible(true);
        });
    }
    
    /**
     * Kết nối đến server
     * 
     * @param serverAddress Địa chỉ server
     * @param playerName Tên người chơi
     */
    public void connectToServer(String serverAddress, String playerName) {
        client = new UnoClient(serverAddress, this);
        if (client.connect(playerName)) {
            // Kết nối thành công, chờ phản hồi từ server
            loginGUI.setStatusText("Đang kết nối đến server...");
        } else {
            // Kết nối thất bại
            loginGUI.setStatusText("Không thể kết nối đến server!");
        }
    }
    
    /**
     * Ngắt kết nối với server
     */
    public void disconnectFromServer() {
        if (client != null) {
            client.disconnect();
        }
    }
    
    /**
     * Tạo phòng mới
     * 
     * @param roomName Tên phòng
     */
    public void createRoom(String roomName) {
        if (client != null) {
            client.createRoom(roomName);
        }
    }
    
    /**
     * Tham gia phòng
     * 
     * @param roomId ID của phòng
     */
    public void joinRoom(String roomId) {
        if (client != null) {
            client.joinRoom(roomId);
        }
    }
    
    /**
     * Rời khỏi phòng
     */
    public void leaveRoom() {
        if (client != null) {
            client.leaveRoom();
        }
    }
    
    /**
     * Bắt đầu game
     */
    public void startGame() {
        if (client != null) {
            client.startGame();
        }
    }
    
    /**
     * Đánh lá bài
     * 
     * @param cardIndex Vị trí lá bài trong tay
     * @param declaredColor Màu được chọn nếu là lá Wild
     */
    public void playCard(int cardIndex, CardColor declaredColor) {
        if (client != null) {
            client.playCard(cardIndex, declaredColor);
        }
    }
    
    /**
     * Rút lá bài
     */
    public void drawCard() {
        if (client != null) {
            client.drawCard();
        }
    }
    
    /**
     * Kết thúc lượt
     */
    public void endTurn() {
        if (client != null) {
            client.endTurn();
        }
    }
    
    /**
     * Hô Uno
     */
    public void callUno() {
        if (client != null) {
            client.callUno();
        }
    }
    
    /**
     * Thách thức Wild Draw Four
     * 
     * @param challengedPlayerId ID của người chơi bị thách thức
     */
    public void challenge(String challengedPlayerId) {
        if (client != null) {
            client.challenge(challengedPlayerId);
        }
    }
    
    /**
     * Gửi tin nhắn chat
     * 
     * @param chatMessage Nội dung tin nhắn
     */
    public void sendChatMessage(String chatMessage) {
        if (client != null) {
            client.sendChatMessage(chatMessage);
        }
    }
    
    /**
     * Xử lý sự kiện kết nối thành công
     * 
     * @param clientId ID của client
     */
    @Override
    public void onConnected(String clientId) {
        // Lưu trữ ID đã được trim
        if (clientId != null) {
            this.playerId = clientId.trim();
        } else {
            this.playerId = "";
        }
        
        // In thông tin về ID nhận được
        System.out.println("UnoClientMain: Nhận ID từ server: '" + clientId + "', sau khi trim: '" + this.playerId + "'");
        
        SwingUtilities.invokeLater(() -> {
            // Đóng màn hình đăng nhập và mở màn hình sảnh
            loginGUI.dispose();
            lobbyGUI = new LobbyGUI(this);
            lobbyGUI.setVisible(true);
        });
    }
    
    /**
     * Xử lý sự kiện ngắt kết nối
     */
    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            // Đóng tất cả các màn hình hiện tại
            if (gameGUI != null) {
                gameGUI.dispose();
                gameGUI = null;
            }
            
            if (lobbyGUI != null) {
                lobbyGUI.dispose();
                lobbyGUI = null;
            }
            
            // Mở lại màn hình đăng nhập
            loginGUI = new LoginGUI(this);
            loginGUI.setVisible(true);
            loginGUI.setStatusText("Đã ngắt kết nối với server");
        });
    }
    
    /**
     * Xử lý sự kiện kết nối bị từ chối
     * 
     * @param reason Lý do từ chối
     */
    @Override
    public void onConnectionRejected(String reason) {
        SwingUtilities.invokeLater(() -> {
            loginGUI.setStatusText("Kết nối bị từ chối: " + reason);
        });
    }
    
    /**
     * Xử lý sự kiện lỗi kết nối
     * 
     * @param message Thông báo lỗi
     */
    @Override
    public void onConnectionError(String message) {
        SwingUtilities.invokeLater(() -> {
            if (loginGUI != null && loginGUI.isVisible()) {
                loginGUI.setStatusText("Lỗi kết nối: " + message);
            } else {
                JOptionPane.showMessageDialog(null, "Lỗi kết nối: " + message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                onDisconnected();
            }
        });
    }
    
    /**
     * Xử lý sự kiện nhận danh sách phòng
     * 
     * @param roomList Danh sách phòng
     */
    @Override
    public void onRoomListReceived(List<Map<String, Object>> roomList) {
        SwingUtilities.invokeLater(() -> {
            if (lobbyGUI != null) {
                lobbyGUI.updateRoomList(roomList);
            }
        });
    }
    
    /**
     * Xử lý sự kiện cập nhật phòng
     * 
     * @param roomData Dữ liệu phòng
     */
    @Override
    public void onRoomUpdated(Object roomData) {
        SwingUtilities.invokeLater(() -> {
            // Cập nhật thông tin phòng hiện tại
            // Đây là đối tượng GameRoom được gửi từ server
            GameRoom room = (GameRoom) roomData;
            // currentRoomId = room.getId(); // Removed as it's not used
            
            if (gameGUI == null) {
                // Nếu chưa có giao diện game, hiển thị phòng chờ
                if (lobbyGUI != null) {
                    lobbyGUI.updateRoomInfo(room);
                }
            }
        });
    }
    
    /**
     * Xử lý sự kiện game bắt đầu
     * 
     * @param gameData Dữ liệu game
     */
    @Override
    public void onGameStarted(Object gameData) {
        SwingUtilities.invokeLater(() -> {
            // Đóng màn hình sảnh và mở màn hình game
            if (lobbyGUI != null) {
                lobbyGUI.setVisible(false);
            }
            
            // In ra ID người chơi trước khi khởi tạo GameGUI
            System.out.println("UnoClientMain.onGameStarted: Khởi tạo GameGUI với playerId = '" + playerId + "'");
            
            gameGUI = new GameGUI(this, playerId);
            gameGUI.setVisible(true);
            
            // Cập nhật trạng thái game ban đầu
            gameGUI.updateGameState((GameState) gameData);
        });
    }
    
    /**
     * Xử lý sự kiện cập nhật game
     * 
     * @param gameState Trạng thái game
     */
    @Override
    public void onGameUpdated(Object gameState) {
        System.out.println("UnoClientMain.onGameUpdated: gameState class = " + 
                          (gameState != null ? gameState.getClass().getName() : "null"));
        
        if (gameState instanceof GameState) {
            GameState state = (GameState) gameState;
            System.out.println("UnoClientMain: currentPlayerId = " + state.getCurrentPlayerId());
        }
        
        SwingUtilities.invokeLater(() -> {
            if (gameGUI != null) {
                gameGUI.updateGameState((GameState) gameState);
            }
        });
    }
    
    /**
     * Xử lý sự kiện game kết thúc
     * 
     * @param winner Người chơi thắng cuộc
     */
    @Override
    public void onGameOver(Object winner) {
        SwingUtilities.invokeLater(() -> {
            if (gameGUI != null) {
                // Hiển thị thông báo người thắng
                String winnerName = winner.toString();
                JOptionPane.showMessageDialog(gameGUI, "Người chơi " + winnerName + " đã thắng!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                
                // Đóng màn hình game và mở lại sảnh
                gameGUI.dispose();
                gameGUI = null;
                
                if (lobbyGUI != null) {
                    lobbyGUI.setVisible(true);
                }
            }
        });
    }
    
    /**
     * Xử lý sự kiện nhận tin nhắn chat
     * 
     * @param message Nội dung tin nhắn
     * @param senderId ID của người gửi
     */
    @Override
    public void onChatMessageReceived(String message, String senderId) {
        SwingUtilities.invokeLater(() -> {
            if (gameGUI != null) {
                gameGUI.addChatMessage(message);
            } else if (lobbyGUI != null) {
                lobbyGUI.addChatMessage(message);
            }
        });
    }
    
    /**
     * Xử lý sự kiện nhận thông báo lỗi
     * 
     * @param errorMessage Thông báo lỗi
     */
    @Override
    public void onErrorReceived(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, errorMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Xử lý sự kiện nhận thông báo
     * 
     * @param infoMessage Nội dung thông báo
     */
    @Override
    public void onInfoReceived(String infoMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, infoMessage, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    /**
     * Lấy ID của người chơi
     * 
     * @return ID của người chơi
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Entry point của client
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Đặt look and feel của Java
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Khởi tạo client
        new UnoClientMain();
    }
}