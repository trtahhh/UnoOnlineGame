package com.uno.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for string operations and logging
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
     * @param source Source of the log (class or component name)
     * @param message Message to log
     * @return Formatted log message with timestamp
     */
    public static String formatLogMessage(String source, String message) {
        return String.format("[%s][%s] %s", 
                DATE_FORMAT.format(new Date()), 
                source, 
                message);
    }
    
    /**
     * Formats network-related log messages in Vietnamese with accents
     * 
     * @param className Class name generating the log
     * @param methodName Method name generating the log
     * @param message Message to log
     * @return Formatted log message for network operations
     */
    public static String formatNetworkLog(String className, String methodName, String message) {
        return formatLogMessage("MẠNG", 
                String.format("%s.%s: %s", className, methodName, message));
    }
}