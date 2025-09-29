package com.uno.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class đại diện cho một người chơi Uno
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private String name;
    private final List<Card> hand;
    private boolean calledUno;
    
    public Player(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.hand = new ArrayList<>();
        this.calledUno = false;
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
    
    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }
    
    /**
     * Thêm lá bài vào tay
     * 
     * @param card Lá bài cần thêm
     */
    public void addCard(Card card) {
        hand.add(card);
        if (calledUno && hand.size() > 1) {
            calledUno = false;
        }
    }
    
    /**
     * Thêm nhiều lá bài vào tay
     * 
     * @param cards Danh sách lá bài cần thêm
     */
    public void addCards(List<Card> cards) {
        hand.addAll(cards);
        if (calledUno && hand.size() > 1) {
            calledUno = false;
        }
    }
    
    /**
     * Đánh lá bài từ tay
     * 
     * @param index Vị trí lá bài trong tay
     * @return Lá bài được đánh
     */
    public Card playCard(int index) {
        if (index < 0 || index >= hand.size()) {
            return null;
        }
        return hand.remove(index);
    }
    
    /**
     * Kiểm tra xem người chơi có thể đánh lá bài nào không
     * 
     * @param topCard Lá bài trên cùng của chồng bài đã đánh
     * @return true nếu có thể đánh ít nhất một lá bài, ngược lại false
     */
    public boolean canPlay(Card topCard) {
        for (Card card : hand) {
            if (card.canPlayOn(topCard)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy các lá bài có thể đánh
     * 
     * @param topCard Lá bài trên cùng của chồng bài đã đánh
     * @return Danh sách các vị trí lá bài có thể đánh
     */
    public List<Integer> getPlayableCardIndices(Card topCard) {
        List<Integer> playableIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).canPlayOn(topCard)) {
                playableIndices.add(i);
            }
        }
        return playableIndices;
    }
    
    /**
     * Lấy số lượng lá bài trong tay
     * 
     * @return Số lượng lá bài
     */
    public int getHandSize() {
        return hand.size();
    }
    
    /**
     * Hô Uno
     */
    public void callUno() {
        if (hand.size() == 1) {
            calledUno = true;
        }
    }
    
    /**
     * Kiểm tra xem người chơi đã hô Uno chưa
     * 
     * @return true nếu đã hô, ngược lại false
     */
    public boolean hasCalledUno() {
        return calledUno;
    }
    
    /**
     * Kiểm tra xem người chơi đã thắng chưa (hết bài trên tay)
     * 
     * @return true nếu đã thắng, ngược lại false
     */
    public boolean hasWon() {
        return hand.isEmpty();
    }
}