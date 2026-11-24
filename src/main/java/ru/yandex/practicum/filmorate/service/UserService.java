package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
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
        getUserById(userId);
        getUserById(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public List<User> getFriends(int userId) {
        getUserById(userId);
        return userStorage.getFriends(userId);
    }

}
