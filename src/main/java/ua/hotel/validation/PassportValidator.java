package ua.hotel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for {@link ValidPassport}.
 */
public class PassportValidator implements ConstraintValidator<ValidPassport, String> {

    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Z]{2}\\d{6}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Null/blank checks are handled by @NotBlank
        }
        return PASSPORT_PATTERN.matcher(value.trim()).matches();
    }
}
