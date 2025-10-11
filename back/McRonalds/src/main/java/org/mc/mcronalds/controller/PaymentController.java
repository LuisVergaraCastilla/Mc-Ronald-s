package org.mc.mcronalds.controller;

import org.mc.mcronalds.model.Payment;
import org.mc.mcronalds.model.PaymentStatus;
import org.mc.mcronalds.model.Order;
import org.mc.mcronalds.repository.PaymentRepository;
import org.mc.mcronalds.repository.OrderRepository;
import org.mc.mcronalds.mercadopago.MercadoPagoService;
import org.mc.mcronalds.mercadopago.MercadoPreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MercadoPagoService mercadoPagoService;

    // Obtener todos los pagos
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Obtener un pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo pago
    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) {
        return paymentRepository.save(payment);
    }

    // Actualizar un pago existente
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment updatedPayment) {
        return paymentRepository.findById(id).map(payment -> {
            payment.setPaymentDate(updatedPayment.getPaymentDate());
            payment.setAmount(updatedPayment.getAmount());
            payment.setPaymentMethod(updatedPayment.getPaymentMethod());
            payment.setPaymentStatus(updatedPayment.getPaymentStatus());
            payment.setOrder(updatedPayment.getOrder());
            payment.setTransactionId(updatedPayment.getTransactionId());
            return ResponseEntity.ok(paymentRepository.save(payment));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Eliminar un pago
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        if (paymentRepository.existsById(id)) {
            paymentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtener pagos por order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Payment> payments = paymentRepository.findByOrder(order.get());
        return ResponseEntity.ok(payments);
    }

    // Obtener pagos por estado
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByPaymentStatus(status);
        return ResponseEntity.ok(payments);
    }

    // Crear preferencia de pago para una orden
    @PostMapping("/create-preference/{orderId}")
    public ResponseEntity<?> createPaymentPreference(@PathVariable Long orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            
            // Crear request para MercadoPago
            MercadoPreferenceRequest request = new MercadoPreferenceRequest();
            request.setId(order.getIdOrder().toString());
            request.setTitle("Orden McRonalds #" + order.getIdOrder());
            request.setDescription("Pago de orden del " + order.getOrderDate());
            request.setQuantity(1);
            request.setUnitPrice(order.getTotalAmount());
            request.setCurrencyId("PEN");

            // Crear preferencia en MercadoPago
            Preference preference = mercadoPagoService.createPreference(request);
            
            // Crear registro de pago en base de datos
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod("MERCADOPAGO")
                    .paymentStatus(PaymentStatus.PENDING)
                    .transactionId(preference.getId())
                    .paymentDate(LocalDateTime.now())
                    .build();
            
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("preference_id", preference.getId());
            response.put("init_point", preference.getInitPoint());
            response.put("sandbox_init_point", preference.getSandboxInitPoint());
            response.put("payment_id", payment.getIdPayment());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear preferencia de pago");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Actualizar estado de pago (para webhook)
    @PostMapping("/update-status/{paymentId}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long paymentId, 
                                                @RequestBody Map<String, String> statusUpdate) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Payment payment = paymentOpt.get();
            String newStatus = statusUpdate.get("status");
            
            // Mapear estados de MercadoPago a PaymentStatus
            PaymentStatus paymentStatus;
            switch (newStatus.toUpperCase()) {
                case "APPROVED":
                    paymentStatus = PaymentStatus.APPROVED;
                    break;
                case "REJECTED":
                    paymentStatus = PaymentStatus.REJECTED;
                    break;
                case "CANCELLED":
                    paymentStatus = PaymentStatus.CANCELLED;
                    break;
                case "PENDING":
                    paymentStatus = PaymentStatus.PENDING;
                    break;
                case "IN_PROCESS":
                    paymentStatus = PaymentStatus.IN_PROCESS;
                    break;
                default:
                    paymentStatus = PaymentStatus.PENDING;
            }

            payment.setPaymentStatus(paymentStatus);
            if (paymentStatus == PaymentStatus.APPROVED) {
                payment.setPaymentDate(LocalDateTime.now());
            }

            paymentRepository.save(payment);

            // Actualizar estado de la orden si el pago fue exitoso
            if (paymentStatus == PaymentStatus.APPROVED) {
                Order order = payment.getOrder();
                order.setStatus(org.mc.mcronalds.model.OrderStatus.CONFIRMED);
                orderRepository.save(order);
            }

            return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar estado del pago");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
