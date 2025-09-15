package org.mc.mcronalds.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="order_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetail;

    private Long quantity;

    @Column(precision = 10,scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10,scale = 2)
    private BigDecimal subtotal;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "idOrder")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "idItem")
    private MenuItem item;
}
