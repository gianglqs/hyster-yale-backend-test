/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model_h2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "margin_data")
public class MarginData {
    @EmbeddedId
    private MarginDataId id;
    private String plant;

    @Column(name = "list_price")
    private double listPrice;

    @Column(name = "manufacturing_cost")
    private double manufacturingCost;

    @Column(name = "dealer_net")
    private double dealerNet;

    @Column(name = "file_uuid")
    private String fileUUID;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "is_sped")
    private boolean isSPED;

    private String series;
    private String region;

    public MarginData(MarginDataId id, String plant, double listPrice, double dealerNet, String series) {
        this.id = id;
        this.plant = plant;
        this.listPrice = listPrice;
        this.dealerNet = dealerNet;
        this.series = series;
    }
}
