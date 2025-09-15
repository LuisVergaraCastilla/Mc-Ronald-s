package org.mc.mcronalds.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPayment;

    private LocalDateTime paymentDate;

    @Column(precision = 10,scale = 2)
    private BigDecimal amount;

    private String paymentMethod;

    private String paymentStatus;

    @ManyToOne
    @JoinColumn(name = "idOrder")
    private Order order;

    private String transactionId;


}
