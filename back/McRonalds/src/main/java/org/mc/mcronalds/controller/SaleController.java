package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.Sale;
import org.mc.mcronalds.model.SaleDetail;
import org.mc.mcronalds.model.Order;
import org.mc.mcronalds.model.User;
import org.mc.mcronalds.repository.SaleRepository;
import org.mc.mcronalds.repository.SaleDetailRepository;
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
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private SaleDetailRepository saleDetailRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Obtener todas las ventas
    @GetMapping
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    // Obtener una venta por ID
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        Optional<Sale> sale = saleRepository.findById(id);
        return sale.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear una nueva venta desde una orden
    @PostMapping("/from-order/{orderId}")
    public ResponseEntity<?> createSaleFromOrder(@PathVariable Long orderId) {
        try {
            // Verificar que la orden existe
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Order order = orderOpt.get();
            
            // Verificar que la orden esté confirmada
            if (order.getStatus() != org.mc.mcronalds.model.OrderStatus.CONFIRMED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "La orden debe estar confirmada para crear una venta");
                error.put("current_status", order.getStatus().toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar que no exista ya una venta para esta orden
            Optional<Sale> existingSale = saleRepository.findByOrder(order);
            if (existingSale.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ya existe una venta para esta orden");
                error.put("sale_id", existingSale.get().getIdSale().toString());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Crear la venta
            Sale sale = Sale.builder()
                    .order(order)
                    .user(order.getUser())
                    .saleDate(LocalDateTime.now())
                    .totalAmount(order.getTotalAmount())
                    .build();
            
            Sale savedSale = saleRepository.save(sale);
            
            // Copiar los detalles de la orden a los detalles de venta
            if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                for (var orderDetail : order.getOrderDetails()) {
                    SaleDetail saleDetail = SaleDetail.builder()
                            .sale(savedSale)
                            .item(orderDetail.getItem())
                            .quantity(orderDetail.getQuantity())
                            .unitPrice(orderDetail.getUnitPrice())
                            .subtotal(orderDetail.getSubtotal())
                            .build();
                    saleDetailRepository.save(saleDetail);
                }
            }
            
            // Actualizar el estado de la orden a CONFIRMED (ya que no hay COMPLETED en el enum)
            // La orden ya está confirmada, no necesitamos cambiar el estado
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Venta creada exitosamente");
            response.put("sale", savedSale);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear la venta");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Crear una venta manual (sin orden previa)
    @PostMapping
    public ResponseEntity<?> createSale(@RequestBody Sale sale) {
        try {
            // Validaciones básicas
            if (sale.getUser() == null || sale.getUser().getId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El usuario es requerido");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (sale.getTotalAmount() == null || sale.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El monto total debe ser mayor a 0");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Verificar que el usuario existe
            Optional<User> userOpt = userRepository.findById(sale.getUser().getId());
            if (userOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El usuario especificado no existe");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Establecer valores por defecto
            sale.setUser(userOpt.get());
            sale.setSaleDate(LocalDateTime.now());
            
            Sale savedSale = saleRepository.save(sale);
            return ResponseEntity.ok(savedSale);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear la venta");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Actualizar una venta existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSale(@PathVariable Long id, @RequestBody Sale updatedSale) {
        try {
            Optional<Sale> saleOpt = saleRepository.findById(id);
            if (saleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sale sale = saleOpt.get();
            sale.setTotalAmount(updatedSale.getTotalAmount());
            
            Sale savedSale = saleRepository.save(sale);
            return ResponseEntity.ok(savedSale);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar la venta");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Eliminar una venta
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSale(@PathVariable Long id) {
        try {
            Optional<Sale> saleOpt = saleRepository.findById(id);
            if (saleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Sale sale = saleOpt.get();
            
            // Eliminar detalles de venta primero
            List<SaleDetail> saleDetails = saleDetailRepository.findBySale(sale);
            saleDetailRepository.deleteAll(saleDetails);
            
            saleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar la venta");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener ventas por usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getSalesByUser(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<Sale> sales = saleRepository.findByUser(userOpt.get());
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener ventas del usuario");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener ventas por rango de fechas
    @GetMapping("/date-range")
    public ResponseEntity<?> getSalesByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener ventas por rango de fechas");
            error.put("message", "Formato de fecha inválido. Use: yyyy-MM-ddTHH:mm:ss");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Obtener detalles de una venta
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getSaleDetails(@PathVariable Long id) {
        try {
            Optional<Sale> saleOpt = saleRepository.findById(id);
            if (saleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            List<SaleDetail> details = saleDetailRepository.findBySale(saleOpt.get());
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener detalles de la venta");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // Obtener estadísticas de ventas
    @GetMapping("/statistics")
    public ResponseEntity<?> getSalesStatistics(@RequestParam(required = false) String startDate, 
                                               @RequestParam(required = false) String endDate) {
        try {
            List<Sale> sales;
            
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.parse(startDate);
                LocalDateTime end = LocalDateTime.parse(endDate);
                sales = saleRepository.findBySaleDateBetween(start, end);
            } else {
                sales = saleRepository.findAll();
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total_sales", sales.size());
            statistics.put("total_amount", sales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            
            // Calcular promedio
            if (!sales.isEmpty()) {
                BigDecimal totalAmount = sales.stream()
                        .map(Sale::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(sales.size()), 2, java.math.RoundingMode.HALF_UP);
                statistics.put("average_amount", averageAmount);
            } else {
                statistics.put("average_amount", BigDecimal.ZERO);
            }
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al calcular estadísticas");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
