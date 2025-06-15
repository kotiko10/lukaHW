package com.example.dao;

import com.example.model.User;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class HibernateUserDao implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUserDao.class);

    @Override
    public void create(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.openSession()) {
            transaction = session.beginTransaction();

            session.save(user);

            transaction.commit();
            logger.debug("User created successfully: {}", user.getLogin());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error creating user: {}", user.getLogin(), e);
            throw new RuntimeException("Error creating user", e);
        }
    }

    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.openSession()) {
            transaction = session.beginTransaction();

            session.update(user);

            transaction.commit();
            logger.debug("User updated successfully: {}", user.getLogin());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", user.getLogin(), e);
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public void remove(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.openSession()) {
            transaction = session.beginTransaction();

            session.delete(user);

            transaction.commit();
            logger.debug("User removed successfully: {}", user.getLogin());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error removing user: {}", user.getLogin(), e);
            throw new RuntimeException("Error removing user", e);
        }
    }

    @Override
    public User findById(Long id) {
        try (Session session = HibernateUtil.openSession()) {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root).where(cb.equal(root.get("id"), id));

            return session.createQuery(cq).uniqueResult();
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.openSession()) {

            String hql = "FROM User u LEFT JOIN FETCH u.role ORDER BY u.login";
            Query<User> query = session.createQuery(hql, User.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Error finding all users", e);
        }
    }

    @Override
    public User findByLogin(String login) {
        try (Session session = HibernateUtil.openSession()) {

            String hql = "FROM User u LEFT JOIN FETCH u.role WHERE u.login = :login";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("login", login);

            return query.uniqueResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            logger.error("Error finding user by login: {}", login, e);
            throw new RuntimeException("Error finding user by login", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        try (Session session = HibernateUtil.openSession()) {

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            root.fetch("role");
            cq.select(root).where(cb.equal(root.get("email"), email));

            return session.createQuery(cq).uniqueResult();
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    public List<User> findByRole(String roleName) {
        try (Session session = HibernateUtil.openSession()) {

            String hql = "FROM User u JOIN FETCH u.role r WHERE r.name = :roleName ORDER BY u.login";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("roleName", roleName);

            return query.list();
        } catch (Exception e) {
            logger.error("Error finding users by role: {}", roleName, e);
            throw new RuntimeException("Error finding users by role", e);
        }
    }

}