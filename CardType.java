import java.util.List;

// 牌型类型枚举
public enum CardType {
    INVALID,          // 无效牌型
    SINGLE,           // 单张
    PAIR,             // 对子
    TRIPLE,           // 三张
    TRIPLE_WITH_SINGLE,  // 三带一
    TRIPLE_WITH_PAIR,    // 三带二
    STRAIGHT,         // 顺子
    CONSECUTIVE_PAIRS,   // 连对
    PLANE,            // 飞机（纯三张）
    PLANE_WITH_SINGLES,  // 飞机带单牌
    PLANE_WITH_PAIRS,    // 飞机带对子
    BOMB,             // 炸弹
    ROCKET,           // 火箭
    FOUR_WITH_TWO_SINGLES,  // 四带两单
    FOUR_WITH_TWO_PAIRS     // 四带两对
}

// 牌型分析结果类
class CardAnalysis {
    private CardType type;
    private int rank;        // 主牌点数（用于比较）
    private int length;      // 长度（顺子、连对、飞机的组数）
    
    public CardAnalysis(CardType type, int rank, int length) {
        this.type = type;
        this.rank = rank;
        this.length = length;
    }
    
    public CardAnalysis(CardType type, int rank) {
        this(type, rank, 0);
    }
    
    public CardAnalysis(CardType type) {
        this(type, 0, 0);
    }
    
    public CardType getType() {
        return type;
    }
    
    public int getRank() {
        return rank;
    }
    
    public int getLength() {
        return length;
    }
    
    // 判断是否能大过上家的牌
    public boolean beats(CardAnalysis other) {
        if (other == null) return true; // 首次出牌
        
        // 火箭最大
        if (this.type == CardType.ROCKET) return true;
        if (other.type == CardType.ROCKET) return false;
        
        // 炸弹次之
        if (this.type == CardType.BOMB && other.type != CardType.BOMB) return true;
        if (this.type != CardType.BOMB && other.type == CardType.BOMB) return false;
        
        // 同类型比较
        if (this.type != other.type) return false;
        
        // 炸弹比较点数
        if (this.type == CardType.BOMB) {
            return this.rank > other.rank;
        }
        
        // 顺子、连对、飞机需要长度相同
        if (this.type == CardType.STRAIGHT || 
            this.type == CardType.CONSECUTIVE_PAIRS || 
            this.type == CardType.PLANE ||
            this.type == CardType.PLANE_WITH_SINGLES ||
            this.type == CardType.PLANE_WITH_PAIRS) {
            return this.length == other.length && this.rank > other.rank;
        }
        
        // 其他牌型直接比较点数
        return this.rank > other.rank;
    }
}
