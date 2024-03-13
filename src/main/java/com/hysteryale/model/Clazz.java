package com.hysteryale.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Clazz {
    @Id
    @GeneratedValue(generator = "clazz_sequence", strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(name = "clazz_name")
    private String clazzName;
}
