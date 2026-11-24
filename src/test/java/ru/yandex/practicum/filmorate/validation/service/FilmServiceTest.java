package ru.yandex.practicum.filmorate.validation.service;

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

        verify(filmStorage).addLike(1, 1);
    }

    @Test
    void removeLike_shouldRemoveLike() {
        Film film = new Film();
        film.setId(1);
        User user = new User();
        user.setId(1);

        when(filmStorage.getFilmById(1)).thenReturn(Optional.of(film));
        when(userStorage.getUserById(1)).thenReturn(Optional.of(user));

        filmService.removeLike(1, 1);

        verify(filmStorage).removeLike(1, 1);
    }

    @Test
    void getPopularFilms_shouldUseStorageQuery() {
        List<Film> films = List.of(new Film(), new Film());
        when(filmStorage.getPopularFilms(10)).thenReturn(films);

        List<Film> popular = filmService.getPopularFilms(10);

        assertEquals(films, popular);
        verify(filmStorage).getPopularFilms(10);
    }
}