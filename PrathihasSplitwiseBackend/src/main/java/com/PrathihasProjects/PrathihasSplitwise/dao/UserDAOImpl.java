package com.PrathihasProjects.PrathihasSplitwise.dao;

import com.PrathihasProjects.PrathihasSplitwise.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



@Repository
public class UserDAOImpl implements UserDAO {

    private final EntityManager entityManager;

    @Autowired
    public UserDAOImpl(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(User user) {
        entityManager.persist(user);
    }

    @Override
    public User findUserByName(String userName) {
        return entityManager.find(User.class, userName);
    }

    @Override
    public User findUserByEmail(String email) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Handle the case where no user is found with the provided email
        }
    }

    @Override
    @Transactional
    public void updateUser(User user){
        entityManager.merge(user);
    }
}
