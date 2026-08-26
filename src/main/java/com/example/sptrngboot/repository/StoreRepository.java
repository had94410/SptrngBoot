package com.example.sptrngboot.repository;

import com.example.sptrngboot.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    @Query("select distinct s from Store s left join fetch s.images i left join fetch s.category c left join fetch s.coupons co left join fetch s.girls g where " +
            "(:keyword is null or lower(s.name) like lower(concat('%', :keyword, '%')) " +
            "or lower(s.description) like lower(concat('%', :keyword, '%')) " +
            "or lower(s.genre) like lower(concat('%', :keyword, '%'))) " +
            "and (:categoryId is null or c.id = :categoryId) " +
            "and (:prefecture is null or s.prefecture = :prefecture) " +
            "and (:priceRange is null or s.priceRange = :priceRange)")
    List<Store> search(@Param("keyword") String keyword,
                       @Param("categoryId") Long categoryId,
                       @Param("prefecture") String prefecture,
                       @Param("priceRange") String priceRange);

    @Query("select distinct s.prefecture from Store s order by s.prefecture")
    List<String> findDistinctPrefectures();

    @Query("select distinct s.priceRange from Store s order by s.priceRange")
    List<String> findDistinctPriceRanges();
}
