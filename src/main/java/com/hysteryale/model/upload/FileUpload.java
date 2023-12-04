package com.hysteryale.model.upload;

import com.hysteryale.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_upload")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fileUploadedSeq")
    private int id;
    private String uuid;

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne(fetch = FetchType.EAGER)
    private User uploadedBy;

    @Column(name = "uploaded_time")
    private Date uploadedTime;
}
