import javax.swing.*;
import java.awt.*;
import java.util.List;

// 游戏主窗口
public class GameFrame extends JFrame implements GameController.GameListener {
    private GameController gameController;
    private AIPlayer aiPlayer;
    private GamePanel gamePanel;
    private ControlPanel controlPanel;
    
    public GameFrame() {
        setTitle("斗地主游戏");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 初始化游戏控制器
        gameController = new GameController(this);
        aiPlayer = new AIPlayer(gameController);
        
        // 创建面板
        gamePanel = new GamePanel(gameController);
        controlPanel = new ControlPanel(gameController, this);
        
        // 布局
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // 开始游戏
        gameController.startGame();
    }
    
    @Override
    public void onGameStateChange() {
        gamePanel.repaint();
        controlPanel.updateButtons();
        
        // 如果是AI的回合，自动出牌
        if (gameController.isGameStarted() && !gameController.isBiddingPhase()) {
            int currentPlayer = gameController.getCurrentPlayerIndex();
            if (currentPlayer != 0) { // 不是人类玩家
                Timer timer = new Timer(1000, e -> {
                    aiTurn(currentPlayer);
                    ((Timer)e.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
        
        // 叫分阶段AI自动叫分
        if (gameController.isBiddingPhase()) {
            int currentBidder = gameController.getCurrentBidderIndex();
            if (currentBidder != 0) {
                Timer timer = new Timer(800, e -> {
                    aiBid(currentBidder);
                    ((Timer)e.getSource()).stop();
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }
    
    // AI叫分
    private void aiBid(int playerIndex) {
        Player player = gameController.getPlayers()[playerIndex];
        int bid = aiPlayer.makeBid(player);
        gameController.playerBid(bid, playerIndex);
    }
    
    // AI出牌
    private void aiTurn(int playerIndex) {
        Player player = gameController.getPlayers()[playerIndex];
        
        if (aiPlayer.shouldPass(player)) {
            gameController.playCards(playerIndex, new java.util.ArrayList<>());
        } else {
            List<Card> cards = aiPlayer.makePlay(player);
            if (cards != null && !cards.isEmpty()) {
                gameController.playCards(playerIndex, cards);
            } else {
                gameController.playCards(playerIndex, new java.util.ArrayList<>());
            }
        }
    }
    
    @Override
    public void onGameEnd(Player winner) {
        JOptionPane.showMessageDialog(this, 
                winner.getName() + " 获胜！", 
                "游戏结束", 
                JOptionPane.INFORMATION_MESSAGE);
        
        // 询问是否重新开始
        int option = JOptionPane.showConfirmDialog(this,
                "是否开始新游戏？",
                "新游戏",
                JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        }
    }
    
    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void restartGame() {
        gameController = new GameController(this);
        aiPlayer = new AIPlayer(gameController);
        gamePanel.setGameController(gameController);
        controlPanel.setGameController(gameController);
        gameController.startGame();
    }
    
    public GamePanel getGamePanel() {
        return gamePanel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
