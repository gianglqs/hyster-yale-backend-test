package com.hysteryale.model.upload;

import com.hysteryale.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "uploadedBy_id")
    private User uploadedBy;

    @Column(name = "uploaded_time")
    private LocalDateTime uploadedTime;

    @Column(name = "model_type")
    private String modelType;

    @Column(name = "success")
    private boolean success;
}
