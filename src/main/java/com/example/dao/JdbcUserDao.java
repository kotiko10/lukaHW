package com.example.dao;

import com.example.exceptions.DatabaseReadException;
import com.example.model.User;
import com.example.model.Role;
import com.example.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class JdbcUserDao extends GenericJdbcDao<User> implements UserDao {
    private static final String TABLE_NAME = "users";
    private static final String INSERT_QUERY =
            "INSERT INTO users (login, password, email, first_name, last_name, birthday, role_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET login = ?, password = ?, email = ?, first_name = ?, last_name = ?, birthday = ?, role_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT u.id, u.login, u.password, u.email, u.first_name, u.last_name, u.birthday, u.role_id, r.name as role_name " +
                    "FROM users u LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.login";
    private static final String FIND_BY_ID_QUERY =
            "SELECT u.id, u.login, u.password, u.email, u.first_name, u.last_name, u.birthday, u.role_id, r.name as role_name " +
                    "FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
    private static final String FIND_BY_LOGIN_QUERY =
            "SELECT u.id, u.login, u.password, u.email, u.first_name, u.last_name, u.birthday, u.role_id, r.name as role_name " +
                    "FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.login = ?";
    private static final String FIND_BY_EMAIL_QUERY =
            "SELECT u.id, u.login, u.password, u.email, u.first_name, u.last_name, u.birthday, u.role_id, r.name as role_name " +
                    "FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.email = ?";

    public JdbcUserDao(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    public JdbcUserDao() {
        super();
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getInsertQuery() {
        return INSERT_QUERY;
    }

    @Override
    protected String getUpdateQuery() {
        return UPDATE_QUERY;
    }

    @Override
    protected String getDeleteQuery() {
        return DELETE_QUERY;
    }

    @Override
    protected String getFindAllQuery() {
        return FIND_ALL_QUERY;
    }

    @Override
    protected String getFindByIdQuery() {
        return FIND_BY_ID_QUERY;
    }

    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setLogin(rs.getString("login"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));

        Date sqlDate = rs.getDate("birthday");
        if (sqlDate != null) {
            user.setBirthday(sqlDate.toLocalDate()); // convert SQL Date to LocalDate
        }

        Long roleId = rs.getLong("role_id");
        if (!rs.wasNull()) {
            Role role = new Role();
            role.setId(roleId);
            role.setName(rs.getString("role_name"));
            user.setRole(role);
        }
        return user;
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getLogin());
        ps.setString(2, user.getPassword());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getFirstName());
        ps.setString(5, user.getLastName());

        if (user.getBirthday() != null) {
            ps.setDate(6, Date.valueOf(user.getBirthday()));
        } else {
            ps.setNull(6, java.sql.Types.DATE);
        }

        if (user.getRole() != null) {
            ps.setLong(7, user.getRole().getId());
        } else {
            ps.setNull(7, java.sql.Types.BIGINT);
        }
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getLogin());
        ps.setString(2, user.getPassword());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getFirstName());
        ps.setString(5, user.getLastName());

        if (user.getBirthday() != null) {
            ps.setDate(6, Date.valueOf(user.getBirthday())); //convert LocalDate to SQL Date
        } else {
            ps.setNull(6, java.sql.Types.DATE);
        }

        if (user.getRole() != null) {
            ps.setLong(7, user.getRole().getId());
        } else {
            ps.setNull(7, java.sql.Types.BIGINT);
        }

        ps.setLong(8, user.getId());
    }

    @Override
    protected Long getEntityId(User user) {
        return user.getId();
    }

    @Override
    protected void setEntityId(User user, Long id) {
        user.setId(id);
    }

    @Override
    public User findByLogin(String login) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_LOGIN_QUERY)) {

            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.error("Error finding user by login: {}", login, e);
            throw new DatabaseReadException("Error finding user by login", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_EMAIL_QUERY)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DatabaseReadException("Error finding user by email", e);
        }
    }
}