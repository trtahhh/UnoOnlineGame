package com.uno.utils;

/**
 * Class chứa các phương thức tiện ích cho xử lý chuỗi
 */
public class StringUtils {
    /**
     * So sánh hai chuỗi một cách an toàn, đảm bảo trim và kiểm tra null
     * 
     * @param str1 Chuỗi thứ nhất
     * @param str2 Chuỗi thứ hai
     * @return true nếu hai chuỗi bằng nhau sau khi trim, ngược lại false
     */
    public static boolean safeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.trim().equals(str2.trim());
    }
}