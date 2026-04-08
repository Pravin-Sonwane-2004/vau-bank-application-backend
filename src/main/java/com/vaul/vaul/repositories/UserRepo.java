package com.vaul.vaul.repositories;

import com.vaul.vaul.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {

}
