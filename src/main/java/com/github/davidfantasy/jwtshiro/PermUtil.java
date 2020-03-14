package com.github.davidfantasy.jwtshiro;

import java.util.HashMap;
import java.util.Map;

public class PermUtil {

    private static Map<String, String> permUrlMapping = new HashMap<>();

    protected static void addUrlMapping(String url, String perm) {
        permUrlMapping.put(url, perm);
    }

    public static Map<String, String> getAllUrlMapping() {
        return permUrlMapping;
    }

}
