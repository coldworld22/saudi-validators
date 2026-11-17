package io.github.coldworld22.saudi;

/**
 * Input clean-up shared by every parser. Saudi users often type numbers on an Arabic keyboard, so
 * Arabic-Indic and Extended Arabic-Indic digits are converted to ASCII before any check runs.
 * Spaces, dashes, dots and parentheses used as separators are dropped.
 */
public final class Digits {

    private Digits() {}

    /**
     * Converts Arabic-Indic digits to ASCII and removes spaces, dashes, dots and parentheses.
     *
     * @param input raw user input, may be {@code null}
     * @return the cleaned string, or {@code null} if the input was {@code null}
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= '٠' && c <= '٩') {
                out.append((char) ('0' + (c - '٠')));
            } else if (c >= '۰' && c <= '۹') {
                out.append((char) ('0' + (c - '۰')));
            } else if (!isSeparator(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean isSeparator(char c) {
        return c == ' ' || c == '-' || c == '.' || c == '(' || c == ')' || c == ' ';
    }

    static boolean allDigits(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
