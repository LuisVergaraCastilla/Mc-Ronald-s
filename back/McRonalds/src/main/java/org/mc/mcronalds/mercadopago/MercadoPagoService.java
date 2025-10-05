package org.mc.mcronalds.mercadopago;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    public Preference createPreference(MercadoPreferenceRequest mercadoPreferenceRequest) throws Exception{
        PreferenceItemRequest itemRequest =
                PreferenceItemRequest.builder()
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
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items).build();
        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }
}
