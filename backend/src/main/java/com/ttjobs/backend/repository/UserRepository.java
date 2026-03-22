package com.ttjobs.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ttjobs.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}