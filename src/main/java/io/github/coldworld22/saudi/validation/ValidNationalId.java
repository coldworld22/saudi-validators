package io.github.coldworld22.saudi.validation;

import io.github.coldworld22.saudi.NationalId;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.Set;

/**
 * The annotated string must be a valid Saudi National ID or Iqama number. {@code null} is valid;
 * combine with {@code @NotNull} when the field is required.
 *
 * <pre>{@code
 * record OpenAccountRequest(@NotNull @ValidNationalId(allow = Type.CITIZEN) String nationalId) {}
 * }</pre>
 */
@Documented
@Constraint(validatedBy = ValidNationalId.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNationalId {

    /**
     * The error message.
     *
     * @return the message template
     */
    String message() default "must be a valid Saudi National ID or Iqama number";

    /**
     * Which holders are accepted; both by default.
     *
     * @return the accepted ID types
     */
    NationalId.Type[] allow() default {NationalId.Type.CITIZEN, NationalId.Type.RESIDENT};

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

    /** Validator for {@link ValidNationalId}. */
    final class Validator implements ConstraintValidator<ValidNationalId, CharSequence> {

        private Set<NationalId.Type> allowed = EnumSet.allOf(NationalId.Type.class);

        /** Creates the validator; called by the validation provider. */
        public Validator() {}

        @Override
        public void initialize(ValidNationalId annotation) {
            allowed = annotation.allow().length == 0
                    ? EnumSet.noneOf(NationalId.Type.class)
                    : EnumSet.of(annotation.allow()[0], annotation.allow());
        }

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            return NationalId.parse(value.toString()).map(id -> allowed.contains(id.type())).orElse(false);
        }
    }
}
