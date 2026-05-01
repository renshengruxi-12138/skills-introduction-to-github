import java.util.*;

public class GameController {
    private List<Card> deck;
    private Player[] players;
    private List<Card> bottomCards; // 底牌
    private int currentPlayerIndex; // 当前出牌玩家
    private int landlordIndex; // 地主索引
    private List<Card> lastPlayedCards; // 上次出的牌
    private CardAnalysis lastCardAnalysis; // 上次牌的牌型分析
    private int lastPlayerIndex; // 最后出牌的玩家索引
    private int passCount; // 连续过牌次数
    private boolean gameStarted;
    private int currentBid; // 当前叫分
    private int currentBidderIndex; // 当前叫分玩家
    private boolean biddingPhase; // 是否在叫分阶段
    private boolean firstRound; // 是否是该轮第一个出牌
    
    private GameListener listener;
    
    public interface GameListener {
        void onGameStateChange();
        void onGameEnd(Player winner);
        void showMessage(String message);
    }
    
    public GameController(GameListener listener) {
        this.listener = listener;
        this.players = new Player[3];
        this.players[0] = new Player(0, "玩家");
        this.players[1] = new Player(1, "电脑1");
        this.players[2] = new Player(2, "电脑2");
        this.bottomCards = new ArrayList<>();
        this.gameStarted = false;
        this.biddingPhase = false;
        this.firstRound = true;
    }
    
    // 初始化游戏
    public void startGame() {
        // 创建牌组
        createDeck();
        // 洗牌
        shuffleDeck();
        // 发牌
        dealCards();
        // 开始叫地主
        startBidding();
    }
    
    // 创建牌组
    private void createDeck() {
        deck = new ArrayList<>();
        
        // 创建52张普通牌
        for (Card.Suit suit : Card.Suit.values()) {
            if (suit == Card.Suit.JOKER) continue;
            for (Card.Rank rank : Card.Rank.values()) {
                if (rank.getValue() <= 15) { // 不包括大小王
                    deck.add(new Card(suit, rank));
                }
            }
        }
        
        // 添加大小王
        deck.add(new Card(Card.Suit.JOKER, Card.Rank.SMALL_JOKER));
        deck.add(new Card(Card.Suit.JOKER, Card.Rank.BIG_JOKER));
    }
    
    // 洗牌
    private void shuffleDeck() {
        Collections.shuffle(deck);
    }
    
    // 发牌
    private void dealCards() {
        // 清空玩家手牌
        for (Player player : players) {
            player.getHand().clear();
        }
        bottomCards.clear();
        
        // 每人发17张
        for (int i = 0; i < 17; i++) {
            for (Player player : players) {
                player.addCard(deck.get(i * 3 + player.getId()));
            }
        }
        
        // 剩余3张作为底牌
        for (int i = 51; i < 54; i++) {
            bottomCards.add(deck.get(i));
        }
        
        // 排序手牌
        for (Player player : players) {
            Collections.sort(player.getHand());
        }
    }
    
    // 开始叫地主
    private void startBidding() {
        biddingPhase = true;
        currentBid = 0;
        currentBidderIndex = new Random().nextInt(3); // 随机起始叫分玩家
        gameStarted = true;
        
        if (listener != null) {
            listener.showMessage("开始叫地主！");
            listener.onGameStateChange();
        }
    }
    
    // 玩家叫分
    public void playerBid(int bidScore, int playerIndex) {
        if (!biddingPhase) return;
        
        if (bidScore > currentBid) {
            currentBid = bidScore;
            landlordIndex = playerIndex;
        }
        
        currentBidderIndex = (currentBidderIndex + 1) % 3;
        
        // 如果都叫完了
        if (currentBidderIndex == (landlordIndex + 1) % 3 || currentBid == 3) {
            finishBidding();
        } else {
            if (listener != null) {
                listener.onGameStateChange();
            }
        }
    }
    
    // 结束叫地主
    private void finishBidding() {
        biddingPhase = false;
        
        if (currentBid == 0) {
            // 无人叫分，重新发牌
            if (listener != null) {
                listener.showMessage("无人叫分，重新发牌！");
            }
            startGame();
            return;
        }
        
        // 设置地主
        players[landlordIndex].setLandlord(true);
        
        // 给地主底牌
        players[landlordIndex].addCards(bottomCards);
        Collections.sort(players[landlordIndex].getHand());
        
        // 地主先出牌
        currentPlayerIndex = landlordIndex;
        lastPlayerIndex = landlordIndex;
        passCount = 0;
        firstRound = true;
        
        if (listener != null) {
            listener.showMessage(players[landlordIndex].getName() + " 成为地主！");
            listener.onGameStateChange();
        }
    }
    
    // 玩家出牌
    public boolean playCards(int playerIndex, List<Card> cards) {
        if (!gameStarted || biddingPhase) return false;
        if (playerIndex != currentPlayerIndex) return false;
        
        // 首次出牌或重新获得出牌权
        if (firstRound) {
            CardAnalysis analysis = CardAnalyzer.analyzeCards(cards);
            if (analysis.getType() == CardType.INVALID) {
                return false;
            }
            executePlay(playerIndex, cards, analysis);
            return true;
        }
        
        // 过牌
        if (cards.isEmpty()) {
            pass(playerIndex);
            return true;
        }
        
        // 验证牌型
        CardAnalysis analysis = CardAnalyzer.analyzeCards(cards);
        if (analysis.getType() == CardType.INVALID) {
            return false;
        }
        
        // 判断是否能大过
        if (!analysis.beats(lastCardAnalysis)) {
            return false;
        }
        
        executePlay(playerIndex, cards, analysis);
        return true;
    }
    
    // 执行出牌
    private void executePlay(int playerIndex, List<Card> cards, CardAnalysis analysis) {
        Player player = players[playerIndex];
        player.playCards(cards);
        
        lastPlayedCards = new ArrayList<>(cards);
        lastCardAnalysis = analysis;
        lastPlayerIndex = playerIndex;
        passCount = 0;
        firstRound = false;
        
        // 检查是否获胜
        if (player.isHandEmpty()) {
            endGame(player);
            return;
        }
        
        // 下一个玩家
        currentPlayerIndex = (currentPlayerIndex + 1) % 3;
        
        if (listener != null) {
            listener.onGameStateChange();
        }
    }
    
    // 过牌
    private void pass(int playerIndex) {
        passCount++;
        
        if (passCount >= 2) {
            // 连续两家过，最后出牌者重新出牌
            currentPlayerIndex = lastPlayerIndex;
            firstRound = true;
            passCount = 0;
            lastPlayedCards = null;
            lastCardAnalysis = null;
            
            if (listener != null) {
                String playerName = players[currentPlayerIndex].getName();
                String role = players[currentPlayerIndex].isLandlord() ? "(地主)" : "";
                listener.showMessage("轮到 " + playerName + role + " 出牌");
            }
        } else {
            // 下一个玩家
            currentPlayerIndex = (currentPlayerIndex + 1) % 3;
        }
        
        if (listener != null) {
            listener.onGameStateChange();
        }
    }
    
    // 结束游戏
    private void endGame(Player winner) {
        gameStarted = false;
        
        if (listener != null) {
            String winnerType = winner.isLandlord() ? "地主" : "农民";
            listener.showMessage(winner.getName() + "(" + winnerType + ") 获胜！");
            listener.onGameEnd(winner);
        }
    }
    
    // 获取游戏状态
    public Player[] getPlayers() {
        return players;
    }
    
    public List<Card> getBottomCards() {
        return bottomCards;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public int getLandlordIndex() {
        return landlordIndex;
    }
    
    public List<Card> getLastPlayedCards() {
        return lastPlayedCards;
    }
    
    public CardAnalysis getLastCardAnalysis() {
        return lastCardAnalysis;
    }
    
    public int getLastPlayerIndex() {
        return lastPlayerIndex;
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public boolean isBiddingPhase() {
        return biddingPhase;
    }
    
    public int getCurrentBid() {
        return currentBid;
    }
    
    public int getCurrentBidderIndex() {
        return currentBidderIndex;
    }
    
    public boolean isFirstRound() {
        return firstRound;
    }
    
    // 获取可以出的牌
    public List<List<Card>> getValidPlays(List<Card> hand) {
        List<List<Card>> validPlays = new ArrayList<>();
        
        if (firstRound || lastCardAnalysis == null) {
            // 首次出牌，可以出任意合法牌型
            // 这里简化处理，只返回部分常见牌型
            generateValidPlays(hand, validPlays);
        } else {
            // 需要大过上家的牌
            generateBeatingPlays(hand, validPlays);
        }
        
        return validPlays;
    }
    
    // 生成合法出牌（简化版）
    private void generateValidPlays(List<Card> hand, List<List<Card>> validPlays) {
        // 单张
        for (Card card : hand) {
            List<Card> play = new ArrayList<>();
            play.add(card);
            validPlays.add(play);
        }
        
        // 对子
        Map<Integer, List<Card>> rankMap = groupByRank(hand);
        for (List<Card> cards : rankMap.values()) {
            if (cards.size() >= 2) {
                List<Card> pair = new ArrayList<>(cards.subList(0, 2));
                validPlays.add(pair);
            }
        }
        
        // 炸弹
        for (List<Card> cards : rankMap.values()) {
            if (cards.size() == 4) {
                validPlays.add(new ArrayList<>(cards));
            }
        }
    }
    
    // 生成能大过的牌（简化版）
    private void generateBeatingPlays(List<Card> hand, List<List<Card>> validPlays) {
        if (lastCardAnalysis == null) return;
        
        CardType type = lastCardAnalysis.getType();
        int rank = lastCardAnalysis.getRank();
        int length = lastCardAnalysis.getLength();
        
        Map<Integer, List<Card>> rankMap = groupByRank(hand);
        
        // 根据上家牌型生成对应牌型
        switch (type) {
            case SINGLE:
                // 出更大的单张
                for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
                    if (entry.getKey() > rank) {
                        validPlays.add(new ArrayList<>(entry.getValue().subList(0, 1)));
                    }
                }
                break;
                
            case PAIR:
                // 出更大的对子
                for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
                    if (entry.getKey() > rank && entry.getValue().size() >= 2) {
                        validPlays.add(new ArrayList<>(entry.getValue().subList(0, 2)));
                    }
                }
                break;
        }
        
        // 总是可以出炸弹或火箭
        addBombsAndRocket(hand, validPlays, rank);
    }
    
    // 添加炸弹和火箭
    private void addBombsAndRocket(List<Card> hand, List<List<Card>> validPlays, int minRank) {
        Map<Integer, List<Card>> rankMap = groupByRank(hand);
        
        // 炸弹
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 4 && entry.getKey() > minRank) {
                validPlays.add(new ArrayList<>(entry.getValue()));
            }
        }
        
        // 火箭
        boolean hasSmallJoker = false;
        boolean hasBigJoker = false;
        for (Card card : hand) {
            if (card.getRank() == Card.Rank.SMALL_JOKER) hasSmallJoker = true;
            if (card.getRank() == Card.Rank.BIG_JOKER) hasBigJoker = true;
        }
        if (hasSmallJoker && hasBigJoker) {
            List<Card> rocket = new ArrayList<>();
            rocket.add(new Card(Card.Suit.JOKER, Card.Rank.SMALL_JOKER));
            rocket.add(new Card(Card.Suit.JOKER, Card.Rank.BIG_JOKER));
            validPlays.add(rocket);
        }
    }
    
    // 按点数分组
    private Map<Integer, List<Card>> groupByRank(List<Card> hand) {
        Map<Integer, List<Card>> map = new LinkedHashMap<>();
        for (Card card : hand) {
            int rank = card.getRank().getValue();
            map.computeIfAbsent(rank, k -> new ArrayList<>()).add(card);
        }
        return map;
    }
}
