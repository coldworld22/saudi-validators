package io.github.coldworld22.saudi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SaudiIbanTest {

    private static final String AL_RAJHI = "SA0380000000608010167519";

    @Test
    void parsesAndExposesParts() {
        SaudiIban iban = SaudiIban.parse(AL_RAJHI).orElseThrow();

        assertThat(iban.bankCode()).isEqualTo("80");
        assertThat(iban.accountNumber()).isEqualTo("000000608010167519");
        assertThat(iban.bankName()).contains("Al Rajhi Bank");
    }

    @Test
    void acceptsPrintFormLowerCaseAndArabicDigits() {
        assertThat(SaudiIban.parse("sa03 8000 0000 6080 1016 7519")).get()
                .extracting(SaudiIban::value).isEqualTo(AL_RAJHI);
        assertThat(SaudiIban.isValid("SA٠٣" + AL_RAJHI.substring(4))).isTrue();
    }

    @Test
    void knowsMajorBanksButAcceptsUnknownCodes() {
        assertThat(SaudiIban.parse("SA4210000001234567890123").orElseThrow().bankName())
                .contains("Saudi National Bank");
    }

    @Test
    void formatsAndMasks() {
        SaudiIban iban = SaudiIban.parse(AL_RAJHI).orElseThrow();

        assertThat(iban.formatted()).isEqualTo("SA03 8000 0000 6080 1016 7519");
        assertThat(iban.masked()).isEqualTo("SA******************7519");
        assertThat(iban.toString()).doesNotContain(AL_RAJHI);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "SA0380000000608010167518",   // wrong check digits
        "AE070331234567890123456",    // another country
        "SA038000000060801016751",    // too short
        "SA03800000006080101675190",  // too long
        "SAX380000000608010167519",   // letters in check digits
        "SA0380000000608010167_19",   // illegal character
    })
    void rejectsInvalidInput(String input) {
        assertThat(SaudiIban.isValid(input)).isFalse();
        assertThat(SaudiIban.parse(input)).isEmpty();
    }

    @Test
    void constructorRequiresElectronicForm() {
        assertThatThrownBy(() -> new SaudiIban("SA03 8000 0000 6080 1016 7519"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new SaudiIban(AL_RAJHI).value()).isEqualTo(AL_RAJHI);
    }

    @Test
    void unknownBankHasNoName() {
        // Same account with a bank code nobody uses; recompute the check digits for it.
        String bban = "99" + "000000608010167519";
        String iban = withCheckDigits(bban);

        assertThat(SaudiIban.parse(iban).orElseThrow().bankName()).isEmpty();
    }

    private static String withCheckDigits(String bban) {
        for (int cd = 0; cd < 100; cd++) {
            String candidate = String.format("SA%02d%s", cd, bban);
            if (SaudiIban.isValid(candidate)) {
                return candidate;
            }
        }
        throw new AssertionError("no check digits found");
    }
}
