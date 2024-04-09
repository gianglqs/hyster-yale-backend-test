package com.hysteryale.model.embedId;

import com.hysteryale.model.Product;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class ResidualValueId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "product")
    private Product product;
    private int hours;
}
