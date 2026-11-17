package io.github.coldworld22.saudi.validation;

import io.github.coldworld22.saudi.SaudiIban;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The annotated string must be a valid Saudi IBAN (spaces and lower case allowed). {@code null} is valid. */
@Documented
@Constraint(validatedBy = ValidSaudiIban.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaudiIban {

    /**
     * The error message.
     *
     * @return the message template
     */
    String message() default "must be a valid Saudi IBAN";

    /**
     * Validation groups.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * Payload.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

    /** Validator for {@link ValidSaudiIban}. */
    final class Validator implements ConstraintValidator<ValidSaudiIban, CharSequence> {

        /** Creates the validator; called by the validation provider. */
        public Validator() {}

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            return value == null || SaudiIban.isValid(value.toString());
        }
    }
}
