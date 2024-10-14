package com.java.firebase.demo.image;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageService {

    // Method to upload an image to Firebase Storage and return the URL
    public String uploadImage(MultipartFile file) throws IOException {
        // Get a reference to the default bucket
        Bucket bucket = StorageClient.getInstance().bucket();

        // Create a unique blob ID for the image (can add more unique identifiers if needed)
        String blobId = "images/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Upload the file to Firebase Storage
        Blob blob = bucket.create(blobId, file.getBytes(), file.getContentType());

        // Generate and return the public URL of the uploaded image
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucket.getName(), blob.getName());
    }
}
