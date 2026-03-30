package com.ttjobs.backend.repository;

<<<<<<< HEAD
import java.util.Optional;

=======

import java.util.Optional;


>>>>>>> b4878935db9414e0c24d4d74e95f030f7ad59ad0
import org.springframework.data.jpa.repository.JpaRepository;
import com.ttjobs.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
<<<<<<< HEAD
    Optional<User> findByEmail(String email);

=======

    Optional<User> findByEmail(String email);


>>>>>>> b4878935db9414e0c24d4d74e95f030f7ad59ad0
}