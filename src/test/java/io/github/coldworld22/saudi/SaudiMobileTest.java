package io.github.coldworld22.saudi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SaudiMobileTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "0551234567",
        "551234567",
        "966551234567",
        "00966551234567",
        "+966551234567",
        "+966 55 123 4567",
        "(055) 123-4567",
        "٠٥٥١٢٣٤٥٦٧",
    })
    void normalisesEveryCommonFormatToE164(String input) {
        assertThat(SaudiMobile.parse(input)).get()
                .extracting(SaudiMobile::e164).isEqualTo("+966551234567");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "0112345678",       // Riyadh landline
        "055123456",        // too short
        "05512345678",      // too long
        "+971551234567",    // UAE
        "0097155123456",    // UAE, international prefix
        "05512a4567",       // not a digit
    })
    void rejectsNonSaudiMobiles(String input) {
        assertThat(SaudiMobile.isValid(input)).isFalse();
    }

    @Test
    void convertsAndMasks() {
        SaudiMobile mobile = SaudiMobile.parse("0551234567").orElseThrow();

        assertThat(mobile.national()).isEqualTo("0551234567");
        assertThat(mobile.masked()).isEqualTo("+96655*****67");
        assertThat(mobile.toString()).doesNotContain("551234567");
    }

    @Test
    void constructorRequiresE164() {
        assertThatThrownBy(() -> new SaudiMobile("0551234567")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SaudiMobile("+96655123456a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SaudiMobile("+966112345678")).isInstanceOf(IllegalArgumentException.class);
    }
}
