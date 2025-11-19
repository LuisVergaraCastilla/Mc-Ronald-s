package org.mc.mcronalds.mercadopago;
import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.token}")
    private String token;

    @PostConstruct
    public void init(){
        MercadoPagoConfig.setAccessToken(token);
    }
}
