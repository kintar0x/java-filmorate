package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = getUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + userId + " не найден"));
        getUserById(friendId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + friendId + " не найден"));
        user.getFriends().add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = getUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + userId + " не найден"));
        getUserById(friendId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + friendId + " не найден"));
        user.getFriends().remove(friendId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}