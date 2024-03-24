package com.hysteryale.model.importFailure;

import com.hysteryale.model.enums.ImportFailureType;
import com.hysteryale.service.ImportService;
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

    private String reason;

    @Column(name = "file_name")
    private String fileName;

    private String type;

    public ImportFailure(String primaryKey, String reason, String type) {
        this.primaryKey = primaryKey;
        this.reason = reason;
        this.type = type;
    }


}
