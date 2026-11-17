package io.github.coldworld22.saudi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NationalIdTest {

    @Test
    void parsesCitizenId() {
        NationalId id = NationalId.parse("1012345672").orElseThrow();

        assertThat(id.value()).isEqualTo("1012345672");
        assertThat(id.type()).isEqualTo(NationalId.Type.CITIZEN);
    }

    @Test
    void parsesResidentIqama() {
        assertThat(NationalId.parse("2123456788")).get()
                .extracting(NationalId::type).isEqualTo(NationalId.Type.RESIDENT);
    }

    @Test
    void acceptsArabicIndicDigitsAndSeparators() {
        assertThat(NationalId.parse("١٠١٢٣٤٥٦٧٢"))
                .get().extracting(NationalId::value).isEqualTo("1012345672");
        assertThat(NationalId.parse("۱۰۹۸۷۶۵۴۳۹")).isPresent();
        assertThat(NationalId.isValid("101-234-5672")).isTrue();
        assertThat(NationalId.isValid(" 1012 345 672 ")).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "1012345678",   // wrong check digit
        "1012345627",   // neighbouring digits swapped
        "3012345675",   // must start with 1 or 2
        "101234567",    // too short
        "10123456720",  // too long
        "10123456a2",   // not a digit
    })
    void rejectsInvalidInput(String input) {
        assertThat(NationalId.isValid(input)).isFalse();
        assertThat(NationalId.parse(input)).isEmpty();
    }

    @Test
    void masksForLogs() {
        NationalId id = NationalId.parse("1012345672").orElseThrow();

        assertThat(id.masked()).isEqualTo("101****672");
        assertThat(id.toString()).isEqualTo("NationalId[101****672, CITIZEN]").doesNotContain("1012345672");
    }

    @Test
    void constructorRejectsInvalidOrMislabelledValues() {
        assertThatThrownBy(() -> new NationalId("1012345678", NationalId.Type.CITIZEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NationalId("1012345672", NationalId.Type.RESIDENT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NationalId("101 234 5672", NationalId.Type.CITIZEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NationalId(null, NationalId.Type.CITIZEN))
                .isInstanceOf(NullPointerException.class);
    }
}
