package com.example.sptrngboot.config;

import com.example.sptrngboot.model.AdminUser;
import com.example.sptrngboot.model.Category;
import com.example.sptrngboot.model.Reservation;
import com.example.sptrngboot.model.Review;
import com.example.sptrngboot.model.Store;
import com.example.sptrngboot.repository.AdminUserRepository;
import com.example.sptrngboot.repository.CategoryRepository;
import com.example.sptrngboot.repository.ReservationRepository;
import com.example.sptrngboot.repository.ReviewRepository;
import com.example.sptrngboot.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:adminpass}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initializeData(CategoryRepository categoryRepository,
                                            StoreRepository storeRepository,
                                            ReservationRepository reservationRepository,
                                            ReviewRepository reviewRepository,
                                            AdminUserRepository adminUserRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            AdminUser existingAdmin = adminUserRepository.findByUsername(adminUsername).orElse(null);
            if (existingAdmin == null) {
                adminUserRepository.save(new AdminUser(adminUsername, passwordEncoder.encode(adminPassword), "ADMIN"));
            } else if (existingAdmin.getPassword() == null || existingAdmin.getPassword().isBlank() || !existingAdmin.getPassword().startsWith("$2a$")) {
                existingAdmin.setPassword(passwordEncoder.encode(adminPassword));
                adminUserRepository.save(existingAdmin);
            }

            if (categoryRepository.count() > 0) {
                return;
            }

            Category cafe = categoryRepository.save(new Category("カフェ・喫茶"));
            Category dining = categoryRepository.save(new Category("ダイニング"));
            Category bar = categoryRepository.save(new Category("バー・居酒屋"));
            Category snack = categoryRepository.save(new Category("スナック・ラウンジ"));

            List<Category> categoryList = List.of(cafe, dining, bar, snack);
            String[] prefectures = {"東京都", "神奈川県", "大阪府", "京都府", "北海道", "愛知県", "福岡県", "埼玉県", "千葉県", "兵庫県"};
            String[] names = {"紺の街カフェ", "月光ダイニング", "宵闇バー", "風雅スナック", "和泉酒場", "海風ビストロ", "銀座和食", "札幌洋食", "名古屋バル", "京都茶寮"};
            String[] keywords = {"桜", "光", "海", "風", "月", "葉", "夢", "星", "花", "鈴"};
            String[] priceRanges = {"～2000円", "2000円〜3000円", "3000円〜5000円", "5000円〜8000円", "8000円〜12000円"};
            String[] descriptions = {
                    "落ち着いた雰囲気でゆっくりと過ごせるお店です。",
                    "地元の食材を活かした料理が人気の空間です。",
                    "大切なひとときにふさわしい贅沢な時間を提供します。",
                    "カジュアルで気軽に利用できる雰囲気が魅力です。",
                    "夜景を楽しみながらゆったり過ごせる大人の空間です。"
            };

            for (int i = 1; i <= 300; i++) {
                Category category = categoryList.get((i - 1) % categoryList.size());
                String prefecture = prefectures[(i - 1) % prefectures.length];
                String baseName = names[(i - 1) % names.length];
                String key = keywords[(i - 1) % keywords.length];
                String priceRange = priceRanges[(i - 1) % priceRanges.length];
                String genre = switch (category.getName()) {
                    case "カフェ・喫茶" -> "カフェ";
                    case "ダイニング" -> "ダイニング";
                    case "バー・居酒屋" -> "バー";
                    case "スナック・ラウンジ" -> "スナック";
                    default -> "グルメ";
                };
                String name = String.format("%s %03d", baseName, i);
                String address = String.format("%s区%s町%d-%d-%d", prefecture, key, (i % 10) + 1, (i % 8) + 1, (i % 20) + 1);
                String imageUrl = "/images/placeholders/store-placeholder.svg";
                String description = descriptions[(i - 1) % descriptions.length];

                storeRepository.save(new Store(
                        name,
                        genre,
                        address,
                        prefecture,
                        String.format("03-%04d-%04d", i % 10000, (i * 3) % 10000),
                        priceRange,
                        imageUrl,
                        description,
                        category));
            }

            for (int i = 1; i <= 20; i++) {
                Store store = storeRepository.findById((long) i).orElse(null);
                if (store == null) {
                    continue;
                }
                reservationRepository.save(new Reservation(
                        store,
                        String.format("顧客%02d", i),
                        String.format("customer%02d@example.com", i),
                        String.format("080-%04d-%04d", (i * 7) % 10000, (i * 13) % 10000),
                        LocalDateTime.now().plusDays(i).withHour(18 + (i % 5)).withMinute(30),
                        String.format("%d名で予約します。", (i % 4) + 1)));

                if (i <= 5) {
                    String[] reviewNames = {"さくら", "たけし", "まな", "ゆうこ", "ひかる"};
                    String[] reviewComments = {
                            "落ち着いた空間で、接客も丁寧でした。",
                            "雰囲気が良く、デートにもおすすめです。",
                            "メニューの質が高く、また行きたいです。",
                            "店員さんの対応が素敵でした。",
                            "夜の雰囲気が特に良かったです。"
                    };
                    for (int r = 0; r < 2; r++) {
                        reviewRepository.save(new Review(reviewNames[(i + r) % reviewNames.length], 4 + (r % 2), reviewComments[(i + r) % reviewComments.length], store));
                    }
                }
            }
        };
    }
}
