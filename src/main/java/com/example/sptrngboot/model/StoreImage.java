package com.example.sptrngboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_images")
public class StoreImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;               // 1400 jpg
    private String filenameWebp;            // 1400 webp
    private String largeFilename;          // 1024 jpg
    private String largeFilenameWebp;       // 1024 webp
    private String mediumFilename;         // 640 jpg
    private String mediumFilenameWebp;     // 640 webp
    private String thumbnail;              // 320 jpg
    private String thumbnailWebp;           // 320 webp

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    protected StoreImage() {
    }

    public StoreImage(String filename, String filenameWebp, String largeFilename, String largeFilenameWebp, String mediumFilename, String mediumFilenameWebp, String thumbnail, String thumbnailWebp, Store store) {
        this.filename = filename;
        this.filenameWebp = filenameWebp;
        this.largeFilename = largeFilename;
        this.largeFilenameWebp = largeFilenameWebp;
        this.mediumFilename = mediumFilename;
        this.mediumFilenameWebp = mediumFilenameWebp;
        this.thumbnail = thumbnail;
        this.thumbnailWebp = thumbnailWebp;
        this.store = store;
    }

    public String getFilenameWebp() {
        return filenameWebp;
    }

    public void setFilenameWebp(String filenameWebp) {
        this.filenameWebp = filenameWebp;
    }

    public String getLargeFilename() {
        return largeFilename;
    }

    public void setLargeFilename(String largeFilename) {
        this.largeFilename = largeFilename;
    }

    public String getLargeFilenameWebp() {
        return largeFilenameWebp;
    }

    public void setLargeFilenameWebp(String largeFilenameWebp) {
        this.largeFilenameWebp = largeFilenameWebp;
    }

    public String getMediumFilename() {
        return mediumFilename;
    }

    public void setMediumFilename(String mediumFilename) {
        this.mediumFilename = mediumFilename;
    }

    public String getMediumFilenameWebp() {
        return mediumFilenameWebp;
    }

    public void setMediumFilenameWebp(String mediumFilenameWebp) {
        this.mediumFilenameWebp = mediumFilenameWebp;
    }

    public String getThumbnailWebp() {
        return thumbnailWebp;
    }

    public void setThumbnailWebp(String thumbnailWebp) {
        this.thumbnailWebp = thumbnailWebp;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}