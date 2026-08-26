package com.example.sptrngboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import com.example.sptrngboot.repository.StoreRepository;
import com.example.sptrngboot.repository.GirlRepository;
import com.example.sptrngboot.model.Store;
import com.example.sptrngboot.model.Girl;

import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SptrngBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(SptrngBootApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public org.springframework.boot.CommandLineRunner seedGirls(StoreRepository storeRepository, GirlRepository girlRepository) {
        return args -> {
            List<Store> stores = storeRepository.findAll();
            if (stores.isEmpty()) return;
            // Add a few sample girls to the first 5 stores (only if similar named girls not present per store)
            int idx = 0;
            for (Store s : stores) {
                idx++;
                List<com.example.sptrngboot.model.Girl> existing = girlRepository.findByStoreId(s.getId());
                boolean hasYuri = existing.stream().anyMatch(g -> "ゆり".equals(g.getName()));
                boolean hasAya = existing.stream().anyMatch(g -> "あや".equals(g.getName()));

                if (!hasYuri) {
                    Girl g1 = new Girl("ゆり", 24, "元気で明るい", "/images/placeholders/girl-placeholder.svg", s);
                    // 出勤情報（曜日ごと）をセット: 日曜〜土曜
                    g1.setSchedule("日: 休み\n月: 12:00-20:00\n火: 12:00-20:00\n水: 12:00-20:00\n木: 12:00-20:00\n金: 12:00-21:00\n土: 11:00-19:00");
                    g1.setTwitterUrl("https://twitter.com/example_yuri");
                    g1.setInstagramUrl("https://instagram.com/example_yuri");
                    g1.setLineUrl("https://line.me/ti/p/example_yuri");
                    g1.setGallery("/images/placeholders/girl-placeholder.svg;/images/placeholders/girl-placeholder.svg;/images/placeholders/girl-placeholder.svg");
                    String g1d1 = "2026-08-16|今日の写メ日記：元気に出勤中！|/images/placeholders/girl-placeholder.svg";
                    String g1d2 = "2026-08-14|先日の写真日記：お店で撮った写メ|/images/placeholders/girl-placeholder.svg,/images/placeholders/girl-placeholder.svg";
                    g1.setDiary(g1d1 + ";" + g1d2);
                    girlRepository.save(g1);
                }

                if (!hasAya) {
                    Girl g2 = new Girl("あや", 22, "笑顔が素敵", "/images/placeholders/girl-placeholder.svg", s);
                    g2.setSchedule("日: 11:00-17:00\n月: 休み\n火: 13:00-19:00\n水: 13:00-19:00\n木: 13:00-19:00\n金: 15:00-22:00\n土: 12:00-18:00");
                    g2.setTwitterUrl("https://twitter.com/example_aya");
                    g2.setInstagramUrl("https://instagram.com/example_aya");
                    g2.setLineUrl("https://line.me/ti/p/example_aya");
                    g2.setGallery("/images/placeholders/girl-placeholder.svg;/images/placeholders/girl-placeholder.svg");
                    String g2d1 = "2026-08-15|今日は12時から出勤します|/images/placeholders/girl-placeholder.svg";
                    g2.setDiary(g2d1);
                    girlRepository.save(g2);
                }

                if (idx >= 5) break;
            }
        };
    }

    @Bean
    public org.springframework.boot.CommandLineRunner ensureGirlSchedules(com.example.sptrngboot.repository.GirlRepository girlRepository) {
        return args -> {
            java.util.List<com.example.sptrngboot.model.Girl> girls = girlRepository.findAll();
            if (girls == null || girls.isEmpty()) return;
            String defaultSchedule = "日: 休み\n月: 12:00-20:00\n火: 12:00-20:00\n水: 12:00-20:00\n木: 12:00-20:00\n金: 12:00-21:00\n土: 11:00-19:00";
            for (com.example.sptrngboot.model.Girl g : girls) {
                // Force overwrite schedule for all girls
                g.setSchedule(defaultSchedule);
                girlRepository.save(g);
            }
        };
    }
}

