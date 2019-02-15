package it.sagelab.reqv.user;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.getPrincipal() != null) {
            return  userRepository.findByUsername(auth.getPrincipal().toString());
        } else {
            logger.info("User not authenticated");
            return null;
        }
    }

    public User updateAuthenticatedUser(User user) {
        User authUser = getAuthenticatedUser();
        if(authUser == null || user == null
                || !authUser.getId().equals(user.getId())
                || ! authUser.getUsername().equals(user.getUsername()))
            return null;
        if(user.getPassword() != null)
            authUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        if(user.getEmail() != null)
            authUser.setEmail(user.getEmail());

        return userRepository.save(authUser);
    }

    public User findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User create(@Valid User user) {
        logger.info("Creating User with username {}", user.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
