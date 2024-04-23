/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.embedId;

import com.hysteryale.model.Country;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class GDPId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "country")
    private Country country;
    private int years;
}
