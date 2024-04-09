package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "product")
public class Product extends BaseModel{
    @Id
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    private int id;

    @NaturalId
    @Column(name = "model_code")
    private String modelCode;

    @NaturalId
    @Column(name = "series")
    private String series;

    private String brand;
    private String plant;

    @ManyToOne
    private Clazz clazz;
    private String segment;
    private String family;
    private String truckType;
    private String image;
    private String description;
    private String modelType;

    public Product(String plant, Clazz clazz, String modelCode) {
        this.plant = plant;
        this.clazz = clazz;
        this.modelCode = modelCode;
    }

    public Product(String modelCode, String series, String brand, String plant, Clazz clazz, String segment, String family, String truckType, String image, String description) {
        this.modelCode = modelCode;
        this.series = series;
        this.brand = brand;
        this.plant = plant;
        this.clazz = clazz;
        this.segment = segment;
        this.family = family;
        this.truckType = truckType;
        this.image = image;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(modelCode, product.modelCode) && Objects.equals(series, product.series);
    }

    public boolean equals(String modelCode, String series) {
        return this.modelCode.equals(modelCode) && this.series.equals(series);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelCode, series);
    }
}
