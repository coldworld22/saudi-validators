package io.github.coldworld22.saudi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class PiiMaskerTest {

    private final PiiMasker masker = PiiMasker.all();

    @Test
    void masksSaudiIdentifiersAndRestoresThem() {
        String text = "My ID is 1012345672, IBAN SA0380000000608010167519, call 0551234567, "
                + "card 4111 1111 1111 1111, mail a.b@example.com";

        PiiMasker.Result result = masker.mask(text);

        assertThat(result.text())
                .isEqualTo("My ID is [NATIONAL_ID_1], IBAN [IBAN_1], call [PHONE_1], "
                        + "card [CARD_1], mail [EMAIL_1]");
        assertThat(result.count()).isEqualTo(5);
        assertThat(result.restore(result.text())).isEqualTo(text);
    }

    @Test
    void sameValueGetsSamePlaceholderAcrossMessages() {
        PiiMasker.Result result = masker.mask(List.of("ID 2123456788", "again: 2123456788", "other 1098765439"));

        assertThat(result.texts())
                .containsExactly("ID [NATIONAL_ID_1]", "again: [NATIONAL_ID_1]", "other [NATIONAL_ID_2]");
        assertThat(result.replacements()).containsEntry("[NATIONAL_ID_2]", "1098765439");
    }

    @Test
    void restoresPlaceholdersInAModelAnswer() {
        PiiMasker.Result result = masker.mask("Transfer to SA0380000000608010167519 please");

        assertThat(result.restore("I have scheduled the transfer to [IBAN_1]."))
                .isEqualTo("I have scheduled the transfer to SA0380000000608010167519.");
    }

    @Test
    void leavesNumbersThatFailTheirChecksumAlone() {
        String text = "Order 12345 shipped 2026-11-16, total 349.99 SAR, ref 1012345678, "
                + "card-like 4111111111111112, iban-like SA0380000000608010167518";

        assertThat(masker.mask(text).text()).isEqualTo(text);
    }

    @Test
    void phonesInInternationalFormsAreNotMistakenForCards() {
        PiiMasker.Result result = masker.mask("+966551234567 / 00966551234567 / 966551234567");

        assertThat(result.text()).isEqualTo("[PHONE_1] / [PHONE_2] / [PHONE_3]");
    }

    @Test
    void canBeLimitedToSomeTypes() {
        PiiMasker idsOnly = PiiMasker.of(PiiType.NATIONAL_ID);

        assertThat(idsOnly.mask("1012345672 a.b@example.com").text()).isEqualTo("[NATIONAL_ID_1] a.b@example.com");
    }

    @Test
    void resultIsImmutable() {
        PiiMasker.Result result = masker.mask("1012345672");

        assertThatThrownBy(() -> result.replacements().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.texts().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
