/**
============================================================
MEMBER   : IT26101136 - [Name]
MODULE   : Shared UI and local photos
FILE     : LocalPhotoAdvice.java
WHAT IT DOES (simple words): Lists only local photos that really exist.
REAL-LIFE EXAMPLE: Reception displays a photograph only when it is in the school album.
OOP USED : Encapsulation and constructor injection
CRUD     : READ local photo availability
============================================================
*/
package com.drivingschool.learningpackage.controller;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Map;
import java.util.LinkedHashMap;


@ControllerAdvice
// SOLID: S - This helper checks availability; JSPs decide where to display pictures.
public class LocalPhotoAdvice {
    private final ResourceLoader context;
    // Receives the existing web application's local resource lookup.
    public LocalPhotoAdvice(ResourceLoader context) { this.context = context; }
    // CRUD: READ   - finds or lists local photos
    // Omits missing files so browsers keep gradients instead of requesting broken images.
    @ModelAttribute("localPhotos")
    public Map<String, String> photos() {
        Map<String, String> found = new LinkedHashMap<>();
        String[] names = {"hero-road", "promo-lesson", "safety-bg", "location", "cta-drive",
                "flexible-bg", "mission", "vehicle-manual", "vehicle-automatic", "vehicle-motorcycle"};
        // Step 1: Use fixed local names only, never a user-provided path or remote URL.
        for (String name : names) {
            for (String extension : new String[]{"webp", "jpg", "jpeg", "png"}) {
                String path = "/images/photos/" + name + "." + extension;
                if (context.getResource("classpath:/static" + path).exists()) { found.put(name, path); break; }
                // Step 2: Keep supplied files in their existing images folder; never move them.
                path = "/images/" + name + "." + extension;
                if (context.getResource("classpath:/static" + path).exists()) { found.put(name, path); break; }
            }
        }
        return found;
    }
}
