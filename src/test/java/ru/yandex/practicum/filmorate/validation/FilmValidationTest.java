package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmValidationTest {

    @Autowired
    private Validator validator;

    private Film validFilm() {
        Film film = new Film();
        film.setName("belyash");
        film.setDescription("belyash");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    // ==================== НАЗВАНИЕ ====================

    @Test
    void shouldPassWhenNameIsValid() {
        Film film = validFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        Film film = validFilm();
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = validFilm();
        film.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailWhenNameIsEmpty() {
        Film film = validFilm();
        film.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    // ==================== ОПИСАНИЕ ====================

    @Test
    void shouldPassWhenDescriptionIs200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDescriptionIs201Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void shouldPassWhenDescriptionIsNull() {
        Film film = validFilm();
        film.setDescription(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    // ==================== ДАТА РЕЛИЗА ====================

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = validFilm();
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate")));
    }

    @Test
    void shouldFailWhenReleaseDateIsBefore1895() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate")));
    }

    @Test
    void shouldPassWhenReleaseDateIsExact1895() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassWhenReleaseDateIsToday() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.now());
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    // ==================== ПРОДОЛЖИТЕЛЬНОСТЬ ====================

    @Test
    void shouldFailWhenDurationIsZero() {
        Film film = validFilm();
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = validFilm();
        film.setDuration(-10);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void shouldPassWhenDurationIsPositive() {
        Film film = validFilm();
        film.setDuration(1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }
}