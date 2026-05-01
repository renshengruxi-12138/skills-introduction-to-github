import java.util.*;
import java.util.stream.Collectors;

public class CardAnalyzer {
    
    // 分析一手牌的牌型
    public static CardAnalysis analyzeCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new CardAnalysis(CardType.INVALID);
        }
        
        // 按点数排序
        List<Card> sorted = new ArrayList<>(cards);
        Collections.sort(sorted);
        
        // 获取点数列表
        List<Integer> ranks = sorted.stream()
                .map(c -> c.getRank().getValue())
                .collect(Collectors.toList());
        
        // 统计每个点数的出现次数
        Map<Integer, Integer> countMap = new LinkedHashMap<>();
        for (int rank : ranks) {
            countMap.put(rank, countMap.getOrDefault(rank, 0) + 1);
        }
        
        int size = cards.size();
        
        // 火箭
        if (size == 2 && ranks.contains(16) && ranks.contains(17)) {
            return new CardAnalysis(CardType.ROCKET);
        }
        
        // 单张
        if (size == 1) {
            return new CardAnalysis(CardType.SINGLE, ranks.get(0));
        }
        
        // 对子
        if (size == 2 && countMap.size() == 1) {
            return new CardAnalysis(CardType.PAIR, ranks.get(0));
        }
        
        // 三张
        if (size == 3 && countMap.size() == 1) {
            return new CardAnalysis(CardType.TRIPLE, ranks.get(0));
        }
        
        // 炸弹
        if (size == 4 && countMap.size() == 1) {
            return new CardAnalysis(CardType.BOMB, ranks.get(0));
        }
        
        // 三带一
        if (size == 4 && countMap.size() == 2) {
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() == 3) {
                    return new CardAnalysis(CardType.TRIPLE_WITH_SINGLE, entry.getKey());
                }
            }
        }
        
        // 三带二
        if (size == 5 && countMap.size() == 2) {
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() == 3) {
                    return new CardAnalysis(CardType.TRIPLE_WITH_PAIR, entry.getKey());
                }
            }
        }
        
        // 四带两单
        if (size == 6 && countMap.size() == 3) {
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() == 4) {
                    return new CardAnalysis(CardType.FOUR_WITH_TWO_SINGLES, entry.getKey());
                }
            }
        }
        
        // 四带两对
        if (size == 8 && countMap.size() == 3) {
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() == 4) {
                    // 检查其他两个是否都是对子
                    boolean twoPairs = true;
                    for (Map.Entry<Integer, Integer> e : countMap.entrySet()) {
                        if (!e.getKey().equals(entry.getKey()) && e.getValue() != 2) {
                            twoPairs = false;
                            break;
                        }
                    }
                    if (twoPairs) {
                        return new CardAnalysis(CardType.FOUR_WITH_TWO_PAIRS, entry.getKey());
                    }
                }
            }
        }
        
        // 顺子：至少5张，全部单张，连续，无2和王
        if (size >= 5 && countMap.size() == size) {
            if (isStraight(countMap)) {
                int maxRank = Collections.max(countMap.keySet());
                return new CardAnalysis(CardType.STRAIGHT, maxRank, size);
            }
        }
        
        // 连对：至少3对，每对点数相同，连续，无2和王
        if (size >= 6 && size % 2 == 0 && countMap.size() == size / 2) {
            boolean allPairs = countMap.values().stream().allMatch(count -> count == 2);
            if (allPairs && isStraight(countMap)) {
                int maxRank = Collections.max(countMap.keySet());
                return new CardAnalysis(CardType.CONSECUTIVE_PAIRS, maxRank, size / 2);
            }
        }
        
        // 飞机相关（需要三张连续）
        // 找出所有出现3次的点数
        List<Integer> tripleRanks = new ArrayList<>();
        List<Integer> otherRanks = new ArrayList<>();
        Map<Integer, Integer> otherRanksCount = new HashMap<>();
        
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() == 3) {
                tripleRanks.add(entry.getKey());
            } else {
                otherRanks.add(entry.getKey());
                otherRanksCount.put(entry.getKey(), entry.getValue());
            }
        }
        Collections.sort(tripleRanks);
        
        // 纯飞机
        if (tripleRanks.size() >= 2 && otherRanks.isEmpty()) {
            if (isConsecutive(tripleRanks)) {
                int maxRank = Collections.max(tripleRanks);
                return new CardAnalysis(CardType.PLANE, maxRank, tripleRanks.size());
            }
        }
        
        // 飞机带单牌
        if (tripleRanks.size() >= 2 && otherRanks.size() == tripleRanks.size()) {
            if (isConsecutive(tripleRanks)) {
                // 检查其他牌是否都是单张
                boolean allSingles = otherRanksCount.values().stream().allMatch(count -> count == 1);
                if (allSingles) {
                    int maxRank = Collections.max(tripleRanks);
                    return new CardAnalysis(CardType.PLANE_WITH_SINGLES, maxRank, tripleRanks.size());
                }
            }
        }
        
        // 飞机带对子
        if (tripleRanks.size() >= 2 && otherRanks.size() == tripleRanks.size()) {
            if (isConsecutive(tripleRanks)) {
                // 检查其他牌是否都是对子
                boolean allPairs = otherRanksCount.values().stream().allMatch(count -> count == 2);
                if (allPairs) {
                    int maxRank = Collections.max(tripleRanks);
                    return new CardAnalysis(CardType.PLANE_WITH_PAIRS, maxRank, tripleRanks.size());
                }
            }
        }
        
        return new CardAnalysis(CardType.INVALID);
    }
    
    // 检查是否是顺子（连续且最大值<=14，即不包含2和王）
    private static boolean isStraight(Map<Integer, Integer> countMap) {
        List<Integer> ranks = new ArrayList<>(countMap.keySet());
        Collections.sort(ranks);
        
        // 不能包含2(15)和王(16,17)
        if (ranks.stream().anyMatch(r -> r >= 15)) {
            return false;
        }
        
        return isConsecutive(ranks);
    }
    
    // 检查是否连续
    private static boolean isConsecutive(List<Integer> ranks) {
        for (int i = 1; i < ranks.size(); i++) {
            if (ranks.get(i) - ranks.get(i - 1) != 1) {
                return false;
            }
        }
        return true;
    }
    
    // 判断一组牌是否能大过上家的牌
    public static boolean canBeat(List<Card> newCards, CardAnalysis lastAnalysis) {
        CardAnalysis newAnalysis = analyzeCards(newCards);
        if (newAnalysis.getType() == CardType.INVALID) {
            return false;
        }
        return newAnalysis.beats(lastAnalysis);
    }
}
