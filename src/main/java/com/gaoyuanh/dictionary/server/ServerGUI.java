package com.gaoyuanh.dictionary.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Graphical User Interface for the Dictionary Server.
 * Displays server status and information about connected clients.
 */
public class ServerGUI extends JFrame {
    private final JLabel statusLabel;
    private final JLabel portLabel;
    private final JLabel clientCountLabel;
    private final JTextArea logArea;
    private final JButton startStopButton;
    private final AtomicInteger connectedClients = new AtomicInteger(0);
    
    private DictionaryServer server;
    private int port;
    private String dictionaryFilePath;
    private boolean isRunning = false;

    /**
     * Constructor initializes the GUI components
     * 
     * @param port Initial port to use
     * @param dictionaryFilePath Path to the dictionary file
     */
    public ServerGUI(int port, String dictionaryFilePath) {
        this.port = port;
        this.dictionaryFilePath = dictionaryFilePath;
        
        // Set up the window
        setTitle("Dictionary Server");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set UI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("Button.background", new Color(240, 240, 240));
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Label.foreground", Color.BLACK);
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Set layout
        setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create server controls panel
        JPanel controlsPanel = createControlsPanel();
        add(controlsPanel, BorderLayout.CENTER);
        
        // Create log panel
        JPanel logPanel = createLogPanel();
        add(logPanel, BorderLayout.SOUTH);
        
        // Initialize components that need to be accessed from methods
        statusLabel = new JLabel("Server Stopped");
        statusLabel.setForeground(Color.RED);
        
        portLabel = new JLabel("Port: " + port);
        
        clientCountLabel = new JLabel("Connected Clients: 0");
        
        startStopButton = new JButton("Start Server");
        startStopButton.addActionListener(e -> toggleServer());
        
        // Add components to the header
        headerPanel.add(statusLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(portLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(clientCountLabel);
        headerPanel.add(Box.createHorizontalGlue());
        headerPanel.add(startStopButton);
        
        // Create log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Window close handling
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (server != null && isRunning) {
                    stopServer();
                }
            }
        });
    }
    
    /**
     * Creates the header panel
     * 
     * @return The header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * Creates the server controls panel
     * 
     * @return The server controls panel
     */
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Server Configuration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portPanel.setBackground(Color.WHITE);
        JLabel portPanelLabel = new JLabel("Port Number:");
        JTextField portField = new JTextField(String.valueOf(port), 10);
        portPanel.add(portPanelLabel);
        portPanel.add(portField);
        portPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBackground(Color.WHITE);
        JLabel fileLabel = new JLabel("Dictionary File:");
        JTextField fileField = new JTextField(dictionaryFilePath, 30);
        filePanel.add(fileLabel);
        filePanel.add(fileField);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton applyButton = new JButton("Apply Settings");
        applyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyButton.addActionListener(e -> {
            // Update settings only if server is not running
            if (!isRunning) {
                try {
                    int newPort = Integer.parseInt(portField.getText().trim());
                    if (newPort >= 1 && newPort <= 65535) {
                        port = newPort;
                        portLabel.setText("Port: " + port);
                        dictionaryFilePath = fileField.getText().trim();
                        logMessage("Settings updated: Port=" + port + ", File=" + dictionaryFilePath);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Port must be between 1 and 65535",
                            "Invalid Port",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid port number",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cannot change settings while server is running",
                    "Server Running",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(portPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(filePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(applyButton);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Creates the log panel
     * 
     * @return The log panel
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Server Log");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * Toggles the server between started and stopped states
     */
    private void toggleServer() {
        if (isRunning) {
            stopServer();
        } else {
            startServer();
        }
    }
    
    /**
     * Starts the server
     */
    private void startServer() {
        try {
            // Create a custom output stream to redirect System.out to log area
            CustomOutputStream outputStream = new CustomOutputStream(logArea);
            System.setOut(new java.io.PrintStream(outputStream, true));
            System.setErr(new java.io.PrintStream(outputStream, true));
            
            // Start the server on a separate thread to keep GUI responsive
            new Thread(() -> {
                try {
                    server = new DictionaryServer(port, dictionaryFilePath);
                    server.setClientConnectionListener(this::updateClientCount);
                    boolean started = server.startServer();
                    
                    if (!started) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                "Failed to start server",
                                "Server Error",
                                JOptionPane.ERROR_MESSAGE);
                            updateServerStatus(false);
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                            "Error starting server: " + e.getMessage(),
                            "Server Error",
                            JOptionPane.ERROR_MESSAGE);
                        logMessage("Error starting server: " + e.getMessage());
                        updateServerStatus(false);
                    });
                }
            }).start();
            
            updateServerStatus(true);
            logMessage("Server starting on port " + port + "...");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error starting server: " + e.getMessage(),
                "Server Error",
                JOptionPane.ERROR_MESSAGE);
            logMessage("Error starting server: " + e.getMessage());
        }
    }
    
    /**
     * Stops the server
     */
    private void stopServer() {
        if (server != null) {
            server.stopServer();
            server = null;
            logMessage("Server stopped");
            updateServerStatus(false);
            // Reset client count
            connectedClients.set(0);
            updateClientCountLabel();
        }
    }
    
    /**
     * Updates client count and the display
     * 
     * @param delta Change in client count (+1 for connect, -1 for disconnect)
     */
    public void updateClientCount(int delta) {
        connectedClients.addAndGet(delta);
        SwingUtilities.invokeLater(this::updateClientCountLabel);
    }
    
    /**
     * Updates the client count label
     */
    private void updateClientCountLabel() {
        clientCountLabel.setText("Connected Clients: " + connectedClients.get());
    }
    
    /**
     * Updates the server status in the UI
     * 
     * @param running Whether the server is running
     */
    private void updateServerStatus(boolean running) {
        isRunning = running;
        SwingUtilities.invokeLater(() -> {
            if (running) {
                statusLabel.setText("Server Running");
                statusLabel.setForeground(new Color(0, 128, 0)); // Dark green
                startStopButton.setText("Stop Server");
            } else {
                statusLabel.setText("Server Stopped");
                statusLabel.setForeground(Color.RED);
                startStopButton.setText("Start Server");
            }
        });
    }
    
    /**
     * Logs a message to the log area
     * 
     * @param message The message to log
     */
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Scroll to the bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Custom output stream for redirecting System.out to the text area
     */
    private static class CustomOutputStream extends java.io.OutputStream {
        private final JTextArea textArea;
        private final StringBuilder buffer = new StringBuilder();
        
        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        
        @Override
        public void write(int b) {
            char c = (char) b;
            buffer.append(c);
            
            if (c == '\n') {
                final String text = buffer.toString();
                SwingUtilities.invokeLater(() -> {
                    textArea.append(text);
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
                buffer.setLength(0);
            }
        }
    }
    
    /**
     * Main method to start the server GUI
     * 
     * @param args Command line arguments: port dictionaryFilePath
     */
    public static void main(String[] args) {
        // Set up the UI look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            // Default values
            int port = 8080;
            String dictionaryFilePath = "dictionary.json";
            
            // Parse command line arguments if provided
            if (args.length >= 1) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number, using default: 8080");
                }
            }
            
            if (args.length >= 2) {
                dictionaryFilePath = args[1];
            }
            
            ServerGUI gui = new ServerGUI(port, dictionaryFilePath);
            gui.setVisible(true);
        });
    }
} 