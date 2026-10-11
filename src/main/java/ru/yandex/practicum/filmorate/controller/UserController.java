package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Всего: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);

        User created = new User();
        created.setId(nextId++);
        created.setEmail(user.getEmail());
        created.setLogin(user.getLogin());
        created.setName(user.getName());
        created.setBirthday(user.getBirthday());

        users.put(created.getId(), created);
        log.info("Пользователь успешно создан: id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new RuntimeException("Пользователь с id=" + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        log.info("Пользователь успешно обновлён: id={}", user.getId());
        return user;
    }
}