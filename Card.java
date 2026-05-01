public class Card implements Comparable<Card> {
    // 花色枚举
    public enum Suit {
        SPADE("♠"),     // 黑桃
        HEART("♥"),    // 红桃
        CLUB("♣"),     // 梅花
        DIAMOND("♦"),  // 方块
        JOKER("王");   // 王
        
        private final String symbol;
        
        Suit(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
    }
    
    // 点数枚举
    public enum Rank {
        THREE("3", 3),
        FOUR("4", 4),
        FIVE("5", 5),
        SIX("6", 6),
        SEVEN("7", 7),
        EIGHT("8", 8),
        NINE("9", 9),
        TEN("10", 10),
        JACK("J", 11),
        QUEEN("Q", 12),
        KING("K", 13),
        ACE("A", 14),
        TWO("2", 15),
        SMALL_JOKER("小王", 16),
        BIG_JOKER("大王", 17);
        
        private final String name;
        private final int value;
        
        Rank(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private Suit suit;
    private Rank rank;
    private boolean selected = false; // 用于界面选牌
    
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    public Suit getSuit() {
        return suit;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public int compareTo(Card other) {
        int valueCompare = Integer.compare(this.rank.getValue(), other.rank.getValue());
        if (valueCompare != 0) {
            return valueCompare;
        }
        return Integer.compare(this.suit.ordinal(), other.suit.ordinal());
    }
    
    @Override
    public String toString() {
        if (rank == Rank.SMALL_JOKER || rank == Rank.BIG_JOKER) {
            return rank.getName();
        }
        return suit.getSymbol() + rank.getName();
    }
    
    // 用于显示在界面上的简短文本
    public String getDisplayText() {
        return toString();
    }
}
