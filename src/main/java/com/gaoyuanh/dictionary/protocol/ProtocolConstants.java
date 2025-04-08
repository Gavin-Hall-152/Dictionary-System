package com.gaoyuanh.dictionary.protocol;

/**
 * Constants for the dictionary protocol.
 * Defines operation types and status codes for client-server communication.
 */
public class ProtocolConstants {
    // Operation types
    public static final String OPERATION_SEARCH = "SEARCH";
    public static final String OPERATION_ADD = "ADD";
    public static final String OPERATION_REMOVE = "REMOVE";
    public static final String OPERATION_ADD_MEANING = "ADD_MEANING";
    public static final String OPERATION_UPDATE_MEANING = "UPDATE_MEANING";
    
    // Status codes
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";
    public static final String STATUS_DUPLICATE = "DUPLICATE";
    public static final String STATUS_MEANING_NOT_FOUND = "MEANING_NOT_FOUND";
    
    // Dictionary file format
    public static final String WORD_SEPARATOR = ":";
    public static final String MEANING_PREFIX = "    ";
    public static final String COMMENT_PREFIX = "#";
} 