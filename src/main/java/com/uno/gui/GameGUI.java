package com.uno.gui;

import com.uno.client.UnoClientMain;
import com.uno.model.Card;
import com.uno.model.CardColor;
import com.uno.model.CardType;
import com.uno.server.GameRoom.GameState;
import com.uno.server.GameRoom.PlayerInfo;
import com.uno.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Giao diện game Uno
 */
public class GameGUI extends JFrame {
    private final UnoClientMain clientMain;
    private final String playerId;
    
    private JPanel gamePanel;
    private JPanel topCardPanel;
    private JPanel playerHandPanel;
    private List<JPanel> otherPlayerPanels;
    private JLabel topCardLabel;
    private JLabel currentPlayerLabel;
    private JLabel directionLabel;
    private JButton drawCardButton;
    private JButton endTurnButton;
    private JButton unoButton;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendButton;
    
    private boolean canPlay;
    private Card topCard;
    private List<Card> playerHand;
    
    public GameGUI(UnoClientMain clientMain, String playerId) {
        this.clientMain = clientMain;
        // Đảm bảo playerId không null và được trim
        this.playerId = (playerId != null) ? playerId.trim() : "";
        
        // Debug ID của người chơi
        System.out.println("GameGUI constructor: Nhận playerId = '" + playerId + "', sau khi trim: '" + this.playerId + "'");
        
        this.canPlay = false;
        this.playerHand = new ArrayList<>();
        
        initComponents();
        setupListeners();
        
        setTitle("🃏 UNO ONLINE - GAME");
        // FIX: Kích thước cửa sổ cân đối hơn
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Clean, minimalist background
        getContentPane().setBackground(new Color(248, 249, 250)); // Light gray
        
        // Khi đóng cửa sổ, đảm bảo thông báo cho server
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                    GameGUI.this,
                    "Bạn có chắc chắn muốn thoát khỏi game?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (option == JOptionPane.YES_OPTION) {
                    clientMain.leaveRoom();
                    dispose();
                }
            }
        });
    }
    
    /**
     * Khởi tạo các thành phần UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Clean main panel
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(new Color(248, 249, 250)); // Light gray
        
        // Clean info panel at top
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        currentPlayerLabel = new JLabel("Current Turn: ");
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentPlayerLabel.setForeground(new Color(73, 80, 87));
        
        // Clean separator
        JLabel separator = new JLabel(" • ");
        separator.setFont(new Font("Arial", Font.BOLD, 16));
        separator.setForeground(new Color(173, 181, 189));
        
        directionLabel = new JLabel("Direction: →");
        directionLabel.setFont(new Font("Arial", Font.BOLD, 14));
        directionLabel.setForeground(new Color(73, 80, 87));
        
        infoPanel.add(currentPlayerLabel);
        infoPanel.add(separator);
        infoPanel.add(directionLabel);
        
        // Clean center panel for top card
        topCardPanel = new JPanel();
        topCardPanel.setLayout(new BoxLayout(topCardPanel, BoxLayout.Y_AXIS));
        topCardPanel.setBackground(Color.WHITE);
        topCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Simple label for current card
        JLabel currentCardLabel = new JLabel("Current Card");
        currentCardLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentCardLabel.setForeground(new Color(73, 80, 87));
        currentCardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topCardLabel = new JLabel("No Card");
        topCardLabel.setPreferredSize(new Dimension(120, 180));
        topCardLabel.setMaximumSize(new Dimension(120, 180));
        topCardLabel.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230), 2));
        topCardLabel.setOpaque(true);
        topCardLabel.setBackground(Color.WHITE);
        topCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topCardLabel.setVerticalAlignment(SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topCardLabel.setForeground(new Color(173, 181, 189));
        topCardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topCardPanel.add(Box.createVerticalGlue());
        topCardPanel.add(currentCardLabel);
        topCardPanel.add(Box.createVerticalStrut(15));
        topCardPanel.add(topCardLabel);
        topCardPanel.add(Box.createVerticalGlue());
        
        // Clean other players panel
        JPanel otherPlayersPanel = new JPanel();
        otherPlayersPanel.setLayout(new BoxLayout(otherPlayersPanel, BoxLayout.Y_AXIS));
        otherPlayersPanel.setBackground(new Color(248, 249, 250));
        otherPlayersPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(222, 226, 230)),
                "Other Players",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(73, 80, 87)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        otherPlayersPanel.setPreferredSize(new Dimension(200, 0));
        
        otherPlayerPanels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setBackground(Color.WHITE);
            playerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            playerPanel.setMaximumSize(new Dimension(180, 90));
            
            JLabel nameLabel = new JLabel("Player " + (i + 1));
            nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameLabel.setForeground(new Color(73, 80, 87));
            
            JLabel cardCountLabel = new JLabel("Cards: 0");
            cardCountLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            cardCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardCountLabel.setForeground(new Color(108, 117, 125));
            
            JLabel unoLabel = new JLabel("UNO: No");
            unoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            unoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            unoLabel.setForeground(new Color(220, 53, 69));
            
            playerPanel.add(nameLabel);
            playerPanel.add(Box.createVerticalStrut(5));
            playerPanel.add(cardCountLabel);
            playerPanel.add(Box.createVerticalStrut(3));
            playerPanel.add(unoLabel);
            
            otherPlayersPanel.add(playerPanel);
            if (i < 2) otherPlayersPanel.add(Box.createVerticalStrut(15));
            otherPlayerPanels.add(playerPanel);
        }
        
        otherPlayersPanel.add(Box.createVerticalGlue());
        
        // FIX: Redesign khu vực bài của người chơi
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
                "BAI CUA BAN",
                0, 0,
                new Font("Arial", Font.BOLD, 16),
                new Color(25, 25, 112)
            ),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        playerPanel.setBackground(new Color(240, 248, 255));
        
        // Sử dụng WrapLayout với spacing nhỏ hơn
        playerHandPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 6, 6));
        playerHandPanel.setBackground(new Color(255, 255, 255));
        
        // Tối ưu kích thước
        playerHandPanel.setPreferredSize(new Dimension(1000, 200));
        
        JScrollPane handScrollPane = new JScrollPane(playerHandPanel);
        handScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        handScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        handScrollPane.setPreferredSize(new Dimension(1000, 180));
        handScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createLineBorder(new Color(176, 196, 222), 1)
        ));
        handScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // FIX: Cải thiện panel chứa các nút hành động
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));
        
        drawCardButton = createCleanButton("Draw Card", new Color(0, 123, 255), Color.WHITE);
        endTurnButton = createCleanButton("End Turn", new Color(108, 117, 125), Color.WHITE);
        unoButton = createCleanButton("UNO!", new Color(220, 53, 69), Color.WHITE);
        
        actionPanel.add(drawCardButton);
        actionPanel.add(endTurnButton);
        actionPanel.add(unoButton);
        
        playerPanel.add(handScrollPane, BorderLayout.CENTER);
        playerPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // FIX: Cải thiện panel chat ở bên phải
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "CHAT",
            0, 0,
            new Font("Arial", Font.BOLD, 12),
            new Color(25, 25, 112)
        ));
        chatPanel.setPreferredSize(new Dimension(220, 700));
        chatPanel.setBackground(new Color(245, 245, 245)); // White smoke
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 11));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 5));
        chatInputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        chatInputPanel.setBackground(new Color(245, 245, 245));
        
        chatField = new JTextField();
        chatField.setFont(new Font("Arial", Font.PLAIN, 11));
        chatField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        
        sendButton = new JButton("GUI");
        sendButton.setFont(new Font("Arial", Font.BOLD, 10));
        sendButton.setPreferredSize(new Dimension(50, 25));
        sendButton.setBackground(new Color(135, 206, 250)); // Light sky blue
        sendButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);
        
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
        
        // Thêm các panel vào panel chính
        gamePanel.add(infoPanel, BorderLayout.NORTH);
        gamePanel.add(topCardPanel, BorderLayout.CENTER);
        gamePanel.add(otherPlayersPanel, BorderLayout.WEST);
        gamePanel.add(playerPanel, BorderLayout.SOUTH);
        
        add(gamePanel, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.EAST);
        
        // Vô hiệu hóa các nút ban đầu
        drawCardButton.setEnabled(false);
        endTurnButton.setEnabled(false);
        unoButton.setEnabled(false);
        
        // Thiết lập trạng thái ban đầu
        canPlay = false;
        
        // Hiển thị hướng dẫn ngắn cho người chơi mới
        showGameInstructions();
    }
    
    /**
     * Thiết lập các listener
     */
    private void setupListeners() {
        drawCardButton.addActionListener(e -> {
            if (canPlay) {
                clientMain.drawCard();
                drawCardButton.setEnabled(false);
                endTurnButton.setEnabled(true);
                canPlay = false;
            }
        });
        
        endTurnButton.addActionListener(e -> {
            clientMain.endTurn();
            endTurnButton.setEnabled(false);
            canPlay = false;
        });
        
        unoButton.addActionListener(e -> {
            clientMain.callUno();
        });
        
        sendButton.addActionListener(e -> {
            String message = chatField.getText().trim();
            if (!message.isEmpty()) {
                clientMain.sendChatMessage(message);
                chatField.setText("");
            }
        });
        
        chatField.addActionListener(e -> {
            String message = chatField.getText().trim();
            if (!message.isEmpty()) {
                clientMain.sendChatMessage(message);
                chatField.setText("");
            }
        });
    }
    
    /**
     * Cập nhật trạng thái game
     * 
     * @param gameState Trạng thái game
     */
    public void updateGameState(GameState gameState) {
        System.out.println("GameGUI.updateGameState được gọi với gameState: " + gameState);
        
        // Cập nhật lá bài trên cùng
        topCard = gameState.getTopCard();
        updateTopCard();
        
        // Cập nhật thông tin người chơi hiện tại
        String currentPlayerId = gameState.getCurrentPlayerId();
        
        // FIX: Xác định isMyTurn ngay từ đầu và log rõ ràng
        boolean isMyTurn = StringUtils.safeEquals(currentPlayerId, this.playerId);
        
        // Debug chi tiết
        System.out.println("DEBUG GameGUI: currentPlayerId='" + currentPlayerId + "', this.playerId='" + 
                          this.playerId + "', isMyTurn=" + isMyTurn);

        Map<String, PlayerInfo> playerInfos = gameState.getPlayerInfos();
        System.out.println("GameGUI: Thông tin người chơi trong game: " + playerInfos.keySet());
        
        PlayerInfo currentPlayerInfo = playerInfos.get(currentPlayerId);
        
        if (currentPlayerInfo != null) {
            String currentPlayerName = currentPlayerInfo.getName();
            
            // FIX: Hiển thị lượt không dùng emoji
            if (isMyTurn) {
                currentPlayerLabel.setText(">>> LUOT CUA BAN (" + currentPlayerName + ") <<<");
                currentPlayerLabel.setForeground(new Color(255, 69, 0)); // Red orange
                currentPlayerLabel.setFont(currentPlayerLabel.getFont().deriveFont(Font.BOLD));
            } else {
                currentPlayerLabel.setText("LUOT CUA: " + currentPlayerName);
                currentPlayerLabel.setForeground(Color.WHITE);
                currentPlayerLabel.setFont(currentPlayerLabel.getFont().deriveFont(Font.BOLD));
            }
            
            System.out.println("GameGUI: Hiển thị lượt của người chơi: " + currentPlayerName + 
                              " (ID: " + currentPlayerId + "), là lượt của tôi: " + isMyTurn);
        } else {
            System.out.println("GameGUI: KHÔNG TÌM THẤY THÔNG TIN CỦA currentPlayerId = " + currentPlayerId);
        }
        
        // FIX: Cập nhật chiều chơi không dùng ký tự đặc biệt
        directionLabel.setText("CHIEU CHOI: " + (gameState.isClockwise() ? "THUAN" : "NGUOC"));
        
        // Cập nhật bài của người chơi
        PlayerInfo playerInfo = playerInfos.get(this.playerId); // Luôn lấy thông tin của chính người chơi này
        if (playerInfo != null && playerInfo.getHand() != null) {
            playerHand = playerInfo.getHand();
            
            // FIX: Truyền isMyTurn vào updatePlayerHand để nó biết lúc nào có thể đánh bài
            updatePlayerHand(isMyTurn);
        }
        
        // FIX: Đặt canPlay và các nút dựa trên isMyTurn
        // Lưu ý: Biến này cũng đã được đặt trong updatePlayerHand để đảm bảo nhất quán
        this.canPlay = isMyTurn; 
        System.out.println("****** THIẾT LẬP canPlay = " + this.canPlay + " cho player " + this.playerId + " ******");
        
        drawCardButton.setEnabled(isMyTurn);
        endTurnButton.setEnabled(false); // Chỉ bật sau khi rút bài
        unoButton.setEnabled(playerHand.size() == 2); // Chỉ bật khi còn 2 lá bài
        
        // Cập nhật thông tin người chơi khác
        List<PlayerInfo> otherPlayers = new ArrayList<>();
        for (String pid : playerInfos.keySet()) {
            // FIX: So sánh với this.playerId để loại bỏ chính người chơi này
            if (!StringUtils.safeEquals(pid, this.playerId)) {
                otherPlayers.add(playerInfos.get(pid));
            }
        }
        
        // Cập nhật giao diện người chơi khác
        for (int i = 0; i < otherPlayerPanels.size(); i++) {
            JPanel panel = otherPlayerPanels.get(i);
            if (i < otherPlayers.size()) {
                PlayerInfo otherPlayer = otherPlayers.get(i);
                
                // Đánh dấu rõ ràng khi đến lượt của người chơi khác
                boolean isOtherPlayersTurn = StringUtils.safeEquals(currentPlayerId, otherPlayer.getId());
                
                // Thay đổi tiêu đề và màu sắc để nổi bật người chơi đang có lượt
                if (isOtherPlayersTurn) {
                    panel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.RED, 2),
                        "⟹ " + otherPlayer.getName() + " ⟸"));
                } else {
                    panel.setBorder(BorderFactory.createTitledBorder(otherPlayer.getName()));
                }
                
                // FIX: Cập nhật thông tin với số lá bài chính xác
                // Tìm các component trong panel (bỏ qua Box.createVerticalStrut)
                Component[] components = panel.getComponents();
                JLabel nameLabel = null;
                JLabel cardCountLabel = null; 
                JLabel unoLabel = null;
                
                for (Component comp : components) {
                    if (comp instanceof JLabel) {
                        JLabel label = (JLabel) comp;
                        String text = label.getText();
                        if (text.startsWith("Ten:")) {
                            nameLabel = label;
                        } else if (text.startsWith("So bai:")) {
                            cardCountLabel = label;
                        } else if (text.startsWith("UNO:")) {
                            unoLabel = label;
                        }
                    }
                }
                
                if (nameLabel != null) nameLabel.setText("Ten: " + otherPlayer.getName());
                if (cardCountLabel != null) {
                    cardCountLabel.setText("So bai: " + otherPlayer.getHandSize());
                    System.out.println("Cap nhat so bai cho " + otherPlayer.getName() + ": " + otherPlayer.getHandSize());
                }
                if (unoLabel != null) unoLabel.setText("UNO: " + (otherPlayer.hasCalledUno() ? "Co" : "Khong"));
                
                panel.setVisible(true);
                System.out.println("Đã cập nhật thông tin cho đối thủ " + otherPlayer.getName() + 
                                  ", số lá bài: " + otherPlayer.getHandSize());
            } else {
                panel.setVisible(false);
            }
        }
        
        // Nếu game kết thúc, hiển thị thông báo
        if (gameState.isGameOver()) {
            String winnerId = gameState.getWinnerId();
            String winnerName = playerInfos.get(winnerId).getName();
            JOptionPane.showMessageDialog(this, "Người chơi " + winnerName + " đã chiến thắng!", "Game kết thúc", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Cập nhật bài của người chơi
     * 
     * @param isCurrentPlayer Có phải lượt của người chơi hiện tại không
     */
    private void updatePlayerHand(boolean isCurrentPlayer) {
        playerHandPanel.removeAll();
        
        // FIX: Đảm bảo canPlay phản ánh chính xác lúc nào người chơi có thể đánh bài
        this.canPlay = isCurrentPlayer;
        System.out.println("updatePlayerHand: isCurrentPlayer = " + isCurrentPlayer + 
                          ", this.canPlay được cập nhật thành = " + this.canPlay + 
                          ", số lá bài = " + playerHand.size());

        for (Card card : playerHand) {
            JButton cardButton = createCardButton(card);
            final int cardIndex = playerHand.indexOf(card);
            
            // FIX: Chỉ bật lá bài có thể chơi được khi là lượt của người chơi này
            boolean canPlayThisCard = isCurrentPlayer && card.canPlayOn(topCard);
            cardButton.setEnabled(canPlayThisCard);
            
            System.out.println("  Lá bài: " + card + ", có thể đánh: " + canPlayThisCard);
            
            cardButton.addActionListener(e -> {
                System.out.println("Card clicked, canPlay = " + canPlay + ", card = " + card);
                
                // Kiểm tra lại xem có thật sự là lượt của người chơi này không
                if (canPlay) {
                    // Kiểm tra xem lá bài có thể đánh được không
                    if (card.canPlayOn(topCard)) {
                        // Nếu là lá wild, hiển thị dialog chọn màu
                        if (card.getType() == CardType.WILD || card.getType() == CardType.WILD_DRAW_FOUR) {
                            CardColor selectedColor = showColorSelectionDialog();
                            if (selectedColor != null) {
                                clientMain.playCard(cardIndex, selectedColor);
                                // FIX: Vô hiệu hóa ngay sau khi đánh bài
                                canPlay = false;
                                drawCardButton.setEnabled(false);
                                endTurnButton.setEnabled(false);
                            }
                        } else {
                            clientMain.playCard(cardIndex, card.getColor());
                            // FIX: Vô hiệu hóa ngay sau khi đánh bài
                            canPlay = false;
                            drawCardButton.setEnabled(false);
                            endTurnButton.setEnabled(false);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Bạn không thể đánh lá bài này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.out.println("Không thể đánh bài vì không phải lượt của bạn! canPlay = " + canPlay);
                }
            });
            
            playerHandPanel.add(cardButton);
        }
        
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }
    
    /**
     * Tạo nút hiển thị lá bài
     * 
     * @param card Lá bài
     * @return Nút hiển thị lá bài
     */
    private JButton createCardButton(Card card) {
        JButton cardButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Clean, minimalist card design
                int width = getWidth();
                int height = getHeight();
                
                // Background color based on card color - vibrant and clear
                Color bgColor;
                String colorSymbol = "";
                switch (card.getColor()) {
                    case RED:
                        bgColor = new Color(220, 53, 69); // Bright red
                        colorSymbol = "🔴";
                        break;
                    case GREEN: 
                        bgColor = new Color(40, 167, 69); // Bright green
                        colorSymbol = "🟢";
                        break;
                    case BLUE:
                        bgColor = new Color(0, 123, 255); // Bright blue
                        colorSymbol = "🔵";
                        break;
                    case YELLOW:
                        bgColor = new Color(255, 193, 7); // Bright yellow
                        colorSymbol = "🟡";
                        break;
                    default:
                        bgColor = new Color(52, 58, 64); // Dark gray for Wild cards
                        colorSymbol = "⚫";
                        break;
                }
                
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, width, height, 15, 15);
                
                // Clean white border
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, width-2, height-2, 15, 15);
                
                // Card content
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                
                String displayText;
                String typeSymbol = "";
                
                if (card.getType() == CardType.NUMBER) {
                    displayText = String.valueOf(card.getValue());
                } else {
                    // Sử dụng biểu tượng Unicode rõ ràng hơn cho các lá đặc biệt
                    switch (card.getType()) {
                        case SKIP:
                            displayText = "⊘";  // Ký hiệu cấm
                            typeSymbol = "SKIP";
                            break;
                        case REVERSE:
                            displayText = "⤸";  // Mũi tên đảo chiều
                            typeSymbol = "REV";
                            break;
                        case DRAW_TWO:
                            displayText = "+2";
                            typeSymbol = "RÚT 2";
                            break;
                        case WILD:
                            displayText = "★";  // Ngôi sao cho Wild
                            typeSymbol = "ĐỔI MÀU";
                            break;
                        case WILD_DRAW_FOUR:
                            displayText = "+4";
                            typeSymbol = "ĐỔI & +4";
                            break;
                        default:
                            displayText = "?";
                            break;
                    }
                }
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(displayText);
                int textHeight = fm.getAscent();
                
                // Center the main symbol/number
                int x = (width - textWidth) / 2;
                int y = (height + textHeight) / 2 - 10;
                
                g2.drawString(displayText, x, y);
                
                // Vẽ tên loại lá bài phía dưới (nếu không phải số)
                if (card.getType() != CardType.NUMBER && !typeSymbol.isEmpty()) {
                    g2.setFont(new Font("Arial", Font.BOLD, 9));
                    FontMetrics fmSmall = g2.getFontMetrics();
                    int typeWidth = fmSmall.stringWidth(typeSymbol);
                    int typeX = (width - typeWidth) / 2;
                    g2.drawString(typeSymbol, typeX, height - 15);
                }
                
                // Vẽ biểu tượng màu ở góc trên bên trái
                g2.setFont(new Font("Arial", Font.PLAIN, 16));
                g2.drawString(colorSymbol, 3, 20);
                
                // Draw corner symbols for better visual balance
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString(displayText, 5, 15);
                
                // Rotate and draw bottom-right corner
                g2.rotate(Math.PI, width/2.0, height/2.0);
                g2.drawString(displayText, 5, 15);
                
                g2.dispose();
            }
        };
        
        cardButton.setPreferredSize(new Dimension(85, 130));
        cardButton.setOpaque(false);
        cardButton.setContentAreaFilled(false);
        cardButton.setBorderPainted(false);
        cardButton.setFocusPainted(false);
        
        // Thêm tooltip giải thích cho từng loại lá bài
        String tooltip = getCardTooltip(card);
        cardButton.setToolTipText(tooltip);
        
        // Simple hover effect for clean design
        cardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (cardButton.isEnabled()) {
                    // Slight scale up effect
                    cardButton.setPreferredSize(new Dimension(90, 135));
                    cardButton.revalidate();
                    cardButton.repaint();
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Return to normal size
                cardButton.setPreferredSize(new Dimension(85, 130));
                cardButton.revalidate();
                cardButton.repaint();
            }
        });
        return cardButton;
    }
    
    /**
     * Hiển thị hướng dẫn ngắn gọn cho người chơi mới
     */
    private void showGameInstructions() {
        String instructions = "<html><div style='width: 400px;'>" +
                "<h2>🎮 HƯỚNG DẪN CHƠI UNO</h2>" +
                "<h3>🎯 Mục tiêu:</h3>" +
                "<p>Đánh hết lá bài trên tay để thắng!</p>" +
                
                "<h3>📋 Luật cơ bản:</h3>" +
                "<p>• Đánh lá bài <b>cùng màu</b> hoặc <b>cùng số/ký hiệu</b> với lá trên cùng</p>" +
                "<p>• Lá <b>Đổi màu (★)</b> và <b>+4</b> có thể đánh bất kỳ lúc nào</p>" +
                "<p>• Khi còn 2 lá, nhấn <b>'UNO'</b> để tuyên bố</p>" +
                
                "<h3>🃏 Lá đặc biệt:</h3>" +
                "<p>⊘ <b>Bỏ lượt:</b> Người kế tiếp mất lượt</p>" +
                "<p>⤸ <b>Đảo chiều:</b> Thay đổi chiều chơi</p>" +
                "<p>+2 <b>Rút 2:</b> Người kế tiếp rút 2 lá và mất lượt</p>" +
                "<p>★ <b>Đổi màu:</b> Chọn màu tiếp theo</p>" +
                "<p>+4 <b>Đổi màu +4:</b> Chọn màu + người kế tiếp rút 4 lá</p>" +
                
                "<h3>💡 Mẹo:</h3>" +
                "<p>• Di chuột qua lá bài để xem chi tiết</p>" +
                "<p>• Màu sắc và biểu tượng giúp dễ phân biệt</p>" +
                "</div></html>";
        
        JOptionPane.showMessageDialog(this, instructions, "Hướng dẫn UNO", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Lấy tên màu hiển thị bằng tiếng Việt
     * 
     * @param color Màu lá bài
     * @return Tên màu bằng tiếng Việt
     */
    private String getColorDisplayName(CardColor color) {
        switch (color) {
            case RED:
                return "ĐỎ";
            case GREEN:
                return "XANH LÁ";
            case BLUE:
                return "XANH DƯƠNG";
            case YELLOW:
                return "VÀNG";
            default:
                return "ĐEN";
        }
    }
    
    /**
     * Lấy tooltip giải thích cho từng loại lá bài
     * 
     * @param card Lá bài
     * @return Chú thích giải thích
     */
    private String getCardTooltip(Card card) {
        String colorName = getColorDisplayName(card.getColor());
        
        if (card.getType() == CardType.NUMBER) {
            return "<html><b>Lá số " + card.getValue() + " màu " + colorName + "</b><br>" +
                   "Có thể đánh khi lá trên cùng cùng số hoặc cùng màu</html>";
        } else {
            switch (card.getType()) {
                case SKIP:
                    return "<html><b>Lá Bỏ Lượt màu " + colorName + "</b><br>" +
                           "Người chơi kế tiếp sẽ bị mất lượt chơi<br>" +
                           "Có thể đánh khi lá trên cùng cùng màu hoặc cũng là lá Bỏ Lượt</html>";
                case REVERSE:
                    return "<html><b>Lá Đảo Chiều màu " + colorName + "</b><br>" +
                           "Thay đổi chiều chơi (thuận ↔ ngược)<br>" +
                           "Có thể đánh khi lá trên cùng cùng màu hoặc cũng là lá Đảo Chiều</html>";
                case DRAW_TWO:
                    return "<html><b>Lá Rút 2 màu " + colorName + "</b><br>" +
                           "Người chơi kế tiếp phải rút 2 lá và mất lượt<br>" +
                           "Có thể đánh khi lá trên cùng cùng màu hoặc cũng là lá Rút 2</html>";
                case WILD:
                    return "<html><b>Lá Đổi Màu</b><br>" +
                           "Có thể đánh bất kỳ lúc nào<br>" +
                           "Cho phép bạn chọn màu tiếp theo</html>";
                case WILD_DRAW_FOUR:
                    return "<html><b>Lá Đổi Màu +4</b><br>" +
                           "Chỉ đánh khi không có lá nào cùng màu với lá trên cùng<br>" +
                           "Người chơi kế tiếp có thể thách thức nếu nghi ngờ<br>" +
                           "Nếu hợp lệ: người kế tiếp rút 4 lá và mất lượt<br>" +
                           "Nếu thách thức thành công: bạn rút 4 lá</html>";
                default:
                    return "Lá bài đặc biệt";
            }
        }
    }
    
    /**
     * Updates the display of the top card with clean, modern design
     */
    private void updateTopCard() {
        if (topCard != null) {
            // Clean background colors
            Color bgColor;
            switch (topCard.getColor()) {
                case RED:
                    bgColor = new Color(220, 53, 69);
                    break;
                case GREEN:
                    bgColor = new Color(40, 167, 69);
                    break;
                case BLUE:
                    bgColor = new Color(0, 123, 255);
                    break;
                case YELLOW:
                    bgColor = new Color(255, 193, 7);
                    break;
                default:
                    bgColor = new Color(52, 58, 64);
                    break;
            }
            
            topCardLabel.setBackground(bgColor);
            topCardLabel.setForeground(Color.WHITE);
            topCardLabel.setOpaque(true);
            
            // Hiển thị rõ ràng hơn cho lá bài trên cùng
            String displayText;
            String cardDescription = "";
            
            if (topCard.getType() == CardType.NUMBER) {
                displayText = "<html><div style='text-align: center;'><font size='6'>" + 
                             topCard.getValue() + "</font><br><font size='3'>" + 
                             getColorDisplayName(topCard.getColor()) + "</font></div></html>";
            } else {
                String symbol = "";
                switch (topCard.getType()) {
                    case SKIP:
                        symbol = "⊘";
                        cardDescription = "BỎ LƯỢT";
                        break;
                    case REVERSE:
                        symbol = "⤸";
                        cardDescription = "ĐẢO CHIỀU";
                        break;
                    case DRAW_TWO:
                        symbol = "+2";
                        cardDescription = "RÚT 2 LÁ";
                        break;
                    case WILD:
                        symbol = "★";
                        cardDescription = "ĐỔI MÀU";
                        break;
                    case WILD_DRAW_FOUR:
                        symbol = "+4";
                        cardDescription = "ĐỔI MÀU & RÚT 4";
                        break;
                    default:
                        symbol = "?";
                        cardDescription = "KHÔNG RÕ";
                        break;
                }
                
                displayText = "<html><div style='text-align: center;'><font size='6'>" + 
                             symbol + "</font><br><font size='2'>" + 
                             cardDescription + "</font><br><font size='3'>" + 
                             getColorDisplayName(topCard.getColor()) + "</font></div></html>";
            }
            
            topCardLabel.setText(displayText);
            
            // Clean rounded border
            topCardLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        } else {
            topCardLabel.setText("No Card");
            topCardLabel.setOpaque(false);
            topCardLabel.setBackground(null);
            topCardLabel.setForeground(Color.BLACK);
            topCardLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        }
    }
    

    
    /**
     * Hiển thị dialog chọn màu cho lá Wild
     * 
     * @return Màu được chọn
     */
    private CardColor showColorSelectionDialog() {
        Object[] options = {
            "Red", "Green", "Blue", "Yellow"
        };
        
        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose color for Wild card:",
            "Select Color",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        switch (choice) {
            case 0:
                return CardColor.RED;
            case 1:
                return CardColor.GREEN;
            case 2:
                return CardColor.BLUE;
            case 3:
                return CardColor.YELLOW;
            default:
                return null;
        }
    }
    
    /**
     * Thêm tin nhắn vào khu vực chat
     * 
     * @param message Tin nhắn
     */
    public void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /**
     * Creates a clean, modern button with consistent styling
     */
    private JButton createCleanButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(backgroundColor.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
}
