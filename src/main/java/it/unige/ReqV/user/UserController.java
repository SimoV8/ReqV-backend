package it.unige.ReqV.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getPrincipal() != null) {
            logger.info("Fetching User with username {}", auth.getPrincipal());
            return  userRepository.findByUsername(auth.getPrincipal().toString());
        } else {
            logger.info("User not authenticated");
            return null;
        }
    }

    @GetMapping()
    public ResponseEntity<?> getUser() {
            User user = getAuthenticatedUser();
            if(user != null)
                return new ResponseEntity<>(user, HttpStatus.OK);
            else
                return new ResponseEntity(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping()
    public ResponseEntity<?> update(@RequestBody User newUser) {

        User user = getAuthenticatedUser();
        if (user == null || newUser == null
                || !user.getId().equals(newUser.getId())
                || !user.getUsername().equals(newUser.getUsername()))
        {
            logger.error("{} not matching authenticated user {}", newUser, user);
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

        if(newUser.getPassword() != null)
            user.setPassword(bCryptPasswordEncoder.encode(newUser.getPassword()));
        if(newUser.getEmail() != null)
            user.setEmail(newUser.getEmail());

        user = userRepository.save(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") long id) {
        logger.info("Fetching User with id {}", id);
        User user = userRepository.findById(id);
        if (user == null) {
            logger.error("User with id {} not found.", id);
            return new ResponseEntity(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody User user) {
        try {
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                user = userRepository.save(user);
                if (user != null)
                    return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (DataAccessException e) {
            logger.error("/users/sing-up failed", e);
        }
        return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
    }
}
