package com.curso.boot.service;

import com.curso.boot.dto.AdminProductForm;
import com.curso.boot.dao.ProductDao;
import com.curso.boot.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    public List<Product> getFeaturedProducts() {
        return productDao.findFeatured();
    }

    public Product getProductById(Long id) {
        return productDao.findById(id);
    }

    public Product getRequiredProduct(String id) {
        Product product = getProductById(Long.parseLong(id));
        if (product == null) throw new RuntimeException("Product not found");
        return product;
    }

    @Transactional
    public void save(Product product) {
        if (product.getId() == null) {
            productDao.save(product);
        } else {
            productDao.update(product);
        }
    }

    @Transactional
    public void createProduct(AdminProductForm form) {
        Product product = new Product();
        product.setName(form.getName());
        product.setSubtitle(form.getSubtitle());
        product.setWeight(form.getWeight());
        product.setPrice(form.getPrice());
        product.setImageFileName(form.getImageFileName());
        product.setAltText(form.getAltText());
        product.setFeatured(form.isFeatured());
        save(product);
    }

    @Transactional
    public void updateProduct(String id, AdminProductForm form) {
        Product product = getRequiredProduct(id);
        product.setName(form.getName());
        product.setSubtitle(form.getSubtitle());
        product.setWeight(form.getWeight());
        product.setPrice(form.getPrice());
        product.setImageFileName(form.getImageFileName());
        product.setAltText(form.getAltText());
        product.setFeatured(form.isFeatured());
        save(product);
    }

    @Transactional
    public void deleteProduct(String id) {
        delete(Long.parseLong(id));
    }

    @Transactional
    public void delete(Long id) {
        productDao.delete(id);
    }
}
