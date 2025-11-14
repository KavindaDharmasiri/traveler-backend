package com.traveler.storage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    private String uuid;
    private String fileName;
    private String filePath;

    public FileEntity() {}

    public FileEntity(String uuid, String fileName, String filePath) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
