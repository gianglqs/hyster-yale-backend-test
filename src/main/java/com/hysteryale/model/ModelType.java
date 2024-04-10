package com.hysteryale.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.ui.Model;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "model_type")
@NoArgsConstructor
public class ModelType {

    @Id
    private int id;
    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "frequency")
    private Frequency frequency;

    public ModelType(String type) {
        this.type = type;
    }
}
