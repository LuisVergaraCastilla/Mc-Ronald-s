package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.OrderDetail;
import org.mc.mcronalds.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

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
    public OrderDetail createOrderDetail(@RequestBody OrderDetail detail) {
        return orderDetailRepository.save(detail);
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
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Long id) {
        if (orderDetailRepository.existsById(id)) {
            orderDetailRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
