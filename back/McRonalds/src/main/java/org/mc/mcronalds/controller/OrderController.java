package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.Order;
import org.mc.mcronalds.model.OrderStatus;
import org.mc.mcronalds.model.User;
import org.mc.mcronalds.repository.OrderRepository;
import org.mc.mcronalds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Obtener todas las órdenes
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Obtener una orden por ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear una nueva orden
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            // Validaciones básicas
            if (order.getUser() == null || order.getUser().getId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El usuario es requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El monto total debe ser mayor a 0");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar que el usuario existe
            Optional<User> userOpt = userRepository.findById(order.getUser().getId());
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El usuario especificado no existe");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Establecer valores por defecto
            order.setUser(userOpt.get());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);
            
            Order savedOrder = orderRepository.save(order);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear la orden");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Actualizar una orden existente
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        return orderRepository.findById(id).map(order -> {
            order.setOrderDate(updatedOrder.getOrderDate());
            order.setTotalAmount(updatedOrder.getTotalAmount());
            order.setStatus(updatedOrder.getStatus());
            order.setNotes(updatedOrder.getNotes());
            order.setUser(updatedOrder.getUser());
            order.setStimatedTime(updatedOrder.getStimatedTime());
            order.setActualTime(updatedOrder.getActualTime());
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Eliminar una orden
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // Solo permitir eliminar órdenes pendientes
            if (order.getStatus() != OrderStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se pueden eliminar órdenes con estado PENDING");
                error.put("current_status", order.getStatus().toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            orderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar la orden");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener órdenes por usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Order> orders = orderRepository.findByUser(userOpt.get());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener órdenes del usuario");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener órdenes por estado
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    // Actualizar estado de orden
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            String newStatusStr = statusUpdate.get("status");
            
            try {
                OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
                order.setStatus(newStatus);
                
                // Actualizar tiempo real si se está completando
                if (newStatus == OrderStatus.COMPLETED) {
                    order.setActualTime(System.currentTimeMillis() - order.getOrderDate().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
                }
                
                Order savedOrder = orderRepository.save(order);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Estado de orden actualizado correctamente");
                response.put("order", savedOrder);
                
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Estado de orden inválido");
                error.put("valid_statuses", "PENDING, CONFIRMED, IN_PREPARATION, READY, COMPLETED, CANCELLED");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar el estado de la orden");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener órdenes por rango de fechas
    @GetMapping("/date-range")
    public ResponseEntity<?> getOrdersByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener órdenes por rango de fechas");
            error.put("message", "Formato de fecha inválido. Use: yyyy-MM-ddTHH:mm:ss");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
