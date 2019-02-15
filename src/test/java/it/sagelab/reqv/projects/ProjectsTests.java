package it.sagelab.reqv.projects;

import it.sagelab.reqv.user.User;
import org.junit.Before;
import org.junit.Test;
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
    public void setUp() {

        //user = new User("admin", bCryptPasswordEncoder.encode("1234"), "admin@test.it");
    }

    @Test
    public void testGetProjects() {

    }
}
