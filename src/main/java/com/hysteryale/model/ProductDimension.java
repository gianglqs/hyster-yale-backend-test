package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductDimension {
    @Id
    private String modelCode;
    private String metaSeries;
    private String brand;
    private String plant;
    private String clazz;
    private String segment;
    private String family;
    private String description;
    private String image;
    public ProductDimension(String plant, String clazz, String model) {
        this.plant = plant;
        this.clazz = clazz;
        this.modelCode = model;
    }
}
