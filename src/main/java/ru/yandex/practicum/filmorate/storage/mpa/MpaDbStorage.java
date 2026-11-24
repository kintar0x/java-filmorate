package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query("SELECT mpa_id, name FROM mpa ORDER BY mpa_id", mpaRowMapper);
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?", mpaRowMapper, id);
        return ratings.stream().findFirst();
    }
}
