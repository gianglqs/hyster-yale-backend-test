/*
 * Copyright (c) 2024. Hyster-Yale Group
 * All rights reserved.
 */

package com.hysteryale.model.versionTag;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "version_app")
public class VersionApp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String type; // backend or frontend
    private String version;
}
