package com.java.firebase.demo.image;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    // Endpoint to upload an image to Firebase Storage and save the URL to Firestore DOCUMENT
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
