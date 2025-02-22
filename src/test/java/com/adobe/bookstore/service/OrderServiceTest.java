package com.adobe.bookstore.service;

import com.adobe.bookstore.model.BookStock;
import com.adobe.bookstore.model.Order;
import com.adobe.bookstore.model.OrderItem;
import com.adobe.bookstore.model.Order.Status;
import com.adobe.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private BookStockService bookStockService;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOrderSuccess() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        OrderItem item = new OrderItem(1, order, UUID.randomUUID().toString());
        order.setItems(Collections.singletonList(item));

        when(bookStockService.getBookStock(item.getBookId())).thenReturn(Optional.of(new BookStock(item.getBookId(), 10)));
        doNothing().when(bookStockService).decreaseStock(item.getBookId(), item.getQuantity());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String orderId = orderService.createOrder(order);

        assertEquals(order.getId(), orderId);
        assertEquals(Status.SUCCESS, order.getStatus());
        verify(bookStockService, times(1)).decreaseStock(item.getBookId(), item.getQuantity());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @Sql(statements = {
        "DELETE FROM book_stock WHERE id = 'existing-book-id'",
        "INSERT INTO book_stock (id, name, quantity) VALUES ('existing-book-id', 'some book', 7)"
    })
    public void testCreateOrderInsufficientStock() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        OrderItem item = new OrderItem(10, order, "existing-book-id");
        order.setItems(Collections.singletonList(item));

        when(bookStockService.getBookStock(item.getBookId())).thenReturn(Optional.of(new BookStock(item.getBookId(), 7)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
        assertEquals("Not enough stock for book: " + item.getBookId(), exception.getMessage());
    }

    @Test
    public void testCreateOrderWithExistingInventory() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        OrderItem item = new OrderItem(2, order, "ae1666d6-6100-4ef0-9037-b45dd0d5bb0e");
        order.setItems(Collections.singletonList(item));

        when(bookStockService.getBookStock(item.getBookId())).thenReturn(Optional.of(new BookStock(item.getBookId(), 7)));
        doNothing().when(bookStockService).decreaseStock(item.getBookId(), item.getQuantity());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        String orderId = orderService.createOrder(order);

        assertEquals(order.getId(), orderId);
        assertEquals(Status.SUCCESS, order.getStatus());
        verify(bookStockService, times(1)).decreaseStock(item.getBookId(), item.getQuantity());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testCreateOrderWithInventedBook() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        OrderItem item = new OrderItem(1, order, "invented-book-id");
        order.setItems(Collections.singletonList(item));

        when(bookStockService.getBookStock(item.getBookId())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
        assertEquals("Not enough stock for book: " + item.getBookId(), exception.getMessage());
    }
}
