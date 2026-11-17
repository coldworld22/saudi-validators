package io.github.coldworld22.saudi.validation;

import io.github.coldworld22.saudi.SaudiMobile;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The annotated string must be a Saudi mobile number in any common format. {@code null} is valid. */
@Documented
@Constraint(validatedBy = ValidSaudiMobile.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaudiMobile {

    /**
     * The error message.
     *
     * @return the message template
     */
    String message() default "must be a valid Saudi mobile number";

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

    /** Validator for {@link ValidSaudiMobile}. */
    final class Validator implements ConstraintValidator<ValidSaudiMobile, CharSequence> {

        /** Creates the validator; called by the validation provider. */
        public Validator() {}

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            return value == null || SaudiMobile.isValid(value.toString());
        }
    }
}
