package com.curso.boot.dao;

import com.curso.boot.domain.Order;
import org.springframework.stereotype.Repository;

import jakarta.persistence.NoResultException;
import java.util.List;

@Repository
public class OrderDao extends AbstractDAO<Order, Long> {

    public List<Order> findByCustomerEmail(String email) {
        return getEntityManager()
                .createQuery("select o from Order o where o.customerEmail = :email order by o.createdAt desc", Order.class)
                .setParameter("email", email)
                .getResultList();
    }

    public Order findByOrderNumber(String orderNumber) {
        try {
            return getEntityManager()
                    .createQuery("select o from Order o where o.orderNumber = :orderNumber", Order.class)
                    .setParameter("orderNumber", orderNumber)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
