package it.unige.ReqV.projects;

import it.unige.ReqV.ReqVApplication;
import it.unige.ReqV.user.User;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProjectsTests {

    User user;

    @Before
    void setUp() {

        //user = new User("admin", bCryptPasswordEncoder.encode("1234"), "admin@test.it");
    }
}
