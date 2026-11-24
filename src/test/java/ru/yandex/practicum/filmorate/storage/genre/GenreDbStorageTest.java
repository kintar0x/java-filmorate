package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {
    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void shouldReturnAllGenresInIdOrder() {
        assertThat(genreStorage.getAllGenres()).hasSize(6)
                .extracting("id").containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void shouldFindGenreById() {
        assertThat(genreStorage.getGenreById(1)).isPresent()
                .get().extracting("name").isEqualTo("Комедия");
        assertThat(genreStorage.getGenreById(99)).isEmpty();
    }
}
