package ua.hotel.model;

import ua.common.BaseEntity;
import ua.hotel.util.ValidationHelper;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class Guest extends BaseEntity {
    private final String passportNumber;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;

    public Guest(String passportNumber, String firstName, String lastName, LocalDate birthDate) {
        super();
        
        String trimmedPassport = ValidationHelper.requireNonNullOrBlank(passportNumber, "passportNumber").trim();
        String trimmedFirstName = ValidationHelper.requireNonNullOrBlank(firstName, "firstName").trim();
        String trimmedLastName = ValidationHelper.requireNonNullOrBlank(lastName, "lastName").trim();
        ValidationHelper.requireNonNull(birthDate, "birthDate");

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException(
                "Guest must be at least 18 years old. Expected >= 18, but got age: " + age
            );
        }

        this.passportNumber = trimmedPassport;
        this.firstName = trimmedFirstName;
        this.lastName = trimmedLastName;
        this.birthDate = birthDate;
    }

    public String getPassportNumber() { return passportNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(passportNumber, guest.passportNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passportNumber);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "passportNumber='" + passportNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                '}';
    }
}