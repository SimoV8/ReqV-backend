package it.unige.ReqV.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping()
    public ResponseEntity<?> getUser() {
            User user = userService.getAuthenticatedUser();
            if(user != null)
                return new ResponseEntity<>(user, HttpStatus.OK);
            else
                return new ResponseEntity(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping()
    public ResponseEntity<?> update(@RequestBody User newUser) {

        User user = userService.updateAuthenticatedUser(newUser);
        if (user == null)
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        else
            return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") long id) {
        User user = userService.findById(id);
        if (user == null) {
            logger.error("User with id {} not found.", id);
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user) {
        try {
                user = userService.create(user);
                if (user != null)
                    return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (DataAccessException e) {
            logger.error("/users/sing-up failed", e);
        }
        return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    }
}
