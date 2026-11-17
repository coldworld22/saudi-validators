package io.github.coldworld22.saudi;

/** The Luhn (mod 10) checksum used by payment cards and by Saudi National ID and Iqama numbers. */
public final class Luhn {

    private Luhn() {}

    /**
     * Checks a string of ASCII digits against the Luhn algorithm.
     *
     * @param digits ASCII digits only, no separators
     * @return {@code true} if the checksum is valid; {@code false} for empty or non-digit input
     */
    public static boolean isValid(String digits) {
        if (!Digits.allDigits(digits)) {
            return false;
        }
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
