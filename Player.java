import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand;
    private boolean isLandlord; // 是否为地主
    private int id; // 玩家ID（0-2）
    
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.hand = new ArrayList<>();
        this.isLandlord = false;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Card> getHand() {
        return hand;
    }
    
    public boolean isLandlord() {
        return isLandlord;
    }
    
    public void setLandlord(boolean landlord) {
        isLandlord = landlord;
    }
    
    public int getId() {
        return id;
    }
    
    // 添加牌到手牌
    public void addCard(Card card) {
        hand.add(card);
        Collections.sort(hand);
    }
    
    // 添加多张牌
    public void addCards(List<Card> cards) {
        hand.addAll(cards);
        Collections.sort(hand);
    }
    
    // 出牌
    public void playCards(List<Card> cards) {
        hand.removeAll(cards);
    }
    
    // 手牌数量
    public int getHandCount() {
        return hand.size();
    }
    
    // 手牌是否为空
    public boolean isHandEmpty() {
        return hand.isEmpty();
    }
    
    @Override
    public String toString() {
        return name + (isLandlord ? "(地主)" : "") + " [" + hand.size() + "张]";
    }
}
