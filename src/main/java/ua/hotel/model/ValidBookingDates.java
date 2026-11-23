package ua.hotel.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookingDatesValidator.class)
@Documented
public @interface ValidBookingDates {
    String message() default "Дата виїзду має бути пізнішою за дату заїзду";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
