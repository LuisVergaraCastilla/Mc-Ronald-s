package org.mc.mcronalds.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="sale")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSale;

    private LocalDateTime saleDate;

    @Column(precision = 10,scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "idOrder")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "id")
    private User user;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<SaleDetail> saleDetails;
}
