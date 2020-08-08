package com.tnpacs.dicoogle.dicomwebplugin.utils;

public class StringUtils {
    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    public static String padLeft(String s, int n, char padChar) {
        return String.format("%" + n + "s", s).replace(' ', padChar);
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padRight(String s, int n, char padChar) {
        return String.format("%-" + n + "s", s).replace(' ', padChar);
    }
}
