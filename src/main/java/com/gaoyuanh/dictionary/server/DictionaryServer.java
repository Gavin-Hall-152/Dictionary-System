package com.gaoyuanh.dictionary.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

import com.gaoyuanh.dictionary.dictionary.Dictionary;

/**
 * Main server class that initializes the dictionary and handles client connections.
 * Implements a multi-threaded server using a thread pool architecture.
 */
public class DictionaryServer {
    private ServerSocket serverSocket;
    private final Dictionary dictionary;
    private ExecutorService threadPool;
    private boolean running;
    private final int port;
    private final String dictionaryFilePath;
    private final int threadPoolSize;

    // Default port if not specified
    public static final int DEFAULT_PORT = 8080;
    
    // Listener for client connection events
    private IntConsumer clientConnectionListener;

    private Thread acceptThread;

    /**
     * Constructor for the DictionaryServer
     * 
     * @param port The port number to listen on
     * @param dictionaryFilePath Path to the dictionary file
     */
    public DictionaryServer(int port, String dictionaryFilePath) {
        this(port, dictionaryFilePath, 10); // Default thread pool size is 10
    }
    
    /**
     * Constructor for the DictionaryServer with custom thread pool size
     * 
     * @param port The port number to listen on
     * @param dictionaryFilePath Path to the dictionary file
     * @param threadPoolSize The size of the thread pool for handling client connections
     */
    public DictionaryServer(int port, String dictionaryFilePath, int threadPoolSize) {
        this.port = port;
        this.dictionaryFilePath = dictionaryFilePath;
        this.dictionary = new Dictionary();
        this.running = false;
        this.threadPoolSize = threadPoolSize;
    }
    
    /**
     * Sets a listener to be notified when clients connect or disconnect
     * 
     * @param listener Consumer that receives +1 when client connects, -1 when client disconnects
     */
    public void setClientConnectionListener(IntConsumer listener) {
        this.clientConnectionListener = listener;
    }
    
    /**
     * Notifies the client connection listener if one is set
     * 
     * @param delta +1 for client connection, -1 for disconnection
     */
    private void notifyClientConnectionChange(int delta) {
        if (clientConnectionListener != null) {
            clientConnectionListener.accept(delta);
        }
    }

    /**
     * Initializes the server by loading the dictionary and starting to listen for connections
     */
    public boolean startServer() {
        if (running) {
            return false; // Server is already running
        }
        
        try {
            // Try to load dictionary from file
            boolean dictionaryLoaded = false;
            if (dictionaryFilePath != null && !dictionaryFilePath.trim().isEmpty()) {
                try {
                    dictionary.loadFromFile(dictionaryFilePath);
                    dictionaryLoaded = true;
                    System.out.println("Loaded dictionary from file: " + dictionaryFilePath);
                } catch (IOException e) {
                    System.err.println("Error loading dictionary file: " + e.getMessage());
                    System.out.println("Using default dictionary...");
                }
            }
            
            // If dictionary wasn't loaded from file, use default dictionary
            if (!dictionaryLoaded) {
                loadDefaultDictionary();
                System.out.println("Loaded default dictionary");
            }
            
            // Create server socket
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(threadPoolSize);
            running = true;
            
            System.out.println("Dictionary Server started on port " + port);
            System.out.println("Dictionary contains " + dictionary.size() + " entries");
            
            // Start the client accept thread
            acceptClients();
            
            return true;
        } catch (IOException e) {
            System.err.println("Server initialization error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Thread method that accepts client connections
     */
    private void acceptClients() {
        // Create a new thread to handle accepting client connections
        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClientConnection(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        });
        
        acceptThread.start();
    }

    private void handleClientConnection(Socket clientSocket) {
        try {
            System.out.println("New client connected: " + clientSocket.getInetAddress());
            
            // Create a new client handler to process the client's requests
            ClientHandler clientHandler = new ClientHandler(clientSocket, dictionary, dictionaryFilePath);
            
            // Set the disconnect callback to notify when client disconnects
            clientHandler.setDisconnectCallback(() -> {
                notifyClientConnectionChange(-1);
            });
            
            // Submit client handler to the thread pool
            threadPool.submit(clientHandler);
            
            // Notify listener about the new client connection
            notifyClientConnectionChange(1);
        } catch (Exception e) {
            System.err.println("Error handling client connection: " + e.getMessage());        }
    }

    /**
     * Loads a simple default dictionary with basic words
     */
    private void loadDefaultDictionary() {
        // Simple words with definitions
        addDefaultWord("hello", Arrays.asList("a greeting", "used when answering the phone"));
        addDefaultWord("world", Arrays.asList("the earth and all people on it", "a particular region or group"));
        addDefaultWord("apple", Arrays.asList("a round fruit with red, yellow, or green skin", "the tree bearing this fruit"));
        addDefaultWord("book", Arrays.asList("a written or printed work", "a collection of sheets bound together"));
        addDefaultWord("cat", Arrays.asList("a small carnivorous mammal", "a domesticated feline pet"));
        addDefaultWord("dog", Arrays.asList("a domesticated carnivorous mammal", "a common household pet"));
        addDefaultWord("house", Arrays.asList("a building for human habitation", "a place where someone lives"));
        addDefaultWord("car", Arrays.asList("a road vehicle with an engine", "an automobile"));
        addDefaultWord("sun", Arrays.asList("the star at the center of our solar system", "the light or warmth from this star"));
        addDefaultWord("moon", Arrays.asList("the natural satellite of the earth", "a celestial body that orbits a planet"));
    }
    
    /**
     * Helper method to add a word to the default dictionary
     * 
     * @param word The word to add
     * @param meanings The list of meanings
     */
    private void addDefaultWord(String word, List<String> meanings) {
        dictionary.add(word, meanings);
    }

    /**
     * Gets the thread pool size used by this server
     * 
     * @return The thread pool size
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    /**
     * Stops the server and releases resources
     */
    public void stopServer() {
        if (!running) {
            return; // Server is not running
        }
        
        running = false;
        
        try {
            // Close the server socket to stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Shut down the thread pool
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
            
            // Interrupt and wait for the accept thread to terminate
            if (acceptThread != null) {
                acceptThread.interrupt();
                try {
                    acceptThread.join(5000); // Wait up to 5 seconds for the thread to terminate
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting for accept thread to terminate");
                }
            }
            
            System.out.println("Server stopped");
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
} 