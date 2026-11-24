package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashSet;
import java.util.Set;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Override
    @Transactional
    public Film addFilm(Film film) {
        validateMpa(film);
        validateGenres(film);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO films (name, description, release_date, duration, mpa_id)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("База данных не вернула id созданного фильма");
        }
        film.setId(key.intValue());
        saveGenres(film);
        loadMpa(film);
        return film;
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        validateMpa(film);
        validateGenres(film);
        int updated = jdbcTemplate.update("""
                        UPDATE films
                        SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                        WHERE film_id = ?
                        """, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());
        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        saveGenres(film);
        loadMpa(film);
        return film;
    }

    @Override
    @Transactional
    public void deleteFilm(int id) {
        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", id);
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        List<Film> films = jdbcTemplate.query(
                "SELECT * FROM films WHERE film_id = ?", filmRowMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        loadRelatedData(film);
        return Optional.of(film);
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(
                "SELECT * FROM films ORDER BY film_id", filmRowMapper);
        films.forEach(this::loadRelatedData);
        return films;
    }

    private void loadRelatedData(Film film) {
        loadGenres(film);
        loadMpa(film);
    }

    private void validateMpa(Film film) {
        if (film.getMpa() == null || jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa WHERE mpa_id = ?", Integer.class, film.getMpa().getId()) == 0) {
            throw new NotFoundException("Указанный рейтинг MPA не найден");
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(List.of());
            return;
        }
        for (Genre genre : film.getGenres()) {
            if (genre == null) {
                throw new NotFoundException("Передан пустой жанр");
            }
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM genres WHERE genre_id = ?", Integer.class, genre.getId());
            if (count == null || count == 0) {
                throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
            }
        }
    }

    private void loadMpa(Film film) {
        Mpa mpa = jdbcTemplate.queryForObject("""
                        SELECT mpa_id, name FROM mpa WHERE mpa_id = ?
                        """, (rs, rowNum) -> new Mpa(rs.getInt("mpa_id"), rs.getString("name")),
                film.getMpa().getId());
        film.setMpa(mpa);
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbcTemplate.query("""
                SELECT g.genre_id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.genre_id
                """, genreRowMapper, film.getId());
        film.setGenres(genres);
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() == null) {
            return;
        }
        Set<Integer> genreIds = new LinkedHashSet<>();
        for (Genre genre : film.getGenres()) {
            genreIds.add(genre.getId());
        }
        for (Integer genreId : genreIds) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genreId);
        }
    }


    @Override
    public void addLike(int filmId, int userId) {
        jdbcTemplate.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query("""
                SELECT f.*
                FROM films f
                LEFT JOIN likes l ON l.film_id = f.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(l.user_id) DESC, f.film_id
                LIMIT ?
                """, filmRowMapper, count);
        films.forEach(this::loadRelatedData);
        return films;
    }

}
