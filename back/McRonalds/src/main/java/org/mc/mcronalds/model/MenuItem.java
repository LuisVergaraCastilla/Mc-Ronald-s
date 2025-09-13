package org.mc.mcronalds.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="menu_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItem;

    private String name;

    private String description;

    @Column(precision = 10,scale = 2)
    private BigDecimal price;

    private String imageUrl;

    private Long preparationTime;

    @ManyToOne
    @JoinColumn(name = "idCategory")
    private MenuCategory category;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<SaleDetail> saleDetails;

}
