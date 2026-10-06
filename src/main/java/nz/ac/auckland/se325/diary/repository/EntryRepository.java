package nz.ac.auckland.se325.diary.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import nz.ac.auckland.se325.diary.model.Entry;

@Repository
public class EntryRepository {

    private static final String SELECT_ENTRY = "SELECT id, entry_date, title, body FROM entries ";

    private static final RowMapper<Entry> ENTRY = (rs, i) -> new Entry(
            rs.getLong("id"),
            LocalDate.parse(rs.getString("entry_date")),
            rs.getString("title"),
            rs.getString("body"));

    private final JdbcTemplate jdbc;

    public EntryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * One user's diary entries, newest first.
     *
     * @param search case-insensitive text to look for in the title or body (may be empty)
     */
    public List<Entry> findForUser(long userId, String search) {
        String sql = SELECT_ENTRY
                + "WHERE user_id = " + userId + " "
                + "AND (LOWER(title) LIKE LOWER('%" + search + "%') "
                + "OR LOWER(body) LIKE LOWER('%" + search + "%')) "
                + "ORDER BY entry_date DESC, id DESC";

        return jdbc.query(sql, ENTRY);
    }

    public Optional<Entry> findForUser(long userId, long entryId) {
        String sql = SELECT_ENTRY + "WHERE id = " + entryId + " AND user_id = " + userId;
        return jdbc.query(sql, ENTRY).stream().findFirst();
    }

    public long insert(long userId, LocalDate date, String title, String body) {
        jdbc.update("INSERT INTO entries (user_id, entry_date, title, body) VALUES ("
                + userId + ", '" + date + "', '" + title + "', '" + body + "')");
        return jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
    }

    public int delete(long userId, long entryId) {
        return jdbc.update("DELETE FROM entries WHERE id = " + entryId + " AND user_id = " + userId);
    }
}
