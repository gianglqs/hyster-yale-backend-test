package com.hysteryale.model.competitor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "competitor_color")
public class CompetitorColor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "color_code")
    private String colorCode;

    public CompetitorColor(String groupName, String colorCode) {
        this.groupName = groupName;
        this.colorCode = colorCode;
    }
}
