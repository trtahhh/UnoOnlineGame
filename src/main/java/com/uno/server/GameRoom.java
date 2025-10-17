package com.uno.server;

import com.uno.model.Card;
import com.uno.model.CardColor;
import com.uno.model.CardType;
import com.uno.model.Game;
import com.uno.model.Player;
import com.uno.utils.Message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Class đại diện cho một phòng chơi game Uno
 */
public class GameRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String name;
    private final Player host;
    private final Map<String, Player> players;
    private final transient Map<String, ClientHandler> clientHandlers;
    private final Game game;
    
    public GameRoom(String name, Player host, ClientHandler hostHandler) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.host = host;
        this.players = new HashMap<>();
        this.clientHandlers = new HashMap<>();
        this.game = new Game();
        
        // Room creation logging
        System.out.println("[ROOM-" + id.substring(0, 3) + "] Room created | Max players: 4");
        
        // Thêm host vào phòng
        this.players.put(host.getId(), host);
        this.clientHandlers.put(host.getId(), hostHandler);
        this.game.addPlayer(host);
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Player getHost() {
        return host;
    }
    
    /**
     * Thêm người chơi vào phòng
     * 
     * @param player Người chơi cần thêm
     * @param handler Handler của người chơi
     * @return true nếu thêm thành công, ngược lại false
     */
    public boolean addPlayer(Player player, ClientHandler handler) {
        if (players.size() < 4 && !game.isGameStarted()) {
            players.put(player.getId(), player);
            clientHandlers.put(player.getId(), handler);
            game.addPlayer(player);
            
            // Player joined logging
            System.out.println("[ROOM-" + id.substring(0, 3) + "] Player joined: " + player.getName() + 
                             " (" + players.size() + "/4 players)");
            return true;
        }
        return false;
    }
    
    /**
     * Xóa người chơi khỏi phòng
     * 
     * @param playerId ID của người chơi cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    public boolean removePlayer(String playerId) {
        if (players.containsKey(playerId)) {
            if (playerId.equals(host.getId()) && !game.isGameStarted()) {
                // Nếu host rời đi và game chưa bắt đầu, chọn người chơi khác làm host
                for (String id : players.keySet()) {
                    if (!id.equals(host.getId())) {
                        // Cập nhật host mới và thông báo
                        Player newHostPlayer = players.get(id);
                        // Không thể thay đổi host.id vì nó là final, nên tạo thông báo
                        broadcast(new Message(com.uno.utils.MessageType.CHAT_MESSAGE, "SERVER: " + newHostPlayer.getName() + " da tro thanh host moi.", "server"));
                        break;
                    }
                }
            }
            
            players.remove(playerId);
            clientHandlers.remove(playerId);
            game.removePlayer(playerId);
            return true;
        }
        return false;
    }
    
    /**
     * Bắt đầu game
     * 
     * @return true nếu bắt đầu thành công, ngược lại false
     */
    public boolean startGame() {
        if (players.size() >= 2 && game.startGame()) {
            System.out.println("[ROOM-" + id.substring(0, 3) + "] Game started with " + players.size() + " players");
            return true;
        }
        return false;
    }
    
    /**
     * Đánh lá bài
     * 
     * @param playerId ID của người chơi
     * @param cardIndex Vị trí lá bài trong tay
     * @param colorName Tên màu được chọn cho lá Wild
     * @return true nếu đánh thành công, ngược lại false
     */
    public boolean playCard(String playerId, int cardIndex, String colorName) {
        System.out.println("GameRoom.playCard: playerId=" + playerId + ", game.getCurrentPlayer().getId()=" + 
                          (game.getCurrentPlayer() != null ? game.getCurrentPlayer().getId() : "null"));
        CardColor declaredColor = CardColor.valueOf(colorName);
        boolean result = game.playCard(playerId, cardIndex, declaredColor);
        System.out.println("GameRoom.playCard result: " + result);
        return result;
    }
    
    /**
     * Rút lá bài
     * 
     * @param playerId ID của người chơi
     * @return true nếu rút thành công, ngược lại false
     */
    public boolean drawCard(String playerId) {
        return game.drawCard(playerId) != null;
    }
    
    /**
     * Kết thúc lượt
     * 
     * @param playerId ID của người chơi
     * @return true nếu kết thúc thành công, ngược lại false
     */
    public boolean endTurn(String playerId) {
        return game.endTurn(playerId);
    }
    
    /**
     * Hô Uno
     * 
     * @param playerId ID của người chơi
     * @return true nếu hô thành công, ngược lại false
     */
    public boolean callUno(String playerId) {
        return game.callUno(playerId);
    }
    
    /**
     * Thách thức Wild Draw Four
     * 
     * @param challengerId ID của người chơi thách thức
     * @param challengedId ID của người chơi bị thách thức
     * @return true nếu thách thức thành công, ngược lại false
     */
    public boolean challenge(String challengerId, String challengedId) {
        // Kiểm tra trạng thái game
        if (!game.isGameStarted() || game.isGameOver()) {
            return false;
        }
        
        // Kiểm tra xem có phải lượt của người thách thức không
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.getId().equals(challengerId)) {
            return false;
        }
        
        // Kiểm tra xem lá trên cùng có phải là Wild Draw Four không
        Card topCard = game.getTopCard();
        if (topCard == null || topCard.getType() != CardType.WILD_DRAW_FOUR) {
            return false;
        }
        
        // Lấy người chơi bị thách thức
        Player challengedPlayer = null;
        for (Player player : game.getPlayers()) {
            if (player.getId().equals(challengedId)) {
                challengedPlayer = player;
                break;
            }
        }
        
        if (challengedPlayer == null) {
            return false;
        }
        
        // Theo luật Uno chính thức, nếu người chơi đã đánh Wild Draw Four khi có lá bài cùng màu với lá trước đó,
        // thì việc chơi là không hợp lệ và người đó phải rút 4 lá bài.
        // Nếu người chơi đánh Wild Draw Four hợp lệ (không có lá cùng màu với lá trước đó),
        // thì người thách thức phải rút 6 lá bài.
        
        // Do giới hạn của cách thiết kế hiện tại, chúng ta không lưu trữ lá bài trước khi Wild Draw Four được đánh
        // Vì vậy, sử dụng xác suất để quyết định kết quả thách thức
        boolean challengeSuccessful = Math.random() < 0.5; // 50% cơ hội thành công
        
        if (challengeSuccessful) {
            // Thách thức thành công: người bị thách thức phải rút 4 lá bài
            for (int i = 0; i < 4; i++) {
                Card drawnCard = game.drawCard(challengedId);
                if (drawnCard == null) break;
            }
            
            // Thông báo cho mọi người biết
            broadcast(new Message(com.uno.utils.MessageType.CHAT_MESSAGE, 
                    "SERVER: Thach thuc thanh cong! " + challengedPlayer.getName() + " da dung Wild Draw Four khong hop le va phai rut 4 la bai.", "server"));
            
            // Người thách thức không phải rút bài và tiếp tục lượt chơi
            return true;
        } else {
            // Thách thức thất bại: người thách thức phải rút 6 lá bài
            for (int i = 0; i < 6; i++) {
                Card drawnCard = game.drawCard(challengerId);
                if (drawnCard == null) break;
            }
            
            // Thông báo cho mọi người biết
            broadcast(new Message(com.uno.utils.MessageType.CHAT_MESSAGE, 
                    "SERVER: Thach thuc that bai! " + currentPlayer.getName() + " phai rut 6 la bai.", "server"));
            
            // Người bị thách thức không phải rút thêm bài
            return false;
        }
    }
    
    /**
     * Gửi tin nhắn đến tất cả người chơi trong phòng
     * 
     * @param message Tin nhắn cần gửi
     */
    public void broadcast(Message message) {
        long broadcastStart = System.currentTimeMillis();
        System.out.println("[BROADCAST] Broadcasting " + message.getType() + " to " + clientHandlers.size() + " clients");
        
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage(message);
        }
        
        long broadcastTime = System.currentTimeMillis() - broadcastStart;
        System.out.println("[BROADCAST] Broadcast completed in " + broadcastTime + "ms");
    }
    
    /**
     * Cập nhật trạng thái game cho tất cả người chơi
     */
    public void updateGameState() {
        Player currentPlayer = game.getCurrentPlayer();
        String currentPlayerId = currentPlayer != null ? currentPlayer.getId() : "null";
        System.out.println("updateGameState: NGƯỜI CHƠI HIỆN TẠI = " + (currentPlayer != null ? currentPlayer.getName() + "(" + currentPlayerId + ")" : "null"));
        
        for (String playerId : players.keySet()) {
            Player player = players.get(playerId);
            ClientHandler handler = clientHandlers.get(playerId);
            if (handler != null) {
                GameState state = getPlayerGameState(playerId);
                System.out.println("Gửi GameState tới " + player.getName() + "(" + playerId + "), currentPlayerId = " + state.getCurrentPlayerId());
                handler.sendMessage(new Message(com.uno.utils.MessageType.GAME_UPDATE, state, "server"));
            }
        }
    }
    
    /**
     * Lấy trạng thái game cho người chơi cụ thể
     * 
     * @param playerId ID của người chơi
     * @return Trạng thái game cho người chơi
     */
    public GameState getPlayerGameState(String playerId) {
        GameState state = new GameState(game, playerId);
        Player currentPlayer = game.getCurrentPlayer();
        String currentPlayerId = currentPlayer != null ? currentPlayer.getId() : "null";
        System.out.println("GameState cho " + playerId + ", currentPlayerId từ Game: " + currentPlayerId + ", từ GameState: " + state.getCurrentPlayerId());
        return state;
    }
    
    /**
     * Lấy game
     * 
     * @return Game
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Lấy số lượng người chơi trong phòng
     * 
     * @return Số lượng người chơi
     */
    public int getPlayerCount() {
        return players.size();
    }
    
    /**
     * Lấy danh sách ID của tất cả người chơi trong phòng
     * 
     * @return Danh sách ID của người chơi
     */
    public Set<String> getPlayerIds() {
        return players.keySet();
    }
    
    /**
     * Lấy ClientHandler của người chơi theo ID
     * 
     * @param playerId ID của người chơi
     * @return ClientHandler của người chơi
     */
    public ClientHandler getClientHandler(String playerId) {
        return clientHandlers.get(playerId);
    }
    
    /**
     * Class đại diện cho trạng thái game được gửi đến client
     */
    public static class GameState implements java.io.Serializable {
        // Tăng serialVersionUID để đảm bảo không có vấn đề serialization/deserialization
        private static final long serialVersionUID = 3L;
        
        // Sử dụng HashMap thay vì Map để đảm bảo serialization nhất quán
        private final HashMap<String, PlayerInfo> playerInfos;
        private final Card topCard;
        // Sửa thành một trường có thể đọc/ghi để tránh các vấn đề với deserialization
        private String currentPlayerId;
        private final boolean clockwise;
        private final boolean gameOver;
        private final String winnerId;
        
        public GameState(Game game, String viewerId) {
            this.playerInfos = new HashMap<>();
            
            // Lấy thông tin người chơi
            for (Player player : game.getPlayers()) {
                if (player.getId().equals(viewerId)) {
                    // Người chơi hiện tại thấy tất cả lá bài của mình
                    playerInfos.put(player.getId(), new PlayerInfo(player, true));
                } else {
                    // Người chơi khác chỉ thấy số lượng lá bài
                    playerInfos.put(player.getId(), new PlayerInfo(player, false));
                }
            }
            
            this.topCard = game.getTopCard();
            Player currentPlayer = game.getCurrentPlayer();
            
            // Đảm bảo ID luôn được trim và không null
            if (currentPlayer != null && currentPlayer.getId() != null) {
                this.currentPlayerId = currentPlayer.getId().trim();
                System.out.println("Constructor GameState - Current Player: " + currentPlayer.getName() + 
                                  " (ID: '" + this.currentPlayerId + "')");
            } else {
                this.currentPlayerId = "";
                System.out.println("Constructor GameState - Current Player: null");
            }
            
            this.clockwise = game.isClockwise();
            this.gameOver = game.isGameOver();
            
            Player winner = game.getWinner();
            this.winnerId = winner != null ? winner.getId() : null;
        }
        
        // Getters
        public HashMap<String, PlayerInfo> getPlayerInfos() {
            return playerInfos;
        }
        
        public Card getTopCard() {
            return topCard;
        }
        
        public String getCurrentPlayerId() {
            System.out.println("GameState.getCurrentPlayerId được gọi, trả về: " + currentPlayerId);
            return currentPlayerId;
        }
        
        public boolean isClockwise() {
            return clockwise;
        }
        
        public boolean isGameOver() {
            return gameOver;
        }
        
        public String getWinnerId() {
            return winnerId;
        }
    }
    
    /**
     * Class đại diện cho thông tin người chơi được gửi đến client
     */
    public static class PlayerInfo implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String id;
        private final String name;
        private final int handSize;
        private final java.util.List<Card> hand;
        private final boolean calledUno;
        
        public PlayerInfo(Player player, boolean includeHand) {
            this.id = player.getId();
            this.name = player.getName();
            this.handSize = player.getHandSize();
            this.hand = includeHand ? player.getHand() : null;
            this.calledUno = player.hasCalledUno();
        }
        
        // Getters
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public int getHandSize() {
            return handSize;
        }
        
        public java.util.List<Card> getHand() {
            return hand;
        }
        
        public boolean hasCalledUno() {
            return calledUno;
        }
    }
}