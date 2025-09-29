package com.uno.gui;

import com.uno.client.UnoClientMain;
import com.uno.server.GameRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

/**
 * Class đại diện cho giao diện sảnh
 */
public class LobbyGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private final UnoClientMain clientMain;
    
    private JTable roomTable;
    private DefaultTableModel roomTableModel;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JButton refreshButton;
    private JPanel roomInfoPanel;
    private JLabel roomNameLabel;
    private JLabel hostNameLabel;
    private JLabel playerCountLabel;
    private JButton startGameButton;
    private JButton leaveRoomButton;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendButton;
    
    private boolean inRoom;
    
    public LobbyGUI(UnoClientMain clientMain) {
        this.clientMain = clientMain;
        this.inRoom = false;
        
        initComponents();
        setupListeners();
        
        setTitle("Uno Online - Sảnh chờ");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Hiển thị giao diện sảnh (không phải phòng chờ)
        showLobbyView();
    }
    
    /**
     * Khởi tạo các thành phần giao diện
     */
    private void initComponents() {
        // Panel chính với BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // === Sảnh ===
        
        // Panel bảng phòng
        JPanel roomListPanel = new JPanel(new BorderLayout(5, 5));
        roomListPanel.setBorder(BorderFactory.createTitledBorder("Danh sách phòng"));
        
        // Tạo bảng phòng
        String[] columnNames = {"ID", "Tên phòng", "Chủ phòng", "Số người chơi", "Trạng thái"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable = new JTable(roomTableModel);
        roomTable.getTableHeader().setReorderingAllowed(false);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Ẩn cột ID
        roomTable.getColumnModel().getColumn(0).setMinWidth(0);
        roomTable.getColumnModel().getColumn(0).setMaxWidth(0);
        roomTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Đặt chiều rộng cho các cột
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane roomScrollPane = new JScrollPane(roomTable);
        roomListPanel.add(roomScrollPane, BorderLayout.CENTER);
        
        // Panel button cho sảnh
        JPanel lobbyButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        createRoomButton = new JButton("Tạo phòng");
        joinRoomButton = new JButton("Tham gia");
        refreshButton = new JButton("Làm mới");
        
        lobbyButtonPanel.add(createRoomButton);
        lobbyButtonPanel.add(joinRoomButton);
        lobbyButtonPanel.add(refreshButton);
        
        roomListPanel.add(lobbyButtonPanel, BorderLayout.SOUTH);
        
        // === Phòng chờ ===
        
        // Panel thông tin phòng
        roomInfoPanel = new JPanel();
        roomInfoPanel.setLayout(new BoxLayout(roomInfoPanel, BoxLayout.Y_AXIS));
        roomInfoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin phòng"));
        
        roomNameLabel = new JLabel("Tên phòng: ");
        hostNameLabel = new JLabel("Chủ phòng: ");
        playerCountLabel = new JLabel("Số người chơi: 1/4");
        
        // Panel button cho phòng
        JPanel roomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        startGameButton = new JButton("Bắt đầu game");
        leaveRoomButton = new JButton("Rời phòng");
        
        roomButtonPanel.add(startGameButton);
        roomButtonPanel.add(leaveRoomButton);
        
        roomInfoPanel.add(roomNameLabel);
        roomInfoPanel.add(Box.createVerticalStrut(5));
        roomInfoPanel.add(hostNameLabel);
        roomInfoPanel.add(Box.createVerticalStrut(5));
        roomInfoPanel.add(playerCountLabel);
        roomInfoPanel.add(Box.createVerticalStrut(10));
        roomInfoPanel.add(roomButtonPanel);
        
        // Panel chat
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 0));
        chatField = new JTextField();
        sendButton = new JButton("Gửi");
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);
        
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
        
        // Thêm các panel vào panel chính
        mainPanel.add(roomListPanel, BorderLayout.CENTER);
        
        // Panel bên phải chứa thông tin phòng và chat
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(roomInfoPanel, BorderLayout.NORTH);
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);
        rightPanel.setPreferredSize(new Dimension(300, 600));
        
        // Thêm panel chính vào frame
        add(mainPanel);
    }
    
    /**
     * Thiết lập các listener cho các thành phần
     */
    private void setupListeners() {
        // Listener cho button tạo phòng
        createRoomButton.addActionListener(e -> {
            String roomName = JOptionPane.showInputDialog(this, "Nhập tên phòng:", "Tạo phòng", JOptionPane.QUESTION_MESSAGE);
            if (roomName != null && !roomName.trim().isEmpty()) {
                clientMain.createRoom(roomName.trim());
            }
        });
        
        // Listener cho button tham gia phòng
        joinRoomButton.addActionListener(e -> {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow >= 0) {
                String roomId = (String) roomTable.getValueAt(selectedRow, 0);
                String status = (String) roomTable.getValueAt(selectedRow, 4);
                
                if ("Đang chơi".equals(status)) {
                    JOptionPane.showMessageDialog(this, "Không thể tham gia phòng đang chơi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                clientMain.joinRoom(roomId);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để tham gia", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Listener cho button làm mới
        refreshButton.addActionListener(e -> {
            // Gửi yêu cầu danh sách phòng mới
            // Thông thường server sẽ tự cập nhật
        });
        
        // Listener cho button bắt đầu game
        startGameButton.addActionListener(e -> {
            clientMain.startGame();
        });
        
        // Listener cho button rời phòng
        leaveRoomButton.addActionListener(e -> {
            clientMain.leaveRoom();
            showLobbyView();
        });
        
        // Listener cho button gửi chat
        sendButton.addActionListener(e -> {
            String message = chatField.getText().trim();
            if (!message.isEmpty()) {
                clientMain.sendChatMessage(message);
                chatField.setText("");
            }
        });
        
        // Listener cho nhập chat và nhấn Enter
        chatField.addActionListener(e -> {
            String message = chatField.getText().trim();
            if (!message.isEmpty()) {
                clientMain.sendChatMessage(message);
                chatField.setText("");
            }
        });
        
        // Listener cho window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(LobbyGUI.this, 
                    "Bạn có chắc muốn thoát khỏi game?", "Xác nhận thoát", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    clientMain.disconnectFromServer();
                    dispose();
                    System.exit(0);
                }
            }
        });
    }
    
    /**
     * Cập nhật danh sách phòng
     * 
     * @param roomList Danh sách phòng
     */
    public void updateRoomList(List<Map<String, Object>> roomList) {
        // Xóa tất cả các hàng
        roomTableModel.setRowCount(0);
        
        // Thêm các phòng mới
        for (Map<String, Object> room : roomList) {
            String id = (String) room.get("id");
            String name = (String) room.get("name");
            String hostName = (String) room.get("hostName");
            int playerCount = (int) room.get("playerCount");
            boolean gameStarted = (boolean) room.get("gameStarted");
            
            String status = gameStarted ? "Đang chơi" : "Đang chờ";
            
            roomTableModel.addRow(new Object[] {id, name, hostName, playerCount + "/4", status});
        }
    }
    
    /**
     * Cập nhật thông tin phòng
     * 
     * @param room Thông tin phòng
     */
    public void updateRoomInfo(GameRoom room) {
        roomNameLabel.setText("Tên phòng: " + room.getName());
        hostNameLabel.setText("Chủ phòng: " + room.getHost().getName());
        playerCountLabel.setText("Số người chơi: " + room.getPlayerCount() + "/4");
        
        // Kiểm tra xem người chơi hiện tại có phải là chủ phòng không
        boolean isHost = room.getHost().getId().equals(clientMain.getPlayerId());
        startGameButton.setEnabled(isHost);
        
        // Chuyển sang giao diện phòng chờ
        showRoomView();
    }
    
    /**
     * Hiển thị giao diện sảnh
     */
    public void showLobbyView() {
        inRoom = false;
        createRoomButton.setEnabled(true);
        joinRoomButton.setEnabled(true);
        refreshButton.setEnabled(true);
        
        roomTable.setEnabled(true);
        
        // Vô hiệu hóa các thành phần của phòng chờ
        startGameButton.setEnabled(false);
        
        // Xóa nội dung chat
        chatArea.setText("");
    }
    
    /**
     * Hiển thị giao diện phòng chờ
     */
    public void showRoomView() {
        inRoom = true;
        createRoomButton.setEnabled(false);
        joinRoomButton.setEnabled(false);
        refreshButton.setEnabled(false);
        
        roomTable.setEnabled(false);
        
        // Kích hoạt các thành phần của phòng chờ
        leaveRoomButton.setEnabled(true);
    }
    
    /**
     * Thêm tin nhắn chat
     * 
     * @param message Tin nhắn chat
     */
    public void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}