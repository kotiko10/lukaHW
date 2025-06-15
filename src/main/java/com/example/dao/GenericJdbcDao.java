package com.example.dao;

import com.example.exceptions.DatabaseWriteException;
import com.example.exceptions.DatabaseReadException;
import com.example.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public abstract class GenericJdbcDao<E> implements Dao<E> {
    protected static final Logger logger = LoggerFactory.getLogger(GenericJdbcDao.class);
    protected DatabaseManager databaseManager;

    public GenericJdbcDao() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public GenericJdbcDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void create(E entity) {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(getInsertQuery(), RETURN_GENERATED_KEYS)) {
                setInsertParameters(ps, entity);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Insert failed");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        setEntityId(entity, rs.getLong(1));
                    }
                }
                connection.commit();
                logger.debug("Entity created: {}", entity);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error creating entity: {}", entity, e);
            throw new DatabaseWriteException("Error creating entity", e);
        }
    }

    public void update(E entity) {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(getUpdateQuery())) {
                setUpdateParameters(ps, entity);
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating entity failed, no rows affected.");
                }
                connection.commit();
                logger.debug("Entity updated successfully: {}", entity);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error updating entity: {}", entity, e);
            throw new DatabaseWriteException("Error updating entity", e);
        }
    }

    public void remove(E entity) {
        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(getDeleteQuery())) {
                ps.setLong(1, getEntityId(entity));
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Deleting entity failed, no rows affected.");
                }
                connection.commit();
                logger.debug("Entity deleted successfully: {}", entity);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error deleting entity: {}", entity, e);
            throw new DatabaseWriteException("Error deleting entity", e);
        }
    }

    public List<E> findAll() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getFindAllQuery());
             ResultSet rs = ps.executeQuery()) {

            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding all entities", e);
            throw new DatabaseReadException("Error finding all entities", e);
        }
    }

    public E findById(Long id) {
        if (id == null) {
            logger.debug("Attempted to find entity with null id, returning null");
            return null;
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(getFindByIdQuery())) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            logger.error("Error finding entity by id: {}", id, e);
            throw new DatabaseReadException("Error finding entity by id", e);
        }
    }

    protected List<E> mapResultSetToList(ResultSet rs) throws SQLException {
        List<E> entities = new java.util.ArrayList<>();
        while (rs.next()) {
            entities.add(mapResultSetToEntity(rs));
        }
        return entities;
    }


    protected abstract String getTableName();
    protected abstract String getInsertQuery();
    protected abstract String getUpdateQuery();
    protected abstract String getDeleteQuery();
    protected abstract String getFindAllQuery();
    protected abstract String getFindByIdQuery();

    protected abstract void setInsertParameters(PreparedStatement preparedStatement, E entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement preparedStatement, E entity) throws SQLException;

    protected abstract Long getEntityId(E entity);
    protected abstract void setEntityId(E entity, Long id);
    protected abstract E mapResultSetToEntity(ResultSet resultSet) throws SQLException;
}