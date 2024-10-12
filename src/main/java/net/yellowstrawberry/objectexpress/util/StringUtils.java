package net.yellowstrawberry.objectexpress.util;

public class StringUtils {
    public static String camelToSnake(String s) {
        return s.replaceAll("([a-z])([A-Z]+)","$1_$2").toLowerCase();
    }
}
