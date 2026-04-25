package com.auth.util;

public final class NormalizationUtil {
    private NormalizationUtil() {}

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public static boolean isGmail(String email) {
        if (email == null) return false;
        String e = normalizeEmail(email);
        return e.matches("^[a-z0-9._%+-]+@gmail\\.com$");
    }
}