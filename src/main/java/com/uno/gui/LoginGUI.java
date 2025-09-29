package com.uno.gui;

import com.uno.client.UnoClientMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Class đại diện cho giao diện đăng nhập
 */
public class LoginGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private final UnoClientMain clientMain;
    
    private JTextField serverAddressField;
    private JTextField playerNameField;
    private JButton connectButton;
    private JLabel statusLabel;
    
    public LoginGUI(UnoClientMain clientMain) {
        this.clientMain = clientMain;
        
        initComponents();
        setupListeners();
        
        setTitle("Uno Online - Đăng nhập");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Khởi tạo các thành phần giao diện
     */
    private void initComponents() {
        // Panel chính với BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel form với GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        
        // Label cho địa chỉ server
        JLabel serverAddressLabel = new JLabel("Địa chỉ server:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(serverAddressLabel, gbc);
        
        // Text field cho địa chỉ server
        serverAddressField = new JTextField("localhost");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(serverAddressField, gbc);
        
        // Label cho tên người chơi
        JLabel playerNameLabel = new JLabel("Tên người chơi:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(playerNameLabel, gbc);
        
        // Text field cho tên người chơi
        playerNameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(playerNameField, gbc);
        
        // Button kết nối
        connectButton = new JButton("Kết nối");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(connectButton);
        
        // Label trạng thái
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Thêm các panel vào panel chính
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        
        // Thêm panel chính vào frame
        add(mainPanel);
    }
    
    /**
     * Thiết lập các listener cho các thành phần
     */
    private void setupListeners() {
        // Listener cho button kết nối
        connectButton.addActionListener(e -> {
            String serverAddress = serverAddressField.getText().trim();
            String playerName = playerNameField.getText().trim();
            
            if (serverAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập địa chỉ server", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên người chơi", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Vô hiệu hóa button kết nối và hiển thị trạng thái đang kết nối
            connectButton.setEnabled(false);
            statusLabel.setText("Đang kết nối đến server...");
            
            // Kết nối đến server
            new Thread(() -> clientMain.connectToServer(serverAddress, playerName)).start();
        });
        
        // Listener cho window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clientMain.disconnectFromServer();
            }
        });
    }
    
    /**
     * Cập nhật trạng thái kết nối
     * 
     * @param status Trạng thái kết nối
     */
    public void setStatusText(String status) {
        statusLabel.setText(status);
        connectButton.setEnabled(true);
    }
}