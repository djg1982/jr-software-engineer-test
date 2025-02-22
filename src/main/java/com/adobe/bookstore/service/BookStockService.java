package com.adobe.bookstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adobe.bookstore.BookStockRepository;
import com.adobe.bookstore.model.BookStock;

import java.util.Optional;

@Service
public class BookStockService {

    @Autowired
    private BookStockRepository bookStockRepository;

    public Optional<BookStock> getBookStock(String bookId) {
        return bookStockRepository.findById(bookId);
    }

    public void saveBookStock(BookStock bookStock) {
        bookStockRepository.save(bookStock);
    }

    public void decreaseStock(String bookId, int quantity) {
        Optional<BookStock> bookStock = bookStockRepository.findById(bookId);
        if (bookStock.isPresent()) {
            BookStock stock = bookStock.get();
            stock.setQuantity(stock.getQuantity() - quantity);
            bookStockRepository.save(stock);
        }
    }
}