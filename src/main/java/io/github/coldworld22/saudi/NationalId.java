package io.github.coldworld22.saudi;

import java.util.Objects;
import java.util.Optional;

/**
 * A Saudi National ID (citizens, starts with 1) or Iqama number (residents, starts with 2).
 *
 * <p>Both are 10 digits and end in a Luhn check digit, so a typo in any single digit, and most
 * swaps of two neighbouring digits, are caught before the number reaches a database or a
 * government API.
 *
 * @param value the 10 ASCII digits
 * @param type  citizen or resident, from the first digit
 */
public record NationalId(String value, Type type) {

    /** Who the number belongs to, decided by its first digit. */
    public enum Type {
        /** Saudi citizen, National ID starting with 1. */
        CITIZEN,
        /** Resident, Iqama number starting with 2. */
        RESIDENT
    }

    /**
     * Canonical constructor; rejects anything that is not a valid ID in canonical form.
     *
     * @throws IllegalArgumentException if {@code value} is not a valid National ID or Iqama
     */
    public NationalId {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(type, "type");
        if (!isCanonical(value) || typeOf(value) != type) {
            throw new IllegalArgumentException("Not a valid Saudi National ID or Iqama number");
        }
    }

    /**
     * Parses user input, accepting Arabic-Indic digits and separators.
     *
     * @param input raw input, may be {@code null}
     * @return the ID, or empty if the input is not a valid National ID or Iqama
     */
    public static Optional<NationalId> parse(String input) {
        String digits = Digits.normalize(input);
        if (!isCanonical(digits)) {
            return Optional.empty();
        }
        return Optional.of(new NationalId(digits, typeOf(digits)));
    }

    /**
     * Checks user input.
     *
     * @param input raw input, may be {@code null}
     * @return {@code true} if {@code input} is a valid National ID or Iqama after normalisation
     */
    public static boolean isValid(String input) {
        return isCanonical(Digits.normalize(input));
    }

    private static boolean isCanonical(String digits) {
        return digits != null
                && digits.length() == 10
                && (digits.charAt(0) == '1' || digits.charAt(0) == '2')
                && Luhn.isValid(digits);
    }

    private static Type typeOf(String digits) {
        return digits.charAt(0) == '1' ? Type.CITIZEN : Type.RESIDENT;
    }

    /**
     * Masked form for logs and screens, keeping the first and last three digits.
     *
     * @return for example {@code 101****672}
     */
    public String masked() {
        return value.substring(0, 3) + "****" + value.substring(7);
    }

    /** Never prints the full number, so an ID logged by accident stays masked. */
    @Override
    public String toString() {
        return "NationalId[" + masked() + ", " + type + "]";
    }
}
