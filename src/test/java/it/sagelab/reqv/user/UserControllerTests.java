package it.sagelab.reqv.user;

import it.sagelab.reqv.ReqVApplication;
import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ReqVApplication.class)
@ActiveProfiles("test")
public class UserControllerTests {

    @Value("${local.server.port}")
    int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository repository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User user1, user2;

    @Before
    public void setUp() {

        user1 = new User("admin", bCryptPasswordEncoder.encode("1234"), "admin@test.it");
        user2 = new User("test", bCryptPasswordEncoder.encode("abcde"), "test@test.it");


        repository.deleteAll();
        user1 = repository.save(user1);
        user2 = repository.save(user2);

    }

    @Test
    public void testGetUserDetails() {
        HttpHeaders header = AuthorizationTests.headerWithAuthorizationToken(restTemplate, user1.getUsername(), "1234");
        HttpEntity<String> request = new HttpEntity<>("", header);
        ResponseEntity<User> response = restTemplate.exchange("/user", HttpMethod.GET, request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertEquals(response.getBody().getId(), user1.getId());
        assertEquals(response.getBody().getUsername(), user1.getUsername());
        assertEquals(response.getBody().getEmail(), user1.getEmail());
        assertThat(response.getBody().getPassword(), isEmptyOrNullString());
    }

    @Test
    public void testGetUserDetailsNotAuthorized() {
        ResponseEntity<User> response = restTemplate.getForEntity("/user", User.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(response.getBody().getId(), nullValue());
        assertThat(response.getBody().getUsername(), nullValue());
        assertThat(response.getBody().getPassword(), nullValue());
        assertThat(response.getBody().getEmail(), nullValue());
    }

    @Test
    public void testGetUserDetailsById() {
        HttpHeaders header = AuthorizationTests.headerWithAuthorizationToken(restTemplate, user1.getUsername(), "1234");
        HttpEntity<String> request = new HttpEntity<>("", header);
        String url = "/user/" + user2.getId();
        ResponseEntity<User> response = restTemplate.exchange(url, HttpMethod.GET, request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertEquals(response.getBody().getId(), user2.getId());
        assertEquals(response.getBody().getUsername(), user2.getUsername());
        assertEquals(response.getBody().getEmail(), user2.getEmail());
        assertThat(response.getBody().getPassword(), isEmptyOrNullString());
    }

    @Test
    public void testGetUserDetailsByIdNotAuthorized() {
        ResponseEntity<User> response = restTemplate.getForEntity("/user/" + user1.getId(), User.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(response.getBody().getId(), nullValue());
        assertThat(response.getBody().getUsername(), nullValue());
        assertThat(response.getBody().getPassword(), nullValue());
        assertThat(response.getBody().getEmail(), nullValue());
    }

    @Test
    public void testEditUserEmail() {
        HttpHeaders header = AuthorizationTests.headerWithAuthorizationToken(restTemplate, user1.getUsername(), "1234");

        JSONObject body = new JSONObject();
        body.put("id", user1.getId());
        body.put("username", user1.getUsername());
        body.put("email", "new_email@test.com");
        HttpEntity<String> request = new HttpEntity<>(body.toString(), header);
        ResponseEntity<User> response = restTemplate.exchange("/user", HttpMethod.POST, request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getId(), is(user1.getId()));
        assertThat(response.getBody().getUsername(), is("admin"));
        assertThat(response.getBody().getEmail(), is("new_email@test.com"));
        assertThat(response.getBody().getPassword(), nullValue());

        User updatedUser = repository.findById(user1.getId());
        assertThat(response.getBody().getUsername(), equalTo(updatedUser.getUsername()));
        assertThat(response.getBody().getEmail(), equalTo(updatedUser.getEmail()));
        assertTrue(bCryptPasswordEncoder.matches("1234", updatedUser.getPassword()));
    }

    @Test
    public void testEditUserEmailAndPassword() {
        HttpHeaders header = AuthorizationTests.headerWithAuthorizationToken(restTemplate, user1.getUsername(), "1234");

        JSONObject body = new JSONObject();
        body.put("id", user1.getId());
        body.put("username", user1.getUsername());
        body.put("email", "new_email@test.com");
        body.put("password", "new_password");
        HttpEntity<String> request = new HttpEntity<>(body.toString(), header);
        ResponseEntity<User> response = restTemplate.exchange("/user", HttpMethod.POST, request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getId(), is(user1.getId()));
        assertThat(response.getBody().getUsername(), is("admin"));
        assertThat(response.getBody().getEmail(), is("new_email@test.com"));
        assertThat(response.getBody().getPassword(), nullValue());

        User updatedUser = repository.findById(user1.getId());
        assertThat(updatedUser.getUsername(), equalTo(response.getBody().getUsername()));
        assertThat(updatedUser.getEmail(), equalTo(response.getBody().getEmail()));
        assertTrue(bCryptPasswordEncoder.matches("new_password", updatedUser.getPassword()));
    }

    @Test
    public void testEditUserEmailAndPasswordWithWrongId() {
        HttpHeaders header = AuthorizationTests.headerWithAuthorizationToken(restTemplate, user1.getUsername(), "1234");

        JSONObject body = new JSONObject();
        body.put("id", "-1");
        body.put("username", user1.getUsername());
        body.put("email", "new_email@test.com");
        body.put("password", "new_password");
        HttpEntity<String> request = new HttpEntity<>(body.toString(), header);
        ResponseEntity<User> response = restTemplate.exchange("/user", HttpMethod.POST, request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), nullValue());

        // Check that the user didn't change
        User user = repository.findById(user1.getId());
        assertThat(user.getUsername(), equalTo(user1.getUsername()));
        assertThat(user.getEmail(), equalTo(user1.getEmail()));
        assertThat(user.getPassword(), equalTo(user1.getPassword()));
    }

    @Test
    public void testEditUserEmailAndPasswordWithWrongUsername() {
        HttpHeaders header = AuthorizationTests.headerWithAuthorizationToken(restTemplate, user1.getUsername(), "1234");

        JSONObject body = new JSONObject();
        body.put("id", user1.getId());
        body.put("username", "wrong");
        body.put("email", "new_email@test.com");
        body.put("password", "new_password");
        HttpEntity<String> request = new HttpEntity<>(body.toString(), header);
        ResponseEntity<User> response = restTemplate.exchange("/user", HttpMethod.POST, request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), nullValue());

        // Check that the user didn't change
        User user = repository.findById(user1.getId());
        assertThat(user.getUsername(), equalTo(user1.getUsername()));
        assertThat(user.getEmail(), equalTo(user1.getEmail()));
        assertThat(user.getPassword(), equalTo(user1.getPassword()));
    }

    @Test
    public void testEditUserDetailsNotAuthorized() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", "admin");
        body.put("email", "new_email@test.com");
        HttpEntity<String> request = new HttpEntity<>(body.toString(), header);

        ResponseEntity<String> response = restTemplate.exchange("/user", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
    }

    @Test
    public void testSignUp() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", "simone");
        body.put("password", "1234");
        body.put("email", "simone@test.com");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/user/sign-up", request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testSignUpWithoutUsername() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("password", "1234");
        body.put("email", "simone@test.com");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/user/sign-up", request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testSignUpWithoutPassword() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", "simone");
        body.put("email", "simone@test.com");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/user/sign-up", request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testSignUpWithoutEmail() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", "simone");
        body.put("password", "1234");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/user/sign-up", request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testSignUpWithoutBody() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(null, headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/user/sign-up", request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    /**
     * Check that it's not possible to create a new user with an existing username
     */
    @Test
    public void testSignUpWithExistingUsername() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", user1.getUsername());
        body.put("password", "1234");
        body.put("email", "simone@test.com");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/user/sign-up", request, User.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }






}
