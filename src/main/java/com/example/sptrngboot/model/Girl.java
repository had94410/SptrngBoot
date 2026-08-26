package com.example.sptrngboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "girls")
public class Girl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String bio;
    private String imageUrl;

    // SNS and profile extras
    private String twitterUrl;
    private String instagramUrl;
    private String lineUrl;

    // Semicolon-separated gallery image URLs and newline-separated schedule lines (development convenience)
    @jakarta.persistence.Column(length = 2000)
    private String gallery;

    @jakarta.persistence.Column(length = 2000)
    private String schedule;

    // Simple diary storage: entries separated by ';' with format date|body|img1,img2
    @jakarta.persistence.Column(length = 4000)
    private String diary;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    protected Girl() {}

    public Girl(String name, Integer age, String bio, String imageUrl, Store store) {
        this.name = name;
        this.age = age;
        this.bio = bio;
        this.imageUrl = imageUrl;
        this.store = store;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTwitterUrl() { return twitterUrl; }
    public void setTwitterUrl(String twitterUrl) { this.twitterUrl = twitterUrl; }
    public String getInstagramUrl() { return instagramUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }
    public String getLineUrl() { return lineUrl; }
    public void setLineUrl(String lineUrl) { this.lineUrl = lineUrl; }

    public String getGallery() { return gallery; }
    public void setGallery(String gallery) { this.gallery = gallery; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getDiary() { return diary; }
    public void setDiary(String diary) { this.diary = diary; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
}
