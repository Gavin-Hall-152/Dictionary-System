package com.gaoyuanh.dictionary.dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * WordEntry class that encapsulates a word and its meanings.
 * Implements Serializable to support transmission over sockets.
 */
public class WordEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String word;
    private final List<String> meanings;

    /**
     * Constructor for WordEntry
     * 
     * @param word The word
     * @param meanings List of meanings for the word
     */
    public WordEntry(String word, List<String> meanings) {
        this.word = word;
        this.meanings = new ArrayList<>(meanings);
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
     * Gets the list of meanings
     * 
     * @return List of meanings
     */
    public List<String> getMeanings() {
        return new ArrayList<>(meanings);
    }

    /**
     * Adds a new meaning to the word
     * 
     * @param meaning The meaning to add
     * @return true if the meaning was added, false if it already exists
     */
    public boolean addMeaning(String meaning) {
        if (meaning == null || meaning.trim().isEmpty()) {
            return false;
        }
        
        String normalizedMeaning = meaning.trim();
        
        if (meanings.contains(normalizedMeaning)) {
            return false;
        }
        
        meanings.add(normalizedMeaning);
        return true;
    }

    /**
     * Updates an existing meaning with a new one
     * 
     * @param oldMeaning The existing meaning to update
     * @param newMeaning The new meaning to replace with
     * @return true if the meaning was updated, false if old meaning doesn't exist
     */
    public boolean updateMeaning(String oldMeaning, String newMeaning) {
        if (oldMeaning == null || oldMeaning.trim().isEmpty() || 
            newMeaning == null || newMeaning.trim().isEmpty()) {
            return false;
        }
        
        String normalizedOldMeaning = oldMeaning.trim();
        String normalizedNewMeaning = newMeaning.trim();
        
        int index = meanings.indexOf(normalizedOldMeaning);
        if (index == -1) {
            return false;
        }
        
        // Check if the new meaning already exists elsewhere
        int newIndex = meanings.indexOf(normalizedNewMeaning);
        if (newIndex != -1 && newIndex != index) {
            // If new meaning already exists in another position, just remove the old one
            meanings.remove(index);
        } else {
            // Replace old meaning with new one
            meanings.set(index, normalizedNewMeaning);
        }
        
        return true;
    }

    /**
     * Removes a meaning from the word
     * 
     * @param meaning The meaning to remove
     * @return true if the meaning was removed, false if it doesn't exist
     */
    public boolean removeMeaning(String meaning) {
        if (meaning == null || meaning.trim().isEmpty()) {
            return false;
        }
        
        return meanings.remove(meaning.trim());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word).append(":\n");
        
        for (String meaning : meanings) {
            sb.append("    ").append(meaning).append("\n");
        }
        
        return sb.toString();
    }
} 