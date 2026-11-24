package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage
    ) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    @Transactional
    public void deleteUser(int id) {
        getUserById(id);

        // In-memory хранилище не имеет внешних ключей, поэтому очищаем ссылки вручную.
        // В БД связанные записи дополнительно удаляются в UserDbStorage.deleteUser.
        for (User otherUser : userStorage.getAllUsers()) {
            if (otherUser.getFriends().remove(id)) {
                userStorage.updateUser(otherUser);
            }
        }

        for (Film film : filmStorage.getAllFilms()) {
            if (film.getLikes().remove(id)) {
                filmStorage.updateFilm(film);
            }
        }

        userStorage.deleteUser(id);
        log.info("Пользователь с id={} удалён", id);
    }

    @Transactional
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    @Transactional
    public void removeFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserById(userId);
        User other = getUserById(otherId);

        Set<Integer> commonIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getFriends(int userId) {
        return getUserById(userId).getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }
}
