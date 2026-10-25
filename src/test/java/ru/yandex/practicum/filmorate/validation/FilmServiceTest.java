package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private FilmService filmService;

    @Test
    void addLike_shouldAddLike() {
        Film film = new Film();
        film.setId(1);
        User user = new User();
        user.setId(1);

        when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));
        when(userStorage.getUserById(1)).thenReturn(Optional.of(user));

        filmService.addLike(1, 1);

        assertTrue(film.getLikes().contains(1));
    }

    @Test
    void removeLike_shouldRemoveLike() {
        Film film = new Film();
        film.setId(1);
        film.getLikes().add(1);

        User user = new User();
        user.setId(1);

        when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));
        when(userStorage.getUserById(1)).thenReturn(Optional.of(user));

        filmService.removeLike(1, 1);

        assertFalse(film.getLikes().contains(1));
    }

    @Test
    void getPopularFilms_shouldSortByLikes() {
        Film f1 = new Film();
        f1.setId(1);
        f1.getLikes().add(1); // 1 лайк

        Film f2 = new Film();
        f2.setId(2);
        f2.getLikes().add(1); // 2 лайка
        f2.getLikes().add(2);

        when(filmStorage.getAllFilms()).thenReturn(List.of(f1, f2));

        List<Film> popular = filmService.getPopularFilms(10);

        assertEquals(2, popular.get(0).getId()); // Фильм с 2 лайками первый
        assertEquals(1, popular.get(1).getId());
    }
}