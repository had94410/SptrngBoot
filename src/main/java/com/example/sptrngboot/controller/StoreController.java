package com.example.sptrngboot.controller;

import com.example.sptrngboot.model.Category;
import com.example.sptrngboot.model.Reservation;
import com.example.sptrngboot.model.Review;
import com.example.sptrngboot.model.Store;
import com.example.sptrngboot.repository.CategoryRepository;
import com.example.sptrngboot.repository.ReservationRepository;
import com.example.sptrngboot.repository.ReviewRepository;
import com.example.sptrngboot.repository.StoreRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class StoreController {
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;

    public StoreController(StoreRepository storeRepository, CategoryRepository categoryRepository, ReservationRepository reservationRepository, ReviewRepository reviewRepository) {
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
        this.reservationRepository = reservationRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "q", required = false) String keyword,
                        @RequestParam(value = "category", required = false) Long categoryId,
                        @RequestParam(value = "prefecture", required = false) String prefecture,
                        @RequestParam(value = "priceRange", required = false) String priceRange,
                        Model model) {
        List<Category> categories = categoryRepository.findAll();
        List<String> prefectures = storeRepository.findDistinctPrefectures();
        List<String> priceRanges = storeRepository.findDistinctPriceRanges();

        String normalizedPrefecture = (prefecture == null || prefecture.isBlank()) ? null : prefecture;
        String normalizedPriceRange = (priceRange == null || priceRange.isBlank()) ? null : priceRange;

        List<Store> stores = storeRepository.search(keyword, categoryId, normalizedPrefecture, normalizedPriceRange);
        Map<Long, Long> reviewCounts = new HashMap<>();
        Map<Long, Double> reviewAverages = new HashMap<>();
        for (Store store : stores) {
            List<Review> reviews = reviewRepository.findByStoreIdOrderByCreatedAtDesc(store.getId());
            long count = reviews.size();
            double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            reviewCounts.put(store.getId(), count);
            reviewAverages.put(store.getId(), average);
        }

        model.addAttribute("categories", categories);
        model.addAttribute("stores", stores);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("reviewAverages", reviewAverages);
        model.addAttribute("prefectures", prefectures);
        model.addAttribute("priceRanges", priceRanges);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedPrefecture", normalizedPrefecture);
        model.addAttribute("selectedPriceRange", normalizedPriceRange);
        model.addAttribute("keyword", keyword);
        return "index";
    }

    @GetMapping("/store/{id}")
    public String storeDetail(@PathVariable("id") Long id, Model model) {
        Optional<Store> store = storeRepository.findById(id);
        if (store.isEmpty()) {
            return "redirect:/";
        }
        Store currentStore = store.get();
        List<Review> reviews = reviewRepository.findByStoreIdOrderByCreatedAtDesc(id);
        double averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        model.addAttribute("store", currentStore);
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewAverage", averageRating);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("reviewForm", new Review());
        return "store";
    }

    @PostMapping("/store/{id}/reviews")
    public String addReview(@PathVariable("id") Long id,
                            @RequestParam("reviewerName") String reviewerName,
                            @RequestParam("rating") Integer rating,
                            @RequestParam(value = "comment", required = false) String comment,
                            RedirectAttributes redirectAttributes) {
        Optional<Store> storeOptional = storeRepository.findById(id);
        if (storeOptional.isEmpty()) {
            return "redirect:/";
        }
        Store store = storeOptional.get();
        String safeName = (reviewerName == null || reviewerName.isBlank()) ? "匿名" : reviewerName.trim();
        Integer safeRating = (rating == null) ? 5 : Math.max(1, Math.min(5, rating));
        String safeComment = (comment == null) ? "" : comment.trim();

        Review review = new Review(safeName, safeRating, safeComment, store);
        reviewRepository.save(review);
        redirectAttributes.addFlashAttribute("reviewMessage", "口コミを投稿しました。");
        return "redirect:/store/" + id + "#reviews";
    }

    @PostMapping("/store/{id}/reserve")
    public String reserve(@PathVariable("id") Long id,
                          @Valid @ModelAttribute Reservation reservation,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        Optional<Store> storeOptional = storeRepository.findById(id);
        if (storeOptional.isEmpty()) {
            return "redirect:/";
        }
        Store store = storeOptional.get();
        if (bindingResult.hasErrors()) {
            model.addAttribute("store", store);
            return "store";
        }

        reservation.setStore(store);
        reservationRepository.save(reservation);

        redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。店舗から確認の連絡が入る場合があります。");
        return "redirect:/store/" + id + "/complete";
    }

    @GetMapping("/store/{id}/complete")
    public String reservationComplete(@PathVariable("id") Long id, Model model) {
        Optional<Store> store = storeRepository.findById(id);
        if (store.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("store", store.get());
        return "booking-complete";
    }
}
