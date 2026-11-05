package at.technikum.repository;

import at.technikum.model.Media;
import at.technikum.model.MediaType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import at.technikum.services.database.UnitOfWork;
import at.technikum.services.database.DataAccessException;

public class JdbcMediaRepository implements MediaRepository {

    @Override
    public Media create(Media media) {
        String sql = "INSERT INTO media (creator_id, title, description, media_type, release_year, age_restriction) VALUES (?, ?, ?, ?, ?, ?) RETURNING id, creator_id, title, description, media_type, release_year, age_restriction";

        try (UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {

            stmt.setInt(1, media.getCreatorId());
            stmt.setString(2, media.getTitle());
            stmt.setString(3, media.getDescription());
            stmt.setString(4, media.getMediaType().name());
            stmt.setInt(5, media.getReleaseYear());
            stmt.setInt(6, media.getAgeRestriction());

            var rs = stmt.executeQuery();

            if(rs.next()) {
                Media createdMedia = new Media(
                    rs.getInt("id"),
                    rs.getInt("creator_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    MediaType.valueOf(rs.getString("media_type")),
                    rs.getInt("release_year"),
                    List.of(),
                    rs.getInt("age_restriction")
                );

                unitOfWork.commitTransaction();
                return createdMedia;
            }

            unitOfWork.rollbackTransaction();
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error creating media", e);
        }
    }

    @Override
    public Media readById(Integer id) {
        String sql = "SELECT * FROM media WHERE id = ?";

        try (UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setInt(1, id);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return new Media(
                    rs.getInt("id"),
                    rs.getInt("creator_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    MediaType.valueOf(rs.getString("media_type")),
                    rs.getInt("release_year"),
                    List.of(),
                    rs.getInt("age_restriction")
                );
            }
        }

        catch (SQLException e) {
            throw new DataAccessException("Error reading media", e);
        }

        return null;
    }

    @Override
    public List<Media> readAll() {
        String sql = "SELECT * FROM media";
        List<Media> mediaList = new ArrayList<>();

        try (UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {

            var rs = stmt.executeQuery();

            while (rs.next()) {
                Media media = new Media(
                    rs.getInt("id"),
                    rs.getInt("creator_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    MediaType.valueOf(rs.getString("media_type")),
                    rs.getInt("release_year"),
                    List.of(),
                    rs.getInt("age_restriction")
                );

                mediaList.add(media);
            }
        }

        catch (SQLException e) {
            throw new DataAccessException("Error reading media list", e);
        }

        return mediaList;
    }

    @Override
    public Media updateById(Integer id, Media media) {
        String sql = "UPDATE media SET creator_id = ?, title = ?, description = ?, media_type = ?, release_year = ?, age_restriction = ? WHERE id= ? RETURNING id, creator_id, title, description, media_type, release_year, age_restriction";

        try(UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setInt(1, media.getCreatorId());
            stmt.setString(2, media.getTitle());
            stmt.setString(3, media.getDescription());
            stmt.setString(4, media.getMediaType().name());
            stmt.setInt(5, media.getReleaseYear());
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setInt(7, id);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                Media updatedMedia = new Media(
                    rs.getInt("id"),
                    rs.getInt("creator_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    MediaType.valueOf(rs.getString("media_type")),
                    rs.getInt("release_year"),
                    List.of(),
                    rs.getInt("age_restriction")
                );

                unitOfWork.commitTransaction();
                return updatedMedia;
            }

            unitOfWork.rollbackTransaction();
            return null;
        }

        catch(SQLException e) {
            throw new DataAccessException("Error updating media", e);
        }
    }

    @Overrice
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM media WHERE id = ?";

        try(UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            unitOfWork.commitTransaction();
            return affectedRows > 0;
        }

        catch(SQLException e) {
            throw new DataAccessException("Error deleting media",  e),
        }
    }
}
