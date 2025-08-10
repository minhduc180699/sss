package com.sss.user.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {
  Optional<UserDocument> findByUsername(String username);
  Optional<UserDocument> findByEmail(String email);
}
