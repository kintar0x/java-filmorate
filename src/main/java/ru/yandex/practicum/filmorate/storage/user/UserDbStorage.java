
package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private static final int UNCONFIRMED = 1;
    private static final int CONFIRMED = 2;

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    @Transactional
    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(
                    4,
                    user.getBirthday() == null ? null : Date.valueOf(user.getBirthday())
            );

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "База данных не вернула id созданного пользователя"
            );
        }

        user.setId(key.intValue());
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        Date birthday = user.getBirthday() == null
                ? null
                : Date.valueOf(user.getBirthday());

        int updated = jdbcTemplate.update("""
                        UPDATE users
                        SET email = ?, login = ?, name = ?, birthday = ?
                        WHERE user_id = ?
                        """,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                birthday,
                user.getId()
        );

        if (updated == 0) {
            throw new NotFoundException(
                    "Пользователь с id=" + user.getId() + " не найден"
            );
        }

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(int id) {
        jdbcTemplate.update(
                "DELETE FROM users WHERE user_id = ?",
                id
        );
    }

    @Override
    public Optional<User> getUserById(int id) {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users WHERE user_id = ?",
                userRowMapper,
                id
        );

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        loadFriends(user);

        return Optional.of(user);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = jdbcTemplate.query(
                "SELECT * FROM users ORDER BY user_id",
                userRowMapper
        );

        users.forEach(this::loadFriends);

        return users;
    }

    /**
     * Загружает всех друзей пользователя.
     * Учитываются как неподтверждённые, так и подтверждённые заявки.
     */
    private void loadFriends(User user) {
        List<Integer> friendIds = jdbcTemplate.query("""
                        SELECT friend_id
                        FROM friendships
                        WHERE user_id = ?
                        ORDER BY friend_id
                        """,
                (rs, rowNum) -> rs.getInt("friend_id"),
                user.getId()
        );

        user.getFriends().clear();
        user.getFriends().addAll(friendIds);
    }

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException(
                    "Нельзя добавить самого себя в друзья"
            );
        }

        Integer exists = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM friendships
                        WHERE user_id = ? AND friend_id = ?
                        """,
                Integer.class,
                userId,
                friendId
        );

        // Если пользователь уже добавил этого друга, ничего не делаем.
        if (exists != null && exists > 0) {
            return;
        }

        Integer reverseExists = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM friendships
                        WHERE user_id = ? AND friend_id = ?
                        """,
                Integer.class,
                friendId,
                userId
        );

        if (reverseExists != null && reverseExists > 0) {
            // Встречная заявка подтверждает дружбу.
            jdbcTemplate.update("""
                            UPDATE friendships
                            SET status_id = ?
                            WHERE user_id = ? AND friend_id = ?
                            """,
                    CONFIRMED,
                    friendId,
                    userId
            );

            jdbcTemplate.update("""
                            INSERT INTO friendships (user_id, friend_id, status_id)
                            VALUES (?, ?, ?)
                            """,
                    userId,
                    friendId,
                    CONFIRMED
            );
        } else {
            // Первая заявка создаёт неподтверждённую дружбу.
            jdbcTemplate.update("""
                            INSERT INTO friendships (user_id, friend_id, status_id)
                            VALUES (?, ?, ?)
                            """,
                    userId,
                    friendId,
                    UNCONFIRMED
            );
        }
    }

    @Override
    @Transactional
    public void removeFriend(int userId, int friendId) {
        List<Integer> statuses = jdbcTemplate.query("""
                        SELECT status_id
                        FROM friendships
                        WHERE user_id = ? AND friend_id = ?
                        """,
                (rs, rowNum) -> rs.getInt("status_id"),
                userId,
                friendId
        );

        if (statuses.isEmpty()) {
            return;
        }

        int status = statuses.get(0);

        jdbcTemplate.update("""
                        DELETE FROM friendships
                        WHERE user_id = ? AND friend_id = ?
                        """,
                userId,
                friendId
        );

        // При подтверждённой дружбе удаляем и встречную запись.
        if (status == CONFIRMED) {
            jdbcTemplate.update("""
                            DELETE FROM friendships
                            WHERE user_id = ? AND friend_id = ?
                            """,
                    friendId,
                    userId
            );
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        return jdbcTemplate.query("""
                        SELECT u.*
                        FROM users u
                        JOIN friendships f ON f.friend_id = u.user_id
                        WHERE f.user_id = ?
                        ORDER BY u.user_id
                        """,
                userRowMapper,
                userId
        );
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        return jdbcTemplate.query("""
                        SELECT u.*
                        FROM users u
                        JOIN friendships f1 ON f1.friend_id = u.user_id
                        JOIN friendships f2 ON f2.friend_id = u.user_id
                        WHERE f1.user_id = ?
                          AND f2.user_id = ?
                        ORDER BY u.user_id
                        """,
                userRowMapper,
                userId,
                otherId
        );
    }
}
