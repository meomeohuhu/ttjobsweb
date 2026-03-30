package com.ttjobs.backend.repository;

<<<<<<< HEAD
import java.util.Optional;

=======
>>>>>>> f43db024027cc48109eb85f3d54b21584fef54c4
import org.springframework.data.jpa.repository.JpaRepository;
import com.ttjobs.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
<<<<<<< HEAD
    Optional<User> findByEmail(String email);
=======
>>>>>>> f43db024027cc48109eb85f3d54b21584fef54c4

}