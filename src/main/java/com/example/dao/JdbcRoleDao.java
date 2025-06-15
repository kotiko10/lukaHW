package com.example.dao;

import com.example.exceptions.DatabaseReadException;
import com.example.model.Role;
import com.example.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcRoleDao extends GenericJdbcDao<Role> implements RoleDao {
    private static final String TABLE_NAME = "roles";
    private static final String INSERT_QUERY = "INSERT INTO roles (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE roles SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM roles WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT id, name FROM roles ORDER BY name";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM roles WHERE id = ?";
    public static final String FIND_BY_NAME_QUERY = "SELECT id, name FROM roles WHERE name = ?";


    public JdbcRoleDao(DatabaseManager databaseManager) {
        super(databaseManager);
    }
    public JdbcRoleDao() {
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
    protected Role mapResultSetToEntity(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        return role;
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Role role) throws SQLException {
        ps.setString(1, role.getName());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Role role) throws SQLException {
        ps.setString(1, role.getName());
        ps.setLong(2, role.getId());
    }

    @Override
    protected Long getEntityId(Role role) {
        return role.getId();
    }

    @Override
    protected void setEntityId(Role role, Long id) {
        role.setId(id);
    }

    @Override
    public Role findByName(String name) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_NAME_QUERY)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.error("Error finding role by name: {}", name, e);
            throw new DatabaseReadException("Error finding role by name", e);
        }
    }
}
