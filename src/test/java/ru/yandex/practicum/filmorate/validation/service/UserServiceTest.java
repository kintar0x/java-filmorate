package ru.yandex.practicum.filmorate.validation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private UserService userService;

    @Test
    void addFriend_shouldAddBothSides() {
        User user1 = new User();
        user1.setId(1);
        User user2 = new User();
        user2.setId(2);

        when(userStorage.getUserById(1)).thenReturn(Optional.of(user1));
        when(userStorage.getUserById(2)).thenReturn(Optional.of(user2));

        userService.addFriend(1, 2);

        assertTrue(user1.getFriends().contains(2));
        assertTrue(user2.getFriends().contains(1));
    }

    @Test
    void removeFriend_shouldRemoveBothSides() {
        User user1 = new User();
        user1.setId(1);
        user1.getFriends().add(2);
        User user2 = new User();
        user2.setId(2);
        user2.getFriends().add(1);

        when(userStorage.getUserById(1)).thenReturn(Optional.of(user1));
        when(userStorage.getUserById(2)).thenReturn(Optional.of(user2));

        userService.removeFriend(1, 2);

        assertFalse(user1.getFriends().contains(2));
        assertFalse(user2.getFriends().contains(1));
    }
}