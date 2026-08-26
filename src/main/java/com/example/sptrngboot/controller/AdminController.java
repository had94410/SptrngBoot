package com.example.sptrngboot.controller;

import com.example.sptrngboot.repository.ReservationRepository;
import com.example.sptrngboot.repository.StoreRepository;
import com.example.sptrngboot.repository.StoreImageRepository;
import com.example.sptrngboot.repository.CategoryRepository;
import com.example.sptrngboot.repository.BalletConsultationRepository;
import com.example.sptrngboot.model.Store;
import com.example.sptrngboot.model.StoreImage;
import com.example.sptrngboot.model.BalletConsultation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.sptrngboot.repository.CouponRepository;
import com.example.sptrngboot.model.Coupon;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import java.util.Optional;
import java.util.stream.Collectors;
import com.example.sptrngboot.repository.GirlRepository;
import com.example.sptrngboot.model.Girl;

@Controller
public class AdminController {
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final StoreImageRepository storeImageRepository;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final GirlRepository girlRepository;
    private final BalletConsultationRepository balletConsultationRepository;

    public AdminController(StoreRepository storeRepository, ReservationRepository reservationRepository, StoreImageRepository storeImageRepository, CategoryRepository categoryRepository, CouponRepository couponRepository, GirlRepository girlRepository, BalletConsultationRepository balletConsultationRepository) {
        this.storeRepository = storeRepository;
        this.reservationRepository = reservationRepository;
        this.storeImageRepository = storeImageRepository;
        this.categoryRepository = categoryRepository;
        this.couponRepository = couponRepository;
        this.girlRepository = girlRepository;
        this.balletConsultationRepository = balletConsultationRepository;
    }

    private final Path storeImagesDir = Paths.get("target/classes/static/images/stores");
    private final Path girlImagesDir = Paths.get("target/classes/static/images/girls");

    private void deleteGirlImageFiles(String webPath) {
        if (webPath == null || webPath.isBlank()) {
            return;
        }
        String fileName = webPath.substring(webPath.lastIndexOf('/') + 1);
        Path mainPath = girlImagesDir.resolve(fileName);
        Path thumbPath = girlImagesDir.resolve(fileName.replace("-640.jpg", "-320.jpg"));
        try {
            if (Files.exists(mainPath)) {
                Files.delete(mainPath);
            }
            if (Files.exists(thumbPath)) {
                Files.delete(thumbPath);
            }
        } catch (IOException ignored) {
            // Ignore cleanup failures for stale or already-removed files.
        }
    }

    private String normalizeImageName(String originalName) {
        String safeName = originalName == null || originalName.isBlank() ? "upload" : originalName.replaceAll("\\s+", "_");
        return safeName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("stores", storeRepository.findAll());
        model.addAttribute("reservations", reservationRepository.findAll());
        model.addAttribute("consultations", balletConsultationRepository.findAllByOrderByCreatedAtDesc());
        return "admin";
    }

    @GetMapping("/admin/stores/{id}/edit")
    public String editStoreForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Store> opt = storeRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する店舗が見つかりません。");
            return "redirect:/admin";
        }
        model.addAttribute("store", opt.get());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("prefectures", new String[]{"東京都","神奈川県","大阪府","京都府","北海道","愛知県","福岡県","埼玉県","千葉県","兵庫県"});
        model.addAttribute("coupons", couponRepository.findByStoreIdAndActiveTrue(id));
        model.addAttribute("girls", girlRepository.findByStoreId(id));
        return "admin-edit";
    }

    @PostMapping("/admin/stores/{id}/coupons/create")
    public String createCoupon(@PathVariable("id") Long id,
                               @RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam(value = "discountType", required = false) String discountType,
                               @RequestParam(value = "discountValue", required = false) Double discountValue,
                               @RequestParam(value = "validFrom", required = false) String validFromStr,
                               @RequestParam(value = "validTo", required = false) String validToStr,
                               RedirectAttributes redirectAttributes) {
        Optional<Store> opt = storeRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する店舗が見つかりません。");
            return "redirect:/admin";
        }
        Store store = opt.get();
        LocalDateTime validFrom = null;
        LocalDateTime validTo = null;
        try {
            if (validFromStr != null && !validFromStr.isBlank()) validFrom = LocalDateTime.parse(validFromStr);
            if (validToStr != null && !validToStr.isBlank()) validTo = LocalDateTime.parse(validToStr);
        } catch (DateTimeParseException ex) {
            // ignore parse errors - null dates
        }
        Coupon coupon = new Coupon(code, title, description, discountType, discountValue, validFrom, validTo, true, null, store);
        couponRepository.save(coupon);
        store.addCoupon(coupon);
        storeRepository.save(store);
        redirectAttributes.addFlashAttribute("adminMessage", "クーポンを追加しました。");
        return "redirect:/admin/stores/" + id + "/edit";
    }

    @PostMapping("/admin/stores/{id}/girls/create")
    public String createGirl(@PathVariable("id") Long id,
                             @RequestParam("name") String name,
                             @RequestParam(value = "age", required = false) Integer age,
                             RedirectAttributes redirectAttributes) {
        Optional<Store> opt = storeRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する店舗が見つかりません。");
            return "redirect:/admin";
        }
        Store store = opt.get();
        Girl g = new Girl(name, age, null, null, store);
        girlRepository.save(g);
        store.addGirl(g);
        storeRepository.save(store);
        redirectAttributes.addFlashAttribute("adminMessage", "在籍者を追加しました。");
        return "redirect:/admin/stores/" + id + "/edit";
    }

    @GetMapping("/admin/stores/{storeId}/girls/{girlId}/edit")
    public String editGirlForm(@PathVariable("storeId") Long storeId, @PathVariable("girlId") Long girlId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        model.addAttribute("girl", og.get());
        model.addAttribute("storeId", storeId);
        return "admin-girl-edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/update")
    public String updateGirl(@PathVariable("storeId") Long storeId,
                             @PathVariable("girlId") Long girlId,
                             @RequestParam("name") String name,
                             @RequestParam(value = "age", required = false) Integer age,
                             @RequestParam(value = "bio", required = false) String bio,
                             @RequestParam(value = "twitterUrl", required = false) String twitterUrl,
                             @RequestParam(value = "instagramUrl", required = false) String instagramUrl,
                             @RequestParam(value = "lineUrl", required = false) String lineUrl,
                             @RequestParam(value = "schedule", required = false) String schedule,
                             RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        Girl g = og.get();
        g.setName(name);
        g.setAge(age);
        g.setBio(bio);
        g.setTwitterUrl(twitterUrl);
        g.setInstagramUrl(instagramUrl);
        g.setLineUrl(lineUrl);
        g.setSchedule(schedule);
        girlRepository.save(g);
        redirectAttributes.addFlashAttribute("adminMessage", "在籍者情報を更新しました。");
        return "redirect:/admin/stores/" + storeId + "/edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/profile/upload")
    public String uploadGirlProfileImage(@PathVariable("storeId") Long storeId,
                                       @PathVariable("girlId") Long girlId,
                                       @RequestParam("file") MultipartFile file,
                                       RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        Girl g = og.get();
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "プロフィール写真を選択してください。");
            return "redirect:/admin/stores/" + storeId + "/girls/" + girlId + "/edit";
        }
        try {
            Files.createDirectories(girlImagesDir);
            if (g.getImageUrl() != null && !g.getImageUrl().isBlank()) {
                deleteGirlImageFiles(g.getImageUrl());
            }
            String safeName = normalizeImageName(file.getOriginalFilename());
            String base = System.currentTimeMillis() + "-profile-" + safeName;
            String nameMedium = base + "-640.jpg";
            String nameThumb = base + "-320.jpg";
            Path pathMedium = girlImagesDir.resolve(nameMedium);
            Path pathThumb = girlImagesDir.resolve(nameThumb);
            byte[] bytes = file.getBytes();
            Thumbnails.of(new ByteArrayInputStream(bytes)).size(640, 640).outputFormat("jpg").toFile(pathMedium.toFile());
            Thumbnails.of(new ByteArrayInputStream(bytes)).size(320, 320).outputFormat("jpg").toFile(pathThumb.toFile());
            g.setImageUrl("/images/girls/" + nameMedium);
            girlRepository.save(g);
            redirectAttributes.addFlashAttribute("adminMessage", "プロフィール写真を更新しました。");
        } catch (IOException ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("adminMessage", "プロフィール写真のアップロードに失敗しました。");
        }
        return "redirect:/admin/stores/" + storeId + "/girls/" + girlId + "/edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/profile/delete")
    public String deleteGirlProfileImage(@PathVariable("storeId") Long storeId,
                                       @PathVariable("girlId") Long girlId,
                                       RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        Girl g = og.get();
        if (g.getImageUrl() != null && !g.getImageUrl().isBlank()) {
            deleteGirlImageFiles(g.getImageUrl());
            g.setImageUrl(null);
            girlRepository.save(g);
        }
        redirectAttributes.addFlashAttribute("adminMessage", "プロフィール写真を削除しました。");
        return "redirect:/admin/stores/" + storeId + "/girls/" + girlId + "/edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/upload")
    public String uploadGirlImages(@PathVariable("storeId") Long storeId,
                                   @PathVariable("girlId") Long girlId,
                                   @RequestParam(value = "files", required = false) MultipartFile[] files,
                                   RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        Girl g = og.get();
        if (files == null || files.length == 0) {
            redirectAttributes.addFlashAttribute("adminMessage", "アップロードする画像を選択してください。");
            return "redirect:/admin/stores/" + storeId + "/girls/" + girlId + "/edit";
        }
        try {
            Files.createDirectories(girlImagesDir);
            StringBuilder gallerySb = new StringBuilder(g.getGallery() == null ? "" : g.getGallery());
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String safeName = normalizeImageName(file.getOriginalFilename());
                String base = String.valueOf(System.currentTimeMillis()) + "-" + safeName;
                String nameMedium = base + "-640.jpg";
                String nameThumb = base + "-320.jpg";
                Path pathMedium = girlImagesDir.resolve(nameMedium);
                Path pathThumb = girlImagesDir.resolve(nameThumb);
                byte[] bytes = file.getBytes();
                Thumbnails.of(new ByteArrayInputStream(bytes)).size(640, 640).outputFormat("jpg").toFile(pathMedium.toFile());
                Thumbnails.of(new ByteArrayInputStream(bytes)).size(320, 320).outputFormat("jpg").toFile(pathThumb.toFile());
                String webPath = "/images/girls/" + nameMedium;
                if (gallerySb.length() > 0) gallerySb.append(';');
                gallerySb.append(webPath);
            }
            g.setGallery(gallerySb.toString());
            girlRepository.save(g);
            redirectAttributes.addFlashAttribute("adminMessage", "ギャラリー画像をアップロードしました。");
        } catch (IOException ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("adminMessage", "画像のアップロードに失敗しました。");
        }
        return "redirect:/admin/stores/" + storeId + "/girls/" + girlId + "/edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/gallery/delete")
    public String deleteGirlGalleryImage(@PathVariable("storeId") Long storeId,
                                       @PathVariable("girlId") Long girlId,
                                       @RequestParam("imageUrl") String imageUrl,
                                       RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        Girl g = og.get();
        String existing = g.getGallery();
        if (existing != null && !existing.isBlank()) {
            String filtered = java.util.Arrays.stream(existing.split(";"))
                .map(String::trim)
                .filter(item -> !item.isEmpty() && !item.equals(imageUrl))
                .collect(Collectors.joining(";"));
            g.setGallery(filtered.isBlank() ? null : filtered);
            deleteGirlImageFiles(imageUrl);
            girlRepository.save(g);
        }
        redirectAttributes.addFlashAttribute("adminMessage", "画像を削除しました。");
        return "redirect:/admin/stores/" + storeId + "/girls/" + girlId + "/edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/diary/add")
    public String addGirlDiaryEntry(@PathVariable("storeId") Long storeId,
                                    @PathVariable("girlId") Long girlId,
                                    @RequestParam(value = "date", required = false) String date,
                                    @RequestParam(value = "body", required = false) String body,
                                    @RequestParam(value = "files", required = false) MultipartFile[] files,
                                    RedirectAttributes redirectAttributes) {
        Optional<Girl> og = girlRepository.findById(girlId);
        if (og.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する在籍者が見つかりません。");
            return "redirect:/admin/stores/" + storeId + "/edit";
        }
        Girl g = og.get();
        try {
            Files.createDirectories(girlImagesDir);
            StringBuilder imagesSb = new StringBuilder();
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file == null || file.isEmpty()) continue;
                    String original = file.getOriginalFilename();
                    String base = String.valueOf(System.currentTimeMillis()) + "-" + (original == null ? "upload" : original.replaceAll("\\s+", "_"));
                    String nameMedium = base + "-640.jpg";
                    Path pathMedium = girlImagesDir.resolve(nameMedium);
                    byte[] bytes = file.getBytes();
                    Thumbnails.of(new ByteArrayInputStream(bytes)).size(640, 640).outputFormat("jpg").toFile(pathMedium.toFile());
                    String webPath = "/images/girls/" + nameMedium;
                    if (imagesSb.length() > 0) imagesSb.append(',');
                    imagesSb.append(webPath);
                }
            }
            String entryDate = (date == null || date.isBlank()) ? "" : date.trim();
            String entryBody = (body == null) ? "" : body.trim();
            String entryImages = imagesSb.toString();
            String newEntry = entryDate + "|" + entryBody + "|" + entryImages;
            // Prepend so newest appears first
            String existing = g.getDiary();
            String combined = (existing == null || existing.isBlank()) ? newEntry : newEntry + ";" + existing;
            g.setDiary(combined);
            girlRepository.save(g);
            redirectAttributes.addFlashAttribute("adminMessage", "日記を追加しました。");
        } catch (IOException ex) {
            ex.printStackTrace();
            redirectAttributes.addFlashAttribute("adminMessage", "日記のアップロードに失敗しました。");
        }
        return "redirect:/admin/stores/" + storeId + "/edit";
    }

    @PostMapping("/admin/stores/{storeId}/girls/{girlId}/delete")
    public String deleteGirl(@PathVariable("storeId") Long storeId, @PathVariable("girlId") Long girlId, RedirectAttributes redirectAttributes) {
        girlRepository.findById(girlId).ifPresent(g -> {
            girlRepository.delete(g);
        });
        redirectAttributes.addFlashAttribute("adminMessage", "在籍者を削除しました。");
        return "redirect:/admin/stores/" + storeId + "/edit";
    }

    @PostMapping("/admin/stores/{id}/coupons/{couponId}/delete")
    public String deleteCoupon(@PathVariable("id") Long id, @PathVariable("couponId") Long couponId, RedirectAttributes redirectAttributes) {
        couponRepository.findById(couponId).ifPresent(c -> {
            couponRepository.delete(c);
        });
        redirectAttributes.addFlashAttribute("adminMessage", "クーポンを削除しました。");
        return "redirect:/admin/stores/" + id + "/edit";
    }

    @PostMapping("/admin/stores/{id}/coupons/{couponId}/toggle")
    public String toggleCoupon(@PathVariable("id") Long id, @PathVariable("couponId") Long couponId, RedirectAttributes redirectAttributes) {
        couponRepository.findById(couponId).ifPresent(c -> {
            c.setActive(!Boolean.TRUE.equals(c.getActive()));
            couponRepository.save(c);
        });
        redirectAttributes.addFlashAttribute("adminMessage", "クーポン状態を更新しました。");
        return "redirect:/admin/stores/" + id + "/edit";
    }

    @PostMapping("/admin/stores/{id}/update")
    public String updateStore(@PathVariable("id") Long id,
                              @RequestParam("name") String name,
                              @RequestParam(value = "genre", required = false) String genre,
                              @RequestParam(value = "address", required = false) String address,
                              @RequestParam(value = "prefecture", required = false) String prefecture,
                              @RequestParam(value = "phone", required = false) String phone,
                              @RequestParam(value = "priceRange", required = false) String priceRange,
                              @RequestParam(value = "description", required = false) String description,
                                  @RequestParam(value = "categoryId", required = false) String categoryIdStr,
                              RedirectAttributes redirectAttributes) {
        Optional<Store> opt = storeRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "該当する店舗が見つかりません。");
            return "redirect:/admin";
        }
        Store store = opt.get();
        store.setName(name);
        store.setGenre(genre);
        store.setAddress(address);
        store.setPrefecture(prefecture);
        store.setPhone(phone);
        store.setPriceRange(priceRange);
        store.setDescription(description);
        if (categoryIdStr != null && !categoryIdStr.isBlank()) {
            try {
                Long categoryId = Long.parseLong(categoryIdStr);
                categoryRepository.findById(categoryId).ifPresent(store::setCategory);
            } catch (NumberFormatException ignored) {
            }
        }
        storeRepository.save(store);
        redirectAttributes.addFlashAttribute("adminMessage", "店舗情報を更新しました。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        reservationRepository.findById(id).ifPresent(reservationRepository::delete);
        redirectAttributes.addFlashAttribute("adminMessage", "予約をキャンセルしました。");
        return "redirect:/admin";
    }

    @PostMapping("/admin/stores/{id}/upload")
    public String uploadStoreImage(@PathVariable("id") Long id,
                                   @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminMessage", "ファイルが選択されていません。");
            return "redirect:/admin";
        }

        try {
            Files.createDirectories(storeImagesDir);
            String original = file.getOriginalFilename();
            String base = String.valueOf(System.currentTimeMillis()) + "-" + (original == null ? "upload" : original.replaceAll("\\s+", "_"));

            String nameLarge = base + "-1400.jpg";
            String nameLargeWebp = base + "-1400.webp";
            String name1024 = base + "-1024.jpg";
            String name1024Webp = base + "-1024.webp";
            String nameMedium = base + "-640.jpg";
            String nameMediumWebp = base + "-640.webp";
            String nameThumb = base + "-320.jpg";
            String nameThumbWebp = base + "-320.webp";

            Path pathLarge = storeImagesDir.resolve(nameLarge);
            Path path1024 = storeImagesDir.resolve(name1024);
            Path pathMedium = storeImagesDir.resolve(nameMedium);
            Path pathThumb = storeImagesDir.resolve(nameThumb);
            Path pathLargeWebp = storeImagesDir.resolve(nameLargeWebp);
            Path path1024Webp = storeImagesDir.resolve(name1024Webp);
            Path pathMediumWebp = storeImagesDir.resolve(nameMediumWebp);
            Path pathThumbWebp = storeImagesDir.resolve(nameThumbWebp);

            // Read file bytes so we can create multiple InputStreams
            byte[] bytes = file.getBytes();

            // Resize and save using Thumbnailator (JPEG)
            Thumbnails.of(new ByteArrayInputStream(bytes)).size(1400, 1400).outputFormat("jpg").toFile(pathLarge.toFile());
            Thumbnails.of(new ByteArrayInputStream(bytes)).size(1024, 1024).outputFormat("jpg").toFile(path1024.toFile());
            Thumbnails.of(new ByteArrayInputStream(bytes)).size(640, 640).outputFormat("jpg").toFile(pathMedium.toFile());
            Thumbnails.of(new ByteArrayInputStream(bytes)).size(320, 320).outputFormat("jpg").toFile(pathThumb.toFile());

            // Check if WebP writer is available before attempting
            boolean webpSupported = Arrays.stream(ImageIO.getWriterFormatNames()).anyMatch(s -> "webp".equalsIgnoreCase(s));

            if (webpSupported) {
                try {
                    Thumbnails.of(new ByteArrayInputStream(bytes)).size(1400, 1400).outputFormat("webp").toFile(pathLargeWebp.toFile());
                    Thumbnails.of(new ByteArrayInputStream(bytes)).size(1024, 1024).outputFormat("webp").toFile(path1024Webp.toFile());
                    Thumbnails.of(new ByteArrayInputStream(bytes)).size(640, 640).outputFormat("webp").toFile(pathMediumWebp.toFile());
                    Thumbnails.of(new ByteArrayInputStream(bytes)).size(320, 320).outputFormat("webp").toFile(pathThumbWebp.toFile());
                } catch (Exception ex) {
                    // WebP generation failed; log and continue without webp
                    ex.printStackTrace();
                    try { Files.deleteIfExists(pathLargeWebp); } catch (IOException ignored) {}
                    try { Files.deleteIfExists(path1024Webp); } catch (IOException ignored) {}
                    try { Files.deleteIfExists(pathMediumWebp); } catch (IOException ignored) {}
                    try { Files.deleteIfExists(pathThumbWebp); } catch (IOException ignored) {}
                }
            }

            Optional<Store> opt = storeRepository.findById(id);
            if (opt.isPresent()) {
                Store store = opt.get();
                StoreImage img = new StoreImage(
                        "/images/stores/" + nameLarge,
                        Files.exists(pathLargeWebp) ? "/images/stores/" + nameLargeWebp : null,
                        "/images/stores/" + name1024,
                        Files.exists(path1024Webp) ? "/images/stores/" + name1024Webp : null,
                        "/images/stores/" + nameMedium,
                        Files.exists(pathMediumWebp) ? "/images/stores/" + nameMediumWebp : null,
                        "/images/stores/" + nameThumb,
                        Files.exists(pathThumbWebp) ? "/images/stores/" + nameThumbWebp : null,
                        store
                );
                storeImageRepository.save(img);
                                // maintain bidirectional relationship in-memory so next render shows the image
                                store.addImage(img);
                                storeRepository.save(store);
                                redirectAttributes.addFlashAttribute("adminMessage", "画像をアップロードしました。");
            } else {
                // cleanup files
                Files.deleteIfExists(pathLarge);
                Files.deleteIfExists(path1024);
                Files.deleteIfExists(pathMedium);
                Files.deleteIfExists(pathThumb);
                Files.deleteIfExists(pathLargeWebp);
                Files.deleteIfExists(path1024Webp);
                Files.deleteIfExists(pathMediumWebp);
                Files.deleteIfExists(pathThumbWebp);
                redirectAttributes.addFlashAttribute("adminMessage", "該当する店舗が見つかりません。");
            }
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("adminMessage", "画像の保存に失敗しました。");
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/stores/images/{imageId}/delete")
    public String deleteStoreImage(@PathVariable("imageId") Long imageId, RedirectAttributes redirectAttributes) {
        storeImageRepository.findById(imageId).ifPresent(img -> {
            try {
                String filename = img.getFilename();
                if (filename != null && filename.startsWith("/images/stores/")) {
                    Path p = Paths.get("target/classes/static" + filename);
                    Files.deleteIfExists(p);
                }
                String filenameWebp = img.getFilenameWebp();
                if (filenameWebp != null && filenameWebp.startsWith("/images/stores/")) {
                    Path pWebp = Paths.get("target/classes/static" + filenameWebp);
                    Files.deleteIfExists(pWebp);
                }

                String large = img.getLargeFilename();
                if (large != null && large.startsWith("/images/stores/")) {
                    Path pLarge = Paths.get("target/classes/static" + large);
                    Files.deleteIfExists(pLarge);
                }
                String largeWebp = img.getLargeFilenameWebp();
                if (largeWebp != null && largeWebp.startsWith("/images/stores/")) {
                    Path pLargeW = Paths.get("target/classes/static" + largeWebp);
                    Files.deleteIfExists(pLargeW);
                }

                String medium = img.getMediumFilename();
                if (medium != null && medium.startsWith("/images/stores/")) {
                    Path pMed = Paths.get("target/classes/static" + medium);
                    Files.deleteIfExists(pMed);
                }
                String mediumWebp = img.getMediumFilenameWebp();
                if (mediumWebp != null && mediumWebp.startsWith("/images/stores/")) {
                    Path pMedW = Paths.get("target/classes/static" + mediumWebp);
                    Files.deleteIfExists(pMedW);
                }

                String thumb = img.getThumbnail();
                if (thumb != null && thumb.startsWith("/images/stores/")) {
                    Path p2 = Paths.get("target/classes/static" + thumb);
                    Files.deleteIfExists(p2);
                }
                String thumbWebp = img.getThumbnailWebp();
                if (thumbWebp != null && thumbWebp.startsWith("/images/stores/")) {
                    Path p2w = Paths.get("target/classes/static" + thumbWebp);
                    Files.deleteIfExists(p2w);
                }

                storeImageRepository.delete(img);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        redirectAttributes.addFlashAttribute("adminMessage", "画像を削除しました。");
        return "redirect:/admin";
    }
}

