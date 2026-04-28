package com.curso.boot.service;

import com.curso.boot.dto.CheckoutForm;
import com.curso.boot.dto.AdminOrderCreateForm;
import com.curso.boot.dto.AdminOrderUpdateForm;
import com.curso.boot.cart.CartSummary;
import com.curso.boot.cart.CartLine;
import com.curso.boot.dao.OrderDao;
import com.curso.boot.domain.Order;
import com.curso.boot.domain.OrderItem;
import com.curso.boot.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductService productService;

    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderDao.findByCustomerEmail(email);
    }
    
    public List<Order> findAll() {
        return orderDao.findAll();
    }

    public Order getOrderById(Long id) {
        return orderDao.findById(id);
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return Optional.ofNullable(orderDao.findByOrderNumber(orderNumber));
    }

    public Order getRequiredByOrderNumber(String orderNumber) {
        Order order = orderDao.findByOrderNumber(orderNumber);
        if (order == null) throw new RuntimeException("Pedido não encontrado");
        return order;
    }

    @Transactional
    public Order createOrder(String customerEmail, CheckoutForm form, CartSummary cartSummary) {
        Order order = new Order();
        String orderNum = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderNumber(orderNum);
        order.setCustomerEmail(customerEmail);
        order.setRecipientName(form.getRecipientName());
        order.setZipCode(form.getZipCode());
        order.setStreet(form.getStreet());
        order.setNumber(form.getNumber());
        order.setComplement(form.getComplement());
        order.setNeighborhood(form.getNeighborhood());
        order.setCity(form.getCity());
        order.setState(form.getState());
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus("Aguardando Pagamento");
        order.setSource("Web");
        order.setCreatedAt(LocalDateTime.now());
        
        order.setSubtotal(cartSummary.getSubtotal());
        order.setShipping(cartSummary.getShipping());
        order.setTotal(cartSummary.getTotal());

        for (CartLine line : cartSummary.getItems()) {
            Product product = productService.getProductById(line.getProduct().getId());
            if (product != null) {
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setProductName(product.getName());
                item.setProductImage(product.getImageFileName());
                item.setQuantity(line.getQuantity());
                item.setUnitPrice(product.getPrice());
                order.addItem(item);
            }
        }

        orderDao.save(order);
        return order;
    }

    @Transactional
    public void createAdminOrder(AdminOrderCreateForm form) {
        Order order = new Order();
        String orderNum = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setOrderNumber(orderNum);
        order.setCustomerEmail(form.getCustomerEmail());
        order.setRecipientName(form.getRecipientName());
        order.setZipCode(form.getZipCode());
        order.setStreet(form.getStreet());
        order.setNumber(form.getNumber());
        order.setComplement(form.getComplement());
        order.setNeighborhood(form.getNeighborhood());
        order.setCity(form.getCity());
        order.setState(form.getState());
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus(form.getStatus());
        order.setSource("Admin");
        order.setCreatedAt(LocalDateTime.now());
        
        // Setup a dummy total for admin created order since we are not processing cart summary here
        order.setSubtotal(java.math.BigDecimal.ZERO);
        order.setShipping(java.math.BigDecimal.ZERO);
        order.setTotal(java.math.BigDecimal.ZERO);

        orderDao.save(order);
    }
    
    @Transactional
    public void updateOrder(String orderNumber, AdminOrderUpdateForm form) {
        Order order = getRequiredByOrderNumber(orderNumber);
        order.setCustomerEmail(form.getCustomerEmail());
        order.setRecipientName(form.getRecipientName());
        order.setZipCode(form.getZipCode());
        order.setStreet(form.getStreet());
        order.setNumber(form.getNumber());
        order.setComplement(form.getComplement());
        order.setNeighborhood(form.getNeighborhood());
        order.setCity(form.getCity());
        order.setState(form.getState());
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus(form.getStatus());
        orderDao.update(order);
    }

    @Transactional
    public void updateOrderStatus(Long id, String status) {
        Order order = orderDao.findById(id);
        if (order != null) {
            order.setStatus(status);
            orderDao.update(order);
        }
    }
    
    @Transactional
    public void deleteOrder(String orderNumber) {
        Order order = orderDao.findByOrderNumber(orderNumber);
        if (order != null) {
            orderDao.delete(order.getId());
        }
    }
}
