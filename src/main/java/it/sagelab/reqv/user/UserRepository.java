package it.sagelab.reqv.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String username);

    Optional<User> findById(Long id);

}
