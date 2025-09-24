package org.mc.mcronalds.mercadopago;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreferenceRequest {
    private String idProducto;
    private String titulo;
    private String descripcion;
    private String pictureUrl;
    private String categoria;
    private int cantidad;
    private BigDecimal precio;
    private String moneda;

}
