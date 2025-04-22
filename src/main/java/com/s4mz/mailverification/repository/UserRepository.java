package com.s4mz.mailverification.repository;

import com.s4mz.mailverification.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUserName(String userName);

    Optional<User> findByVerificationCode(String verificationCode);

}
