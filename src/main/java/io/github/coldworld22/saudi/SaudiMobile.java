package io.github.coldworld22.saudi;

import java.util.Objects;
import java.util.Optional;

/**
 * A Saudi mobile number, stored in E.164 form ({@code +9665XXXXXXXX}).
 *
 * <p>Accepts every way people actually write it: {@code 0551234567}, {@code 551234567},
 * {@code 966551234567}, {@code 00966551234567}, {@code +966 55 123 4567}, and Arabic-Indic digits.
 *
 * @param e164 the number in E.164 form
 */
public record SaudiMobile(String e164) {

    /**
     * Canonical constructor; rejects anything that is not a Saudi mobile number in E.164 form.
     *
     * @throws IllegalArgumentException if {@code e164} is not {@code +9665} followed by 8 digits
     */
    public SaudiMobile {
        Objects.requireNonNull(e164, "e164");
        if (e164.length() != 13 || !e164.startsWith("+9665") || !Digits.allDigits(e164.substring(1))) {
            throw new IllegalArgumentException("Not a Saudi mobile number in E.164 form");
        }
    }

    /**
     * Parses user input in any common format.
     *
     * @param input raw input, may be {@code null}
     * @return the number, or empty if the input is not a Saudi mobile number
     */
    public static Optional<SaudiMobile> parse(String input) {
        String s = Digits.normalize(input);
        if (s == null) {
            return Optional.empty();
        }
        if (s.startsWith("+")) {
            s = s.substring(1);
            if (!s.startsWith("966")) {
                return Optional.empty();
            }
        } else if (s.startsWith("00")) {
            s = s.substring(2);
            if (!s.startsWith("966")) {
                return Optional.empty();
            }
        }
        String subscriber;
        if (s.startsWith("966")) {
            subscriber = s.substring(3);
        } else if (s.startsWith("0")) {
            subscriber = s.substring(1);
        } else {
            subscriber = s;
        }
        if (subscriber.length() != 9 || subscriber.charAt(0) != '5' || !Digits.allDigits(subscriber)) {
            return Optional.empty();
        }
        return Optional.of(new SaudiMobile("+966" + subscriber));
    }

    /**
     * Checks user input.
     *
     * @param input raw input, may be {@code null}
     * @return {@code true} if {@code input} is a Saudi mobile number in any accepted format
     */
    public static boolean isValid(String input) {
        return parse(input).isPresent();
    }

    /**
     * Local form.
     *
     * @return for example {@code 0551234567}
     */
    public String national() {
        return "0" + e164.substring(4);
    }

    /**
     * Masked form for logs and screens.
     *
     * @return the operator prefix and the last two digits, for example {@code +96655*****67}
     */
    public String masked() {
        return e164.substring(0, 6) + "*****" + e164.substring(11);
    }

    /** Never prints the full number. */
    @Override
    public String toString() {
        return "SaudiMobile[" + masked() + "]";
    }
}
