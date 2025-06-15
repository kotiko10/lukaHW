package com.example.util;

import com.example.model.Role;
import com.example.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import lombok.Getter;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    @Getter
    private static SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();

            logger.info("Hibernate configuration loaded");
        } catch (Exception e) {
            logger.error("Failed to initialize hibernate session successfully");
        }
    }

    public static Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public static Session openSession() {
        return sessionFactory.openSession();
    }

    public static void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed");
        }
    }
}
