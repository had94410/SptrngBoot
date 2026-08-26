package com.example.sptrngboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String genre;
    private String address;
    private String prefecture;
    private String phone;
    private String priceRange;
    private String imageUrl;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Coupon> coupons = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Girl> girls = new HashSet<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    protected Store() {
    }

    public Store(String name, String genre, String address, String prefecture, String phone, String priceRange, String imageUrl, String description, Category category) {
        this.name = name;
        this.genre = genre;
        this.address = address;
        this.prefecture = prefecture;
        this.phone = phone;
        this.priceRange = priceRange;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<StoreImage> getImages() {
        return images;
    }

    public void setImages(List<StoreImage> images) {
        this.images = images;
    }

    public void addImage(StoreImage image) {
        images.add(image);
        image.setStore(this);
    }

    public void removeImage(StoreImage image) {
        images.remove(image);
        image.setStore(null);
    }

    public Set<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(Set<Coupon> coupons) {
        this.coupons = coupons;
    }

    public void addCoupon(Coupon coupon) {
        coupons.add(coupon);
        coupon.setStore(this);
    }

    public void removeCoupon(Coupon coupon) {
        coupons.remove(coupon);
        coupon.setStore(null);
    }

    public Set<Girl> getGirls() {
        return girls;
    }

    public void setGirls(Set<Girl> girls) {
        this.girls = girls;
    }

    public void addGirl(Girl g) {
        girls.add(g);
        g.setStore(this);
    }

    public void removeGirl(Girl g) {
        girls.remove(g);
        g.setStore(null);
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setStore(this);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        review.setStore(null);
    }
}

