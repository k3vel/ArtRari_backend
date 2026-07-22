package com.project.ArtRari.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);

    Page<User> findByIdNot(Long id, Pageable pageable);

    @Query("""
            SELECT u
            FROM User u
            WHERE (u.firstName LIKE %?1%
            OR u.lastName LIKE %?1%
            OR u.email LIKE %?1%
            OR u.phone LIKE %?1%)
            AND u.id != ?2""")
    Page<User> findAllBySearchAndIdNot(String search, Long id, Pageable pageable);

}
