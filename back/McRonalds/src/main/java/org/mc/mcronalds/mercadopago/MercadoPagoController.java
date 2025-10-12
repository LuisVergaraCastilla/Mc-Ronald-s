package org.mc.mcronalds.mercadopago;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.client.payment.PaymentClient;
import org.mc.mcronalds.model.PaymentStatus;
import org.mc.mcronalds.repository.PaymentRepository;
import org.mc.mcronalds.repository.OrderRepository;
import org.mc.mcronalds.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/preference")
    public ResponseEntity<?> createPreference(@RequestBody MercadoPreferenceRequest request) throws Exception {
        try {
            Preference preference = mercadoPagoService.createPreference(request);
            return ResponseEntity.ok(Map.of(
                    "id", preference.getId(),
                    "init_point", preference.getInitPoint(),
                    "sandbox_init_point", preference.getSandboxInitPoint()
            ));
        } catch (MPApiException e) {
            int status = e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : 500;
            String body = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            return ResponseEntity.status(HttpStatus.valueOf(status)).body(Map.of(
                    "error", "mercadopago_api_error",
                    "status", String.valueOf(status),
                    "details", body
            ));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam(required = false) String type,
                                        @RequestParam(required = false) String data_id,
                                        @RequestBody(required = false) Map<String, Object> body) {
        try {
            if (type != null && data_id != null) {
                // Procesar notificación de MercadoPago
                if (type.equals("payment")) {
                    processPaymentNotification(data_id);
                } else if (type.equals("preference")) {
                    // Manejar notificaciones de preferencia si es necesario
                    System.out.println("Notificación de preferencia recibida: " + data_id);
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    private void processPaymentNotification(String paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            
            if (payment != null) {
                String externalReference = payment.getExternalReference();
                if (externalReference != null) {
                    // Buscar el pago en nuestra base de datos por external_reference (order ID)
                    Optional<org.mc.mcronalds.model.Payment> ourPayment = 
                        paymentRepository.findByOrder_IdOrder(Long.parseLong(externalReference));
                    
                    if (ourPayment.isPresent()) {
                        org.mc.mcronalds.model.Payment paymentEntity = ourPayment.get();
                        
                        // Mapear el estado de MercadoPago a nuestro enum
                        PaymentStatus newStatus;
                        switch (payment.getStatus()) {
                            case "approved":
                                newStatus = PaymentStatus.APPROVED;
                                break;
                            case "rejected":
                                newStatus = PaymentStatus.REJECTED;
                                break;
                            case "cancelled":
                                newStatus = PaymentStatus.CANCELLED;
                                break;
                            case "pending":
                                newStatus = PaymentStatus.PENDING;
                                break;
                            case "in_process":
                                newStatus = PaymentStatus.IN_PROCESS;
                                break;
                            default:
                                newStatus = PaymentStatus.PENDING;
                        }
                        
                        paymentEntity.setPaymentStatus(newStatus);
                        if (newStatus == PaymentStatus.APPROVED) {
                            paymentEntity.setPaymentDate(LocalDateTime.now());
                        }
                        
                        paymentRepository.save(paymentEntity);
                        
                        // Actualizar estado de la orden si el pago fue exitoso
                        if (newStatus == PaymentStatus.APPROVED) {
                            Order order = paymentEntity.getOrder();
                            order.setStatus(org.mc.mcronalds.model.OrderStatus.CONFIRMED);
                            orderRepository.save(order);
                        }
                        
                        System.out.println("Estado de pago actualizado: " + paymentEntity.getIdPayment() + " -> " + newStatus);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando notificación de pago: " + e.getMessage());
        }
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

}


