package com.uno.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class đại diện cho một ván game Uno
 */
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Constants
    private static final int INITIAL_CARDS = 7;
    private static final int MAX_PLAYERS = 4;
    
    // Game state
    private final Deck deck;
    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean clockwise;
    private boolean gameStarted;
    private boolean gameOver;
    
    public Game() {
        this.deck = new Deck();
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.clockwise = true;
        this.gameStarted = false;
        this.gameOver = false;
    }
    
    /**
     * Thêm người chơi vào game
     * 
     * @param player Người chơi cần thêm
     * @return true nếu thêm thành công, ngược lại false
     */
    public boolean addPlayer(Player player) {
        if (players.size() < MAX_PLAYERS && !gameStarted) {
            players.add(player);
            return true;
        }
        return false;
    }
    
    /**
     * Xóa người chơi khỏi game
     * 
     * @param playerId ID của người chơi cần xóa
     * @return true nếu xóa thành công, ngược lại false
     */
    public boolean removePlayer(String playerId) {
        if (!gameStarted) {
            return players.removeIf(p -> p.getId().equals(playerId));
        }
        return false;
    }
    
    /**
     * Bắt đầu game
     * 
     * @return true nếu bắt đầu thành công, ngược lại false
     */
    public boolean startGame() {
        if (players.size() >= 2 && !gameStarted) {
            gameStarted = true;
            gameOver = false;
            
            // Trộn bài
            deck.shuffle();
            
            // Phát bài cho người chơi
            for (Player player : players) {
                player.addCards(deck.drawCards(INITIAL_CARDS));
            }
            
            // Đặt lá bài đầu tiên
            deck.initializeDiscardPile();
            
            // Xử lý nếu lá bài đầu tiên là lá đặc biệt
            Card topCard = deck.getTopCard();
            if (topCard != null) {
                if (topCard.getType() == CardType.SKIP) {
                    nextPlayer(); // Người chơi đầu tiên bị bỏ qua
                } else if (topCard.getType() == CardType.REVERSE) {
                    clockwise = false; // Đảo chiều
                } else if (topCard.getType() == CardType.DRAW_TWO) {
                    Player currentPlayer = getCurrentPlayer();
                    currentPlayer.addCards(deck.drawCards(2));
                    nextPlayer(); // Người chơi đầu tiên bị bỏ qua sau khi rút 2 lá
                }
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Đánh lá bài
     * 
     * @param playerId ID của người chơi
     * @param cardIndex Vị trí lá bài trong tay
     * @param declaredColor Màu được chọn nếu là lá Wild
     * @return true nếu đánh thành công, ngược lại false
     */
    public boolean playCard(String playerId, int cardIndex, CardColor declaredColor) {
        if (!gameStarted || gameOver) {
            return false;
        }
        
        Player currentPlayer = getCurrentPlayer();
        System.out.println("Lượt của: " + currentPlayer.getId() + ", người đang đánh: " + playerId);
        
        // Sử dụng StringUtils để so sánh an toàn
        if (!com.uno.utils.StringUtils.safeEquals(currentPlayer.getId(), playerId)) {
            System.out.println("Từ chối: Không phải lượt của " + playerId);
            return false;
        }
        
        // Kiểm tra xem lá bài có thể đánh được không
        Card topCard = deck.getTopCard();
        List<Integer> playableCardIndices = currentPlayer.getPlayableCardIndices(topCard);
        if (!playableCardIndices.contains(cardIndex)) {
            return false;
        }
        
        // Đánh lá bài
        Card playedCard = currentPlayer.playCard(cardIndex);
        if (playedCard == null) {
            return false;
        }
        
        // Nếu là lá Wild, cập nhật màu đã chọn
        if (playedCard.getType() == CardType.WILD || playedCard.getType() == CardType.WILD_DRAW_FOUR) {
            playedCard = new Card(declaredColor, playedCard.getType(), playedCard.getValue());
        }
        
        // Đặt lá bài lên chồng bài đã đánh
        deck.discardCard(playedCard);
        
        // Kiểm tra nếu người chơi đã thắng
        if (currentPlayer.getHandSize() == 0) {
            gameOver = true;
            return true;
        }
        
        // Xử lý hiệu ứng của lá bài
        applyCardEffect(playedCard);
        
        return true;
    }
    
    /**
     * Áp dụng hiệu ứng của lá bài
     * 
     * @param card Lá bài cần áp dụng hiệu ứng
     */
    private void applyCardEffect(Card card) {
        switch (card.getType()) {
            case SKIP:
                nextPlayer(); // Bỏ qua lượt người chơi kế tiếp
                break;
            case REVERSE:
                clockwise = !clockwise; // Đảo chiều
                // Nếu chỉ có 2 người chơi, Reverse hoạt động giống Skip
                if (players.size() == 2) {
                    nextPlayer();
                }
                break;
            case DRAW_TWO:
                nextPlayer(); // Chuyển đến người chơi kế tiếp
                Player nextPlayer = getCurrentPlayer();
                nextPlayer.addCards(deck.drawCards(2)); // Người chơi kế tiếp rút 2 lá
                nextPlayer(); // Người chơi kế tiếp bị bỏ qua lượt
                break;
            case WILD_DRAW_FOUR:
                nextPlayer(); // Chuyển đến người chơi kế tiếp
                nextPlayer = getCurrentPlayer();
                nextPlayer.addCards(deck.drawCards(4)); // Người chơi kế tiếp rút 4 lá
                nextPlayer(); // Người chơi kế tiếp bị bỏ qua lượt
                break;
            default:
                nextPlayer(); // Chuyển đến người chơi kế tiếp
                break;
        }
    }
    
    /**
     * Rút lá bài cho người chơi hiện tại
     * 
     * @param playerId ID của người chơi
     * @return Lá bài được rút nếu rút thành công, ngược lại null
     */
    public Card drawCard(String playerId) {
        if (!gameStarted || gameOver) {
            return null;
        }
        
        Player currentPlayer = getCurrentPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            return null;
        }
        
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            currentPlayer.addCard(drawnCard);
        }
        return drawnCard;
    }
    
    /**
     * Kết thúc lượt của người chơi hiện tại sau khi rút bài
     * 
     * @param playerId ID của người chơi
     * @return true nếu kết thúc thành công, ngược lại false
     */
    public boolean endTurn(String playerId) {
        if (!gameStarted || gameOver) {
            return false;
        }
        
        Player currentPlayer = getCurrentPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            return false;
        }
        
        nextPlayer();
        return true;
    }
    
    /**
     * Chuyển đến người chơi kế tiếp
     */
    private void nextPlayer() {
        int oldIndex = currentPlayerIndex;
        String oldPlayerId = players.get(currentPlayerIndex).getId();
        
        if (clockwise) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
        
        System.out.println("Chuyển lượt từ " + oldPlayerId + " (index " + oldIndex + ") sang " + 
                          players.get(currentPlayerIndex).getId() + " (index " + currentPlayerIndex + ")");
    }
    
    /**
     * Lấy người chơi hiện tại
     * 
     * @return Người chơi hiện tại
     */
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            System.out.println("Game.getCurrentPlayer: CẢNH BÁO - Danh sách người chơi rỗng");
            return null;
        }
        
        System.out.println("Game.getCurrentPlayer: players.size=" + players.size() + ", currentPlayerIndex=" + currentPlayerIndex);
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            System.out.println("Game.getCurrentPlayer: CẢNH BÁO - currentPlayerIndex nằm ngoài phạm vi hợp lệ");
            // Đặt về index hợp lệ
            currentPlayerIndex = currentPlayerIndex % players.size();
            if (currentPlayerIndex < 0) currentPlayerIndex += players.size();
        }
        
        Player current = players.get(currentPlayerIndex);
        System.out.println("Game.getCurrentPlayer: index=" + currentPlayerIndex + ", player=" + 
                          (current != null ? current.getName() + "(" + current.getId() + ")" : "null"));
        
        // In ra danh sách tất cả người chơi để debug
        System.out.println("Danh sách tất cả người chơi:");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            System.out.println("- Player[" + i + "]: " + p.getName() + "(" + p.getId() + ")");
        }
        
        return current;
    }
    
    /**
     * Hô Uno cho người chơi
     * 
     * @param playerId ID của người chơi
     * @return true nếu hô Uno thành công, ngược lại false
     */
    public boolean callUno(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId) && player.getHandSize() == 1) {
                player.callUno();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Phạt người chơi vì không hô Uno
     * 
     * @param playerId ID của người chơi
     * @return true nếu phạt thành công, ngược lại false
     */
    public boolean penalizeForNotCallingUno(String playerId) {
        for (Player player : players) {
            if (player.getId().equals(playerId) && player.getHandSize() == 1 && !player.hasCalledUno()) {
                player.addCards(deck.drawCards(2));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy danh sách người chơi
     * 
     * @return Danh sách người chơi
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }
    
    /**
     * Lấy lá bài trên cùng của chồng bài đã đánh
     * 
     * @return Lá bài trên cùng
     */
    public Card getTopCard() {
        return deck.getTopCard();
    }
    
    /**
     * Kiểm tra xem game đã bắt đầu chưa
     * 
     * @return true nếu đã bắt đầu, ngược lại false
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    /**
     * Kiểm tra xem game đã kết thúc chưa
     * 
     * @return true nếu đã kết thúc, ngược lại false
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * Lấy người chơi chiến thắng
     * 
     * @return Người chơi chiến thắng nếu game đã kết thúc, ngược lại null
     */
    public Player getWinner() {
        if (gameOver) {
            for (Player player : players) {
                if (player.hasWon()) {
                    return player;
                }
            }
        }
        return null;
    }
    
    /**
     * Kiểm tra xem chiều chơi có đang theo chiều kim đồng hồ hay không
     * 
     * @return true nếu theo chiều kim đồng hồ, ngược lại false
     */
    public boolean isClockwise() {
        return clockwise;
    }
    
    /**
     * Reset game để chơi ván mới
     */
    public void resetGame() {
        // Reset deck
        this.deck.shuffle();
        
        // Reset players
        for (Player player : players) {
            while (player.getHandSize() > 0) {
                player.playCard(0);
            }
        }
        
        // Reset game state
        this.currentPlayerIndex = 0;
        this.clockwise = true;
        this.gameStarted = false;
        this.gameOver = false;
    }
}