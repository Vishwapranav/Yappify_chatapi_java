package com.yappifychatapp.repositories;

import com.yappifychatapp.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    // Custom query for searchUsers
    List<User> findByIdNotAndNameRegexOrEmailRegex(String id, Pattern name, Pattern email);
}
