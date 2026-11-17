package io.github.coldworld22.saudi;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A Saudi IBAN: {@code SA}, two check digits, a two-digit bank code and an 18-character account
 * number, 24 characters in all, verified with the ISO 13616 mod-97 check.
 *
 * @param value the IBAN in electronic form (upper case, no spaces)
 */
public record SaudiIban(String value) {

    private static final BigInteger NINETY_SEVEN = BigInteger.valueOf(97);

    /** Bank codes of the largest Saudi banks, for display only; unknown codes are still valid. */
    private static final Map<String, String> BANKS = Map.ofEntries(
            Map.entry("05", "Alinma Bank"),
            Map.entry("10", "Saudi National Bank"),
            Map.entry("15", "Bank AlBilad"),
            Map.entry("20", "Riyad Bank"),
            Map.entry("30", "Arab National Bank"),
            Map.entry("45", "Saudi Awwal Bank"),
            Map.entry("55", "Banque Saudi Fransi"),
            Map.entry("60", "Bank AlJazira"),
            Map.entry("65", "Saudi Investment Bank"),
            Map.entry("80", "Al Rajhi Bank"));

    /**
     * Canonical constructor; rejects anything that is not a valid Saudi IBAN in electronic form.
     *
     * @throws IllegalArgumentException if {@code value} is not a valid Saudi IBAN in electronic form
     */
    public SaudiIban {
        Objects.requireNonNull(value, "value");
        if (!isCanonical(value)) {
            throw new IllegalArgumentException("Not a valid Saudi IBAN");
        }
    }

    /**
     * Parses user input such as {@code "sa03 8000 0000 6080 1016 7519"}.
     *
     * @param input raw input, may be {@code null}
     * @return the IBAN, or empty if the input is not a valid Saudi IBAN
     */
    public static Optional<SaudiIban> parse(String input) {
        String cleaned = clean(input);
        return isCanonical(cleaned) ? Optional.of(new SaudiIban(cleaned)) : Optional.empty();
    }

    /**
     * Checks user input.
     *
     * @param input raw input, may be {@code null}
     * @return {@code true} if {@code input} is a valid Saudi IBAN after normalisation
     */
    public static boolean isValid(String input) {
        return isCanonical(clean(input));
    }

    private static boolean isCanonical(String iban) {
        if (iban == null || iban.length() != 24 || !iban.startsWith("SA")) {
            return false;
        }
        if (!Digits.allDigits(iban.substring(2, 6))) {
            return false;
        }
        for (int i = 6; i < 24; i++) {
            char c = iban.charAt(i);
            boolean ok = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z');
            if (!ok) {
                return false;
            }
        }
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder(rearranged.length() * 2);
        for (int i = 0; i < rearranged.length(); i++) {
            numeric.append(Character.digit(rearranged.charAt(i), 36));
        }
        return new BigInteger(numeric.toString()).mod(NINETY_SEVEN).intValue() == 1;
    }

    private static String clean(String input) {
        String normalized = Digits.normalize(input);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * The bank code.
     *
     * @return the two-digit bank code, for example {@code 80}
     */
    public String bankCode() {
        return value.substring(4, 6);
    }

    /**
     * The account number.
     *
     * @return the 18-character account number after the bank code
     */
    public String accountNumber() {
        return value.substring(6);
    }

    /**
     * The bank's name, if known.
     *
     * @return the bank's name when the code is one of the major banks this library knows
     */
    public Optional<String> bankName() {
        return Optional.ofNullable(BANKS.get(bankCode()));
    }

    /**
     * Print form.
     *
     * @return groups of four, for example {@code SA03 8000 0000 6080 1016 7519}
     */
    public String formatted() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i += 4) {
            if (i > 0) {
                out.append(' ');
            }
            out.append(value, i, i + 4);
        }
        return out.toString();
    }

    /**
     * Masked form for logs and screens.
     *
     * @return the country code, stars, and the last four characters
     */
    public String masked() {
        return "SA" + "*".repeat(18) + value.substring(20);
    }

    /** Never prints the full IBAN. */
    @Override
    public String toString() {
        return "SaudiIban[" + masked() + "]";
    }
}
