package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NaturalId
    @Column(name = "model_code")
    private String modelCode;

    @NaturalId
    @Column(name = "meta_series")
    private String metaSeries;

    private String brand;
    private String plant;
    private String clazz;
    private String segment;
    private String family;
    private String truckType;
    private String image;
    private String description;

    public Product(String plant, String clazz, String modelCode) {
        this.plant = plant;
        this.clazz = clazz;
        this.modelCode = modelCode;
    }
}
