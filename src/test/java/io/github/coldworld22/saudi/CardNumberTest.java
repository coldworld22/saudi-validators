package io.github.coldworld22.saudi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CardNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {"4111111111111111", "4111 1111 1111 1111", "5555-5555-5555-4444", "378282246310005"})
    void acceptsValidCards(String input) {
        assertThat(CardNumber.isValid(input)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"4111111111111112", "411111111111", "41111111111111111111", "4111abcd11111111"})
    void rejectsInvalidCards(String input) {
        assertThat(CardNumber.isValid(input)).isFalse();
    }

    @Test
    void masksAllButLastFour() {
        assertThat(CardNumber.mask("4111 1111 1111 1111")).isEqualTo("************1111");
        assertThatThrownBy(() -> CardNumber.mask("1234")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void luhnRejectsNonDigits() {
        assertThat(Luhn.isValid(null)).isFalse();
        assertThat(Luhn.isValid("")).isFalse();
        assertThat(Luhn.isValid("79927398713")).isTrue();
        assertThat(Luhn.isValid("7992739871x")).isFalse();
    }

    @Test
    void digitsNormalisation() {
        assertThat(Digits.normalize(null)).isNull();
        assertThat(Digits.normalize("١ ۲.3")).isEqualTo("123");
    }
}
