package com.example.security.fichierTechnique;


import java.util.Date;

public class TechnicalFileHistoryDTO {

    private TechnicalFile data;
    private Date revisionDate;
    private String revisionType;

    public TechnicalFile getData() {
        return data;
    }
    public void setData(TechnicalFile data) {
        this.data = data;
    }
    public Date getRevisionDate() {
        return revisionDate;
    }
    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }
    public String getRevisionType() {
        return revisionType;
    }
    public void setRevisionType(String revisionType) {
        this.revisionType = revisionType;
    }
}
