package org.mc.mcronalds.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="sale_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SaleDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetail;

    private Long quantity;

    @Column(precision = 10,scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10,scale = 2)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "idSale")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "idItem")
    private MenuItem item;
}
