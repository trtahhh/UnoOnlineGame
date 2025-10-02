package com.uno.model;

import java.io.Serializable;

/**
 * Represents a Uno card with color, type and value
 */
public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final CardColor color;
    private final CardType type;
    private final int value;
    
    /**
     * Creates a new Card with the specified color, type and value
     * 
     * @param color Color of the card
     * @param type Type of the card
     * @param value Value of the card (for number cards)
     */
    public Card(CardColor color, CardType type, int value) {
        this.color = color;
        this.type = type;
        this.value = value;
    }
    
    /**
     * Gets the color of this card
     * 
     * @return The card's color
     */
    public CardColor getColor() {
        return color;
    }
    
    /**
     * Gets the type of this card
     * 
     * @return The card's type
     */
    public CardType getType() {
        return type;
    }
    
    /**
     * Gets the value of this card
     * 
     * @return The card's value (for number cards)
     */
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
     * Determines if this card can be played on top of another card
     * according to Uno rules.
     * 
     * @param topCard The card currently on top of the play pile
     * @return true if this card can be played, false otherwise
     */
    public boolean canPlayOn(Card topCard) {
        // Wild cards can be played anytime
        if (this.type == CardType.WILD || this.type == CardType.WILD_DRAW_FOUR) {
            return true;
        }
        // If top card is wild, must match the selected color
        if (topCard.getType() == CardType.WILD || topCard.getType() == CardType.WILD_DRAW_FOUR) {
            return this.color == topCard.getColor();
        }
        // Otherwise, must match color or match type (or both)
        return this.color == topCard.getColor() || 
               (this.type == topCard.getType() && this.type != CardType.NUMBER) ||
               (this.type == CardType.NUMBER && topCard.getType() == CardType.NUMBER && this.value == topCard.getValue());
    }
    
    /**
     * Gets a string description of card attributes for debugging
     * 
     * @return Detailed string with all card properties
     */
    public String getDebugDetails() {
        return String.format("Card{color=%s, type=%s, value=%d}", 
                color.name(), type.name(), value);
    }
}