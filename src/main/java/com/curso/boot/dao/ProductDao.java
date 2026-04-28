package com.curso.boot.dao;

import com.curso.boot.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDao extends AbstractDAO<Product, Long> {

    public List<Product> findFeatured() {
        return getEntityManager()
                .createQuery("select p from Product p where p.featured = true", Product.class)
                .getResultList();
    }
}
