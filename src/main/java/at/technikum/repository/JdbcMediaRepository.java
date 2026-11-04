package at.technikum.repository;

import at.technikum.model.Media;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JdbcMediaRepository implements MediaRepository {

    private final Connection connection;

    public JdbcMediaRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Media media) {
        String sql = "INSERT INTO media (creator_id, title, description, media_type, release_year, age_restriction) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, media.getCreatorId());
            stmt.setString(2, media.getTitle());
            stmt.setString(3, media.getDescription());
            stmt.setString(4, media.getMediaType().name());
            stmt.setInt(5, media.getReleaseYear());
            stmt.setInt(6, media.getAgeRestriction());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Media findById(int id) {
        return null;
    }

    @Override
    public List<Media> findAll() {
        return null;
    }

    @Override
    public void delete(int id) {
    }
}
