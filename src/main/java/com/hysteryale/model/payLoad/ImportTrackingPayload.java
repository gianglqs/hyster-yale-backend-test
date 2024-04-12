package com.hysteryale.model.payLoad;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportTrackingPayload implements Comparable<ImportTrackingPayload>{

    private String frequency;
    private String fileName;
    private String importAt;
    private String importBy;
    private String status;
    private String fileType;
    private String belongToTime;

    @Override
    public int compareTo(ImportTrackingPayload that) {
        return this.fileType.compareTo(that.fileType);
    }
}
