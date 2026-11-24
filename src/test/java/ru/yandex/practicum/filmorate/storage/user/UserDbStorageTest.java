
package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    @Test
    void shouldCreateAndFindUser() {
        User created = userStorage.createUser(newUser("first"));

        assertThat(userStorage.getUserById(created.getId()))
                .isPresent()
                .get()
                .extracting(User::getLogin)
                .isEqualTo("first");
    }

    @Test
    void shouldAllowNullBirthday() {
        User user = newUser("withoutBirthday");
        user.setBirthday(null);

        User created = userStorage.createUser(user);

        assertThat(userStorage.getUserById(created.getId()))
                .isPresent()
                .get()
                .extracting(User::getBirthday)
                .isNull();
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.createUser(newUser("before"));

        user.setLogin("after");
        user.setEmail("after@mail.com");

        userStorage.updateUser(user);

        assertThat(userStorage.getUserById(user.getId())
                .orElseThrow()
                .getLogin()).isEqualTo("after");
    }

    @Test
    void shouldReturnAllAndDeleteUser() {
        User first = userStorage.createUser(newUser("first"));
        userStorage.createUser(newUser("second"));

        assertThat(userStorage.getAllUsers()).hasSize(2);

        userStorage.deleteUser(first.getId());

        assertThat(userStorage.getUserById(first.getId())).isEmpty();
    }

    @Test
    void shouldCreateOneWayFriendshipAndConfirmOnReverseRequest() {
        User first = userStorage.createUser(newUser("first"));
        User second = userStorage.createUser(newUser("second"));

        userStorage.addFriend(first.getId(), second.getId());

        assertThat(userStorage.getUserById(first.getId())
                .orElseThrow()
                .getFriends()).isEmpty();

        assertThat(userStorage.getUserById(second.getId())
                .orElseThrow()
                .getFriends()).isEmpty();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT status_id FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                first.getId(),
                second.getId()
        )).isEqualTo(1);

        userStorage.addFriend(second.getId(), first.getId());

        assertThat(userStorage.getUserById(first.getId())
                .orElseThrow()
                .getFriends()).containsExactly(second.getId());

        assertThat(userStorage.getUserById(second.getId())
                .orElseThrow()
                .getFriends()).containsExactly(first.getId());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT status_id FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                first.getId(),
                second.getId()
        )).isEqualTo(2);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT status_id FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                second.getId(),
                first.getId()
        )).isEqualTo(2);

        userStorage.removeFriend(first.getId(), second.getId());

        assertThat(userStorage.getUserById(first.getId())
                .orElseThrow()
                .getFriends()).isEmpty();

        assertThat(userStorage.getUserById(second.getId())
                .orElseThrow()
                .getFriends()).isEmpty();
    }

    @Test
    void shouldGetFriendsAsUsers() {
        User first = userStorage.createUser(newUser("first"));
        User second = userStorage.createUser(newUser("second"));

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.addFriend(second.getId(), first.getId());

        List<User> friends = userStorage.getFriends(first.getId());

        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(second.getId());

        assertThat(friends.getFirst().getLogin()).isEqualTo("second");
    }

    @Test
    void shouldGetCommonFriends() {
        User first = userStorage.createUser(newUser("first"));
        User second = userStorage.createUser(newUser("second"));
        User common = userStorage.createUser(newUser("common"));
        User another = userStorage.createUser(newUser("another"));

        userStorage.addFriend(first.getId(), common.getId());
        userStorage.addFriend(common.getId(), first.getId());

        userStorage.addFriend(second.getId(), common.getId());
        userStorage.addFriend(common.getId(), second.getId());

        userStorage.addFriend(first.getId(), another.getId());
        userStorage.addFriend(another.getId(), first.getId());

        List<User> commonFriends = userStorage.getCommonFriends(
                first.getId(),
                second.getId()
        );

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(common.getId());

        assertThat(commonFriends.getFirst().getLogin()).isEqualTo("common");
    }
}
