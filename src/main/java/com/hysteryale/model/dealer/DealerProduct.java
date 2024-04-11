package com.hysteryale.model.dealer;

import com.hysteryale.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dealer_product")
public class DealerProduct {
    @EmbeddedId
    private DealerProductId id;

    @ManyToOne
    @JoinColumn(name = "product")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "dealer")
    private Dealer dealer;

    private long quantity;

    @Column(name = "net_revenue")
    private double netRevenue;

    public DealerProduct (DealerProductId id, Product product, Dealer dealer, long quantity, double netRevenue) {
        this.id = id;
        this.product = product;
        this.dealer = dealer;
        this.quantity = quantity;
        this.netRevenue = netRevenue;
    }

    public DealerProduct (Product product, long quantity, double netRevenue) {
        this.product = product;
        this.quantity = quantity;
        this.netRevenue = netRevenue;
    }
}
