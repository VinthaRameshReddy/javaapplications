package com.medgo.member.domain.response;

import lombok.Data;

@Data
public class PdfResponse {



    private String base64File;
    private String fileName;

    public PdfResponse(String base64File, String fileName) {
        this.base64File = base64File;
        this.fileName = fileName;
    }

    public String getBase64File() {
        return base64File;
    }

    public void setBase64File(String base64File) {
        this.base64File = base64File;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }




}
