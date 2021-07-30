package com.example.demo.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="import_data_temp")
public class ImportDataTemp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="pkimport_data_id")
    private long id;
    @Column(name="file_path",length=150)
    private String filePath;
    @Column(name="status")
    private int status;
    @Column(name="file_type",length=10)
    private String fileType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_datetime",nullable=false,updatable=false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified_datetime",nullable=false)
    private Date updated;

    @Column(name = "unique_Id",length = 50,updatable = false,unique = true)
    private String uniqueId;

    @PrePersist
    public void onCreated() {
        updated = created = new Date();
        uniqueId= UUID.randomUUID().toString();
    }

    @PreUpdate
    public void onUpdated() {
        updated = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}