package at.technikum.repository;

import at.technikum.model.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.technikum.services.database.UnitOfWork;
import at.technikum.services.database.DataAccessException;

public class JdbcUserRepository implements UserRepository {

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (username, password) values (?, ?) RETURNING id, username, password";

        try (UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());

            var rs = stmt.executeQuery();

            if(rs.next()) {
                User createdUser = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            
                unitOfWork.commitTransaction();
                return createdUser;
            }

            unitOfWork.rollbackTransaction();
            return null;
        }

        catch (SQLException e) {
            throw new DataAccessException("Error creating user", e);
        }
    }

    @Override
    public User readById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try(UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setInt(1, id);

            var rs = stmt.executeQuery();

            if(rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");

                return new User(userId, username, password);
            }
        }

        catch (SQLException e) {
            throw new DataAccessException("Error reading user", e);
        }

        return null;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try(UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            
            unitOfWork.commitTransaction();

            return affectedRows > 0;
        }

        catch (SQLException e) {
            throw new DataAccessException("Error deleting user",e);
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setString(1, username);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String dbUsername = rs.getString("username");
                String password = rs.getString("password");

                return new User(id, dbUsername, password);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error reading user by username",  e);
        }

        return null;
    }

    @Override
    public List<User> readAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try(UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            var rs = stmt.executeQuery();

            while(rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");

                users.add(new User(id, username, password));
            }
        }

        catch (SQLException e) {
            throw new DataAccessException("Error reading users", e);
        }

        return users;
    }

    @Override
    public User updateById(Integer id, User user) {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ? RETURNING id, username, password";

        try(UnitOfWork unitOfWork = new UnitOfWork(); PreparedStatement stmt = unitOfWork.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setInt(3, id);

            var rs = stmt.executeQuery();

            if(rs.next()) {
                User updatedUser = new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                unitOfWork.commitTransaction();
                return updatedUser;
            }

            unitOfWork.rollbackTransaction();
            return null;
        }

        catch(SQLException e) {
            throw new DataAccessException("Error updating user", e);
        }
    }
}
