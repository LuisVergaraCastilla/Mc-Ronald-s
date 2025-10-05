package org.mc.mcronalds.mercadopago;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

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
        // Webhook receiver: you can expand this to fetch payment by id and update your order/payment records
        return ResponseEntity.ok().build();
    }

    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("Payment approved");
    }

    @GetMapping("/failure")
    public ResponseEntity<String> failure() {
        return ResponseEntity.ok("Payment failed");
    }

    @GetMapping("/pending")
    public ResponseEntity<String> pending() {
        return ResponseEntity.ok("Payment pending");
    }
}


