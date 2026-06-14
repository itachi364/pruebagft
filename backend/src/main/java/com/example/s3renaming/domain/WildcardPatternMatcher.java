package com.example.s3renaming.domain;

import java.util.regex.Pattern;

public final class WildcardPatternMatcher {

    private WildcardPatternMatcher() {
    }

    public static boolean matches(String wildcardPattern, String value) {
        if (wildcardPattern == null || wildcardPattern.isBlank() || value == null) {
            return false;
        }
        StringBuilder regex = new StringBuilder("^");
        for (char character : wildcardPattern.toCharArray()) {
            if (character == '*') {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }
        regex.append("$");
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE).matcher(value).matches();
    }
}

