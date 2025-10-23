package org.mc.mcronalds.mercadopago;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.backurl.success:http://localhost:8080/api/mercadopago/success}")
    private String backUrlSuccess;

    @Value("${mercadopago.backurl.failure:http://localhost:8080/api/mercadopago/failure}")
    private String backUrlFailure;

    @Value("${mercadopago.backurl.pending:http://localhost:8080/api/mercadopago/pending}")
    private String backUrlPending;

    @Value("${mercadopago.notificationUrl:http://localhost:8080/api/mercadopago/webhook}")
    private String notificationUrl;

    public Preference createPreference(MercadoPreferenceRequest mercadoPreferenceRequest) throws Exception {
        logger.info("Creating MercadoPago preference - ID: {}, Title: {}, Amount: {}, Currency: {}", 
            mercadoPreferenceRequest.getId(), mercadoPreferenceRequest.getTitle(), 
            mercadoPreferenceRequest.getUnitPrice(), mercadoPreferenceRequest.getCurrencyId());
        
        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(mercadoPreferenceRequest.getId())
                .title(mercadoPreferenceRequest.getTitle())
                .description(mercadoPreferenceRequest.getDescription())
                .pictureUrl(mercadoPreferenceRequest.getPictureUrl())
                .categoryId(mercadoPreferenceRequest.getCategoryId())
                .quantity(mercadoPreferenceRequest.getQuantity())
                .currencyId(mercadoPreferenceRequest.getCurrencyId())
                .unitPrice(mercadoPreferenceRequest.getUnitPrice())
                .build();
        
        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(itemRequest);
        
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(backUrlSuccess)
                .failure(backUrlFailure)
                .pending(backUrlPending)
                .build();

        logger.debug("Configured URLs - Success: {}, Failure: {}, Pending: {}, Notification: {}", 
            backUrlSuccess, backUrlFailure, backUrlPending, notificationUrl);

        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .notificationUrl(notificationUrl)
                .externalReference(mercadoPreferenceRequest.getId());

        if (backUrlSuccess != null && !backUrlSuccess.isBlank() && !backUrlSuccess.contains("localhost")) {
            builder.autoReturn("approved");
        }

        PreferenceRequest preferenceRequest = builder.build();
        PreferenceClient client = new PreferenceClient();
        
        logger.info("Sending request to MercadoPago...");
        Preference preference = client.create(preferenceRequest);
        
        logger.info("Preference created successfully - ID: {}, Init Point: {}, Sandbox Init Point: {}", 
            preference.getId(), preference.getInitPoint(), preference.getSandboxInitPoint());
        
        return preference;
    }
}
