package com.example.sptrngboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImageChecklistController {

    @GetMapping("/admin/image-checklist")
    public String imageChecklist() {
        return "image-checklist";
    }
}
