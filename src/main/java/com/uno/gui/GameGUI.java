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
        
        // FIX: Màu nền tổng thể đẹp hơn
        getContentPane().setBackground(new Color(230, 230, 250)); // Lavender
        
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
        
        // FIX: Panel chính với màu nền hài hòa
        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(new Color(230, 230, 250)); // Lavender
        
        // FIX: Redesign thanh thông tin trên đầu
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 12));
        infoPanel.setBackground(new Color(72, 61, 139)); // Dark slate blue
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        currentPlayerLabel = new JLabel("LUOT CUA: ");
        currentPlayerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        currentPlayerLabel.setForeground(Color.WHITE);
        
        // Tạo separator
        JLabel separator = new JLabel(" | ");
        separator.setFont(new Font("Arial", Font.BOLD, 16));
        separator.setForeground(new Color(255, 215, 0)); // Gold
        
        directionLabel = new JLabel("CHIEU CHOI: >>>");
        directionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        directionLabel.setForeground(new Color(255, 215, 0)); // Gold
        
        infoPanel.add(currentPlayerLabel);
        infoPanel.add(separator);
        infoPanel.add(directionLabel);
        
        // FIX: Redesign panel trung tâm với layout đẹp hơn
        topCardPanel = new JPanel();
        topCardPanel.setLayout(new BoxLayout(topCardPanel, BoxLayout.Y_AXIS));
        topCardPanel.setBackground(new Color(25, 111, 61)); // Màu xanh đậm hơn
        topCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Thêm label "Lá bài hiện tại"
        JLabel currentCardLabel = new JLabel("LA BAI HIEN TAI");
        currentCardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentCardLabel.setForeground(Color.WHITE);
        currentCardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topCardLabel = new JLabel("Chưa có lá bài");
        topCardLabel.setPreferredSize(new Dimension(140, 200));
        topCardLabel.setMaximumSize(new Dimension(140, 200));
        topCardLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            )
        ));
        topCardLabel.setOpaque(true);
        topCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topCardLabel.setVerticalAlignment(SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topCardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        topCardPanel.add(Box.createVerticalGlue());
        topCardPanel.add(currentCardLabel);
        topCardPanel.add(Box.createVerticalStrut(15));
        topCardPanel.add(topCardLabel);
        topCardPanel.add(Box.createVerticalGlue());
        
        // FIX: Redesign panel đối thủ đẹp hơn
        JPanel otherPlayersPanel = new JPanel();
        otherPlayersPanel.setLayout(new BoxLayout(otherPlayersPanel, BoxLayout.Y_AXIS));
        otherPlayersPanel.setBackground(new Color(248, 248, 255));
        otherPlayersPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                "DOI THU",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(25, 25, 112)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        otherPlayersPanel.setPreferredSize(new Dimension(200, 0));
        
        otherPlayerPanels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setBackground(Color.WHITE);
            playerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            playerPanel.setMaximumSize(new Dimension(180, 100));
            
            JLabel nameLabel = new JLabel("Ten: ");
            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel cardCountLabel = new JLabel("So bai: 0");
            cardCountLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            cardCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardCountLabel.setForeground(new Color(139, 69, 19)); // Brown color
            
            JLabel unoLabel = new JLabel("UNO: Khong");
            unoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            unoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            unoLabel.setForeground(new Color(220, 20, 60)); // Crimson
            
            playerPanel.add(nameLabel);
            playerPanel.add(Box.createVerticalStrut(3));
            playerPanel.add(cardCountLabel);
            playerPanel.add(Box.createVerticalStrut(3));
            playerPanel.add(unoLabel);
            
            otherPlayersPanel.add(playerPanel);
            if (i < 2) otherPlayersPanel.add(Box.createVerticalStrut(10));
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
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        actionPanel.setBackground(new Color(248, 248, 255)); // Ghost white
        
        drawCardButton = new JButton("RUT BAI");
        drawCardButton.setFont(new Font("Arial", Font.BOLD, 12));
        drawCardButton.setPreferredSize(new Dimension(120, 35));
        drawCardButton.setBackground(new Color(135, 206, 250)); // Light sky blue
        drawCardButton.setForeground(Color.BLACK);
        drawCardButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        endTurnButton = new JButton("KET THUC LUOT");
        endTurnButton.setFont(new Font("Arial", Font.BOLD, 12));
        endTurnButton.setPreferredSize(new Dimension(140, 35));
        endTurnButton.setBackground(new Color(255, 165, 0)); // Orange
        endTurnButton.setForeground(Color.BLACK);
        endTurnButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
        unoButton = new JButton("UNO!");
        unoButton.setFont(new Font("Arial", Font.BOLD, 12));
        unoButton.setPreferredSize(new Dimension(100, 35));
        unoButton.setBackground(new Color(255, 69, 0)); // Red orange
        unoButton.setForeground(Color.WHITE);
        unoButton.setBorder(BorderFactory.createRaisedBevelBorder());
        
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
        JButton cardButton = new JButton();
        // FIX: Kích thước lá bài vừa phải và đẹp
        cardButton.setPreferredSize(new Dimension(85, 130));
        
        // FIX: Cải thiện màu sắc cho lá bài dễ nhìn hơn
        switch (card.getColor()) {
            case RED:
                cardButton.setBackground(new Color(220, 20, 60)); // Crimson red
                cardButton.setForeground(Color.WHITE);
                break;
            case GREEN:
                cardButton.setBackground(new Color(34, 139, 34)); // Forest green
                cardButton.setForeground(Color.WHITE);
                break;
            case BLUE:
                cardButton.setBackground(new Color(30, 144, 255)); // Dodger blue
                cardButton.setForeground(Color.WHITE);
                break;
            case YELLOW:
                cardButton.setBackground(new Color(255, 215, 0)); // Gold
                cardButton.setForeground(Color.BLACK);
                break;
            default:
                cardButton.setBackground(new Color(47, 79, 79)); // Dark slate gray
                cardButton.setForeground(Color.WHITE);
                break;
        }
        
        // Hiển thị giá trị/loại lá bài
        String cardText;
        if (card.getType() == CardType.NUMBER) {
            cardText = String.valueOf(card.getValue());
        } else {
            cardText = getDisplayTextForCardType(card.getType());
        }
        
        // FIX: Thiết kế lá bài đẹp và hiện đại
        cardButton.setText("<html><center><div style='font-family:Arial; font-size:14pt; font-weight:bold;'>" + 
                          cardText + "</div></center></html>");
        
        // Viền đẹp cho lá bài
        cardButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1)
            ),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        
        // Làm cho lá bài có góc bo tròn hơn
        cardButton.setFocusPainted(false);
        cardButton.setBorderPainted(true);
        
        // Hiệu ứng hover đẹp hơn
        cardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (cardButton.isEnabled()) {
                    cardButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 215, 0), 3), // Gold
                            BorderFactory.createLineBorder(Color.DARK_GRAY, 1)
                        ),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                    // Hiệu ứng phóng to nhẹ
                    cardButton.setPreferredSize(new Dimension(87, 132));
                    cardButton.revalidate();
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cardButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createLineBorder(Color.DARK_GRAY, 1)
                    ),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)
                ));
                cardButton.setPreferredSize(new Dimension(85, 130));
                cardButton.revalidate();
            }
        });
        return cardButton;
    }
    
    /**
     * Cập nhật hiển thị lá bài trên cùng
     */
    private void updateTopCard() {
        if (topCard != null) {
            // FIX: Cải thiện màu sắc cho lá bài trên cùng
            switch (topCard.getColor()) {
                case RED:
                    topCardLabel.setBackground(new Color(220, 20, 60)); // Crimson red
                    topCardLabel.setForeground(Color.WHITE);
                    break;
                case GREEN:
                    topCardLabel.setBackground(new Color(34, 139, 34)); // Forest green
                    topCardLabel.setForeground(Color.WHITE);
                    break;
                case BLUE:
                    topCardLabel.setBackground(new Color(30, 144, 255)); // Dodger blue
                    topCardLabel.setForeground(Color.WHITE);
                    break;
                case YELLOW:
                    topCardLabel.setBackground(new Color(255, 215, 0)); // Gold
                    topCardLabel.setForeground(Color.BLACK);
                    break;
                default:
                    topCardLabel.setBackground(new Color(47, 79, 79)); // Dark slate gray
                    topCardLabel.setForeground(Color.WHITE);
                    break;
            }
            
            // Hiển thị giá trị/loại lá bài
            String cardText;
            if (topCard.getType() == CardType.NUMBER) {
                cardText = String.valueOf(topCard.getValue());
            } else {
                cardText = getDisplayTextForCardType(topCard.getType());
            }
            
            topCardLabel.setText("<html><center><div style='font-family:Arial; font-weight:bold;'>" + 
                                topCard.getColor().getDisplayName() + "<br><br>" + 
                                "<span style='font-size:20pt;'>" + cardText + "</span></div></center></html>");
        } else {
            topCardLabel.setText("Chưa có lá bài");
            topCardLabel.setOpaque(false);
            topCardLabel.setBackground(null);
            topCardLabel.setForeground(Color.BLACK);
        }
    }
    
    /**
     * Lấy text hiển thị cho từng loại lá bài
     * 
     * @param cardType Loại lá bài
     * @return Text hiển thị
     */
    private String getDisplayTextForCardType(CardType cardType) {
        switch (cardType) {
            case SKIP:
                return "Bỏ lượt";
            case REVERSE:
                return "Đảo chiều";
            case DRAW_TWO:
                return "+2";
            case WILD:
                return "Đổi màu";
            case WILD_DRAW_FOUR:
                return "+4";
            default:
                return "";
        }
    }
    
    /**
     * Hiển thị dialog chọn màu cho lá Wild
     * 
     * @return Màu được chọn
     */
    private CardColor showColorSelectionDialog() {
        Object[] options = {
            "Đỏ", "Xanh lá", "Xanh dương", "Vàng"
        };
        
        int choice = JOptionPane.showOptionDialog(
            this,
            "Chọn màu cho lá Wild:",
            "Chọn màu",
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
}
