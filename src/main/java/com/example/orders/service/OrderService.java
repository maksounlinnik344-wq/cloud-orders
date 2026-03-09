package com.example.orders.service;

import com.example.orders.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private MinioService minioService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Order> orders = new HashMap<>();

    public OrderService() {
        // Заказы хранятся в памяти (потом можно добавить БД)
    }

    public Order createOrder(String customerName, String description) {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setCustomerName(customerName);
        order.setDescription(description);
        order.setStatus("NEW");
        order.setCreatedAt(LocalDateTime.now());

        orders.put(order.getId(), order);
        return order;
    }

    public void addFileToOrder(String orderId, MultipartFile file) throws IOException {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ не найден");
        }

        minioService.uploadFile(orderId, file.getOriginalFilename(), file);
        order.getFiles().add(file.getOriginalFilename());
    }

    public InputStream getFileFromOrder(String orderId, String fileName) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ не найден");
        }

        return minioService.downloadFile(orderId, fileName);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public void deleteFile(String orderId, String fileName) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ не найден");
        }

        minioService.deleteFile(orderId, fileName);
        order.getFiles().remove(fileName);
    }
}