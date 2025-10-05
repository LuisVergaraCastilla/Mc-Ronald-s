package org.mc.mcronalds.mercadopago;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MercadoPreferenceRequest {
    private String id;
    private String title;
    private String description;
    private String pictureUrl;
    private String categoryId;
    private int quantity;
    private BigDecimal unitPrice;
    private String currencyId;

}
