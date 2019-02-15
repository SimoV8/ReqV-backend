package it.sagelab.reqv.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import it.sagelab.reqv.ReqVApplication;
import net.minidev.json.JSONObject;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static it.sagelab.reqv.security.SecurityConstants.TOKEN_PREFIX;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ReqVApplication.class)
@ActiveProfiles("test")
public class AuthorizationTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    public UserRepository repository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Before
    public void setUp() {
        User admin = new User("admin", bCryptPasswordEncoder.encode("1234"), "admin@test.it");

        repository.deleteAll();
        repository.save(admin);

        HttpClient httpClient = HttpClientBuilder.create().build();
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    @Test
    public void testLoginSuccess() {

        ResponseEntity<String> response = login(restTemplate,"admin", "1234");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertNotNull(response.getHeaders().get("Authorization"));
        assertThat(response.getHeaders().get("Authorization").size(), is(1));

        // Check token is correct
        String token = response.getHeaders().getFirst("Authorization");
        assertThat(token, containsString(TOKEN_PREFIX));
        token = token.replace(TOKEN_PREFIX, "");
        int i = token.lastIndexOf('.');
        String untrustedJwtString = token.substring(0, i+1);
        Jwt<Header,Claims> untrusted = Jwts.parser().parseClaimsJwt(untrustedJwtString);
        assertThat(untrusted.getBody().getSubject(), is("admin"));

    }

    @Test
    public void testLoginFailWithWrongPassword() {

        ResponseEntity<String> response = login(restTemplate,"admin", "abc");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(response.getHeaders().get("Authorization"), nullValue());
    }

    @Test
    public void testLoginFailWithWrongUsername() {

        ResponseEntity<String> response = login(restTemplate,"wrong", "1234");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(response.getHeaders().get("Authorization"), nullValue());
    }

    @Test
    public void testLoginFailWithoutPassword() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", "admin");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange("/login", HttpMethod.POST, request, String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(response.getHeaders().get("Authorization"), nullValue());
    }

    @Test
    public void testLoginFailWithoutBody() {

        ResponseEntity<String> response = restTemplate.postForEntity("/login", "", String.class);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(response.getHeaders().get("Authorization"), nullValue());
    }

    @Test
    public void testLoginFailWrongMethod() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", "admin");
        body.put("password", "1234");

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange("/login", HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(response.getHeaders().get("Authorization"), nullValue());

        response = restTemplate.exchange("/login", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(response.getHeaders().get("Authorization"), nullValue());

        response = restTemplate.exchange("/login", HttpMethod.DELETE, request, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(response.getHeaders().get("Authorization"), nullValue());
    }

    public static ResponseEntity<String> login(TestRestTemplate restTemplate, String username, String password) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        return restTemplate.exchange("/login", HttpMethod.POST, request, String.class);
    }

    public static HttpHeaders headerWithAuthorizationToken(TestRestTemplate restTemplate, String username, String password) {

        ResponseEntity<String> response = login(restTemplate, username, password);
        if(response.getStatusCode() == HttpStatus.OK && response.getHeaders().containsKey("Authorization")) {

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_JSON);
            header.set("Authorization", response.getHeaders().getFirst("Authorization"));
            return header;
        }

        return null;
    }

}
