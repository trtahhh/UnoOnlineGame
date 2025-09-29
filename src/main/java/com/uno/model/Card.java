package com.uno.model;

import java.io.Serializable;

/**
 * Class đại diện cho một lá bài Uno
 */
public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final CardColor color;
    private final CardType type;
    private final int value;
    
    public Card(CardColor color, CardType type, int value) {
        this.color = color;
        this.type = type;
        this.value = value;
    }
    
    public CardColor getColor() {
        return color;
    }
    
    public CardType getType() {
        return type;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        if (type == CardType.NUMBER) {
            return color.getDisplayName() + " " + value;
        } else {
            return color.getDisplayName() + " " + type.getDisplayName();
        }
    }
    
    /**
     * Kiểm tra xem lá bài có thể đánh lên lá bài top hay không
     * 
     * @param topCard Lá bài trên cùng của chồng bài
     * @return true nếu có thể đánh, ngược lại false
     */
    public boolean canPlayOn(Card topCard) {
        // Lá đặc biệt được đánh bất cứ lúc nào
        if (this.type == CardType.WILD || this.type == CardType.WILD_DRAW_FOUR) {
            return true;
        }

        // Nếu lá bài trên cùng là lá đặc biệt, phải khớp với màu đã được chọn
        if (topCard.getType() == CardType.WILD || topCard.getType() == CardType.WILD_DRAW_FOUR) {
            return this.color == topCard.getColor();
        }
        
        // Nếu không phải lá đặc biệt, phải khớp màu hoặc khớp số (hoặc cả hai)
        return this.color == topCard.getColor() || 
               (this.type == topCard.getType() && this.type != CardType.NUMBER) ||
               (this.type == CardType.NUMBER && topCard.getType() == CardType.NUMBER && this.value == topCard.getValue());
    }
}