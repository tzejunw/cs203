package com.java.firebase.demo.user;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
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
public class UserIntegrationTest {

    @LocalServerPort
	private int port;
    
    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    // private static String jwt;
    // @BeforeAll
    // public static void setup() throws Exception {
    //     try {
    //         // Create the URL object
    //         URL url = new URL("http://localhost:" + port_static + "/user/login");
            
    //         // Open the connection
    //         HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
    //         // Set the request method to POST
    //         con.setRequestMethod("POST");
            
    //         // Set the request headers
    //         con.setRequestProperty("Content-Type", "application/json");
            
    //         // Enable the ability to send content
    //         con.setDoOutput(true);
            
    //         // Create the JSON request body
    //         String jsonInputString = "{\"email\": \"user@gmail.com\", \"password\": \"1@Secured\"}";
            
    //         // Send the JSON request body
    //         try (OutputStream os = con.getOutputStream()) {
    //             byte[] input = jsonInputString.getBytes("utf-8");
    //             os.write(input, 0, input.length);
    //         }

    //         int responseCode = con.getResponseCode();
    //         if (responseCode == HttpURLConnection.HTTP_OK) {
    //             // Read the response body
    //             try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
    //                 StringBuilder response = new StringBuilder();
    //                 String responseLine;
    //                 while ((responseLine = br.readLine()) != null) {
    //                     response.append(responseLine.trim());
    //                 }
    //                 // Print the response
    //                 jwt = response.toString();
    //             }
    //         } else {
    //             System.out.println("Error: Failed to get a valid response.");
    //         }

    //         // You can add more code here to handle the response (e.g., read the response body)

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
        
    // }

    // @Test
    // public void testCreateUser_Failure() throws Exception {
    //     URI uri = new URI(baseUrl + port + "/user/create");
    //     Register register = new Register();
    //     register.setEmail("invalidEmail.com");
    //     register.setPassword("1@Secured");

    //     ResponseEntity<String> result = restTemplate.postForEntity(uri, register, String.class);
		
	// 	assertEquals(400, result.getStatusCode().value());
    // }

    // @Test
    // public void testCreateUser_Success() throws Exception {
    //     URI uri = new URI(baseUrl + port + "/user/create");
    //     Register register = new Register();
    //     register.setEmail("garychia14+test@gmail.com");
    //     register.setPassword("1@Secured");

    //     ResponseEntity<String> result = restTemplate.postForEntity(uri, register, String.class);
		
	// 	assertEquals(200, result.getStatusCode().value());
    // }

    // @Test
    // public void testLogin_NotVerifiedFailure() throws Exception {
    //     URI uri = new URI(baseUrl + port + "/user/login");
    //     Login login = new Login();
    //     login.setEmail("garychia14+test@gmail.com");
    //     login.setPassword("1@Secured");

    //     ResponseEntity<String> result = restTemplate.postForEntity(uri, login, String.class);
		
	// 	assertEquals(500, result.getStatusCode().value());
    // }

    // @Test
    // public void testVerifyEmail_Success() throws Exception {
    //     String params = "?email=garychia14%2Btest%40gmail.com";
    //     URI uri = new URI(baseUrl + port + "/user/resendVerification" + params);

    //     ResponseEntity<String> result = restTemplate.postForEntity(uri, null, String.class);
	// 	assertEquals(200, result.getStatusCode().value());

    //     ResponseEntity<String> verifyEmailResult = restTemplate.exchange(result.getBody(), HttpMethod.GET, null, String.class);
    //     assertEquals(200, verifyEmailResult.getStatusCode().value());
    //     System.out.println(verifyEmailResult.getBody());
    // }

    @Test
    public void testLogin_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/login");
        Login login = new Login();
        login.setEmail("user@gmail.com");
        login.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, login, String.class);
		
		assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void testLogin_Failure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/login");
        Login login = new Login();
        login.setEmail("garychia14+test@gmail.com");
        login.setPassword("WrongPassword");

        ResponseEntity<String> result = restTemplate.postForEntity(uri, login, String.class);
		assertEquals(400, result.getStatusCode().value());
    }

    public String login(String email) throws Exception {
        URI url = new URI(baseUrl + port + "/user/login");
        Login login = new Login();
        login.setEmail(email);
        login.setPassword("1@Secured");

        ResponseEntity<String> result = restTemplate.postForEntity(url, login, String.class);
        assertEquals(200, result.getStatusCode().value());
		
        return result.getBody();
    }

    @Test
    public void testCreateUserDetails_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/createDetails");
        User user = new User("hahahaha", "Nina Kan", "12/12/1990", "Female");

        String jwt = login("user@gmail.com");
        System.out.println("jwt:" + jwt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
		assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void testCreateUserDetails_SameUserNameFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/createDetails");
        User user = new User("hahahaha", "Nina Kan", "12/12/1990", "Female");

        String jwt = login("user1@test.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
		assertEquals(400, result.getStatusCode().value());
    }

    @Test
    public void testgetUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/get");

        String jwt = login("user@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
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
    public void testgetEmail_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/getEmail");
        String email = "user@gmail.com";

        String jwt = login(email);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(email, result.getBody());
    }

    @Test
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
    public void testupdateUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/update");
        User user = new User("hahahaha", "Nina", "12/12/2000", "Female");

        String jwt = login("user@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<User> request = new HttpEntity<>(user, headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void testupdateUser_InvalidBirthdayFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/update");
        User user = new User("hahahaha", "Nina", "32/12/2000", "Female");

        String jwt = login("user@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    public void testupdatePassword_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/updatePassword");
        UpdatePassword updatePassword = new UpdatePassword();
        updatePassword.setPassword("1@Secured");

        String jwt = login("user@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<UpdatePassword> request = new HttpEntity<>(updatePassword, headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void testupdatePassword_Below8CharFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/updatePassword");
        UpdatePassword updatePassword = new UpdatePassword();
        updatePassword.setPassword("1@Secur");

        String jwt = login("user@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<UpdatePassword> request = new HttpEntity<>(updatePassword, headers);
        
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.PUT, request, String.class);
        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    public void testdeleteUser_Success() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/delete");

        String jwt = login("garychia14+test@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void testdeleteUser_InvalidJWTFailure() throws Exception {
        URI uri = new URI(baseUrl + port + "/user/delete");

        String jwt = "invalid_jwt";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class);
        assertEquals(401, result.getStatusCode().value());
    }
}
