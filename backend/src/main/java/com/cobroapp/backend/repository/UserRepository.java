package com.cobroapp.backend.repository;

import com.cobroapp.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//el spring Data Jpa nos permite hacer consultas sin sentencias SQL

public interface UserRepository extends JpaRepository<User, Long> {


    //Optional expresa explícitamente:Este resultado puede contener un usuario o puede estar vacío
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
