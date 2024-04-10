package com.hysteryale.model.payLoad;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportTrackingPayload {

    private String frequency;
    private String fileName;
    private String importAt;
    private String importBy;
    private String status;
    private String fileType;
    private String belongToTime;
}
