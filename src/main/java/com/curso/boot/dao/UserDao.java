package com.curso.boot.dao;

import com.curso.boot.domain.User;
import org.springframework.stereotype.Repository;

import jakarta.persistence.NoResultException;

@Repository
public class UserDao extends AbstractDAO<User, Long> {

    public User findByEmail(String email) {
        try {
            return getEntityManager()
                    .createQuery("select u from User u where u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByCpf(String cpf) {
        try {
            return getEntityManager()
                    .createQuery("select u from User u where u.cpf = :cpf", User.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
