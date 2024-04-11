package com.hysteryale.model;

import com.hysteryale.model.upload.FileUpload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "import_tracking")
public class ImportTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_tracking_seq")
    @SequenceGenerator(name = "import_tracking_seq", sequenceName = "import_tracking_seq", allocationSize = 1)
    private int id;

    @Column(name = "belong_to_time")
    private LocalDate belongToTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_upload")
    private FileUpload fileUpload;


}
