package io.github.coldworld22.saudi;

/** Payment card numbers (mada, Visa, Mastercard, ...): 13 to 19 digits with a valid Luhn check digit. */
public final class CardNumber {

    private CardNumber() {}

    /**
     * Checks user input.
     *
     * @param input raw input with optional spaces or dashes, may be {@code null}
     * @return {@code true} if the input is a plausible card number
     */
    public static boolean isValid(String input) {
        String digits = Digits.normalize(input);
        return digits != null && digits.length() >= 13 && digits.length() <= 19 && Luhn.isValid(digits);
    }

    /**
     * Masks all but the last four digits, the most PCI DSS allows to be displayed with the last four.
     *
     * @param input a card number
     * @return for example {@code ************1111}
     * @throws IllegalArgumentException if the input is not a valid card number
     */
    public static String mask(String input) {
        if (!isValid(input)) {
            throw new IllegalArgumentException("Not a valid card number");
        }
        String digits = Digits.normalize(input);
        return "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
    }
}
