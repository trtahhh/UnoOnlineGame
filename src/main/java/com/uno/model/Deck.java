package com.uno.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Class đại diện cho bộ bài Uno
 */
public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Stack<Card> cards;
    private final Stack<Card> discardPile;
    
    public Deck() {
        cards = new Stack<>();
        discardPile = new Stack<>();
        initializeDeck();
    }
    
    /**
     * Khởi tạo bộ bài Uno chuẩn
     */
    private void initializeDeck() {
        // Thêm các lá bài có số (0-9, mỗi màu)
        for (CardColor color : new CardColor[] { CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW }) {
            // Mỗi màu có một lá số 0
            cards.add(new Card(color, CardType.NUMBER, 0));
            
            // Mỗi màu có hai lá từ số 1 đến 9
            for (int i = 1; i <= 9; i++) {
                cards.add(new Card(color, CardType.NUMBER, i));
                cards.add(new Card(color, CardType.NUMBER, i));
            }
            
            // Mỗi màu có hai lá Skip, Reverse, Draw Two
            for (int i = 0; i < 2; i++) {
                cards.add(new Card(color, CardType.SKIP, -1));
                cards.add(new Card(color, CardType.REVERSE, -1));
                cards.add(new Card(color, CardType.DRAW_TWO, -1));
            }
        }
        
        // Thêm lá Wild và Wild Draw Four
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(CardColor.WILD, CardType.WILD, -1));
            cards.add(new Card(CardColor.WILD, CardType.WILD_DRAW_FOUR, -1));
        }
        
        shuffle();
    }
    
    /**
     * Trộn bộ bài
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }
    
    /**
     * Rút một lá bài từ bộ bài
     * 
     * @return Lá bài được rút
     */
    public Card drawCard() {
        // Nếu hết bài, lấy từ chồng bài đã đánh và trộn lại
        if (cards.isEmpty() && !discardPile.isEmpty()) {
            Card topCard = discardPile.pop();
            cards.addAll(discardPile);
            discardPile.clear();
            discardPile.push(topCard);
            shuffle();
        }
        
        return cards.isEmpty() ? null : cards.pop();
    }
    
    /**
     * Rút nhiều lá bài
     * 
     * @param numCards Số lượng lá bài cần rút
     * @return Danh sách các lá bài được rút
     */
    public List<Card> drawCards(int numCards) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < numCards; i++) {
            Card card = drawCard();
            if (card != null) {
                drawnCards.add(card);
            } else {
                break;
            }
        }
        return drawnCards;
    }
    
    /**
     * Đặt lá bài vào chồng bài đã đánh
     * 
     * @param card Lá bài cần đặt
     */
    public void discardCard(Card card) {
        discardPile.push(card);
    }
    
    /**
     * Lấy lá bài trên cùng của chồng bài đã đánh
     * 
     * @return Lá bài trên cùng
     */
    public Card getTopCard() {
        return discardPile.isEmpty() ? null : discardPile.peek();
    }
    
    /**
     * Khởi tạo chồng bài đã đánh với một lá bài
     * 
     * @param card Lá bài đầu tiên
     */
    public void initializeDiscardPile() {
        // Đặt lá bài đầu tiên lên chồng đã đánh
        Card firstCard = drawCard();
        
        // Đảm bảo lá bài đầu tiên không phải Wild Draw Four
        while (firstCard != null && firstCard.getType() == CardType.WILD_DRAW_FOUR) {
            cards.add(0, firstCard);
            firstCard = drawCard();
        }
        
        // Nếu là Wild, đặt màu ngẫu nhiên
        if (firstCard != null && firstCard.getType() == CardType.WILD) {
            CardColor[] colors = { CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW };
            CardColor randomColor = colors[(int) (Math.random() * colors.length)];
            firstCard = new Card(randomColor, CardType.WILD, -1);
        }
        
        if (firstCard != null) {
            discardPile.push(firstCard);
        }
    }
    
    /**
     * Lấy số lượng lá bài còn lại trong bộ bài
     * 
     * @return Số lượng lá bài còn lại
     */
    public int getRemainingCards() {
        return cards.size();
    }
}