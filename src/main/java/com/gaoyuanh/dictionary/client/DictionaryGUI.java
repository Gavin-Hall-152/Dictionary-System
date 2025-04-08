package com.gaoyuanh.dictionary.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
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
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.gaoyuanh.dictionary.protocol.ProtocolConstants;

/**
 * Graphical User Interface for the Dictionary Client.
 * Redesigned with a header-body-footer structure for improved user experience.
 */
public class DictionaryGUI extends JFrame {
    private DictionaryClient client;
    private JTextArea inputArea;
    private JTextArea resultArea;
    private JPanel headerPanel;
    private JPanel bodyPanel;
    private JPanel footerPanel;
    private JLabel statusLabel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Connection components
    private JTextField serverField;
    private JTextField portField;
    private JButton connectButton;
    
    /**
     * Constructor initializes the GUI components with improved design
     */
    public DictionaryGUI() {
        setTitle("Dictionary Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set white background with black text as the default
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", Color.BLACK);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("Button.background", new Color(240, 240, 240));
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("ScrollPane.background", Color.WHITE);
        
        // Set up the main layout
        setLayout(new BorderLayout());
        
        // Create the header, body, and footer sections
        createHeaderPanel();
        createBodyPanel();
        createFooterPanel();
        
        // Add sections to the frame
        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
        
        // Add window close listener to disconnect from server
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                }
            }
        });
        
        // Initialize with connection panel
        showConnectionPanel();
    }
    
    /**
     * Creates the header panel with navigation buttons
     */
    private void createHeaderPanel() {
        headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        headerPanel.setBackground(Color.WHITE);
        
        // Create operation buttons for the header
        JButton searchButton = createHeaderButton("Search Word");
        JButton addButton = createHeaderButton("Add Word");
        JButton removeButton = createHeaderButton("Remove Word");
        JButton addMeaningButton = createHeaderButton("Add Meaning");
        JButton updateMeaningButton = createHeaderButton("Update Meaning");
        
        // Add action listeners to buttons
        searchButton.addActionListener(e -> showPanel("search"));
        addButton.addActionListener(e -> showPanel("add"));
        removeButton.addActionListener(e -> showPanel("remove"));
        addMeaningButton.addActionListener(e -> showPanel("addMeaning"));
        updateMeaningButton.addActionListener(e -> showPanel("updateMeaning"));
        
        // Add buttons to header
        headerPanel.add(searchButton);
        headerPanel.add(addButton);
        headerPanel.add(removeButton);
        headerPanel.add(addMeaningButton);
        headerPanel.add(updateMeaningButton);
        
        // Add status indicator to the right side
        headerPanel.add(Box.createHorizontalGlue());
        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.RED);
        headerPanel.add(statusLabel);
    }
    
    /**
     * Helper method to create styled header buttons
     * 
     * @param text Button text
     * @return Styled JButton
     */
    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        
        return button;
    }
    
    /**
     * Creates the body panel with card layout for different operations
     */
    private void createBodyPanel() {
        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bodyPanel.setBackground(Color.WHITE);
        
        // Create a panel with card layout to switch between operation panels
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        // Create different operation panels
        JPanel searchPanel = createSearchPanel();
        JPanel addWordPanel = createAddWordPanel();
        JPanel removeWordPanel = createRemoveWordPanel();
        JPanel addMeaningPanel = createAddMeaningPanel();
        JPanel updateMeaningPanel = createUpdateMeaningPanel();
        JPanel connectionPanel = createConnectionPanel();
        
        // Add panels to the card layout
        contentPanel.add(connectionPanel, "connection");
        contentPanel.add(searchPanel, "search");
        contentPanel.add(addWordPanel, "add");
        contentPanel.add(removeWordPanel, "remove");
        contentPanel.add(addMeaningPanel, "addMeaning");
        contentPanel.add(updateMeaningPanel, "updateMeaning");
        
        // Create results area
        JPanel resultsPanel = createResultsPanel();
        
        // Add the content panel and results panel to the body
        bodyPanel.add(contentPanel, BorderLayout.CENTER);
        bodyPanel.add(resultsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the footer panel with settings button
     */
    private void createFooterPanel() {
        footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        footerPanel.setBackground(Color.WHITE);
        
        // Create a settings button for the connection module
        JButton settingsButton = new JButton("Connection Settings");
        settingsButton.setBackground(new Color(240, 240, 240));
        settingsButton.setForeground(Color.BLACK);
        settingsButton.setFocusPainted(false);
        settingsButton.addActionListener(e -> showConnectionPanel());
        
        // Add button to the footer
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(settingsButton);
        
        footerPanel.add(leftPanel, BorderLayout.WEST);
        
        // Add version info to the right
        JLabel versionLabel = new JLabel("Dictionary Client v1.0");
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setBorder(new EmptyBorder(5, 0, 5, 10));
        
        footerPanel.add(versionLabel, BorderLayout.EAST);
    }
    
    /**
     * Creates the connection panel for server settings
     * 
     * @return The connection panel
     */
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Server Connection");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverPanel.setBackground(Color.WHITE);
        JLabel serverLabel = new JLabel("Server Address:");
        serverField = new JTextField("localhost", 20);
        serverPanel.add(serverLabel);
        serverPanel.add(serverField);
        serverPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portPanel.setBackground(Color.WHITE);
        JLabel portLabel = new JLabel("Port Number:");
        portField = new JTextField("8080", 10);
        portPanel.add(portLabel);
        portPanel.add(portField);
        portPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        connectButton = new JButton("Connect to Server");
        connectButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        connectButton.addActionListener(e -> connectToServer());
        
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(serverPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(portPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(connectButton);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Creates the search panel for looking up words
     * 
     * @return The search panel
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Search Word");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JLabel promptLabel = new JLabel("Enter a word to search:");
        inputArea = new JTextArea(5, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JScrollPane scrollPane = new JScrollPane(inputArea);
        
        inputPanel.add(promptLabel, BorderLayout.NORTH);
        inputPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchWord());
        
        buttonPanel.add(searchButton);
        
        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the add word panel
     * 
     * @return The add word panel
     */
    private JPanel createAddWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Add New Word");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Word field
        JPanel wordPanel = new JPanel(new BorderLayout());
        wordPanel.setBackground(Color.WHITE);
        JLabel wordLabel = new JLabel("Word to add:");
        JTextField wordField = new JTextField(20);
        wordPanel.add(wordLabel, BorderLayout.NORTH);
        wordPanel.add(wordField, BorderLayout.CENTER);
        
        // Meanings area
        JPanel meaningsPanel = new JPanel(new BorderLayout());
        meaningsPanel.setBackground(Color.WHITE);
        JLabel meaningsLabel = new JLabel("Meanings (one per line):");
        JTextArea meaningsArea = new JTextArea(8, 20);
        meaningsArea.setLineWrap(true);
        meaningsArea.setWrapStyleWord(true);
        meaningsArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollPane = new JScrollPane(meaningsArea);
        meaningsPanel.add(meaningsLabel, BorderLayout.NORTH);
        meaningsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Add Word");
        addButton.addActionListener(e -> {
            addWord(wordField.getText(), meaningsArea.getText());
        });
        
        buttonPanel.add(addButton);
        
        // Assemble the form
        formPanel.add(wordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(meaningsPanel);
        
        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the remove word panel
     * 
     * @return The remove word panel
     */
    private JPanel createRemoveWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Remove Word");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JLabel promptLabel = new JLabel("Enter the word to remove:");
        JTextField wordField = new JTextField(20);
        
        inputPanel.add(promptLabel, BorderLayout.NORTH);
        inputPanel.add(wordField, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> removeWord(wordField.getText()));
        
        buttonPanel.add(removeButton);
        
        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the add meaning panel
     * 
     * @return The add meaning panel
     */
    private JPanel createAddMeaningPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Add New Meaning");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Word field
        JPanel wordPanel = new JPanel(new BorderLayout());
        wordPanel.setBackground(Color.WHITE);
        JLabel wordLabel = new JLabel("Existing word:");
        JTextField wordField = new JTextField(20);
        wordPanel.add(wordLabel, BorderLayout.NORTH);
        wordPanel.add(wordField, BorderLayout.CENTER);
        
        // Meaning field
        JPanel meaningPanel = new JPanel(new BorderLayout());
        meaningPanel.setBackground(Color.WHITE);
        JLabel meaningLabel = new JLabel("New meaning to add:");
        JTextField meaningField = new JTextField(20);
        meaningPanel.add(meaningLabel, BorderLayout.NORTH);
        meaningPanel.add(meaningField, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = new JButton("Add Meaning");
        addButton.addActionListener(e -> {
            addMeaning(wordField.getText(), meaningField.getText());
        });
        
        buttonPanel.add(addButton);
        
        // Assemble the form
        formPanel.add(wordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(meaningPanel);
        
        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the update meaning panel
     * 
     * @return The update meaning panel
     */
    private JPanel createUpdateMeaningPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Update Meaning");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Word field
        JPanel wordPanel = new JPanel(new BorderLayout());
        wordPanel.setBackground(Color.WHITE);
        JLabel wordLabel = new JLabel("Existing word:");
        JTextField wordField = new JTextField(20);
        wordPanel.add(wordLabel, BorderLayout.NORTH);
        wordPanel.add(wordField, BorderLayout.CENTER);
        
        // Old meaning field
        JPanel oldMeaningPanel = new JPanel(new BorderLayout());
        oldMeaningPanel.setBackground(Color.WHITE);
        JLabel oldMeaningLabel = new JLabel("Existing meaning:");
        JTextField oldMeaningField = new JTextField(20);
        oldMeaningPanel.add(oldMeaningLabel, BorderLayout.NORTH);
        oldMeaningPanel.add(oldMeaningField, BorderLayout.CENTER);
        
        // New meaning field
        JPanel newMeaningPanel = new JPanel(new BorderLayout());
        newMeaningPanel.setBackground(Color.WHITE);
        JLabel newMeaningLabel = new JLabel("New meaning:");
        JTextField newMeaningField = new JTextField(20);
        newMeaningPanel.add(newMeaningLabel, BorderLayout.NORTH);
        newMeaningPanel.add(newMeaningField, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton updateButton = new JButton("Update Meaning");
        updateButton.addActionListener(e -> {
            updateMeaning(wordField.getText(), oldMeaningField.getText(), newMeaningField.getText());
        });
        
        buttonPanel.add(updateButton);
        
        // Assemble the form
        formPanel.add(wordPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(oldMeaningPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(newMeaningPanel);
        
        // Assemble the panel
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the results panel
     * 
     * @return The results panel
     */
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("Results:");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        
        resultArea = new JTextArea(8, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(new Color(250, 250, 250));
        resultArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Shows a specific panel by name
     * 
     * @param name Panel name
     */
    private void showPanel(String name) {
        if (client == null || !client.isConnected()) {
            showError("You must connect to the server first");
            return;
        }
        cardLayout.show(contentPanel, name);
    }
    
    /**
     * Shows the connection panel
     */
    private void showConnectionPanel() {
        cardLayout.show(contentPanel, "connection");
    }
    
    /**
     * Connects to the dictionary server
     */
    private void connectToServer() {
        String server = serverField.getText().trim();
        String portText = portField.getText().trim();
        
        if (server.isEmpty()) {
            showError("Server address cannot be empty");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                showError("Port must be between 1 and 65535");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid port number");
            return;
        }
        
        // Create new client instance
        client = new DictionaryClient(server, port, this::handleError);
        
        // Connect to server
        boolean connected = client.connect();
        
        if (connected) {
            statusLabel.setText("Connected to " + server + ":" + port);
            statusLabel.setForeground(new Color(0, 128, 0)); // Dark green
            connectButton.setText("Disconnect");
            connectButton.removeActionListener(connectButton.getActionListeners()[0]);
            connectButton.addActionListener(e -> disconnectFromServer());
            appendToResultArea("Connected to dictionary server at " + server + ":" + port);
            
            // Show the search panel by default after connecting
            cardLayout.show(contentPanel, "search");
        } else {
            statusLabel.setText("Connection failed");
            statusLabel.setForeground(Color.RED);
            appendToResultArea("Failed to connect to server at " + server + ":" + port);
            client = null;
        }
    }
    
    /**
     * Disconnects from the dictionary server
     */
    private void disconnectFromServer() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        
        statusLabel.setText("Not Connected");
        statusLabel.setForeground(Color.RED);
        connectButton.setText("Connect to Server");
        connectButton.removeActionListener(connectButton.getActionListeners()[0]);
        connectButton.addActionListener(e -> connectToServer());
        
        appendToResultArea("Disconnected from server");
        showConnectionPanel();
    }
    
    /**
     * Searches for a word
     */
    private void searchWord() {
        String word = inputArea.getText().trim();
        
        if (word.isEmpty()) {
            showError("Word cannot be empty");
            return;
        }
        
        // Use final variable for lambda
        final String finalWord = word;
        client.searchWord(word).thenAccept(response -> {
            switch (response.getStatus()) {
                case ProtocolConstants.STATUS_SUCCESS:
                    StringBuilder sb = new StringBuilder();
                    sb.append("Word: ").append(response.getWord()).append("\n");
                    sb.append("Meanings:\n");
                    
                List<String> meanings = response.getMeanings();
                for (int i = 0; i < meanings.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(meanings.get(i)).append("\n");
                }
                
                appendToResultArea(sb.toString());
                break;
                case ProtocolConstants.STATUS_NOT_FOUND:
                    appendToResultArea("Word '" + finalWord + "' not found in the dictionary");
                    break;
                case ProtocolConstants.STATUS_ERROR:
                    appendToResultArea("Error: " + response.getErrorMessage());
                    break;
            }
        }).exceptionally(e -> {
            handleError("Error searching for word: " + e.getMessage());
            return null;
        });
    }
    
    /**
     * Adds a new word to the dictionary
     */
    private void addWord(String wordParam, String meaningsTextParam) {
        // Create new variables instead of modifying parameters
        final String word = wordParam.trim();
        final String meaningsText = meaningsTextParam.trim();
        
        if (word.isEmpty()) {
            showError("Word cannot be empty");
            return;
        }
        
        if (meaningsText.isEmpty()) {
            showError("Meanings cannot be empty");
            return;
        }
        
        List<String> meanings = Arrays.asList(meaningsText.split("\n"));
        
        client.addWord(word, meanings).thenAccept(response -> {
            switch (response.getStatus()) {
                case ProtocolConstants.STATUS_SUCCESS:
                    appendToResultArea("Successfully added word: " + word);
                    break;
                case ProtocolConstants.STATUS_DUPLICATE:
                    appendToResultArea("Word '" + word + "' already exists in the dictionary");
                    break;
                case ProtocolConstants.STATUS_ERROR:
                    appendToResultArea("Error: " + response.getErrorMessage());
                    break;
            }
        }).exceptionally(e -> {
            handleError("Error adding word: " + e.getMessage());
            return null;
        });
    }
    
    /**
     * Removes a word from the dictionary
     */
    private void removeWord(String wordParam) {
        // Create new variable instead of modifying parameter
        final String word = wordParam.trim();
        
        if (word.isEmpty()) {
            showError("Word cannot be empty");
            return;
        }
        
        client.removeWord(word).thenAccept(response -> {
            switch (response.getStatus()) {
                case ProtocolConstants.STATUS_SUCCESS:
                    appendToResultArea("Successfully removed word: " + word);
                    break;
                case ProtocolConstants.STATUS_NOT_FOUND:
                    appendToResultArea("Word '" + word + "' not found in the dictionary");
                    break;
                case ProtocolConstants.STATUS_ERROR:
                    appendToResultArea("Error: " + response.getErrorMessage());
                    break;
            }
        }).exceptionally(e -> {
            handleError("Error removing word: " + e.getMessage());
            return null;
        });
    }
    
    /**
     * Adds a new meaning to an existing word
     */
    private void addMeaning(String wordParam, String meaningParam) {
        // Create new variables instead of modifying parameters
        final String word = wordParam.trim();
        final String meaning = meaningParam.trim();
        
        if (word.isEmpty()) {
            showError("Word cannot be empty");
            return;
        }
        
        if (meaning.isEmpty()) {
            showError("Meaning cannot be empty");
            return;
        }
        
        client.addMeaning(word, meaning).thenAccept(response -> {
            switch (response.getStatus()) {
                case ProtocolConstants.STATUS_SUCCESS:
                    appendToResultArea("Successfully added meaning to word: " + word);
                    break;
                case ProtocolConstants.STATUS_NOT_FOUND:
                    appendToResultArea("Word '" + word + "' not found in the dictionary");
                    break;
                case ProtocolConstants.STATUS_DUPLICATE:
                    appendToResultArea("This meaning already exists for word '" + word + "'");
                    break;
                case ProtocolConstants.STATUS_ERROR:
                    appendToResultArea("Error: " + response.getErrorMessage());
                    break;
            }
        }).exceptionally(e -> {
            handleError("Error adding meaning: " + e.getMessage());
            return null;
        });
    }
    
    /**
     * Updates an existing meaning of a word
     */
    private void updateMeaning(String wordParam, String oldMeaningParam, String newMeaningParam) {
        // Create new variables instead of modifying parameters
        final String word = wordParam.trim();
        final String oldMeaning = oldMeaningParam.trim();
        final String newMeaning = newMeaningParam.trim();
        
        if (word.isEmpty()) {
            showError("Word cannot be empty");
            return;
        }
        
        if (oldMeaning.isEmpty()) {
            showError("Existing meaning cannot be empty");
            return;
        }
        
        if (newMeaning.isEmpty()) {
            showError("New meaning cannot be empty");
            return;
        }
        
        client.updateMeaning(word, oldMeaning, newMeaning).thenAccept(response -> {
            switch (response.getStatus()) {
                case ProtocolConstants.STATUS_SUCCESS:
                    appendToResultArea("Successfully updated meaning for word: " + word);
                    break;
                case ProtocolConstants.STATUS_NOT_FOUND:
                    appendToResultArea("Word '" + word + "' not found in the dictionary");
                    break;
                case ProtocolConstants.STATUS_MEANING_NOT_FOUND:
                    appendToResultArea("The specified meaning was not found for word '" + word + "'");
                    break;
                case ProtocolConstants.STATUS_ERROR:
                    appendToResultArea("Error: " + response.getErrorMessage());
                    break;
            }
        }).exceptionally(e -> {
            handleError("Error updating meaning: " + e.getMessage());
            return null;
        });
    }
    
    /**
     * Appends text to the result area
     * 
     * @param text The text to append
     */
    private void appendToResultArea(String text) {
        SwingUtilities.invokeLater(() -> {
            resultArea.append(text + "\n\n");
            // Scroll to the bottom
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });
    }
    
    /**
     * Displays an error message dialog
     * 
     * @param message The error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Handles error messages
     * 
     * @param errorMessage The error message
     */
    private void handleError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            appendToResultArea("Error: " + errorMessage);
            
            // If connection error, update the status
            if (errorMessage.startsWith("Communication error") || 
                errorMessage.startsWith("Connection closed")) {
                disconnectFromServer();
            }
        });
    }
    
    /**
     * Main method to start the client application
     * 
     * @param args Command line arguments: server-address server-port
     */
    public static void main(String[] args) {
        // Set up the UI look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            DictionaryGUI gui = new DictionaryGUI();
            
            // If command line arguments are provided, pre-fill server and port
            if (args.length >= 2) {
                gui.serverField.setText(args[0]);
                gui.portField.setText(args[1]);
            }
            
            gui.setVisible(true);
        });
    }
} 