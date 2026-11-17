package ua.hotel.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Guest(
        @NotBlank(message = "Номер паспорта не може бути порожнім")
        @Pattern(regexp = "^[A-Z]{2}[0-9]{6}$", message = "Невірний формат паспорта (приклад: AA123456)")
        String passportNumber,

        @NotBlank(message = "Ім'я не може бути порожнім")
        @Size(min = 2, max = 50, message = "Ім'я має містити від 2 до 50 символів")
        String firstName,

        @NotBlank(message = "Прізвище не може бути порожнім")
        @Size(min = 2, max = 50, message = "Прізвище має містити від 2 до 50 символів")
        String lastName,

        @NotNull(message = "Дата народження не може бути null")
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
    }

    @Override
    public int compareTo(Guest other) {
        if (other == null) {
            return 1;
        }
        return this.passportNumber.compareTo(other.passportNumber);
    }
}
