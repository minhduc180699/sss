package com.sss.user.domain.repository;

import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/

@Repository
public interface UserRepository {
  Optional<User> findById(UserId id);
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  User save(User user);
  void delete(UserId id);
  java.util.List<User> findAll();
}
