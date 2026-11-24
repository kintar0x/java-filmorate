package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaDbStorageTest {
    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void shouldReturnExactlyFiveRatings() {
        assertThat(mpaStorage.getAllMpa()).hasSize(5)
                .extracting("name").containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void shouldFindRatingById() {
        assertThat(mpaStorage.getMpaById(1)).isPresent()
                .get().extracting("name").isEqualTo("G");
        assertThat(mpaStorage.getMpaById(99)).isEmpty();
    }
}
