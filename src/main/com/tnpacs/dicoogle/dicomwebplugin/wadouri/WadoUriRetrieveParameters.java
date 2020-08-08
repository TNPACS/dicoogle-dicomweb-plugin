package com.tnpacs.dicoogle.dicomwebplugin.wadouri;

import com.tnpacs.dicoogle.dicomwebplugin.exceptions.InvalidParametersException;

import java.util.Map;

public class WadoUriRetrieveParameters {
    private final String requestType;
    private final String studyUID;
    private final String seriesUID;
    private final String objectUID;
    private final String contentType;
    private final String transferSyntax;

    public WadoUriRetrieveParameters(Map<String, String[]> params) throws InvalidParametersException {
        requestType = params.get("requestType")[0];
        studyUID = params.get("studyUID")[0];
        seriesUID = params.get("seriesUID")[0];
        objectUID = params.get("objectUID")[0];
        contentType = params.get("contentType")[0];
        transferSyntax = params.get("transferSyntax")[0];
    }

    public String getRequestType() {
        return requestType;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public String getObjectUID() {
        return objectUID;
    }

    public String getContentType() {
        return contentType;
    }

    public String getTransferSyntax() {
        return transferSyntax;
    }

    @Override
    public String toString() {
        return "WadoUriRetrieveParameters{" +
                "requestType='" + requestType + '\'' +
                ", studyUID='" + studyUID + '\'' +
                ", seriesUID='" + seriesUID + '\'' +
                ", objectUID='" + objectUID + '\'' +
                ", contentType='" + contentType + '\'' +
                ", transferSyntax='" + transferSyntax + '\'' +
                '}';
    }
}
