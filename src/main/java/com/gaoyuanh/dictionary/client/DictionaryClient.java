package com.gaoyuanh.dictionary.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.gaoyuanh.dictionary.protocol.Message;
import com.gaoyuanh.dictionary.protocol.ProtocolConstants;
import com.google.gson.Gson;

/**
 * Client class that manages socket communication with the dictionary server.
 * Provides methods to perform dictionary operations.
 */
public class DictionaryClient {
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;
    private final ExecutorService executor;
    private boolean connected;
    private final Consumer<String> errorHandler;

    /**
     * Constructor for DictionaryClient
     * 
     * @param serverAddress The server address
     * @param serverPort The server port
     * @param errorHandler Handler for error messages
     */
    public DictionaryClient(String serverAddress, int serverPort, Consumer<String> errorHandler) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        this.connected = false;
        this.errorHandler = errorHandler;
    }

    /**
     * Connects to the dictionary server
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            return true;
        } catch (IOException e) {
            handleError("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Disconnects from the dictionary server
     */
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            handleError("Error disconnecting from server: " + e.getMessage());
        }
        executor.shutdown();
    }

    /**
     * Checks if client is connected to the server
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Searches for a word in the dictionary
     * 
     * @param word The word to search for
     * @return CompletableFuture with the response message
     */
    public CompletableFuture<Message> searchWord(String word) {
        if (!isConnected()) {
            CompletableFuture<Message> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Not connected to server"));
            return future;
        }
        
        Message request = new Message(ProtocolConstants.OPERATION_SEARCH);
        request.setWord(word);
        
        return sendRequest(request);
    }

    /**
     * Adds a new word to the dictionary
     * 
     * @param word The word to add
     * @param meanings The meanings of the word
     * @return CompletableFuture with the response message
     */
    public CompletableFuture<Message> addWord(String word, List<String> meanings) {
        if (!isConnected()) {
            CompletableFuture<Message> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Not connected to server"));
            return future;
        }
        
        Message request = new Message(ProtocolConstants.OPERATION_ADD);
        request.setWord(word);
        request.setMeanings(meanings);
        
        return sendRequest(request);
    }

    /**
     * Removes a word from the dictionary
     * 
     * @param word The word to remove
     * @return CompletableFuture with the response message
     */
    public CompletableFuture<Message> removeWord(String word) {
        if (!isConnected()) {
            CompletableFuture<Message> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Not connected to server"));
            return future;
        }
        
        Message request = new Message(ProtocolConstants.OPERATION_REMOVE);
        request.setWord(word);
        
        return sendRequest(request);
    }

    /**
     * Adds a new meaning to an existing word
     * 
     * @param word The existing word
     * @param meaning The new meaning to add
     * @return CompletableFuture with the response message
     */
    public CompletableFuture<Message> addMeaning(String word, String meaning) {
        if (!isConnected()) {
            CompletableFuture<Message> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Not connected to server"));
            return future;
        }
        
        Message request = new Message(ProtocolConstants.OPERATION_ADD_MEANING);
        request.setWord(word);
        
        List<String> meanings = new ArrayList<>();
        meanings.add(meaning);
        request.setMeanings(meanings);
        
        return sendRequest(request);
    }

    /**
     * Updates an existing meaning of a word
     * 
     * @param word The existing word
     * @param oldMeaning The existing meaning to update
     * @param newMeaning The new meaning to replace with
     * @return CompletableFuture with the response message
     */
    public CompletableFuture<Message> updateMeaning(String word, String oldMeaning, String newMeaning) {
        if (!isConnected()) {
            CompletableFuture<Message> future = new CompletableFuture<>();
            future.completeExceptionally(new IOException("Not connected to server"));
            return future;
        }
        
        Message request = new Message(ProtocolConstants.OPERATION_UPDATE_MEANING);
        request.setWord(word);
        request.setOldMeaning(oldMeaning);
        request.setNewMeaning(newMeaning);
        
        return sendRequest(request);
    }

    /**
     * Sends a request to the server and gets the response
     * 
     * @param request The request message to send
     * @return CompletableFuture with the response message
     */
    private CompletableFuture<Message> sendRequest(Message request) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                // Convert request to JSON and send
                String jsonRequest = gson.toJson(request);
                out.println(jsonRequest);
                
                // Read response
                String jsonResponse = in.readLine();
                if (jsonResponse == null) {
                    connected = false;
                    future.completeExceptionally(new IOException("Connection closed by server"));
                    return;
                }
                
                // Parse response
                Message response = gson.fromJson(jsonResponse, Message.class);
                future.complete(response);
            } catch (IOException e) {
                connected = false;
                handleError("Communication error: " + e.getMessage());
                future.completeExceptionally(e);
            } catch (Exception e) {
                handleError("Error processing request: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    /**
     * Handles error messages
     * 
     * @param errorMessage The error message
     */
    private void handleError(String errorMessage) {
        if (errorHandler != null) {
            errorHandler.accept(errorMessage);
        }
    }
} 