package com.example.studyai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Document metadata for quiz generation
 * Contains S3 location and file type information
 */
public class QuestionDocument {
    @JsonProperty("s3_key")
    private String s3Key;

    @JsonProperty("file_type")
    private String fileType;

    public QuestionDocument() {
    }

    public QuestionDocument(String s3Key, String fileType) {
        this.s3Key = s3Key;
        this.fileType = fileType;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
