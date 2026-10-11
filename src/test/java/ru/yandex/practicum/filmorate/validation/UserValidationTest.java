package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserValidationTest {

    @Autowired
    private Validator validator;

    private User validUser() {
        User user = new User();
        user.setEmail("belyash@mail.com");
        user.setLogin("belyash");
        user.setName("belyash");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        return user;
    }

    // ==================== EMAIL ====================

    @Test
    void shouldPassWhenEmailIsValid() {
        User user = validUser();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        User user = validUser();
        user.setEmail(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        User user = validUser();
        user.setEmail("   ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailWhenEmailHasNoAt() {
        User user = validUser();
        user.setEmail("belyashmail.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    // ==================== ЛОГИН ====================

    @Test
    void shouldFailWhenLoginIsNull() {
        User user = validUser();
        user.setLogin(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = validUser();
        user.setLogin("   ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = validUser();
        user.setLogin("belyash belyash");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void shouldPassWhenLoginHasUnderscore() {
        User user = validUser();
        user.setLogin("belyash_belyash");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    // ==================== ИМЯ ====================

    @Test
    void shouldPassWhenNameIsNull() {
        User user = validUser();
        user.setName(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldReturnLoginWhenNameIsNull() {
        User user = validUser();
        user.setName(null);

        assertEquals("belyash", user.getName());
    }

    @Test
    void shouldReturnLoginWhenNameIsBlank() {
        User user = validUser();
        user.setName("   ");

        assertEquals("belyash", user.getName());
    }

    @Test
    void shouldReturnNameWhenNameIsPresent() {
        User user = validUser();
        user.setName("belyash");

        assertEquals("belyash", user.getName());
    }

    // ==================== ДАТА РОЖДЕНИЯ ====================

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("birthday")));
    }

    @Test
    void shouldPassWhenBirthdayIsToday() {
        User user = validUser();
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassWhenBirthdayIsInPast() {
        User user = validUser();
        user.setBirthday(LocalDate.of(1990, 5, 15));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassWhenBirthdayIsNull() {
        User user = validUser();
        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty());
    }
}