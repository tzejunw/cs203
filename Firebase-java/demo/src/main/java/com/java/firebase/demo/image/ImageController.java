package com.java.firebase.demo.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;
    private final FirestoreService firestoreService;

    @Autowired
    public ImageController(ImageService imageService, FirestoreService firestoreService) {
        this.imageService = imageService;
        this.firestoreService = firestoreService;
    }

    // Endpoint to upload an image and save the URL to Firestore
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file,
                              @RequestParam("documentId") String documentId) throws IOException {
        // Upload the image and get its URL
        String imageUrl = imageService.uploadImage(file);

        // Save the URL to Firestore under the specified document
        firestoreService.saveImageUrl(documentId, imageUrl);

        return "Image uploaded successfully: " + imageUrl;
    }
}
