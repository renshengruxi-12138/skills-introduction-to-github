import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

// 游戏面板 - 显示所有卡牌和游戏信息
public class GamePanel extends JPanel {
    private GameController gameController;
    private Card[][] selectedCards; // 存储选中的牌
    
    public GamePanel(GameController gameController) {
        this.gameController = gameController;
        setBackground(new Color(34, 139, 34)); // 深绿色背景
        
        // 添加鼠标点击事件用于选牌
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCardClick(e.getX(), e.getY());
            }
        });
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gameController == null || !gameController.isGameStarted()) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制玩家信息
        drawPlayerInfo(g2d);
        
        // 绘制底牌（如果是地主）
        drawBottomCards(g2d);
        
        // 绘制其他玩家的手牌（背面）
        drawOpponentHands(g2d);
        
        // 绘制上次出的牌
        drawLastPlayedCards(g2d);
        
        // 绘制玩家手牌
        drawPlayerHand(g2d);
    }
    
    // 绘制玩家信息
    private void drawPlayerInfo(Graphics2D g2d) {
        Player[] players = gameController.getPlayers();
        int landlordIndex = gameController.getLandlordIndex();
        
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        
        // 玩家0（人类玩家）
        String player0Info = players[0].getName() + " - " + players[0].getHandCount() + "张";
        if (landlordIndex == 0) player0Info += " [地主]";
        g2d.drawString(player0Info, 50, 30);
        
        // 玩家1
        String player1Info = players[1].getName() + " - " + players[1].getHandCount() + "张";
        if (landlordIndex == 1) player1Info += " [地主]";
        g2d.drawString(player1Info, 500, 30);
        
        // 玩家2
        String player2Info = players[2].getName() + " - " + players[2].getHandCount() + "张";
        if (landlordIndex == 2) player2Info += " [地主]";
        g2d.drawString(player2Info, 950, 30);
        
        // 标记当前出牌玩家
        int currentPlayer = gameController.getCurrentPlayerIndex();
        g2d.setColor(Color.YELLOW);
        String currentText = "◄ 出牌中";
        if (currentPlayer == 0) {
            g2d.drawString(currentText, 250, 30);
        } else if (currentPlayer == 1) {
            g2d.drawString(currentText, 650, 30);
        } else {
            g2d.drawString(currentText, 1100, 30);
        }
    }
    
    // 绘制底牌
    private void drawBottomCards(Graphics2D g2d) {
        List<Card> bottomCards = gameController.getBottomCards();
        if (bottomCards.isEmpty()) return;
        
        // 只有在地主确定后才显示底牌
        if (gameController.isBiddingPhase()) {
            // 叫分阶段，显示背面
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
            g2d.setColor(Color.WHITE);
            g2d.drawString("底牌：(隐藏)", 550, 60);
            
            // 显示牌的背面
            int x = 620;
            for (int i = 0; i < 3; i++) {
                g2d.setColor(new Color(139, 69, 19)); // 棕色背面
                g2d.fillRoundRect(x, 45, 40, 55, 5, 5);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x, 45, 40, 55, 5, 5);
                x += 30;
            }
        } else if (gameController.getLandlordIndex() >= 0) {
            // 地主已确定，显示底牌正面
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 14));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("底牌：", 550, 60);
            
            int x = 620;
            for (Card card : bottomCards) {
                drawCard(g2d, card, x, 45, 40, 55, false);
                x += 30;
            }
        }
    }
    
    // 绘制对手手牌（背面）
    private void drawOpponentHands(Graphics2D g2d) {
        Player[] players = gameController.getPlayers();
        
        // 玩家1的手牌（左侧）
        int player1Cards = players[1].getHandCount();
        g2d.setColor(new Color(139, 69, 19)); // 棕色背面
        for (int i = 0; i < Math.min(player1Cards, 10); i++) {
            int y = 120 + i * 15;
            g2d.fillRoundRect(20, y, 50, 70, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(20, y, 50, 70, 5, 5);
            g2d.setColor(new Color(139, 69, 19));
        }
        
        // 玩家2的手牌（右侧）
        int player2Cards = players[2].getHandCount();
        for (int i = 0; i < Math.min(player2Cards, 10); i++) {
            int y = 120 + i * 15;
            g2d.fillRoundRect(1130, y, 50, 70, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(1130, y, 50, 70, 5, 5);
            g2d.setColor(new Color(139, 69, 19));
        }
    }
    
    // 绘制上次出的牌
    private void drawLastPlayedCards(Graphics2D g2d) {
        List<Card> lastCards = gameController.getLastPlayedCards();
        if (lastCards == null || lastCards.isEmpty()) return;
        
        int lastPlayer = gameController.getLastPlayerIndex();
        int startX, startY;
        
        // 根据不同玩家位置显示
        if (lastPlayer == 0) {
            startX = 400;
            startY = 450;
        } else if (lastPlayer == 1) {
            startX = 200;
            startY = 200;
        } else {
            startX = 800;
            startY = 200;
        }
        
        int x = startX;
        for (Card card : lastCards) {
            drawCard(g2d, card, x, startY, 60, 80, false);
            x += 45;
        }
    }
    
    // 绘制玩家手牌
    private void drawPlayerHand(Graphics2D g2d) {
        Player player = gameController.getPlayers()[0];
        List<Card> hand = player.getHand();
        
        int cardWidth = 70;
        int cardHeight = 95;
        int overlap = 35; // 牌重叠间距
        
        int totalWidth = hand.size() * overlap + cardWidth;
        int startX = (getWidth() - totalWidth) / 2;
        int y = 600;
        
        int x = startX;
        for (Card card : hand) {
            drawCard(g2d, card, x, y, cardWidth, cardHeight, card.isSelected());
            x += overlap;
        }
    }
    
    // 绘制单张牌
    private void drawCard(Graphics2D g2d, Card card, int x, int y, int width, int height, boolean selected) {
        // 牌背景
        g2d.setColor(selected ? Color.YELLOW : Color.WHITE);
        g2d.fillRoundRect(x, y, width, height, 8, 8);
        
        // 牌边框
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 8, 8);
        
        // 根据花色设置颜色
        if (card.getSuit() == Card.Suit.HEART || card.getSuit() == Card.Suit.DIAMOND) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.BLACK);
        }
        
        // 左上角显示花色和点数
        String rankName = card.getRank().getName();
        String suitSymbol = card.getSuit().getSymbol();
        
        // 左上角 - 点数
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(rankName, x + 4, y + 18);
        
        // 左上角 - 花色（在点数下方）
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        g2d.drawString(suitSymbol, x + 4, y + 32);
        
        // 右下角 - 倒过来的点数
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 16));
        int rankWidth = fm.stringWidth(rankName);
        g2d.drawString(rankName, x + width - 4 - rankWidth, y + height - 8);
        
        // 中间显示大号点数和花色
        String text = card.getDisplayText();
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, x + (width - textWidth) / 2, y + height / 2 + textHeight / 3);
    }
    
    // 处理卡牌点击
    private void handleCardClick(int x, int y) {
        if (gameController == null || !gameController.isGameStarted()) return;
        if (gameController.getCurrentPlayerIndex() != 0) return;
        
        Player player = gameController.getPlayers()[0];
        List<Card> hand = player.getHand();
        
        int cardWidth = 70;
        int cardHeight = 95;
        int overlap = 35;
        int totalWidth = hand.size() * overlap + cardWidth;
        int startX = (getWidth() - totalWidth) / 2;
        int startY = 600;
        
        // 检查点击的是哪张牌
        for (int i = 0; i < hand.size(); i++) {
            int cardX = startX + i * overlap;
            if (x >= cardX && x <= cardX + cardWidth && y >= startY && y <= startY + cardHeight) {
                // 切换选中状态
                hand.get(i).setSelected(!hand.get(i).isSelected());
                repaint();
                break;
            }
        }
    }
    
    // 获取选中的牌
    public List<Card> getSelectedCards() {
        if (gameController == null) return null;
        
        Player player = gameController.getPlayers()[0];
        List<Card> selected = new java.util.ArrayList<>();
        for (Card card : player.getHand()) {
            if (card.isSelected()) {
                selected.add(card);
            }
        }
        return selected;
    }
    
    // 清除选中的牌
    public void clearSelectedCards() {
        if (gameController == null) return;
        
        Player player = gameController.getPlayers()[0];
        for (Card card : player.getHand()) {
            card.setSelected(false);
        }
        repaint();
    }
}
