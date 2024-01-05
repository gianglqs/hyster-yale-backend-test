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
@Table(name = "productdimension")
public class ProductDimension {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, name = "metaseries")
    private String metaSeries;
    private String brand;
    private String plant;
    private String clazz;
    private String segment;
    private String model;

    public ProductDimension(String plant, String clazz, String model) {
        this.plant = plant;
        this.clazz = clazz;
        this.model = model;
    }


}
