package ua.hotel.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Constraint(validatedBy = PassportValidator.class)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassport {

    String message() default "Номер паспорта має складатися з 2 великих латинських літер та 6 цифр (наприклад, AA123456)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
