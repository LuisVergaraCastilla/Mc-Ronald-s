package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.OrderDetail;
import org.mc.mcronalds.model.Order;
import org.mc.mcronalds.model.MenuItem;
import org.mc.mcronalds.repository.OrderDetailRepository;
import org.mc.mcronalds.repository.OrderRepository;
import org.mc.mcronalds.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MenuItemRepository menuItemRepository;

    // Obtener todos los detalles de orden
    @GetMapping
    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    // Obtener un detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetail> getOrderDetailById(@PathVariable Long id) {
        Optional<OrderDetail> detail = orderDetailRepository.findById(id);
        return detail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo detalle de orden
    @PostMapping
    public ResponseEntity<?> createOrderDetail(@RequestBody OrderDetail detail) {
        try {
            // Validaciones básicas
            if (detail.getOrder() == null || detail.getOrder().getIdOrder() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La orden es requerida");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (detail.getItem() == null || detail.getItem().getIdItem() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El item del menú es requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La cantidad debe ser mayor a 0");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar que la orden existe
            Optional<Order> orderOpt = orderRepository.findById(detail.getOrder().getIdOrder());
            if (orderOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La orden especificada no existe");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar que el item existe
            Optional<MenuItem> itemOpt = menuItemRepository.findById(detail.getItem().getIdItem());
            if (itemOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El item del menú especificado no existe");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar que la orden está en estado PENDING
            Order order = orderOpt.get();
            if (order.getStatus() != org.mc.mcronalds.model.OrderStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se pueden agregar detalles a órdenes con estado PENDING");
                error.put("current_status", order.getStatus().toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Establecer valores
            detail.setOrder(order);
            detail.setItem(itemOpt.get());
            
            // Calcular subtotal si no está establecido
            if (detail.getSubtotal() == null) {
                BigDecimal subtotal = detail.getItem().getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                detail.setSubtotal(subtotal);
            }
            
            // Establecer precio unitario si no está establecido
            if (detail.getUnitPrice() == null) {
                detail.setUnitPrice(detail.getItem().getPrice());
            }
            
            OrderDetail savedDetail = orderDetailRepository.save(detail);
            
            // Recalcular el total de la orden
            recalculateOrderTotal(order.getIdOrder());
            
            return ResponseEntity.ok(savedDetail);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear el detalle de orden");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Actualizar un detalle existente
    @PutMapping("/{id}")
    public ResponseEntity<OrderDetail> updateOrderDetail(@PathVariable Long id, @RequestBody OrderDetail updatedDetail) {
        return orderDetailRepository.findById(id).map(detail -> {
            detail.setQuantity(updatedDetail.getQuantity());
            detail.setUnitPrice(updatedDetail.getUnitPrice());
            detail.setSubtotal(updatedDetail.getSubtotal());
            detail.setNotes(updatedDetail.getNotes());
            detail.setOrder(updatedDetail.getOrder());
            detail.setItem(updatedDetail.getItem());
            return ResponseEntity.ok(orderDetailRepository.save(detail));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Eliminar un detalle
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderDetail(@PathVariable Long id) {
        try {
            Optional<OrderDetail> detailOpt = orderDetailRepository.findById(id);
            if (detailOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            OrderDetail detail = detailOpt.get();
            Long orderId = detail.getOrder().getIdOrder();
            
            // Verificar que la orden está en estado PENDING
            if (detail.getOrder().getStatus() != org.mc.mcronalds.model.OrderStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se pueden eliminar detalles de órdenes con estado PENDING");
                error.put("current_status", detail.getOrder().getStatus().toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            orderDetailRepository.deleteById(id);
            
            // Recalcular el total de la orden
            recalculateOrderTotal(orderId);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar el detalle de orden");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener detalles por orden
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetailsByOrder(@PathVariable Long orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<OrderDetail> details = orderDetailRepository.findByOrder(orderOpt.get());
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener detalles de la orden");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Actualizar cantidad de un detalle
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<?> updateDetailQuantity(@PathVariable Long id, @RequestBody Map<String, Long> quantityUpdate) {
        try {
            Optional<OrderDetail> detailOpt = orderDetailRepository.findById(id);
            if (detailOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            OrderDetail detail = detailOpt.get();
            
            // Verificar que la orden está en estado PENDING
            if (detail.getOrder().getStatus() != org.mc.mcronalds.model.OrderStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Solo se pueden modificar detalles de órdenes con estado PENDING");
                error.put("current_status", detail.getOrder().getStatus().toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            Long newQuantity = quantityUpdate.get("quantity");
            if (newQuantity == null || newQuantity <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La cantidad debe ser mayor a 0");
                return ResponseEntity.badRequest().body(error);
            }
            
            detail.setQuantity(newQuantity);
            
            // Recalcular subtotal
            BigDecimal newSubtotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity));
            detail.setSubtotal(newSubtotal);
            
            OrderDetail savedDetail = orderDetailRepository.save(detail);
            
            // Recalcular el total de la orden
            recalculateOrderTotal(detail.getOrder().getIdOrder());
            
            return ResponseEntity.ok(savedDetail);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar la cantidad");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Método privado para recalcular el total de una orden
    private void recalculateOrderTotal(Long orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                List<OrderDetail> details = orderDetailRepository.findByOrder(order);
                
                BigDecimal newTotal = details.stream()
                        .map(OrderDetail::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                order.setTotalAmount(newTotal);
                orderRepository.save(order);
            }
        } catch (Exception e) {
            System.err.println("Error al recalcular total de orden: " + e.getMessage());
        }
    }
}
