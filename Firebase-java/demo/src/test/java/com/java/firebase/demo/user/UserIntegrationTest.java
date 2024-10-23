package com.java.firebase.demo.user;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private String uid;

    // Needed for tournaments integration testing.
    // @BeforeAll
    // public void setup() throws Exception {
    //     URI url = new URI(baseUrl + port + "/user/login");
    //     UserCredentials userCredentials = new UserCredentials();
    //     userCredentials.setEmail("garychia14+testing@gmail.com");
    //     userCredentials.setPassword("1@Secured");

    //     ResponseEntity<String> result = restTemplate.postForEntity(url, userCredentials, String.class);
    //     assertEquals(200, result.getStatusCode().value());
		
    //     token = result.getBody();
    // }

    @Test
    @Order(1)
    public void testCreateUser_invalidEmailFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/create");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("invalidEmail.com");
        userCredentials.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, userCredentials, String.class);
		
		assertEquals(400, result.getStatusCode().value());
    }

    @Test
    @Order(2)
    public void testCreateUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/create");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("gary1935+integrationTest@hotmail.co.uk");
        userCredentials.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, userCredentials, String.class);
		
		assertEquals(200, result.getStatusCode().value());
        uid = result.getBody();
    }

    @Test
    @Order(4)
    // This method is just to mock the process of verifying email on firebase
    // as the actual verifying email function is done through firebase.
    public void verifyEmail_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/verifyEmail");
        VerifyEmail verifyEmail = new VerifyEmail();
        verifyEmail.setUid(uid);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, verifyEmail, String.class);
		
		assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @Order(3)
    public void testLogin_NotVerifiedFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/login");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("gary1935+integrationTest@hotmail.co.uk");
        userCredentials.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, userCredentials, String.class);
		
		assertEquals(400, result.getStatusCode().value());
    }

    @Test
    @Order(5)
    public void testLogin_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/login");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("gary1935+integrationTest@hotmail.co.uk");
        userCredentials.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, userCredentials, String.class);
		
		assertEquals(200, result.getStatusCode().value());
        token = result.getBody();
    }

    @Test
    @Order(5)
    public void testLogin_WrongPasswordFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/login");
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setEmail("user30@test.com");
        userCredentials.setPassword("WrongPassword");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, userCredentials, String.class);
		assertEquals(400, result.getStatusCode().value());
    }

    @Test
    @Order(6)
    public void testCreateUserDetails_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/createDetails");
        User user = new User("integration_testing", "Nina Kan", "12/12/1990", "Female");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
		assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @Order(7)
    public void testCreateUserDetails_SameUserNameFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/createDetails");
        User user = new User("integration_testing", "Nina Kan", "12/12/1990", "Female");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
		assertEquals(400, result.getStatusCode().value());
    }

    @Test
    @Order(8)
    public void testgetUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/get");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @Order(9)
    public void testgetUser_InvalidJWTFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/get");

        String jwt = "INVALID_JWT";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
		assertEquals(401, result.getStatusCode().value());
    }

    @Test
    @Order(10)
    public void testgetEmail_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/getEmail");
        String email = "gary1935+integrationtest@hotmail.co.uk";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(email, result.getBody());
    }

    @Test
    @Order(11)
    public void testgetEmail_InvalidJWTFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/getEmail");

        String jwt = "INVALID_JWT";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
		assertEquals(401, result.getStatusCode().value());
    }

    @Test
    @Order(12)
    public void testupdateUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/update");
        User user = new User("hahahaha", "Nina", "12/12/2000", "Female");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @Order(13)
    public void testupdateUser_InvalidBirthdayFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/update");
        User user = new User("hahahaha", "Nina", "32/12/2000", "Female");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    @Order(14)
    public void testupdatePassword_Below8CharFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/updatePassword");
        UpdatePassword updatePassword = new UpdatePassword();
        updatePassword.setPassword("1@Secur");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<UpdatePassword> request = new HttpEntity<>(updatePassword, headers);
        
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    @Order(15)
    public void testupdatePassword_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/updatePassword");
        UpdatePassword updatePassword = new UpdatePassword();
        updatePassword.setPassword("1@Secured");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<UpdatePassword> request = new HttpEntity<>(updatePassword, headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    // public String login(String email) throws Exception {
    //     URI url = new URI(baseUrl + port + "/user/login");
    //     UserCredentials userCredentials = new UserCredentials();
    //     userCredentials.setEmail(email);
    //     userCredentials.setPassword("1@Secured");

    //     ResponseEntity<String> result = restTemplate.postForEntity(url, userCredentials, String.class);
    //     assertEquals(200, result.getStatusCode().value());
		
    //     return result.getBody();
    // }

    @Test
    @Order(16)
    public void testdeleteUser_Success() throws Exception {
        // Need to login again upon changing the password as required by Firebase.
        testLogin_Success();
        URI uri = new URI(baseUrl + port + "/user/delete");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @Order(17)
    public void testdeleteUser_InvalidJWTFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/delete");

        String jwt = "invalid_jwt";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
        assertEquals(401, result.getStatusCode().value());
    }

    // @AfterAll
    // public void tearDown() {
        
    // }
}