package com.gaoyuanh.dictionary.protocol;

import java.io.Serializable;
import java.util.List;

/**
 * Message class for communication between client and server.
 * Encapsulates request and response data in a standardized format.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String operation;
    private String status;
    private String word;
    private List<String> meanings;
    private String oldMeaning;
    private String newMeaning;
    private String errorMessage;

    /**
     * Default constructor
     */
    public Message() {
    }

    /**
     * Constructor with operation
     * 
     * @param operation The operation type
     */
    public Message(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the operation type
     * 
     * @return The operation type
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation type
     * 
     * @param operation The operation type to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the status of the operation
     * 
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the operation
     * 
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the word
     * 
     * @return The word
     */
    public String getWord() {
        return word;
    }

    /**
     * Sets the word
     * 
     * @param word The word to set
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Gets the list of meanings
     * 
     * @return The list of meanings
     */
    public List<String> getMeanings() {
        return meanings;
    }

    /**
     * Sets the list of meanings
     * 
     * @param meanings The list of meanings to set
     */
    public void setMeanings(List<String> meanings) {
        this.meanings = meanings;
    }

    /**
     * Gets the old meaning (for update operations)
     * 
     * @return The old meaning
     */
    public String getOldMeaning() {
        return oldMeaning;
    }

    /**
     * Sets the old meaning (for update operations)
     * 
     * @param oldMeaning The old meaning to set
     */
    public void setOldMeaning(String oldMeaning) {
        this.oldMeaning = oldMeaning;
    }

    /**
     * Gets the new meaning (for update operations)
     * 
     * @return The new meaning
     */
    public String getNewMeaning() {
        return newMeaning;
    }

    /**
     * Sets the new meaning (for update operations)
     * 
     * @param newMeaning The new meaning to set
     */
    public void setNewMeaning(String newMeaning) {
        this.newMeaning = newMeaning;
    }

    /**
     * Gets the error message
     * 
     * @return The error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message
     * 
     * @param errorMessage The error message to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 