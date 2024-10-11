package com.java.firebase.demo.image;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ImageService {

    // Method to upload an image to Firebase Storage and return the URL
    public String uploadImage(MultipartFile file) throws IOException {
        System.out.println("upload file starting"); // Log the URL
        // Get a reference to the default bucket
        Bucket bucket = StorageClient.getInstance().bucket();

        // Create a unique blob ID for the image (can add more unique identifiers if needed)
        String blobId = "images/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Upload the file to Firebase Storage
        Blob blob = bucket.create(blobId, file.getBytes(), file.getContentType());

        // Set the ACL to allow public read access
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        System.out.println("This is the bucket name: " + bucket.getName());
        System.out.println("This is the blob name: " + blob.getName());

        // URL-encode the blob name
        String encodedBlobName = URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8.toString());

        // Construct the public URL
        String imageUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
             bucket.getName(), encodedBlobName);

        System.out.println("Uploaded image URL: " + imageUrl); // Log the URL


        // Generate and return the public URL of the uploaded image
        return imageUrl;
    }
}
