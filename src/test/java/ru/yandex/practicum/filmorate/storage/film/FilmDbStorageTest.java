
package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film newFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(new Mpa(1, "G"));
        film.setGenres(List.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        ));
        return film;
    }

    private int createUser(String login) {
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                login + "@mail.com",
                login,
                login,
                LocalDate.of(2000, 1, 1)
        );

        return jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE login = ?",
                Integer.class,
                login
        );
    }

    @Test
    void shouldAddAndGetFilmWithGenresAndMpa() {
        Film created = filmStorage.addFilm(newFilm());

        Film loaded = filmStorage.getFilmById(created.getId()).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Film");
        assertThat(loaded.getMpa().getName()).isEqualTo("G");
        assertThat(loaded.getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2);
    }

    @Test
    void shouldUpdateFilmAndRelations() {
        Film film = filmStorage.addFilm(newFilm());

        film.setName("Updated");
        film.setGenres(List.of(new Genre(3, "Мультфильм")));
        film.setMpa(new Mpa(2, "PG"));

        filmStorage.updateFilm(film);

        Film loaded = filmStorage.getFilmById(film.getId()).orElseThrow();

        assertThat(loaded.getName()).isEqualTo("Updated");
        assertThat(loaded.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3);
        assertThat(loaded.getMpa().getName()).isEqualTo("PG");
    }

    @Test
    void shouldGetAllAndDeleteFilm() {
        Film film = filmStorage.addFilm(newFilm());

        assertThat(filmStorage.getAllFilms()).hasSize(1);

        filmStorage.deleteFilm(film.getId());

        assertThat(filmStorage.getFilmById(film.getId())).isEmpty();
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = filmStorage.addFilm(newFilm());
        int userId = createUser("likeUser");

        filmStorage.addLike(film.getId(), userId);

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(),
                userId
        );

        assertThat(likesCount).isEqualTo(1);

        filmStorage.addLike(film.getId(), userId);

        likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(),
                userId
        );

        assertThat(likesCount).isEqualTo(1);

        filmStorage.removeLike(film.getId(), userId);

        likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(),
                userId
        );

        assertThat(likesCount).isZero();
    }

    @Test
    void shouldGetPopularFilms() {
        filmStorage.addFilm(newFilm());

        Film secondFilm = newFilm();
        secondFilm.setName("Second Film");
        secondFilm.setGenres(List.of(new Genre(1, "Комедия")));
        secondFilm = filmStorage.addFilm(secondFilm);

        Film thirdFilm = newFilm();
        thirdFilm.setName("Third Film");
        thirdFilm.setGenres(List.of(new Genre(2, "Драма")));
        thirdFilm = filmStorage.addFilm(thirdFilm);

        int firstUser = createUser("firstUser");
        int secondUser = createUser("secondUser");
        int thirdUser = createUser("thirdUser");

        filmStorage.addLike(secondFilm.getId(), firstUser);
        filmStorage.addLike(secondFilm.getId(), secondUser);
        filmStorage.addLike(thirdFilm.getId(), thirdUser);

        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms)
                .extracting(Film::getId)
                .containsExactly(secondFilm.getId(), thirdFilm.getId());

        assertThat(popularFilms)
                .extracting(Film::getName)
                .containsExactly("Second Film", "Third Film");
    }
}
