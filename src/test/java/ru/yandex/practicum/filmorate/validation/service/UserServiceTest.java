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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private UserService userService;

    @Test
    void addFriend_shouldCreateOneWayRequest() {
        when(userStorage.getUserById(1)).thenReturn(Optional.of(new User()));
        when(userStorage.getUserById(2)).thenReturn(Optional.of(new User()));

        userService.addFriend(1, 2);

        verify(userStorage).addFriend(1, 2);
    }

    @Test
    void removeFriend_shouldRemoveRelationship() {
        when(userStorage.getUserById(1)).thenReturn(Optional.of(new User()));
        when(userStorage.getUserById(2)).thenReturn(Optional.of(new User()));

        userService.removeFriend(1, 2);

        verify(userStorage).removeFriend(1, 2);
    }
}
