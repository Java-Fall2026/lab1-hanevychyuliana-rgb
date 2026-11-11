package ua.hotel.model;

import ua.hotel.util.ValidationHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public record Guest(
        String passportNumber,
        String firstName,
        String lastName,
        LocalDate birthDate,
        LocalDateTime createdAt
) implements Comparable<Guest> {
    public Guest {
        passportNumber = ValidationHelper.requireNonEmptyTrimmed(passportNumber, "passportNumber");
        firstName = ValidationHelper.requireNonEmpty(firstName, "firstName");
        lastName = ValidationHelper.requireNonEmpty(lastName, "lastName");
        ValidationHelper.requireNonNull(birthDate, "birthDate");

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (Period.between(birthDate, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Guest must be at least 18 years old");
        }
    }

    @Override
    public int compareTo(Guest other) {
        if (other == null) {
            return 1;
        }
        // Natural ordering by passportNumber
        return this.passportNumber.compareTo(other.passportNumber);
    }
}
