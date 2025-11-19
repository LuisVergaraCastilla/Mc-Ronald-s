package org.mc.mcronalds.mercadopago;

import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.client.payment.PaymentClient;
import org.mc.mcronalds.model.PaymentStatus;
import org.mc.mcronalds.repository.PaymentRepository;
import org.mc.mcronalds.repository.OrderRepository;
import org.mc.mcronalds.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoController.class);
    
    private final MercadoPagoService mercadoPagoService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }


    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam(required = false) String type,
                                        @RequestParam(required = false) String data_id,
                                        @RequestBody(required = false) Map<String, Object> body) {
        try {
            logger.info("Webhook received - Type: {}, Data ID: {}", type, data_id);
            
            if (type != null && data_id != null) {
                if ("payment".equals(type)) {
                    logger.info("Processing payment notification: {}", data_id);
                    processPaymentNotification(data_id);
                } else if ("preference".equals(type)) {
                    logger.info("Preference notification received: {}", data_id);
                } else {
                    logger.warn("Unknown notification type: {}", type);
                }
            } else if (body != null && body.containsKey("topic")) {
                String topic = (String) body.get("topic");
                String resource = (String) body.get("resource");
                logger.info("Alternative webhook - Topic: {}, Resource: {}", topic, resource);
                
                if ("merchant_order".equals(topic) && resource != null) {
                    logger.info("Processing merchant order: {}", resource);
                }
            } else if (body != null && body.containsKey("type") && "payment".equals(body.get("type"))) {
                logger.info("Payment webhook detected in body");
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data != null && data.containsKey("id")) {
                    String paymentId = data.get("id").toString();
                    logger.info("Processing payment from body: {}", paymentId);
                    processPaymentNotification(paymentId);
                }
            } else if (body != null && body.containsKey("action") && "payment.created".equals(body.get("action"))) {
                logger.info("Payment created webhook detected");
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data != null && data.containsKey("id")) {
                    String paymentId = data.get("id").toString();
                    logger.info("Processing created payment: {}", paymentId);
                    processPaymentNotification(paymentId);
                }
            } else {
                logger.warn("Webhook with unrecognized parameters. Body: {}", body);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    private void processPaymentNotification(String paymentId) {
        try {
            logger.info("Getting payment information from MercadoPago: {}", paymentId);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            
            if (payment != null) {
                logger.info("Payment obtained from MercadoPago - ID: {}, Status: {}, External Reference: {}, Amount: {}", 
                    payment.getId(), payment.getStatus(), payment.getExternalReference(), payment.getTransactionAmount());
                
                String externalReference = payment.getExternalReference();
                if (externalReference != null) {
                    logger.info("Searching payment in database by external_reference: {}", externalReference);
                    
                    Optional<org.mc.mcronalds.model.Payment> ourPayment = 
                        paymentRepository.findByOrder_IdOrder(Long.parseLong(externalReference));
                    
                    if (ourPayment.isPresent()) {
                        org.mc.mcronalds.model.Payment paymentEntity = ourPayment.get();
                        logger.info("Payment found in database with ID: {}, current status: {}", 
                            paymentEntity.getIdPayment(), paymentEntity.getPaymentStatus());
                        
                        PaymentStatus newStatus = mapMercadoPagoStatus(payment.getStatus());
                        logger.info("Updating status from {} to {}", paymentEntity.getPaymentStatus(), newStatus);
                        
                        paymentEntity.setPaymentStatus(newStatus);
                        if (newStatus == PaymentStatus.APPROVED) {
                            paymentEntity.setPaymentDate(LocalDateTime.now());
                        }
                        
                        paymentRepository.save(paymentEntity);
                        logger.info("Payment status updated in database: {} -> {}", paymentEntity.getIdPayment(), newStatus);
                        
                        if (newStatus == PaymentStatus.APPROVED) {
                            Order order = paymentEntity.getOrder();
                            order.setStatus(org.mc.mcronalds.model.OrderStatus.CONFIRMED);
                            orderRepository.save(order);
                            logger.info("Order updated to CONFIRMED: {}", order.getIdOrder());
                        }
                    } else {
                        logger.warn("Payment not found in database for external_reference: {}", externalReference);
                        logger.debug("All payments in database: {}", 
                            paymentRepository.findAll().stream()
                                .map(p -> "ID: " + p.getIdPayment() + ", Order: " + p.getOrder().getIdOrder() + ", Status: " + p.getPaymentStatus())
                                .toList());
                    }
                } else {
                    logger.warn("External reference is null in MercadoPago payment");
                }
            } else {
                logger.error("Could not obtain payment from MercadoPago");
            }
        } catch (Exception e) {
            logger.error("Error processing payment notification: {}", e.getMessage(), e);
        }
    }
    
    private PaymentStatus mapMercadoPagoStatus(String mercadoPagoStatus) {
        return switch (mercadoPagoStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "pending" -> PaymentStatus.PENDING;
            case "in_process" -> PaymentStatus.IN_PROCESS;
            default -> PaymentStatus.PENDING;
        };
    }

    @GetMapping("/success")
    public ResponseEntity<String> success(@RequestParam(required = false) String preference_id,
                                          @RequestParam(required = false) String payment_id) {
        // Aquí puedes redirigir a una página de éxito del frontend
        // Por ejemplo: return ResponseEntity.status(302).header("Location", "http://tu-frontend.com/success").build();
        
        String message = "Pago aprobado exitosamente";
        if (preference_id != null) {
            message += " - Preference ID: " + preference_id;
        }
        if (payment_id != null) {
            message += " - Payment ID: " + payment_id;
        }
        
        return ResponseEntity.ok(message);
    }

    @GetMapping("/failure")
    public ResponseEntity<String> failure(@RequestParam(required = false) String preference_id) {
        // Aquí puedes redirigir a una página de error del frontend
        
        String message = "El pago fue rechazado o cancelado";
        if (preference_id != null) {
            message += " - Preference ID: " + preference_id;
        }
        
        return ResponseEntity.ok(message);
    }

    @GetMapping("/pending")
    public ResponseEntity<String> pending(@RequestParam(required = false) String preference_id) {
        // Aquí puedes redirigir a una página de pendiente del frontend
        
        String message = "El pago está pendiente de aprobación";
        if (preference_id != null) {
            message += " - Preference ID: " + preference_id;
        }
        
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<String> base() {
        return ResponseEntity.ok("MercadoPago API activa");
    }

    @GetMapping("/test")
    public ResponseEntity<?> testPreference() {
        try {
            MercadoPreferenceRequest testRequest = createTestRequest();
            Preference preference = mercadoPagoService.createPreference(testRequest);
            
            createTestPayment(preference);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Test preference created successfully",
                    "preference_id", preference.getId(),
                    "init_point", preference.getInitPoint(),
                    "sandbox_init_point", preference.getSandboxInitPoint(),
                    "amount", "25.50",
                    "currency", "PEN"
            ));
        } catch (Exception e) {
            logger.error("Error creating test preference: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error creating test preference",
                    "message", e.getMessage()
            ));
        }
    }
    
    private MercadoPreferenceRequest createTestRequest() {
        MercadoPreferenceRequest testRequest = new MercadoPreferenceRequest();
        String testId = "TEST-" + System.currentTimeMillis();
        testRequest.setId(testId);
        testRequest.setTitle("Test McRonalds - Big Mac Combo");
        testRequest.setDescription("Test payment for Big Mac combo");
        testRequest.setQuantity(1);
        testRequest.setUnitPrice(new java.math.BigDecimal("25.50"));
        testRequest.setCurrencyId("PEN");
        return testRequest;
    }
    
    private void createTestPayment(Preference preference) {
        try {
            Optional<Order> existingOrder = orderRepository.findById(1L);
            Order order = existingOrder.orElseGet(() -> {
                Order newOrder = Order.builder()
                        .idOrder(1L)
                        .orderDate(LocalDateTime.now())
                        .totalAmount(new java.math.BigDecimal("25.50"))
                        .status(org.mc.mcronalds.model.OrderStatus.PENDING)
                        .notes("Test order")
                        .build();
                return orderRepository.save(newOrder);
            });
            
            org.mc.mcronalds.model.Payment payment = org.mc.mcronalds.model.Payment.builder()
                    .order(order)
                    .amount(new java.math.BigDecimal("25.50"))
                    .paymentMethod("MERCADOPAGO")
                    .paymentStatus(PaymentStatus.PENDING)
                    .transactionId(preference.getId())
                    .paymentDate(LocalDateTime.now())
                    .build();
            
            paymentRepository.save(payment);
            logger.info("Test payment created in database with ID: {}", payment.getIdPayment());
            
        } catch (Exception e) {
            logger.error("Error creating test payment in database: {}", e.getMessage(), e);
        }
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupPendingPayments() {
        try {
            List<org.mc.mcronalds.model.Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);
            int cleanedCount = 0;
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            
            for (org.mc.mcronalds.model.Payment payment : pendingPayments) {
                if (payment.getPaymentDate() != null && payment.getPaymentDate().isBefore(cutoffTime)) {
                    paymentRepository.delete(payment);
                    cleanedCount++;
                }
            }
            
            logger.info("Cleaned up {} old pending payments", cleanedCount);
            return ResponseEntity.ok(Map.of(
                    "message", "Cleanup completed",
                    "cleaned_payments", cleanedCount,
                    "remaining_pending", pendingPayments.size() - cleanedCount
            ));
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Cleanup error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/test-payment/{paymentId}")
    public ResponseEntity<?> testPaymentProcessing(@PathVariable String paymentId) {
        try {
            logger.info("Processing test payment: {}", paymentId);
            processPaymentNotification(paymentId);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Payment processed successfully",
                    "payment_id", paymentId
            ));
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Payment processing error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getPaymentStatus() {
        try {
            List<org.mc.mcronalds.model.Payment> allPayments = paymentRepository.findAll();
            List<org.mc.mcronalds.model.Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);
            List<org.mc.mcronalds.model.Payment> approvedPayments = paymentRepository.findByPaymentStatus(PaymentStatus.APPROVED);
            
            return ResponseEntity.ok(Map.of(
                    "total_payments", allPayments.size(),
                    "pending_payments", pendingPayments.size(),
                    "approved_payments", approvedPayments.size(),
                    "payments", allPayments.stream().map(p -> Map.of(
                            "id", p.getIdPayment(),
                            "status", p.getPaymentStatus(),
                            "amount", p.getAmount(),
                            "transaction_id", p.getTransactionId(),
                            "order_id", p.getOrder().getIdOrder()
                    )).toList()
            ));
        } catch (Exception e) {
            logger.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Status retrieval error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/approve-all-pending")
    public ResponseEntity<?> approveAllPendingPayments() {
        try {
            List<org.mc.mcronalds.model.Payment> pendingPayments = paymentRepository.findByPaymentStatus(PaymentStatus.PENDING);
            int approvedCount = 0;
            
            for (org.mc.mcronalds.model.Payment payment : pendingPayments) {
                logger.info("Approving payment manually: {}", payment.getIdPayment());
                payment.setPaymentStatus(PaymentStatus.APPROVED);
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);
                
                Order order = payment.getOrder();
                order.setStatus(org.mc.mcronalds.model.OrderStatus.CONFIRMED);
                orderRepository.save(order);
                
                approvedCount++;
                logger.info("Payment approved: {}", payment.getIdPayment());
            }
            
            logger.info("All pending payments approved: {}", approvedCount);
            return ResponseEntity.ok(Map.of(
                    "message", "All pending payments were approved",
                    "approved_count", approvedCount
            ));
        } catch (Exception e) {
            logger.error("Error approving payments: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Payment approval error",
                    "message", e.getMessage()
            ));
        }
    }

}


