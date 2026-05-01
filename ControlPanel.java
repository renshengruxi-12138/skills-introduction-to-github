import javax.swing.*;
import java.awt.*;
import java.util.List;

// 控制面板 - 包含所有操作按钮
public class ControlPanel extends JPanel {
    private GameController gameController;
    private GameFrame gameFrame;
    
    private JButton playButton;
    private JButton passButton;
    private JButton bid1Button;
    private JButton bid2Button;
    private JButton bid3Button;
    private JButton noBidButton;
    private JButton newGameButton;
    
    private JPanel biddingPanel;
    private JPanel playPanel;
    private CardLayout cardLayout;
    
    public ControlPanel(GameController gameController, GameFrame gameFrame) {
        this.gameController = gameController;
        this.gameFrame = gameFrame;
        
        setPreferredSize(new Dimension(1200, 100));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());
        
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        
        // 叫分面板
        biddingPanel = createBiddingPanel();
        add(biddingPanel, "bidding");
        
        // 出牌面板
        playPanel = createPlayPanel();
        add(playPanel, "play");
        
        updateButtons();
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
    
    // 创建叫分面板
    private JPanel createBiddingPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.DARK_GRAY);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        JLabel label = new JLabel("叫地主：");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(label);
        
        bid1Button = new JButton("1分");
        bid1Button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        bid1Button.addActionListener(e -> gameController.playerBid(1, 0));
        panel.add(bid1Button);
        
        bid2Button = new JButton("2分");
        bid2Button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        bid2Button.addActionListener(e -> gameController.playerBid(2, 0));
        panel.add(bid2Button);
        
        bid3Button = new JButton("3分");
        bid3Button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        bid3Button.addActionListener(e -> gameController.playerBid(3, 0));
        panel.add(bid3Button);
        
        noBidButton = new JButton("不叫");
        noBidButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        noBidButton.addActionListener(e -> gameController.playerBid(0, 0));
        panel.add(noBidButton);
        
        return panel;
    }
    
    // 创建出牌面板
    private JPanel createPlayPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.DARK_GRAY);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        
        playButton = new JButton("出牌");
        playButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        playButton.setBackground(new Color(0, 200, 0));
        playButton.setForeground(Color.WHITE);
        playButton.setPreferredSize(new Dimension(120, 45));
        playButton.addActionListener(e -> playCards());
        panel.add(playButton);
        
        passButton = new JButton("过");
        passButton.setFont(new Font("微软雅黑", Font.BOLD, 18));
        passButton.setBackground(new Color(200, 0, 0));
        passButton.setForeground(Color.WHITE);
        passButton.setPreferredSize(new Dimension(120, 45));
        passButton.addActionListener(e -> passCards());
        panel.add(passButton);
        
        newGameButton = new JButton("新游戏");
        newGameButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        newGameButton.addActionListener(e -> gameFrame.restartGame());
        panel.add(newGameButton);
        
        return panel;
    }
    
    // 更新按钮状态
    public void updateButtons() {
        if (gameController == null) return;
        
        // 根据游戏阶段切换面板
        if (gameController.isBiddingPhase()) {
            cardLayout.show(this, "bidding");
            updateBiddingButtons();
        } else {
            cardLayout.show(this, "play");
            updatePlayButtons();
        }
    }
    
    // 更新叫分按钮
    private void updateBiddingButtons() {
        boolean isPlayerTurn = gameController.getCurrentBidderIndex() == 0;
        bid1Button.setEnabled(isPlayerTurn);
        bid2Button.setEnabled(isPlayerTurn && gameController.getCurrentBid() < 1);
        bid3Button.setEnabled(isPlayerTurn && gameController.getCurrentBid() < 2);
        noBidButton.setEnabled(isPlayerTurn);
    }
    
    // 更新出牌按钮
    private void updatePlayButtons() {
        boolean isPlayerTurn = gameController.getCurrentPlayerIndex() == 0;
        boolean canPass = !gameController.isFirstRound();
        
        playButton.setEnabled(isPlayerTurn);
        passButton.setEnabled(isPlayerTurn && canPass);
    }
    
    // 出牌
    private void playCards() {
        if (gameController.getCurrentPlayerIndex() != 0) return;
        
        GamePanel gamePanel = gameFrame.getGamePanel();
        List<Card> selectedCards = gamePanel.getSelectedCards();
        
        if (selectedCards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要出的牌！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean success = gameController.playCards(0, selectedCards);
        
        if (success) {
            gamePanel.clearSelectedCards();
        } else {
            JOptionPane.showMessageDialog(this, "无效的出牌或无法大过上家的牌！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 过牌
    private void passCards() {
        if (gameController.getCurrentPlayerIndex() != 0) return;
        if (gameController.isFirstRound()) return;
        
        gameController.playCards(0, new java.util.ArrayList<>());
        gameFrame.getGamePanel().clearSelectedCards();
    }
}
