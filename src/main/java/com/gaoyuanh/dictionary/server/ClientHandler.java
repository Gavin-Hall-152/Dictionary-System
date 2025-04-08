package com.gaoyuanh.dictionary.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.gaoyuanh.dictionary.dictionary.Dictionary;
import com.gaoyuanh.dictionary.dictionary.WordEntry;
import com.gaoyuanh.dictionary.protocol.Message;
import com.gaoyuanh.dictionary.protocol.ProtocolConstants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Handles client connections in a separate thread.
 * Processes client requests and sends responses.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Dictionary dictionary;
    private final Gson gson;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running;
    private Runnable disconnectCallback;

    /**
     * Constructor for ClientHandler
     * 
     * @param clientSocket The client socket connection
     * @param dictionary The shared dictionary instance
     */
    public ClientHandler(Socket clientSocket, Dictionary dictionary) {
        this.clientSocket = clientSocket;
        this.dictionary = dictionary;
        this.gson = new Gson();
        this.running = true;
    }
    
    /**
     * Sets a callback to be executed when the client disconnects
     * 
     * @param callback The callback to execute on disconnect
     */
    public void setDisconnectCallback(Runnable callback) {
        this.disconnectCallback = callback;
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // Process client requests
            String inputLine;
            while (running && (inputLine = in.readLine()) != null) {
                try {
                    // Parse the client message from JSON
                    Message request = gson.fromJson(inputLine, Message.class);
                    if (request == null) {
                        sendErrorResponse("Invalid request format");
                        continue;
                    }
                    
                    // Process the request based on operation type
                    processRequest(request);
                } catch (JsonSyntaxException e) {
                    sendErrorResponse("Invalid JSON format: " + e.getMessage());
                } catch (Exception e) {
                    sendErrorResponse("Error processing request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Processes client requests based on operation type
     * 
     * @param request The client request message
     */
    private void processRequest(Message request) {
        switch (request.getOperation()) {
            case ProtocolConstants.OPERATION_SEARCH:
                handleSearch(request);
                break;
            case ProtocolConstants.OPERATION_ADD:
                handleAdd(request);
                break;
            case ProtocolConstants.OPERATION_REMOVE:
                handleRemove(request);
                break;
            case ProtocolConstants.OPERATION_ADD_MEANING:
                handleAddMeaning(request);
                break;
            case ProtocolConstants.OPERATION_UPDATE_MEANING:
                handleUpdateMeaning(request);
                break;
            default:
                sendErrorResponse("Unknown operation: " + request.getOperation());
        }
    }

    /**
     * Handles word search requests
     * 
     * @param request The search request message
     */
    private void handleSearch(Message request) {
        String word = request.getWord();
        if (word == null || word.trim().isEmpty()) {
            sendErrorResponse("Word cannot be empty");
            return;
        }
        
        WordEntry entry = dictionary.search(word);
        if (entry == null) {
            Message response = new Message(ProtocolConstants.OPERATION_SEARCH);
            response.setStatus(ProtocolConstants.STATUS_NOT_FOUND);
            response.setWord(word);
            sendResponse(response);
        } else {
            Message response = new Message(ProtocolConstants.OPERATION_SEARCH);
            response.setStatus(ProtocolConstants.STATUS_SUCCESS);
            response.setWord(entry.getWord());
            response.setMeanings(entry.getMeanings());
            sendResponse(response);
        }
    }

    /**
     * Handles requests to add a new word
     * 
     * @param request The add word request message
     */
    private void handleAdd(Message request) {
        String word = request.getWord();
        List<String> meanings = request.getMeanings();
        
        if (word == null || word.trim().isEmpty()) {
            sendErrorResponse("Word cannot be empty");
            return;
        }
        
        if (meanings == null || meanings.isEmpty()) {
            sendErrorResponse("Meanings cannot be empty");
            return;
        }
        
        boolean success = dictionary.add(word, meanings);
        
        Message response = new Message(ProtocolConstants.OPERATION_ADD);
        response.setWord(word);
        
        if (success) {
            response.setStatus(ProtocolConstants.STATUS_SUCCESS);
        } else {
            response.setStatus(ProtocolConstants.STATUS_DUPLICATE);
        }
        
        sendResponse(response);
    }

    /**
     * Handles requests to remove a word
     * 
     * @param request The remove word request message
     */
    private void handleRemove(Message request) {
        String word = request.getWord();
        
        if (word == null || word.trim().isEmpty()) {
            sendErrorResponse("Word cannot be empty");
            return;
        }
        
        boolean success = dictionary.remove(word);
        
        Message response = new Message(ProtocolConstants.OPERATION_REMOVE);
        response.setWord(word);
        
        if (success) {
            response.setStatus(ProtocolConstants.STATUS_SUCCESS);
        } else {
            response.setStatus(ProtocolConstants.STATUS_NOT_FOUND);
        }
        
        sendResponse(response);
    }

    /**
     * Handles requests to add a new meaning to an existing word
     * 
     * @param request The add meaning request message
     */
    private void handleAddMeaning(Message request) {
        String word = request.getWord();
        List<String> meanings = request.getMeanings();
        
        if (word == null || word.trim().isEmpty()) {
            sendErrorResponse("Word cannot be empty");
            return;
        }
        
        if (meanings == null || meanings.isEmpty()) {
            sendErrorResponse("New meaning cannot be empty");
            return;
        }
        
        String newMeaning = meanings.get(0);
        boolean success = dictionary.addMeaning(word, newMeaning);
        
        Message response = new Message(ProtocolConstants.OPERATION_ADD_MEANING);
        response.setWord(word);
        
        if (success) {
            response.setStatus(ProtocolConstants.STATUS_SUCCESS);
        } else {
            // Could be not found or duplicate, check if word exists
            if (dictionary.search(word) == null) {
                response.setStatus(ProtocolConstants.STATUS_NOT_FOUND);
            } else {
                response.setStatus(ProtocolConstants.STATUS_DUPLICATE);
            }
        }
        
        sendResponse(response);
    }

    /**
     * Handles requests to update an existing meaning of a word
     * 
     * @param request The update meaning request message
     */
    private void handleUpdateMeaning(Message request) {
        String word = request.getWord();
        String oldMeaning = request.getOldMeaning();
        String newMeaning = request.getNewMeaning();
        
        if (word == null || word.trim().isEmpty()) {
            sendErrorResponse("Word cannot be empty");
            return;
        }
        
        if (oldMeaning == null || oldMeaning.trim().isEmpty()) {
            sendErrorResponse("Existing meaning cannot be empty");
            return;
        }
        
        if (newMeaning == null || newMeaning.trim().isEmpty()) {
            sendErrorResponse("New meaning cannot be empty");
            return;
        }
        
        boolean success = dictionary.updateMeaning(word, oldMeaning, newMeaning);
        
        Message response = new Message(ProtocolConstants.OPERATION_UPDATE_MEANING);
        response.setWord(word);
        
        if (success) {
            response.setStatus(ProtocolConstants.STATUS_SUCCESS);
        } else {
            // Could be word not found or meaning not found
            if (dictionary.search(word) == null) {
                response.setStatus(ProtocolConstants.STATUS_NOT_FOUND);
            } else {
                response.setStatus(ProtocolConstants.STATUS_MEANING_NOT_FOUND);
            }
        }
        
        sendResponse(response);
    }

    /**
     * Sends an error response to the client
     * 
     * @param errorMessage The error message to send
     */
    private void sendErrorResponse(String errorMessage) {
        Message errorResponse = new Message();
        errorResponse.setStatus(ProtocolConstants.STATUS_ERROR);
        errorResponse.setErrorMessage(errorMessage);
        sendResponse(errorResponse);
    }

    /**
     * Sends a response message to the client
     * 
     * @param response The response message
     */
    private void sendResponse(Message response) {
        String jsonResponse = gson.toJson(response);
        out.println(jsonResponse);
    }

    /**
     * Closes the client connection and releases resources
     */
    private void closeConnection() {
        running = false;
        
        try {
            if (in != null) {
                in.close();
            }
            
            if (out != null) {
                out.close();
            }
            
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                
                // Execute disconnect callback if set
                if (disconnectCallback != null) {
                    disconnectCallback.run();
                }
            }
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
} 