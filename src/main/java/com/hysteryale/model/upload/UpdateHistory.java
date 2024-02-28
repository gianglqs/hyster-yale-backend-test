package com.hysteryale.model.upload;

import com.hysteryale.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "update_history")
public class UpdateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_upload")
    @Nullable
    private FileUpload fileUpload;

    @Column(name = "model_type")
    private String modelType;

    private LocalDateTime time;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @Column
    private boolean success;

}
