package ua.hotel.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import ua.hotel.validation.ValidPassport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public record Guest(
        @NotBlank(message = "Номер паспорта не може бути порожнім")
        @ValidPassport
        String passportNumber,

        @NotBlank(message = "Ім'я гостя не може бути порожнім")
        String firstName,

        @NotBlank(message = "Прізвище гостя не може бути порожнім")
        String lastName,

        @NotNull(message = "Дата народження не може бути порожньою")
        @Past(message = "Дата народження має бути в минулому")
        LocalDate birthDate,

        LocalDateTime createdAt
) implements Comparable<Guest> {
    public Guest {
        if (passportNumber != null) {
            passportNumber = passportNumber.trim();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (birthDate != null && Period.between(birthDate, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Guest must be at least 18 years old");
        }
    }

    @Override
    public int compareTo(Guest other) {
        if (other == null) {
            return 1;
        }
        return this.passportNumber.compareTo(other.passportNumber);
    }
}
