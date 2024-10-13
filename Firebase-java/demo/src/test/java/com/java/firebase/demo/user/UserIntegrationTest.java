// package com.java.firebase.demo.user;

// import java.net.URI;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.http.ResponseEntity;

// import com.java.firebase.demo.CRUDRunner;

// @SpringBootTest(classes = CRUDRunner.class, webEnvironment = WebEnvironment.RANDOM_PORT)
// // @AutoConfigureMockMvc
// public class UserIntegrationTest {

//     @LocalServerPort
// 	private int port;

//     private final String baseUrl = "http://localhost:";

//     @Autowired
//     private TestRestTemplate restTemplate;

//     @Test
//     public void testCreateUser_Success() throws Exception {
//         URI uri = new URI(baseUrl + port + "/user/create");
//         Register register = new Register();
//         register.setEmail("garychia14+test@gmail.com");
//         register.setPassword("1@Secured");

//         ResponseEntity<String> result = restTemplate.postForEntity(uri, register, String.class);
		
// 		assertEquals(200, result.getStatusCode().value());
//     }

//     @Test
//     public void testCreateUser_Failure() throws Exception {
//         URI uri = new URI(baseUrl + port + "/user/create");
//         Register register = new Register();
//         register.setEmail("invalidEmail.com");
//         register.setPassword("1@Secured");

//         ResponseEntity<String> result = restTemplate.postForEntity(uri, register, String.class);
		
// 		assertEquals(400, result.getStatusCode().value());
//     }
// }