package io.github.coldworld22.saudi.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.coldworld22.saudi.NationalId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConstraintsTest {

    record CustomerRequest(
            @ValidNationalId String nationalId,
            @ValidNationalId(allow = NationalId.Type.CITIZEN) String citizenOnly,
            @ValidSaudiIban String iban,
            @ValidSaudiMobile String mobile) {}

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static Set<String> invalidFields(Object bean) {
        Set<ConstraintViolation<Object>> violations = validator.validate(bean);
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void validRequestPasses() {
        var request = new CustomerRequest(
                "2123456788", "1012345672", "SA03 8000 0000 6080 1016 7519", "+966 55 123 4567");

        assertThat(invalidFields(request)).isEmpty();
    }

    @Test
    void nullsAreLeftToNotNull() {
        assertThat(invalidFields(new CustomerRequest(null, null, null, null))).isEmpty();
    }

    @Test
    void reportsEachInvalidField() {
        var request = new CustomerRequest("1012345678", "2123456788", "SA00", "0112345678");

        assertThat(invalidFields(request)).containsExactlyInAnyOrder("nationalId", "citizenOnly", "iban", "mobile");
    }

    @Test
    void messagesAreReadable() {
        var request = new CustomerRequest("bad", null, null, null);

        assertThat(validator.validate(request))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("must be a valid Saudi National ID or Iqama number");
    }
}
