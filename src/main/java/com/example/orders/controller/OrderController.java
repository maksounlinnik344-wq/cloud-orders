package com.example.orders.controller;

import com.example.orders.model.Order;
import com.example.orders.service.OrderService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "index";
    }

    @GetMapping("/orders/new")
    public String newOrderForm(Model model) {
        model.addAttribute("order", new Order());
        return "order-form";
    }

    @PostMapping("/orders")
    public String createOrder(@RequestParam String customerName,
                              @RequestParam String description,
                              RedirectAttributes redirectAttributes) {
        Order order = orderService.createOrder(customerName, description);
        redirectAttributes.addFlashAttribute("message",
                "Заказ #" + order.getId().substring(0, 8) + " создан!");
        return "redirect:/";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable String id, Model model) {
        Order order = orderService.getAllOrders().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElseThrow();
        model.addAttribute("order", order);
        return "order-detail";
    }

    @PostMapping("/orders/{id}/files")
    public String uploadFile(@PathVariable String id,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) throws IOException {
        orderService.addFileToOrder(id, file);
        redirectAttributes.addFlashAttribute("message",
                "Файл " + file.getOriginalFilename() + " загружен!");
        return "redirect:/orders/" + id;
    }

    @GetMapping("/orders/{id}/files/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String id,
                                                 @PathVariable String fileName) {
        try {
            InputStream inputStream = orderService.getFileFromOrder(id, fileName);
            byte[] content = inputStream.readAllBytes();
            inputStream.close();

            ByteArrayResource resource = new ByteArrayResource(content);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .contentLength(content.length)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/orders/{id}/files/{fileName}/delete")
    public String deleteFile(@PathVariable String id,
                             @PathVariable String fileName,
                             RedirectAttributes redirectAttributes) {
        orderService.deleteFile(id, fileName);
        redirectAttributes.addFlashAttribute("message",
                "Файл " + fileName + " удален!");
        return "redirect:/orders/" + id;
    }
}