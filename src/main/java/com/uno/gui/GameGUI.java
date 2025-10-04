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
import javax.swing.plaf.basic.BasicScrollBarUI;

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
        // Kích thước cửa sổ theo thiết kế
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Màu nền theo thiết kế
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
        
                // Panel thông tin ở trên với thiết kế hiện đại
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        infoPanel.setBackground(new Color(250, 250, 250));
        
        // Tạo gradient nhẹ cho panel
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(52, 152, 219)), // UNO Blue
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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
        
        // Center pile layout theo thiết kế mới hiện đại hơn
        topCardPanel = new JPanel();
        topCardPanel.setLayout(new BoxLayout(topCardPanel, BoxLayout.Y_AXIS));
        
        // Nền gradient nhẹ
        topCardPanel.setBackground(new Color(250, 250, 250));
        topCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(90, 95, 207, 30)),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        
        // Label cho lá bài ở giữa với thiết kế hiện đại
        JPanel topCardHeaderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topCardHeaderPanel.setBackground(new Color(250, 250, 250));
        topCardHeaderPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Sử dụng panel thay vì label để tạo header đẹp hơn
        JPanel headerLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerLabelPanel.setBackground(new Color(90, 95, 207));
        headerLabelPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel currentCardLabel = new JLabel("🎴 LÁ BÀI ĐANG CHƠI");
        currentCardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        currentCardLabel.setForeground(Color.WHITE);
        
        headerLabelPanel.add(currentCardLabel);
        topCardHeaderPanel.add(headerLabelPanel);
        
        // Tạo panel chứa lá bài với hiệu ứng 3D và bóng đổ
        JPanel topCardContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Vẽ đổ bóng nhẹ
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 8, getHeight() - 8, 15, 15);
                
                g2d.dispose();
            }
        };
        
        topCardContainer.setBackground(new Color(250, 250, 250));
        topCardContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 95, 207, 100), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
        ));
        topCardContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        topCardContainer.setMaximumSize(new Dimension(180, 240));
        
        // Label lá bài với hiệu ứng nổi
        topCardLabel = new JLabel("No Card") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ viền sáng
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Vẽ hiệu ứng 3D
                g2d.setColor(new Color(200, 200, 200, 100));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        topCardLabel.setPreferredSize(new Dimension(140, 200));
        topCardLabel.setMaximumSize(new Dimension(140, 200));
        topCardLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(90, 95, 207, 50), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        topCardLabel.setOpaque(false);
        topCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topCardLabel.setVerticalAlignment(SwingConstants.CENTER);
        topCardLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topCardLabel.setForeground(new Color(90, 95, 207));
        
        topCardContainer.add(topCardLabel, BorderLayout.CENTER);
        
        topCardPanel.add(Box.createVerticalGlue());
        topCardPanel.add(topCardHeaderPanel);
        topCardPanel.add(Box.createVerticalStrut(10));
        topCardPanel.add(topCardContainer);
        topCardPanel.add(Box.createVerticalGlue());
        
        // Clean other players panel
        JPanel otherPlayersPanel = new JPanel();
        otherPlayersPanel.setLayout(new BoxLayout(otherPlayersPanel, BoxLayout.Y_AXIS));
        otherPlayersPanel.setBackground(new Color(248, 249, 250));
        otherPlayersPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(222, 226, 230)),
                "NGƯỜI CHƠI KHÁC",
                0, 0,
                new Font("Arial", Font.BOLD, 14),
                new Color(90, 95, 207) // UI Accent color
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        otherPlayersPanel.setPreferredSize(new Dimension(220, 0));
        
        otherPlayerPanels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            // Thiết kế theo layout mẫu cho khu vực other players - cải tiến theo hình ảnh
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setBackground(new Color(248, 249, 250));
            
            // Border với hiệu ứng đổ bóng nhẹ
            playerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            playerPanel.setMaximumSize(new Dimension(200, 120));
            
            // Panel cho tên người chơi với gradient
            JPanel namePanel = new JPanel(new BorderLayout());
            namePanel.setBackground(new Color(90, 95, 207)); // UI Accent
            namePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            namePanel.setMaximumSize(new Dimension(200, 35));
            namePanel.setPreferredSize(new Dimension(200, 35));
            
            JLabel nameLabel = new JLabel("Player " + (i + 1));
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            nameLabel.setForeground(Color.WHITE);
            namePanel.add(nameLabel, BorderLayout.CENTER);
            
            // Thêm biểu tượng người chơi
            JLabel playerIcon = new JLabel("👤");
            playerIcon.setFont(new Font("Arial", Font.PLAIN, 14));
            playerIcon.setForeground(Color.WHITE);
            namePanel.add(playerIcon, BorderLayout.WEST);
            
            // Panel cho thông tin lá bài với thiết kế phẳng hơn
            JPanel cardInfoPanel = new JPanel();
            cardInfoPanel.setLayout(new BoxLayout(cardInfoPanel, BoxLayout.Y_AXIS));
            cardInfoPanel.setBackground(Color.WHITE);
            cardInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Hiển thị số lượng lá bài dưới dạng biểu tượng nhỏ với hiệu ứng
            JPanel cardIconsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            cardIconsPanel.setBackground(Color.WHITE);
            cardIconsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Thêm 7 icon nhỏ để biểu thị số lượng lá bài (mặc định ban đầu)
            for (int j = 0; j < 7; j++) {
                // Sử dụng panel nhỏ để biểu thị lá bài thay vì emoji
                JPanel miniCard = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        g2d.setColor(new Color(231, 76, 60));
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                        g2d.dispose();
                    }
                };
                miniCard.setPreferredSize(new Dimension(12, 15));
                cardIconsPanel.add(miniCard);
            }
            
            JLabel cardCountLabel = new JLabel("Số bài: 7");
            cardCountLabel.setFont(new Font("Arial", Font.BOLD, 13));
            cardCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardCountLabel.setForeground(new Color(52, 152, 219)); // UNO Blue
            
            JLabel unoLabel = new JLabel("UNO: Chưa");
            unoLabel.setFont(new Font("Arial", Font.BOLD, 12));
            unoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            unoLabel.setForeground(new Color(231, 76, 60)); // UNO Red
            
            cardInfoPanel.add(cardIconsPanel);
            cardInfoPanel.add(Box.createVerticalStrut(5));
            cardInfoPanel.add(cardCountLabel);
            cardInfoPanel.add(Box.createVerticalStrut(3));
            cardInfoPanel.add(unoLabel);
            
            playerPanel.add(namePanel);
            playerPanel.add(cardInfoPanel);
            
            otherPlayersPanel.add(playerPanel);
            if (i < 2) otherPlayersPanel.add(Box.createVerticalStrut(15));
            otherPlayerPanels.add(playerPanel);
        }
        
        otherPlayersPanel.add(Box.createVerticalGlue());
        
        // Thiết kế hiện đại cho khu vực bài của người chơi - cải tiến theo ảnh
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(52, 152, 219)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        playerPanel.setBackground(new Color(248, 249, 250));
        
        // Header cho khu vực bài người chơi
        JPanel playerHeaderPanel = new JPanel(new BorderLayout());
        playerHeaderPanel.setBackground(new Color(52, 152, 219)); // UNO Blue
        playerHeaderPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        JLabel playerCardLabel = new JLabel("🎮 BÀI CỦA BẠN");
        playerCardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        playerCardLabel.setForeground(Color.WHITE);
        
        // Thêm biểu tượng số lá bài
        JLabel cardCountLabel = new JLabel();
        cardCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cardCountLabel.setForeground(Color.WHITE);
        cardCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Cập nhật số lượng bài
        Timer updateCardCountTimer = new Timer(500, e -> {
            cardCountLabel.setText("Số bài: " + playerHand.size() + " ");
        });
        updateCardCountTimer.setRepeats(true);
        updateCardCountTimer.start();
        
        playerHeaderPanel.add(playerCardLabel, BorderLayout.WEST);
        playerHeaderPanel.add(cardCountLabel, BorderLayout.EAST);
        
        // Sử dụng WrapLayout với spacing tốt hơn theo thiết kế
        playerHandPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 8, 8));
        playerHandPanel.setBackground(new Color(255, 255, 255));
        
        // Tối ưu kích thước
        playerHandPanel.setPreferredSize(new Dimension(1000, 200));
        
        // Thiết kế scrollPane đẹp hơn
        JScrollPane handScrollPane = new JScrollPane(playerHandPanel);
        handScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        handScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        handScrollPane.setPreferredSize(new Dimension(1000, 180));
        handScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(233, 236, 239)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        handScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Tuỳ chỉnh thanh cuộn
        handScrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = super.createDecreaseButton(orientation);
                button.setBackground(new Color(248, 249, 250));
                button.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY));
                return button;
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = super.createIncreaseButton(orientation);
                button.setBackground(new Color(248, 249, 250));
                button.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY));
                return button;
            }
            
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(52, 152, 219); // UNO Blue
                this.trackColor = new Color(248, 249, 250);
            }
        });
        
        playerPanel.add(playerHeaderPanel, BorderLayout.NORTH);
        
        // Cải thiện panel chứa các nút hành động
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        actionPanel.setBackground(new Color(250, 250, 250));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(233, 236, 239)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Sử dụng UI Accent gradient theo thiết kế
        Color accentColor = new Color(90, 95, 207); // UI Accent color
        drawCardButton = createCleanButton("🃏 Rút bài", accentColor, Color.WHITE);
        endTurnButton = createCleanButton("✓ Kết thúc lượt", new Color(108, 117, 125), Color.WHITE);
        unoButton = createCleanButton("🔊 UNO!", new Color(231, 76, 60), Color.WHITE); // UNO Red
        
        actionPanel.add(drawCardButton);
        actionPanel.add(endTurnButton);
        actionPanel.add(unoButton);
        
        playerPanel.add(handScrollPane, BorderLayout.CENTER);
        playerPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Thiết kế panel chat bên phải theo layout mẫu
        JPanel chatPanel = new JPanel(new BorderLayout());
        
        // Header cho khu vực chat
        JPanel chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(new Color(90, 95, 207)); // UI Accent
        chatHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel chatLabel = new JLabel("💬 CHAT & THÔNG BÁO");
        chatLabel.setFont(new Font("Arial", Font.BOLD, 14));
        chatLabel.setForeground(Color.WHITE);
        chatHeaderPanel.add(chatLabel, BorderLayout.WEST);
        
        // Main chat panel
        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);
        chatPanel.setPreferredSize(new Dimension(250, 700));
        chatPanel.setBackground(new Color(250, 250, 250)); // Light background
        
        // Thiết kế khu vực chat với typography rõ ràng
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 13));
        chatArea.setBackground(Color.WHITE);
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        // Custom scrollpane với viền và màu sắc phù hợp
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(233, 236, 239)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Tùy chỉnh màu nền của scrollbar
        chatScrollPane.getVerticalScrollBar().setBackground(Color.WHITE);
        
        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 5));
        chatInputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        chatInputPanel.setBackground(new Color(245, 245, 245));
        
        // Thiết kế ô nhập chat hiện đại
        chatField = new JTextField();
        chatField.setFont(new Font("Arial", Font.PLAIN, 13));
        chatField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(90, 95, 207, 120), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Thiết kế nút gửi phù hợp với UI Accent
        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setPreferredSize(new Dimension(70, 35));
        sendButton.setBackground(new Color(90, 95, 207)); // UI Accent gradient
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        sendButton.setFocusPainted(false);
        
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
            
            // Turn indicator với glow effect theo thiết kế
            if (isMyTurn) {
                // Tạo glow effect với border màu sáng
                currentPlayerLabel.setText("⟹ LƯỢT CỦA BẠN (" + currentPlayerName + ") ⟸");
                currentPlayerLabel.setForeground(new Color(231, 76, 60)); // UNO Red
                currentPlayerLabel.setFont(currentPlayerLabel.getFont().deriveFont(Font.BOLD, 18));
                
                // Tạo hiệu ứng glow với border
                currentPlayerLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 10, 5, 10),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(231, 76, 60, 180), 2),
                        BorderFactory.createEmptyBorder(5, 15, 5, 15)
                    )
                ));
                
                // Tạo hiệu ứng nổi bật
                currentPlayerLabel.setOpaque(true);
                currentPlayerLabel.setBackground(new Color(231, 76, 60, 30));
            } else {
                currentPlayerLabel.setText("LƯỢT CỦA: " + currentPlayerName);
                currentPlayerLabel.setForeground(new Color(44, 62, 80)); // Wild/Black
                currentPlayerLabel.setFont(currentPlayerLabel.getFont().deriveFont(Font.PLAIN, 16));
                currentPlayerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                currentPlayerLabel.setOpaque(false);
                currentPlayerLabel.setBackground(null);
            }
            
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "TURN_DISPLAY", 
                              "Nguoi choi hien tai: " + currentPlayerName + 
                              " (ID: " + currentPlayerId + "), la luot cua toi: " + isMyTurn));
        } else {
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "ERROR", 
                              "Khong tim thay thong tin cua currentPlayerId = " + currentPlayerId));
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
        System.out.println(StringUtils.formatNetworkLog("CLIENT", "TURN_CONTROL", 
                "Cap nhat trang thai choi: canPlay = " + this.canPlay + " cho nguoi choi " + this.playerId));
        
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
                
                // Tìm namePanel và cardInfoPanel
                Component[] components = panel.getComponents();
                JPanel namePanel = null;
                JPanel cardInfoPanel = null;
                
                if (components.length >= 2) {
                    namePanel = (JPanel) components[0];
                    cardInfoPanel = (JPanel) components[1];
                }
                
                // Cập nhật tên người chơi
                if (namePanel != null) {
                    Component[] nameComps = namePanel.getComponents();
                    for (Component comp : nameComps) {
                        if (comp instanceof JLabel) {
                            ((JLabel) comp).setText(otherPlayer.getName());
                            break;
                        }
                    }
                }
                
                // Cập nhật thông tin lá bài
                if (cardInfoPanel != null) {
                    Component[] infoComps = cardInfoPanel.getComponents();
                    
                    JPanel cardIconsPanel = null;
                    JLabel cardCountLabel = null;
                    JLabel unoLabel = null;
                    
                    for (Component comp : infoComps) {
                        if (comp instanceof JPanel) {
                            cardIconsPanel = (JPanel) comp;
                        } else if (comp instanceof JLabel) {
                            JLabel label = (JLabel) comp;
                            String text = label.getText();
                            if (text != null && text.startsWith("Số bài:")) {
                                cardCountLabel = label;
                            } else if (text != null && text.startsWith("UNO:")) {
                                unoLabel = label;
                            }
                        }
                    }
                    
                    // Cập nhật số lượng lá bài trực quan
                    if (cardIconsPanel != null) {
                        cardIconsPanel.removeAll();
                        int handSize = otherPlayer.getHandSize();
                        for (int j = 0; j < handSize; j++) {
                            JLabel cardIcon = new JLabel("🃏");
                            cardIcon.setFont(new Font("Arial", Font.PLAIN, 12));
                            cardIconsPanel.add(cardIcon);
                        }
                        cardIconsPanel.revalidate();
                        cardIconsPanel.repaint();
                    }
                    
                    // Cập nhật thông tin văn bản
                    if (cardCountLabel != null) {
                        cardCountLabel.setText("Số bài: " + otherPlayer.getHandSize());
                        System.out.println(StringUtils.formatNetworkLog("CLIENT", "PLAYER_INFO", 
                                    "Cap nhat so bai cho " + otherPlayer.getName() + ": " + otherPlayer.getHandSize()));
                    }
                    
                    if (unoLabel != null) {
                        unoLabel.setText("UNO: " + (otherPlayer.hasCalledUno() ? "Có" : "Không"));
                        if (otherPlayer.hasCalledUno()) {
                            unoLabel.setForeground(new Color(231, 76, 60)); // UNO Red
                            unoLabel.setFont(unoLabel.getFont().deriveFont(Font.BOLD, 12));
                            
                            // Hiển thị toast thông báo
                            showToast(otherPlayer.getName() + " đã gọi UNO!", "warning");
                        } else {
                            unoLabel.setForeground(new Color(108, 117, 125)); // Gray
                            unoLabel.setFont(unoLabel.getFont().deriveFont(Font.PLAIN, 11));
                        }
                    }
                }
                
                panel.setVisible(true);
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "UI_UPDATE", 
                                  "Da cap nhat thong tin cho doi thu " + otherPlayer.getName() + 
                                  ", so la bai: " + otherPlayer.getHandSize()));
            } else {
                panel.setVisible(false);
            }
        }
        
        // Nếu game kết thúc, hiển thị thông báo với giao diện đẹp hơn
        if (gameState.isGameOver()) {
            String winnerId = gameState.getWinnerId();
            String winnerName = playerInfos.get(winnerId).getName();
            
            // Tạo thông báo chiến thắng với hiệu ứng đẹp
            String message = "<html><div style='text-align: center;'>" +
                    "<h1 style='color: #e74c3c; margin-bottom: 10px;'>🏆 CHIẾN THẮNG! 🏆</h1>" +
                    "<hr style='border: 1px solid #3498db;'>" +
                    "<h2 style='color: #2c3e50; margin: 15px 0;'>" + winnerName + "</h2>" +
                    "<p style='color: #7f8c8d; font-size: 14px;'>đã hoàn thành trò chơi UNO!</p>" +
                    "<p style='margin-top: 20px;'><i>Chúc mừng bạn!</i></p>" +
                    "</div></html>";
                    
            // Hiển thị dialog tùy chỉnh
            JOptionPane.showMessageDialog(
                this,
                message,
                "🎮 KẾT THÚC GAME",
                JOptionPane.INFORMATION_MESSAGE,
                null  // không dùng icon mặc định
            );
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
        System.out.println(StringUtils.formatNetworkLog("CLIENT", "GAME_STATE", 
                "Hien thi thi luot cua nguoi choi: P" + playerId + " (ID: " + playerId + "), isCurrentPlayer = " + isCurrentPlayer));

        for (Card card : playerHand) {
            JButton cardButton = createCardButton(card);
            final int cardIndex = playerHand.indexOf(card);
            
            // FIX: Chỉ bật lá bài có thể chơi được khi là lượt của người chơi này
            boolean canPlayThisCard = isCurrentPlayer && card.canPlayOn(topCard);
            cardButton.setEnabled(canPlayThisCard);
            
            System.out.println(StringUtils.formatNetworkLog("CLIENT", "CARD_ANALYSIS", 
                    "La bai: " + card + ", co the danh: " + canPlayThisCard));
            
            cardButton.addActionListener(e -> {
                System.out.println(StringUtils.formatNetworkLog("CLIENT", "CARD_ACTION", 
                    "Nguoi choi chon la bai, trang thai canPlay = " + canPlay + ", la bai = " + card));
                
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
                    System.out.println(StringUtils.formatNetworkLog("CLIENT", "INVALID_ACTION", 
                        "Khong the danh bai vi khong phai luot cua nguoi choi! canPlay = " + canPlay));
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
                        bgColor = new Color(231, 76, 60); // UNO Red #e74c3c
                        colorSymbol = "🔴";
                        break;
                    case GREEN: 
                        bgColor = new Color(39, 174, 96); // UNO Green #27ae60
                        colorSymbol = "🟢";
                        break;
                    case BLUE:
                        bgColor = new Color(52, 152, 219); // UNO Blue #3498db
                        colorSymbol = "🔵";
                        break;
                    case YELLOW:
                        bgColor = new Color(241, 196, 15); // UNO Yellow #f1c40f
                        colorSymbol = "🟡";
                        break;
                    default:
                        bgColor = new Color(44, 62, 80); // Wild/Black #2c3e50
                        colorSymbol = "⚫";
                        break;
                }
                
                // Vẽ hiệu ứng gradient cho nền
                GradientPaint gradientPaint = new GradientPaint(
                    0, 0, bgColor, 
                    width, height, bgColor.darker()
                );
                g2.setPaint(gradientPaint);
                g2.fillRoundRect(0, 0, width, height, 15, 15);
                
                // Thêm hiệu ứng nổi 3D
                // Phần sáng ở trên và bên trái
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(3, 3, width-3, 3);
                g2.drawLine(3, 3, 3, height-3);
                
                // Phần tối ở dưới và bên phải
                g2.setColor(new Color(0, 0, 0, 60));
                g2.drawLine(width-3, 3, width-3, height-3);
                g2.drawLine(3, height-3, width-3, height-3);
                
                // Viền trắng đẹp
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
                
                // Vẽ tên loại lá bài phía dưới (nếu không phải số) - Typography cải tiến
                if (card.getType() != CardType.NUMBER && !typeSymbol.isEmpty()) {
                    // Font đậm hơn cho card type theo thiết kế
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
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
        
        // Card hover effect (lift up) theo thiết kế
        cardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            private final Timer hoverTimer = new Timer(20, null);
            private int steps = 0;
            private final int maxSteps = 8;
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (cardButton.isEnabled()) {
                    // Animation nâng lá bài lên
                    hoverTimer.stop();
                    steps = 0;
                    
                    hoverTimer.addActionListener(e -> {
                        if (steps < maxSteps) {
                            steps++;
                            // Tăng kích thước và di chuyển lên trên
                            cardButton.setPreferredSize(new Dimension(
                                85 + (int)(5 * ((float)steps/maxSteps)), 
                                130 + (int)(5 * ((float)steps/maxSteps))
                            ));
                            // Thay đổi margin để tạo hiệu ứng nâng lên
                            ((JComponent)cardButton.getParent()).setBorder(
                                BorderFactory.createEmptyBorder(0, 0, steps, 0)
                            );
                            cardButton.revalidate();
                            cardButton.repaint();
                        } else {
                            hoverTimer.stop();
                        }
                    });
                    
                    hoverTimer.start();
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Animation hạ lá bài xuống
                hoverTimer.stop();
                steps = maxSteps;
                
                hoverTimer.addActionListener(e -> {
                    if (steps > 0) {
                        steps--;
                        // Giảm kích thước và hạ xuống
                        cardButton.setPreferredSize(new Dimension(
                            85 + (int)(5 * ((float)steps/maxSteps)), 
                            130 + (int)(5 * ((float)steps/maxSteps))
                        ));
                        // Thay đổi margin để tạo hiệu ứng hạ xuống
                        ((JComponent)cardButton.getParent()).setBorder(
                            BorderFactory.createEmptyBorder(0, 0, steps, 0)
                        );
                        cardButton.revalidate();
                        cardButton.repaint();
                    } else {
                        hoverTimer.stop();
                        // Trả về kích thước ban đầu
                        cardButton.setPreferredSize(new Dimension(85, 130));
                        ((JComponent)cardButton.getParent()).setBorder(null);
                    }
                });
                
                hoverTimer.start();
            }
        });
        return cardButton;
    }
    
    /**
     * Hiển thị hướng dẫn ngắn gọn cho người chơi mới với thiết kế hiện đại
     */
    private void showGameInstructions() {
        String instructions = "<html><div style='width: 500px; font-family: Arial, sans-serif;'>" +
                "<div style='background: linear-gradient(135deg, #e74c3c, #c0392b); color: white; padding: 15px; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0; text-align: center; text-shadow: 1px 1px 2px rgba(0,0,0,0.3);'>🎮 HƯỚNG DẪN CHƠI UNO</h1>" +
                "</div>" +
                
                "<div style='background-color: #ffffff; padding: 20px; border-left: 1px solid #eee; border-right: 1px solid #eee; box-shadow: 0 2px 5px rgba(0,0,0,0.1) inset;'>" +
                
                "<div style='background: linear-gradient(135deg, #3498db, #2980b9); color: white; padding: 10px 15px; border-radius: 8px; margin-bottom: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h3 style='margin: 0;'>🎯 MỤC TIÊU</h3>" +
                "</div>" +
                "<p style='margin-left: 15px; line-height: 1.5; font-size: 14px;'>Đánh hết lá bài trên tay trước đối thủ để giành chiến thắng!</p>" +
                
                "<div style='background: linear-gradient(135deg, #2ecc71, #27ae60); color: white; padding: 10px 15px; border-radius: 8px; margin: 20px 0 15px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h3 style='margin: 0;'>📋 LUẬT CƠ BẢN</h3>" +
                "</div>" +
                "<ul style='margin-top: 10px; padding-left: 30px; line-height: 1.6; font-size: 14px;'>" +
                "<li>Đánh lá bài <b>cùng màu</b> hoặc <b>cùng số/ký hiệu</b> với lá trên cùng</li>" +
                "<li>Lá <b>Đổi màu (★)</b> và <b>+4</b> có thể đánh bất kỳ lúc nào</li>" +
                "<li>Khi còn 2 lá, bạn <b>phải</b> nhấn nút <b>'UNO!'</b> để tuyên bố</li>" +
                "<li>Nếu không tuyên bố UNO, bạn có thể bị phạt rút 2 lá bài</li>" +
                "</ul>" +
                
                "<div style='background: linear-gradient(135deg, #f39c12, #e67e22); color: white; padding: 10px 15px; border-radius: 8px; margin: 20px 0 15px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h3 style='margin: 0;'>🃏 LÁ ĐẶC BIỆT</h3>" +
                "</div>" +
                
                "<table style='width: 100%; border-collapse: separate; border-spacing: 0; margin: 10px 0; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef; width: 70px;'><div style='background-color: #e74c3c; color: white; width: 40px; height: 40px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; box-shadow: 0 2px 3px rgba(0,0,0,0.1);'>⊘</div></td>" +
                "<td style='padding: 12px; border-bottom: 1px solid #e9ecef;'><b style='color: #e74c3c;'>Bỏ lượt:</b> Người kế tiếp bị mất lượt đi</td>" +
                "</tr>" +
                
                "<tr style='background-color: #ffffff;'>" +
                "<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef;'><div style='background-color: #3498db; color: white; width: 40px; height: 40px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; box-shadow: 0 2px 3px rgba(0,0,0,0.1);'>⤸</div></td>" +
                "<td style='padding: 12px; border-bottom: 1px solid #e9ecef;'><b style='color: #3498db;'>Đảo chiều:</b> Thay đổi hướng lượt chơi</td>" +
                "</tr>" +
                
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef;'><div style='background-color: #f1c40f; color: white; width: 40px; height: 40px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; box-shadow: 0 2px 3px rgba(0,0,0,0.1);'>+2</div></td>" +
                "<td style='padding: 12px; border-bottom: 1px solid #e9ecef;'><b style='color: #f1c40f;'>Rút 2:</b> Người kế tiếp phải rút 2 lá và mất lượt</td>" +
                "</tr>" +
                
                "<tr style='background-color: #ffffff;'>" +
                "<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef;'><div style='background-color: #9b59b6; color: white; width: 40px; height: 40px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; box-shadow: 0 2px 3px rgba(0,0,0,0.1);'>★</div></td>" +
                "<td style='padding: 12px; border-bottom: 1px solid #e9ecef;'><b style='color: #9b59b6;'>Đổi màu:</b> Chọn màu tiếp theo cho lượt chơi</td>" +
                "</tr>" +
                
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 12px 15px; border-bottom: 1px solid #e9ecef;'><div style='background-color: #2c3e50; color: white; width: 40px; height: 40px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; box-shadow: 0 2px 3px rgba(0,0,0,0.1);'>+4</div></td>" +
                "<td style='padding: 12px; border-bottom: 1px solid #e9ecef;'><b style='color: #2c3e50;'>Đổi màu +4:</b> Chọn màu và người kế tiếp rút 4 lá</td>" +
                "</tr>" +
                "</table>" +
                
                "<div style='background: linear-gradient(135deg, #9b59b6, #8e44ad); color: white; padding: 10px 15px; border-radius: 8px; margin: 20px 0 15px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<h3 style='margin: 0;'>💡 MẸO CHƠI</h3>" +
                "</div>" +
                "<ul style='margin-top: 10px; padding-left: 30px; line-height: 1.6; font-size: 14px;'>" +
                "<li>Di chuột qua lá bài để xem thông tin chi tiết và hiệu ứng</li>" +
                "<li>Theo dõi số lượng bài của đối thủ để đưa ra chiến thuật tốt nhất</li>" +
                "<li>Giữ lại lá đặc biệt để phòng thủ khi đối thủ sắp UNO</li>" +
                "<li>Nếu đối thủ quên gọi UNO, hãy nhanh chóng thông báo</li>" +
                "</ul>" +
                "</div>" +
                
                "<div style='background: linear-gradient(135deg, #5A5FCF, #3F4599); color: white; padding: 15px; border-radius: 0 0 10px 10px; text-align: center; font-weight: bold; text-shadow: 1px 1px 2px rgba(0,0,0,0.2);'>" +
                "👑 CHÚC BẠN CHIẾN THẮNG! 🏆" +
                "</div>" +
                
                "</div></html>";
        
        // Tạo JDialog tùy chỉnh với hiệu ứng đổ bóng
        final JDialog dialog = new JDialog(this, "HƯỚNG DẪN UNO", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(245, 245, 245));
        
        // Panel chính với viền và đổ bóng
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // Hiển thị nội dung hướng dẫn
        JLabel instructionLabel = new JLabel(instructions);
        JScrollPane scrollPane = new JScrollPane(instructionLabel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(90, 95, 207, 80), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Nút đóng với hiệu ứng hover
        JButton closeButton = new JButton("ĐÃ HIỂU! BẮT ĐẦU CHƠI");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(90, 95, 207)); // UI Accent
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover cho nút
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(new Color(106, 111, 212));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(new Color(90, 95, 207));
            }
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        // Hiển thị dialog trực tiếp không dùng hiệu ứng opacity
        // vì dialog được trang trí không thể đặt opacity
        dialog.setVisible(true);
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
            
            // Nâng cấp viền cho lá bài trên cùng - thêm hiệu ứng 3D
            topCardLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 3),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    )
                ),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
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
     * Hiển thị dialog chọn màu cho lá Wild với thiết kế hiện đại
     * 
     * @return Màu được chọn
     */
    private CardColor showColorSelectionDialog() {
        // Panel chính với layout mạnh mẽ
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Tạo header cho dialog
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(90, 95, 207), 
                    getWidth(), 0, new Color(123, 128, 235)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerPanel.setOpaque(false);
        
        // Tạo tiêu đề với thiết kế hiện đại
        JLabel titleLabel = new JLabel("CHỌN MÀU CHO LÁ BÀI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Thêm icon wild card vào header
        JLabel wildIcon = new JLabel("★", SwingConstants.CENTER);
        wildIcon.setFont(new Font("Arial", Font.BOLD, 24));
        wildIcon.setForeground(Color.WHITE);
        headerPanel.add(wildIcon, BorderLayout.WEST);
        
        // Tạo các nút màu sắc với thiết kế hiện đại
        JPanel colorGrid = new JPanel(new GridLayout(2, 2, 15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với viền bo tròn
                g2.setColor(new Color(255, 255, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        colorGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        colorGrid.setOpaque(false);
        
        // Tạo các nút màu với thiết kế hiện đại và hover effect
        JButton redButton = createAdvancedColorButton("ĐỎ", new Color(231, 76, 60), Color.WHITE, "♥");
        JButton greenButton = createAdvancedColorButton("XANH LÁ", new Color(39, 174, 96), Color.WHITE, "♣");
        JButton blueButton = createAdvancedColorButton("XANH DƯƠNG", new Color(52, 152, 219), Color.WHITE, "♠");
        JButton yellowButton = createAdvancedColorButton("VÀNG", new Color(241, 196, 15), Color.BLACK, "★");
        
        colorGrid.add(redButton);
        colorGrid.add(greenButton);
        colorGrid.add(blueButton);
        colorGrid.add(yellowButton);
        
        // Tạo hình ảnh hiển thị preview của lá bài wild với màu đã chọn
        JLabel previewLabel = new JLabel("", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(100, 30));
        previewLabel.setFont(new Font("Arial", Font.BOLD, 14));
        previewLabel.setForeground(new Color(90, 95, 207));
        previewLabel.setText("Chọn một màu...");
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(colorGrid, BorderLayout.CENTER);
        mainPanel.add(previewLabel, BorderLayout.SOUTH);
        
        final CardColor[] selectedColor = {null};
        
        // Add action listeners với hiệu ứng
        redButton.addActionListener(e -> {
            selectedColor[0] = CardColor.RED;
            previewLabel.setText("Đã chọn: ĐỎ");
            previewLabel.setForeground(new Color(231, 76, 60));
            
            Timer timer = new Timer(800, evt -> {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        greenButton.addActionListener(e -> {
            selectedColor[0] = CardColor.GREEN;
            previewLabel.setText("Đã chọn: XANH LÁ");
            previewLabel.setForeground(new Color(39, 174, 96));
            
            Timer timer = new Timer(800, evt -> {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        blueButton.addActionListener(e -> {
            selectedColor[0] = CardColor.BLUE;
            previewLabel.setText("Đã chọn: XANH DƯƠNG");
            previewLabel.setForeground(new Color(52, 152, 219));
            
            Timer timer = new Timer(800, evt -> {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        yellowButton.addActionListener(e -> {
            selectedColor[0] = CardColor.YELLOW;
            previewLabel.setText("Đã chọn: VÀNG");
            previewLabel.setForeground(new Color(241, 196, 15));
            
            Timer timer = new Timer(800, evt -> {
                Window window = SwingUtilities.getWindowAncestor(mainPanel);
                window.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
        
        // Tạo dialog hiện đại
        JDialog dialog = new JDialog(this, "Chọn màu", true);
        dialog.setContentPane(mainPanel);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        return selectedColor[0];
    }
    
    /**
     * Tạo nút màu sắc nâng cao cho dialog chọn màu
     * 
     * @param text Văn bản trên nút
     * @param bgColor Màu nền
     * @param textColor Màu chữ
     * @param symbol Biểu tượng đại diện
     * @return Nút màu đã tạo
     */
    private JButton createAdvancedColorButton(String text, Color bgColor, Color textColor, String symbol) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với hiệu ứng gradient
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(
                        0, 0, bgColor.darker(), 
                        0, getHeight(), bgColor
                    );
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(
                        0, 0, bgColor, 
                        0, getHeight(), bgColor.brighter()
                    );
                } else {
                    gradient = new GradientPaint(
                        0, 0, bgColor, 
                        0, getHeight(), bgColor.darker()
                    );
                }
                
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Vẽ viền sáng
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                // Vẽ hiệu ứng highlight
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(3, 3, getWidth()-6, 20, 10, 10);
                }
                
                // Vẽ text
                g2.setColor(textColor);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int x = (getWidth() - textWidth) / 2;
                int y = getHeight() / 2 + 15;
                g2.drawString(text, x, y);
                
                // Vẽ symbol
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                FontMetrics fmSymbol = g2.getFontMetrics();
                int symbolWidth = fmSymbol.stringWidth(symbol);
                int symbolX = (getWidth() - symbolWidth) / 2;
                int symbolY = getHeight() / 2 - 10;
                g2.drawString(symbol, symbolX, symbolY);
                
                g2.dispose();
            }
        };
        
        button.setPreferredSize(new Dimension(140, 90));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    /**
     * Thêm tin nhắn vào khu vực chat với định dạng cải tiến
     * 
     * @param message Tin nhắn
     */
    public void addChatMessage(String message) {
        // Thêm timestamp cho mỗi tin nhắn
        String timestamp = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
        String formattedMessage = "[" + timestamp + "] " + message + "\n";
        
        // Thêm tin nhắn và tự động cuộn xuống
        chatArea.append(formattedMessage);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        
        // Hiển thị toast notification cho tin nhắn mới từ người chơi khác
        if (message.contains(":") && !message.startsWith("Bạn:")) {
            String username = message.substring(0, message.indexOf(":"));
            String content = message.substring(message.indexOf(":") + 1).trim();
            if (!content.isEmpty()) {
                showToast(username + " đã gửi tin nhắn", "info");
            }
        }
    }
    
    /**
     * Hiển thị thông báo toast cho các sự kiện quan trọng
     * 
     * @param message Nội dung thông báo
     * @param type Loại thông báo (info, warning, error, success)
     */
    public void showToast(String message, String type) {
        // Tạo JPanel cho toast notification với hiệu ứng cao cấp
        JPanel toastPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với góc bo tròn
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Thêm hiệu ứng gradient nhẹ
                Paint oldPaint = g2.getPaint();
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 50), 
                                             0, getHeight(), new Color(0, 0, 0, 30)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setPaint(oldPaint);
                
                g2.dispose();
            }
        };
        toastPanel.setLayout(new BorderLayout());
        
        // Chọn màu background theo loại thông báo
        Color bgColor;
        String iconText;
        switch (type) {
            case "success":
                bgColor = new Color(39, 174, 96); // UNO Green
                iconText = "✓ ";
                break;
            case "error":
                bgColor = new Color(231, 76, 60); // UNO Red
                iconText = "✗ ";
                break;
            case "warning":
                bgColor = new Color(241, 196, 15); // UNO Yellow
                iconText = "⚠ ";
                break;
            default: // info
                bgColor = new Color(90, 95, 207); // UI Accent color
                iconText = "ℹ ";
                break;
        }
        
        // Tạo icon panel riêng biệt với hiệu ứng
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 18));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        
        // Tạo label với thông báo
        JLabel toastLabel = new JLabel(message);
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toastLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 20));
        
        toastPanel.add(iconPanel, BorderLayout.WEST);
        toastPanel.add(toastLabel, BorderLayout.CENTER);
        toastPanel.setBackground(bgColor);
        
        // Thêm hiệu ứng đổ bóng
        toastPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 10, 5), // Margin cho đổ bóng
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 3)
            )
        ));
        
        // Panel đổ bóng chính
        JPanel shadowPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ đổ bóng
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth() - 8, getHeight() - 8, 20, 20);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        shadowPanel.setOpaque(false);
        shadowPanel.add(toastPanel, BorderLayout.CENTER);
        
        // Đặt vị trí của toast ở phía dưới trung tâm màn hình
        final JWindow toastWindow = new JWindow();
        toastWindow.setBackground(new Color(0, 0, 0, 0)); // Transparent
        toastWindow.setContentPane(shadowPanel);
        toastWindow.pack();
        
        // Vị trí theo thiết kế - phía trên bên phải
        int x = getX() + getWidth() - toastWindow.getWidth() - 20;
        int y = getY() + 80; // Khoảng cách từ đầu màn hình
        
        toastWindow.setLocation(x, y);
        
        // Hiệu ứng animation hiển thị
        toastWindow.setOpacity(0.0f);
        toastWindow.setVisible(true);
        
        // Hiệu ứng fade in
        Timer fadeInTimer = new Timer(20, null);
        final float[] opacity = {0.0f};
        
        fadeInTimer.addActionListener(e -> {
            opacity[0] += 0.1f;
            if (opacity[0] > 1.0f) {
                opacity[0] = 1.0f;
                fadeInTimer.stop();
                
                // Sau khi hiển thị đầy đủ, đợi 3s rồi fade out
                new Timer(3000, evt -> {
                    // Hiệu ứng fade out
                    Timer fadeOutTimer = new Timer(20, null);
                    fadeOutTimer.addActionListener(evt2 -> {
                        opacity[0] -= 0.1f;
                        if (opacity[0] < 0.0f) {
                            opacity[0] = 0.0f;
                            fadeOutTimer.stop();
                            toastWindow.dispose();
                        } else {
                            toastWindow.setOpacity(opacity[0]);
                        }
                    });
                    fadeOutTimer.start();
                }).start();
            } else {
                toastWindow.setOpacity(opacity[0]);
            }
        });
        fadeInTimer.start();
    }
    
    /**
     * Hiển thị thông báo toast với thời gian tùy chỉnh
     * 
     * @param message Nội dung thông báo
     * @param type Loại thông báo (info, warning, error, success)
     * @param durationMs Thời gian hiển thị (mili giây)
     */
    public void showToast(String message, String type, int durationMs) {
        // Tạo JPanel cho toast notification với hiệu ứng cao cấp
        JPanel toastPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với góc bo tròn
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Thêm hiệu ứng gradient nhẹ
                Paint oldPaint = g2.getPaint();
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 50), 
                                             0, getHeight(), new Color(0, 0, 0, 30)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setPaint(oldPaint);
                
                g2.dispose();
            }
        };
        toastPanel.setLayout(new BorderLayout());
        
        // Chọn màu background theo loại thông báo
        Color bgColor;
        String iconText;
        switch (type) {
            case "success":
                bgColor = new Color(39, 174, 96); // UNO Green
                iconText = "✓ ";
                break;
            case "error":
                bgColor = new Color(231, 76, 60); // UNO Red
                iconText = "✗ ";
                break;
            case "warning":
                bgColor = new Color(241, 196, 15); // UNO Yellow
                iconText = "⚠ ";
                break;
            default: // info
                bgColor = new Color(90, 95, 207); // UI Accent color
                iconText = "ℹ ";
                break;
        }
        
        // Tạo icon panel riêng biệt với hiệu ứng
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 18));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        
        // Tạo label với thông báo
        JLabel toastLabel = new JLabel(message);
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toastLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 20));
        
        toastPanel.add(iconPanel, BorderLayout.WEST);
        toastPanel.add(toastLabel, BorderLayout.CENTER);
        toastPanel.setBackground(bgColor);
        
        // Thêm hiệu ứng đổ bóng
        toastPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 10, 5), // Margin cho đổ bóng
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 3)
            )
        ));
        
        // Panel đổ bóng chính
        JPanel shadowPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ đổ bóng
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, getWidth() - 8, getHeight() - 8, 20, 20);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        shadowPanel.setOpaque(false);
        shadowPanel.add(toastPanel, BorderLayout.CENTER);
        
        // Đặt vị trí của toast ở phía dưới trung tâm màn hình
        final JWindow toastWindow = new JWindow();
        toastWindow.setBackground(new Color(0, 0, 0, 0)); // Transparent
        toastWindow.setContentPane(shadowPanel);
        toastWindow.pack();
        
        // Vị trí theo thiết kế - phía trên bên phải
        int x = getX() + getWidth() - toastWindow.getWidth() - 20;
        int y = getY() + 80; // Khoảng cách từ đầu màn hình
        
        toastWindow.setLocation(x, y);
        
        // Hiệu ứng animation hiển thị
        toastWindow.setOpacity(0.0f);
        toastWindow.setVisible(true);
        
        // Hiệu ứng fade in
        Timer fadeInTimer2 = new Timer(20, null);
        final float[] opacity2 = {0.0f};
        
        fadeInTimer2.addActionListener(e -> {
            opacity2[0] += 0.1f;
            if (opacity2[0] > 1.0f) {
                opacity2[0] = 1.0f;
                fadeInTimer2.stop();
                
                // Sau khi hiển thị đầy đủ, đợi thời gian chỉ định rồi fade out
                new Timer(durationMs, evt -> {
                    // Hiệu ứng fade out
                    Timer fadeOutTimer = new Timer(20, null);
                    fadeOutTimer.addActionListener(evt2 -> {
                        opacity2[0] -= 0.1f;
                        if (opacity2[0] < 0.0f) {
                            opacity2[0] = 0.0f;
                            fadeOutTimer.stop();
                            toastWindow.dispose();
                        } else {
                            toastWindow.setOpacity(opacity2[0]);
                        }
                    });
                    fadeOutTimer.start();
                }).start();
            } else {
                toastWindow.setOpacity(opacity2[0]);
            }
        });
        fadeInTimer2.start();
        
        // Di chuyển toast xuống phía dưới
        Point toastLocation = toastWindow.getLocation();
        toastWindow.setLocation(toastLocation.x, toastLocation.y + 200);
        
        // Hiển thị toast
        toastWindow.setVisible(true);
        
        // Animation fade in
        Timer fadeInTimer = new Timer(20, null);
        final float[] opacity = {0.0f};
        
        fadeInTimer.addActionListener(e -> {
            opacity[0] += 0.05f;
            if (opacity[0] > 1.0f) {
                opacity[0] = 1.0f;
                fadeInTimer.stop();
                
                // Sau khi hiển thị một thời gian, thì fade out
                new Timer(3000, evt -> {
                    Timer fadeOutTimer = new Timer(20, null);
                    fadeOutTimer.addActionListener(fadeOutEvent -> {
                        opacity[0] -= 0.05f;
                        if (opacity[0] < 0.0f) {
                            opacity[0] = 0.0f;
                            fadeOutTimer.stop();
                            toastWindow.dispose();
                        } else {
                            toastWindow.setOpacity(opacity[0]);
                        }
                    });
                    fadeOutTimer.start();
                }).start();
            } else {
                toastWindow.setOpacity(opacity[0]);
            }
        });
        
        fadeInTimer.start();
    }
    
    /**
     * Creates a clean, modern button with consistent styling
     */
    /**
     * Tạo nút với thiết kế hiện đại, bo tròn và hiệu ứng hover
     */
    private JButton createCleanButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 40));
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        
        // Tạo viền bo tròn và đệm
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        
        // Tùy chỉnh giao diện nút
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                
                int width = c.getWidth();
                int height = c.getHeight();
                
                // Gradient background
                GradientPaint gradient;
                if (model.isPressed()) {
                    gradient = new GradientPaint(0, 0, backgroundColor.darker(), 0, height, backgroundColor);
                } else {
                    gradient = new GradientPaint(0, 0, backgroundColor, 0, height, backgroundColor.darker());
                }
                
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, width - 1, height - 1, 12, 12);
                
                // Draw border
                g2.setColor(model.isPressed() ? backgroundColor.darker().darker() : backgroundColor.darker());
                g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);
                
                // Add subtle highlight at top (3D effect)
                if (!model.isPressed()) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawLine(3, 3, width - 4, 3);
                }
                
                g2.dispose();
                super.paint(g, c);
            }
        });
        
        // Hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(backgroundColor.darker());
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
    }
}
