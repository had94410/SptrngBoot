package com.example.sptrngboot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.sptrngboot.model.Girl;
import com.example.sptrngboot.repository.GirlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class GirlController {

    private final GirlRepository girlRepository;

    @Autowired
    public GirlController(GirlRepository girlRepository) {
        this.girlRepository = girlRepository;
    }

    @GetMapping("/girls/{id}")
    public String showGirl(@PathVariable("id") Long id, Model model) {
        Girl girl = girlRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("girl", girl);
        model.addAttribute("store", girl.getStore());

        // Prepare gallery list (semicolon-separated in DB)
        java.util.List<String> galleryList = new java.util.ArrayList<>();
        if (girl.getGallery() != null && !girl.getGallery().isBlank()) {
            java.util.Arrays.stream(girl.getGallery().split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(galleryList::add);
        }
        model.addAttribute("galleryList", galleryList);

        // Prepare schedule entries as day/time pairs and map short weekday to full Japanese name
        java.util.List<java.util.Map<String,String>> scheduleEntries = new java.util.ArrayList<>();
        if (girl.getSchedule() != null && !girl.getSchedule().isBlank()) {
            java.util.Arrays.stream(girl.getSchedule().split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(line -> {
                        String[] parts = line.split("[:：]", 2);
                        String dayShort = parts.length > 0 ? parts[0].trim() : "";
                        String timePart = parts.length > 1 ? parts[1].trim() : "";
                        String dayFull = switch (dayShort) {
                            case "日", "日曜", "日曜日" -> "日曜日";
                            case "月", "月曜", "月曜日" -> "月曜日";
                            case "火", "火曜", "火曜日" -> "火曜日";
                            case "水", "水曜", "水曜日" -> "水曜日";
                            case "木", "木曜", "木曜日" -> "木曜日";
                            case "金", "金曜", "金曜日" -> "金曜日";
                            case "土", "土曜", "土曜日" -> "土曜日";
                            default -> dayShort;
                        };
                        java.util.Map<String,String> m = new java.util.HashMap<>();
                        m.put("day", dayFull);
                        m.put("time", timePart);
                        scheduleEntries.add(m);
                    });
        }
        model.addAttribute("scheduleEntries", scheduleEntries);
        model.addAttribute("scheduleList", scheduleEntries);

        // Prepare diary entries server-side to avoid using '|' in template expressions
        java.util.List<java.util.Map<String,Object>> diaryEntries = new java.util.ArrayList<>();
        if (girl.getDiary() != null && !girl.getDiary().isBlank()) {
            java.util.Arrays.stream(girl.getDiary().split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(entry -> {
                        if (!entry.contains("|")) return;
                        String[] parts = entry.split("\\|", 3);
                        String date = parts.length > 0 ? parts[0].trim() : "";
                        String body = parts.length > 1 ? parts[1].trim() : "";
                        java.util.List<String> imgs = new java.util.ArrayList<>();
                        if (parts.length > 2 && parts[2] != null && !parts[2].isBlank()) {
                            java.util.Arrays.stream(parts[2].split(","))
                                    .map(String::trim)
                                    .filter(u -> !u.isEmpty())
                                    .forEach(imgs::add);
                        }
                        java.util.Map<String,Object> m = new java.util.HashMap<>();
                        m.put("date", date);
                        m.put("body", body);
                        m.put("images", imgs);
                        diaryEntries.add(m);
                    });
        }
        model.addAttribute("diaryEntries", diaryEntries);

        return "girl";
    }

    @GetMapping("/girls/{id}/edit")
    public String editGirlPublic(@PathVariable("id") Long id, Model model) {
        Girl girl = girlRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("girl", girl);
        return "girl-edit";
    }

    @PostMapping("/girls/{id}/update")
    public String updateGirlPublic(@PathVariable("id") Long id,
                                   @RequestParam("name") String name,
                                   @RequestParam(value = "age", required = false) Integer age,
                                   @RequestParam(value = "bio", required = false) String bio,
                                   @RequestParam(value = "twitterUrl", required = false) String twitterUrl,
                                   @RequestParam(value = "instagramUrl", required = false) String instagramUrl,
                                   @RequestParam(value = "lineUrl", required = false) String lineUrl,
                                   @RequestParam(value = "schedule", required = false) String schedule,
                                   Model model) {
        Girl g = girlRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        g.setName(name);
        g.setAge(age);
        g.setBio(bio);
        g.setTwitterUrl(twitterUrl);
        g.setInstagramUrl(instagramUrl);
        g.setLineUrl(lineUrl);
        g.setSchedule(schedule);
        girlRepository.save(g);
        return "redirect:/girls/" + id;
    }
}
