import java.util.*;

public class AIPlayer {
    private GameController gameController;
    private Random random;
    
    public AIPlayer(GameController gameController) {
        this.gameController = gameController;
        this.random = new Random();
    }
    
    // AI叫分
    public int makeBid(Player player) {
        List<Card> hand = player.getHand();
        
        // 简单策略：根据手牌强度叫分
        int strength = calculateHandStrength(hand);
        
        if (strength >= 80) {
            return 3; // 好牌叫3分
        } else if (strength >= 60) {
            return 2; // 中等牌叫2分
        } else if (strength >= 40) {
            return 1; // 一般牌叫1分
        } else {
            return 0; // 差牌不叫
        }
    }
    
    // 计算手牌强度（0-100）
    private int calculateHandStrength(List<Card> hand) {
        int strength = 0;
        Map<Integer, Integer> rankCount = new HashMap<>();
        boolean hasSmallJoker = false;
        boolean hasBigJoker = false;
        
        for (Card card : hand) {
            int value = card.getRank().getValue();
            rankCount.put(value, rankCount.getOrDefault(value, 0) + 1);
            
            if (card.getRank() == Card.Rank.SMALL_JOKER) hasSmallJoker = true;
            if (card.getRank() == Card.Rank.BIG_JOKER) hasBigJoker = true;
            
            // 大牌加分
            if (value >= 15) strength += 10; // 2
            else if (value >= 13) strength += 5; // K, A
        }
        
        // 王加分
        if (hasSmallJoker && hasBigJoker) strength += 20; // 火箭
        else if (hasSmallJoker || hasBigJoker) strength += 8;
        
        // 炸弹加分
        for (int count : rankCount.values()) {
            if (count == 4) strength += 15;
        }
        
        return Math.min(100, strength);
    }
    
    // AI出牌
    public List<Card> makePlay(Player player) {
        List<Card> hand = player.getHand();
        
        // 如果是首次出牌或重新获得出牌权
        if (gameController.isFirstRound() || gameController.getLastCardAnalysis() == null) {
            return findBestFirstPlay(hand);
        }
        
        // 需要大过上家的牌
        return findBeatingPlay(hand, gameController.getLastCardAnalysis());
    }
    
    // 找最佳首出牌
    private List<Card> findBestFirstPlay(List<Card> hand) {
        // 优先出顺子、连对、飞机等组合牌
        List<Card> combo = findCombo(hand);
        if (combo != null && !combo.isEmpty()) {
            return combo;
        }
        
        // 否则出最小的单张
        if (!hand.isEmpty()) {
            List<Card> single = new ArrayList<>();
            single.add(hand.get(0));
            return single;
        }
        
        return new ArrayList<>();
    }
    
    // 查找组合牌
    private List<Card> findCombo(List<Card> hand) {
        Map<Integer, List<Card>> rankMap = groupByRank(hand);
        List<Integer> sortedRanks = new ArrayList<>(rankMap.keySet());
        Collections.sort(sortedRanks);
        
        // 查找顺子（至少5张连续）
        List<Card> straight = findStraight(sortedRanks, rankMap, 5);
        if (straight != null) return straight;
        
        // 查找连对（至少3对连续）
        List<Card> consecutivePairs = findConsecutivePairs(sortedRanks, rankMap, 3);
        if (consecutivePairs != null) return consecutivePairs;
        
        // 查找飞机（至少2组三张连续）
        List<Card> plane = findPlane(sortedRanks, rankMap, 2);
        if (plane != null) return plane;
        
        // 查找三带
        for (int rank : sortedRanks) {
            List<Card> cards = rankMap.get(rank);
            if (cards.size() == 3 && rank < 15) { // 不出2的三带
                // 三带一
                Card kicker = findSmallestSingle(hand, rank);
                if (kicker != null) {
                    List<Card> tripleWithSingle = new ArrayList<>(cards.subList(0, 3));
                    tripleWithSingle.add(kicker);
                    return tripleWithSingle;
                }
            }
        }
        
        return null;
    }
    
    // 查找顺子
    private List<Card> findStraight(List<Integer> ranks, Map<Integer, List<Card>> rankMap, int minLength) {
        for (int i = 0; i <= ranks.size() - minLength; i++) {
            List<Integer> sequence = new ArrayList<>();
            int start = ranks.get(i);
            
            if (start >= 15) continue; // 不能有2和王
            
            sequence.add(start);
            for (int j = 1; j < minLength; j++) {
                if (ranks.contains(start + j) && start + j < 15) {
                    sequence.add(start + j);
                } else {
                    break;
                }
            }
            
            if (sequence.size() >= minLength) {
                List<Card> straight = new ArrayList<>();
                for (int rank : sequence) {
                    straight.add(rankMap.get(rank).get(0));
                }
                return straight;
            }
        }
        return null;
    }
    
    // 查找连对
    private List<Card> findConsecutivePairs(List<Integer> ranks, Map<Integer, List<Card>> rankMap, int minLength) {
        for (int i = 0; i <= ranks.size() - minLength; i++) {
            List<Integer> sequence = new ArrayList<>();
            int start = ranks.get(i);
            
            if (start >= 15) continue;
            
            sequence.add(start);
            for (int j = 1; j < minLength; j++) {
                if (ranks.contains(start + j) && start + j < 15 && rankMap.get(start + j).size() >= 2) {
                    sequence.add(start + j);
                } else {
                    break;
                }
            }
            
            if (sequence.size() >= minLength) {
                List<Card> pairs = new ArrayList<>();
                for (int rank : sequence) {
                    pairs.addAll(rankMap.get(rank).subList(0, 2));
                }
                return pairs;
            }
        }
        return null;
    }
    
    // 查找飞机
    private List<Card> findPlane(List<Integer> ranks, Map<Integer, List<Card>> rankMap, int minLength) {
        for (int i = 0; i <= ranks.size() - minLength; i++) {
            List<Integer> sequence = new ArrayList<>();
            int start = ranks.get(i);
            
            if (start >= 15) continue;
            
            sequence.add(start);
            for (int j = 1; j < minLength; j++) {
                if (ranks.contains(start + j) && start + j < 15 && rankMap.get(start + j).size() >= 3) {
                    sequence.add(start + j);
                } else {
                    break;
                }
            }
            
            if (sequence.size() >= minLength) {
                List<Card> plane = new ArrayList<>();
                for (int rank : sequence) {
                    plane.addAll(rankMap.get(rank).subList(0, 3));
                }
                return plane;
            }
        }
        return null;
    }
    
    // 找最小的单牌（排除指定点数）
    private Card findSmallestSingle(List<Card> hand, int... excludeRanks) {
        for (Card card : hand) {
            int rank = card.getRank().getValue();
            boolean excluded = false;
            for (int exclude : excludeRanks) {
                if (rank == exclude) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) return card;
        }
        return null;
    }
    
    // 找能大过上家的牌
    private List<Card> findBeatingPlay(List<Card> hand, CardAnalysis lastAnalysis) {
        if (lastAnalysis == null) return null;
        
        Map<Integer, List<Card>> rankMap = groupByRank(hand);
        List<Integer> sortedRanks = new ArrayList<>(rankMap.keySet());
        Collections.sort(sortedRanks);
        
        CardType type = lastAnalysis.getType();
        int rank = lastAnalysis.getRank();
        int length = lastAnalysis.getLength();
        
        // 根据上家牌型找对应的更大牌
        switch (type) {
            case SINGLE:
                return findBiggerSingle(rankMap, sortedRanks, rank);
            case PAIR:
                return findBiggerPair(rankMap, sortedRanks, rank);
            case TRIPLE:
                return findBiggerTriple(rankMap, sortedRanks, rank);
            case TRIPLE_WITH_SINGLE:
                return findBiggerTripleWithSingle(rankMap, sortedRanks, rank, hand);
            case TRIPLE_WITH_PAIR:
                return findBiggerTripleWithPair(rankMap, sortedRanks, rank, hand);
            case STRAIGHT:
                return findBiggerStraight(rankMap, sortedRanks, rank, length);
            case BOMB:
                return findBiggerBomb(rankMap, sortedRanks, rank);
            case ROCKET:
                return null; // 无法大过火箭
            default:
                break;
        }
        
        // 如果找不到对应牌型，尝试炸弹或火箭
        return findBombOrRocket(hand, rank);
    }
    
    // 找更大的单张
    private List<Card> findBiggerSingle(Map<Integer, List<Card>> rankMap, List<Integer> ranks, int minRank) {
        for (int rank : ranks) {
            if (rank > minRank && rankMap.containsKey(rank)) {
                List<Card> single = new ArrayList<>();
                single.add(rankMap.get(rank).get(0));
                return single;
            }
        }
        return null;
    }
    
    // 找更大的对子
    private List<Card> findBiggerPair(Map<Integer, List<Card>> rankMap, List<Integer> ranks, int minRank) {
        for (int rank : ranks) {
            if (rank > minRank && rankMap.containsKey(rank) && rankMap.get(rank).size() >= 2) {
                return new ArrayList<>(rankMap.get(rank).subList(0, 2));
            }
        }
        return null;
    }
    
    // 找更大的三张
    private List<Card> findBiggerTriple(Map<Integer, List<Card>> rankMap, List<Integer> ranks, int minRank) {
        for (int rank : ranks) {
            if (rank > minRank && rankMap.containsKey(rank) && rankMap.get(rank).size() >= 3) {
                return new ArrayList<>(rankMap.get(rank).subList(0, 3));
            }
        }
        return null;
    }
    
    // 找更大的三带一
    private List<Card> findBiggerTripleWithSingle(Map<Integer, List<Card>> rankMap, List<Integer> ranks, 
                                                    int minRank, List<Card> hand) {
        List<Card> biggerTriple = findBiggerTriple(rankMap, ranks, minRank);
        if (biggerTriple != null) {
            // 找最小的单牌
            int tripleRank = biggerTriple.get(0).getRank().getValue();
            Card kicker = findSmallestSingle(hand, tripleRank);
            if (kicker != null) {
                biggerTriple.add(kicker);
                return biggerTriple;
            }
        }
        return null;
    }
    
    // 找更大的三带二
    private List<Card> findBiggerTripleWithPair(Map<Integer, List<Card>> rankMap, List<Integer> ranks,
                                                  int minRank, List<Card> hand) {
        List<Card> biggerTriple = findBiggerTriple(rankMap, ranks, minRank);
        if (biggerTriple != null) {
            // 找最小的对子
            int tripleRank = biggerTriple.get(0).getRank().getValue();
            for (int rank : ranks) {
                if (rank != tripleRank && rankMap.get(rank).size() >= 2) {
                    biggerTriple.addAll(rankMap.get(rank).subList(0, 2));
                    return biggerTriple;
                }
            }
        }
        return null;
    }
    
    // 找更大的顺子
    private List<Card> findBiggerStraight(Map<Integer, List<Card>> rankMap, List<Integer> ranks, 
                                           int minRank, int length) {
        // 简化处理，暂不实现
        return null;
    }
    
    // 找更大的炸弹
    private List<Card> findBiggerBomb(Map<Integer, List<Card>> rankMap, List<Integer> ranks, int minRank) {
        for (int rank : ranks) {
            if (rank > minRank && rankMap.containsKey(rank) && rankMap.get(rank).size() == 4) {
                return new ArrayList<>(rankMap.get(rank));
            }
        }
        return null;
    }
    
    // 找炸弹或火箭
    private List<Card> findBombOrRocket(List<Card> hand, int minRank) {
        Map<Integer, List<Card>> rankMap = groupByRank(hand);
        
        // 找炸弹
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 4 && entry.getKey() > minRank) {
                return new ArrayList<>(entry.getValue());
            }
        }
        
        // 找火箭
        boolean hasSmallJoker = false;
        boolean hasBigJoker = false;
        Card smallJoker = null;
        Card bigJoker = null;
        
        for (Card card : hand) {
            if (card.getRank() == Card.Rank.SMALL_JOKER) {
                hasSmallJoker = true;
                smallJoker = card;
            }
            if (card.getRank() == Card.Rank.BIG_JOKER) {
                hasBigJoker = true;
                bigJoker = card;
            }
        }
        
        if (hasSmallJoker && hasBigJoker) {
            List<Card> rocket = new ArrayList<>();
            rocket.add(smallJoker);
            rocket.add(bigJoker);
            return rocket;
        }
        
        return null;
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
    
    // AI决定是否过牌
    public boolean shouldPass(Player player) {
        // 如果没有能大过的牌，就过
        List<Card> play = makePlay(player);
        return play == null || play.isEmpty();
    }
}
