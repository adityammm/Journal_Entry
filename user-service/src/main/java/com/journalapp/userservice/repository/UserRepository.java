package com.journalapp.userservice.repository;

import com.journalapp.userservice.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByUserName(String userName);
    boolean existsByUserName(String userName);
}
