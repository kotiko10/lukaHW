package com.example.dao;

import com.example.model.Role;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.List;

public class HibernateRoleDao implements RoleDao {
    private static final Logger logger = LoggerFactory.getLogger(HibernateRoleDao.class);

    @Override
    public void create(Role role) {
        try (Session session = HibernateUtil.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.save(role);
                transaction.commit();
                logger.debug("Role created successfully: {}", role.getName());
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Error creating role: {}", role.getName(), e);
                throw new RuntimeException("Error creating role", e);
            }
        }
    }

    @Override
    public void update(Role role) {
        try (Session session = HibernateUtil.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.update(role);
                transaction.commit();
                logger.debug("Role updated successfully: {}", role.getName());
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Error updating role: {}", role.getName(), e);
                throw new RuntimeException("Error updating role", e);
            }
        }
    }

    @Override
    public void remove(Role role) {
        try (Session session = HibernateUtil.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.delete(role);
                transaction.commit();
                logger.debug("Role removed successfully: {}", role.getName());
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Error removing role: {}", role.getName(), e);
                throw new RuntimeException("Error removing role", e);
            }
        }
    }

    @Override
    public Role findById(Long id) {
        try (Session session = HibernateUtil.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Role> cq = cb.createQuery(Role.class);
            Root<Role> root = cq.from(Role.class);
            cq.select(root).where(cb.equal(root.get("id"), id));

            return session.createQuery(cq).uniqueResult();
        } catch (Exception e) {
            logger.error("Error finding role by id: {}", id, e);
            throw new RuntimeException("Error finding role by id", e);
        }
    }

    @Override
    public List<Role> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            String hql = "FROM Role r ORDER BY r.name";
            Query<Role> query = session.createQuery(hql, Role.class);

            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all roles", e);
            throw new RuntimeException("Error finding all roles", e);
        }
    }

    @Override
    public Role findByName(String name) {
        try (Session session = HibernateUtil.openSession()) {
            String hql = "FROM Role r WHERE r.name = :name";
            Query<Role> query = session.createQuery(hql, Role.class);
            query.setParameter("name", name);

            return query.uniqueResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error finding role by name: {}", name, e);
            throw new RuntimeException("Error finding role by name", e);
        }
    }

    public long countUsers(String roleName) {
        try (Session session = HibernateUtil.openSession()) {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("roleName", roleName);

            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            logger.error("Error counting users for role: {}", roleName, e);
            throw new RuntimeException("Error counting users for role", e);
        }
    }
}