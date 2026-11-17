package io.github.coldworld22.saudi;

/** Kinds of personal data {@link PiiMasker} finds in free text. The name is used in placeholders. */
public enum PiiType {
    /** Saudi IBAN, checked with mod-97. */
    IBAN,
    /** Payment card number, checked with Luhn. */
    CARD,
    /** Saudi mobile number. */
    PHONE,
    /** National ID or Iqama number, checked with Luhn. */
    NATIONAL_ID,
    /** Email address. */
    EMAIL
}
