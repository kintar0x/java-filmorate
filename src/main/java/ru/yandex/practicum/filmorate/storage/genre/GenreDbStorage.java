package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT genre_id, name FROM genres ORDER BY genre_id", genreRowMapper);
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT genre_id, name FROM genres WHERE genre_id = ?", genreRowMapper, id);
        return genres.stream().findFirst();
    }
}
