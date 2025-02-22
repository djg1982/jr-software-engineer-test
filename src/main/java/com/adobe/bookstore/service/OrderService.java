package com.adobe.bookstore.service;

import com.adobe.bookstore.model.BookStock;
import com.adobe.bookstore.model.Order;
import com.adobe.bookstore.model.OrderItem;
import com.adobe.bookstore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookStockService bookStockService;

    public String createOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            Optional<BookStock> bookStock = bookStockService.getBookStock(item.getBookId());
            if (!bookStock.isPresent() || bookStock.get().getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for book: " + item.getBookId());
            }
        }

        String orderId = UUID.randomUUID().toString();
        order.setId(orderId);
        order.setStatus(Order.Status.SUCCESS);

        // Associate the order with its items and update stock
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
            updateStock(item);
        }

        saveOrder(order);

        return orderId;
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public void updateOrderStatus(String orderId, Order.Status status) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            throw new RuntimeException("Order not found: " + orderId);
        }
    }

    private void updateStock(OrderItem item) {
        bookStockService.decreaseStock(item.getBookId(), item.getQuantity());
    }

    private void saveOrder(Order order) {
        orderRepository.save(order);
    }
}