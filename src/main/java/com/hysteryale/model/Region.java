/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "region_short_name")
    private String regionShortName;

    @Column(name = "region_name")
    private String regionName;

    public Region(String regionName) {
        this.regionName = regionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return Objects.equals(regionShortName, region.regionShortName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regionShortName);
    }
}
