package com.hysteryale.model.importFailure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "import_failure")
@NoArgsConstructor
@AllArgsConstructor
public class ImportFailure {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "importFailure_seq")
    @SequenceGenerator(name = "importFailure_seq", sequenceName = "importFailure_seq", allocationSize = 1)
    private int id;

    @Column(name = "primary_key")
    private String primaryKey;

    @Column(name = "reason_key")
    private String reasonKey;

    @Column(name = "reason_value")
    private String reasonValue;

    @Column(name = "file_uuid")
    private String fileUUID;

    private String type;

    @Transient
    private String reason;

    public ImportFailure(String primaryKey, String reasonKey, String reasonValue, String type) {
        this.primaryKey = primaryKey;
        this.reasonKey = reasonKey;
        this.reasonValue = reasonValue;
        this.type = type;
    }

    public String toString() {
        return String.format("%s (%s): %s", primaryKey, type, reason);
    }

}
