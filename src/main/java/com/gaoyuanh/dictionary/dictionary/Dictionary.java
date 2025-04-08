package com.gaoyuanh.dictionary.dictionary;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Dictionary class that provides methods to query, add, remove, and update words.
 * Implements thread-safe operations using read-write locks.
 * Uses JSON format for file operations.
 */
public class Dictionary {
    // Map word to its meanings
    private final Map<String, WordEntry> dictionary;
    // Read-write lock for thread safety
    private final ReadWriteLock lock;
    // Gson instance for JSON serialization/deserialization
    private final Gson gson;

    /**
     * Constructor initializes an empty dictionary with thread-safe access
     */
    public Dictionary() {
        this.dictionary = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Loads dictionary data from a JSON file
     * 
     * @param filePath Path to the JSON dictionary file
     * @throws IOException If an error occurs while reading the file
     */
    public void loadFromFile(String filePath) throws IOException {
        lock.writeLock().lock();
        try (Reader reader = new FileReader(filePath)) {
            // Define the type for deserializing a Map<String, WordEntry>
            Type dictionaryType = new TypeToken<Map<String, WordEntry>>(){}.getType();
            Map<String, WordEntry> loadedDictionary = gson.fromJson(reader, dictionaryType);
            
            if (loadedDictionary != null) {
                dictionary.clear();
                dictionary.putAll(loadedDictionary);
                System.out.println("Loaded " + dictionary.size() + " entries from JSON file: " + filePath);
            } else {
                System.out.println("No entries found in JSON file or invalid format: " + filePath);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Saves the dictionary to a JSON file
     * 
     * @param filePath Path to save the JSON dictionary file
     * @throws IOException If an error occurs while writing to the file
     */
    public void saveToFile(String filePath) throws IOException {
        lock.readLock().lock();
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(dictionary, writer);
            System.out.println("Saved " + dictionary.size() + " entries to JSON file: " + filePath);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the number of words in the dictionary
     * 
     * @return The number of entries in the dictionary
     */
    public int size() {
        lock.readLock().lock();
        try {
            return dictionary.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Searches for a word in the dictionary
     * 
     * @param word The word to search for
     * @return The WordEntry if found, null otherwise
     */
    public WordEntry search(String word) {
        lock.readLock().lock();
        try {
            return dictionary.get(word.toLowerCase());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a new word with its meanings to the dictionary
     * 
     * @param word The word to add
     * @param meanings List of meanings for the word
     * @return true if the word was added, false if it already exists
     */
    public boolean add(String word, List<String> meanings) {
        if (word == null || word.trim().isEmpty() || meanings == null || meanings.isEmpty()) {
            return false;
        }
        
        String normalizedWord = word.toLowerCase().trim();
        
        lock.writeLock().lock();
        try {
            if (dictionary.containsKey(normalizedWord)) {
                return false;
            }
            
            dictionary.put(normalizedWord, new WordEntry(normalizedWord, new ArrayList<>(meanings)));
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a word from the dictionary
     * 
     * @param word The word to remove
     * @return true if the word was removed, false if it doesn't exist
     */
    public boolean remove(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        String normalizedWord = word.toLowerCase().trim();
        
        lock.writeLock().lock();
        try {
            return dictionary.remove(normalizedWord) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds a new meaning to an existing word
     * 
     * @param word The existing word
     * @param newMeaning The new meaning to add
     * @return true if the meaning was added, false if word doesn't exist or meaning already exists
     */
    public boolean addMeaning(String word, String newMeaning) {
        if (word == null || word.trim().isEmpty() || newMeaning == null || newMeaning.trim().isEmpty()) {
            return false;
        }
        
        String normalizedWord = word.toLowerCase().trim();
        String normalizedMeaning = newMeaning.trim();
        
        lock.writeLock().lock();
        try {
            WordEntry entry = dictionary.get(normalizedWord);
            if (entry == null) {
                return false;
            }
            
            if (entry.getMeanings().contains(normalizedMeaning)) {
                return false;
            }
            
            entry.addMeaning(normalizedMeaning);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates an existing meaning of a word with a new meaning
     * 
     * @param word The existing word
     * @param oldMeaning The existing meaning to update
     * @param newMeaning The new meaning to replace with
     * @return true if the meaning was updated, false if word or old meaning doesn't exist
     */
    public boolean updateMeaning(String word, String oldMeaning, String newMeaning) {
        if (word == null || word.trim().isEmpty() || 
            oldMeaning == null || oldMeaning.trim().isEmpty() ||
            newMeaning == null || newMeaning.trim().isEmpty()) {
            return false;
        }
        
        String normalizedWord = word.toLowerCase().trim();
        String normalizedOldMeaning = oldMeaning.trim();
        String normalizedNewMeaning = newMeaning.trim();
        
        if (normalizedOldMeaning.equals(normalizedNewMeaning)) {
            return true; // No change needed
        }
        
        lock.writeLock().lock();
        try {
            WordEntry entry = dictionary.get(normalizedWord);
            if (entry == null) {
                return false;
            }
            
            return entry.updateMeaning(normalizedOldMeaning, normalizedNewMeaning);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Clears all entries from the dictionary
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            dictionary.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
} 