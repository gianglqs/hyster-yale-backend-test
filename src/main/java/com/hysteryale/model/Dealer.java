package com.hysteryale.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Dealer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dealer_id_seq")
    private int id;
    private String name;
    private String description;

    public Dealer(String name){
        this.name = name;
    }
}
