package com.uno.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for string operations and network logging
 */
public class StringUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Safely compares two strings, handles null and trims both strings
     * 
     * @param str1 First string
     * @param str2 Second string
     * @return true if strings are equal after trimming, false otherwise
     */
    public static boolean safeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.trim().equals(str2.trim());
    }
    
    /**
     * Creates a formatted log message with timestamp
     * 
     * @param logType Type of log (NETWORK, TCP, UDP, etc)
     * @param message Message to log
     * @return Formatted log message with timestamp
     */
    public static String formatLogMessage(String logType, String message) {
        return String.format("[%s][%s] %s", 
                DATE_FORMAT.format(new Date()), 
                logType, 
                message);
    }
    
    /**
     * Formats network-related log messages in Vietnamese without accents
     * Focuses on highlighting networking concepts
     * 
     * @param component Component name (CLIENT, SERVER, SOCKET, etc)
     * @param operation Network operation (CONNECT, SEND, RECEIVE, etc)
     * @param message Detailed message about the operation
     * @return Formatted log message for network operations
     */
    public static String formatNetworkLog(String component, String operation, String message) {
        // Simplified log format focusing on network operations
        return formatLogMessage("NETWORK", 
                String.format("%s | %s | %s", component, operation, message));
    }
}