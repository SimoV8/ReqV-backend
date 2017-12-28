package it.unige.ReqV.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends CrudRepository<User, Long> {

    User findByUsername(String username);

    User findById(Long id);

}
