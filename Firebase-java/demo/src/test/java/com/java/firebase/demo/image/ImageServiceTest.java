package com.java.firebase.demo.image;
import com.java.firebase.demo.image.ImageService; // Import ImageService
import com.java.firebase.demo.image.FirestoreService; // Import FirestoreService
import com.java.firebase.demo.image.ImageController; // Import ImageController

import static org.mockito.Mockito.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.java.firebase.demo.image.ImageService;
import com.java.firebase.demo.image.ImageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ImageServiceTest {

    @Mock
    private ImageService imageService; // Mock the ImageService

    @Mock
    private FirestoreService firestoreService;
    
    @InjectMocks
    private ImageController imageController; // Inject the mock into ImageController
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void uploadImage_Success() throws IOException {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        String mockUrl = "https://mocked-url.com/image.jpg";
        
        // Mock the uploadImage method to return the mock URL
        when(imageService.uploadImage(mockFile)).thenReturn(mockUrl);

        // Act
        String response = imageController.uploadImage(mockFile, "document123");

        // Assert
        assertEquals("Image uploaded successfully: " + mockUrl, response);
    }

    @Test
    void uploadImage_NullFile() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageController.uploadImage(null, "document123");
        });
    
        assertEquals("File must not be null", exception.getMessage());
    }

    @Test
    void uploadImage_EmptyDocumentId() throws IOException {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        String mockUrl = "https://mocked-url.com/image.jpg";
    
        when(imageService.uploadImage(mockFile)).thenReturn(mockUrl);
    
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageController.uploadImage(mockFile, "");
        });
    
        assertEquals("documentId must not be null", exception.getMessage());
    }
        

    
}
