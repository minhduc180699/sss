package com.sss.user.infrastructure.persistence;

import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.domain.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
@Repository
@RequiredArgsConstructor
public class MongoUserRepository implements UserRepository {

  private final SpringDataUserRepository repo;
  private final UserMapper userMapper;

  @Override
  public Optional<User> findById(UserId id) {
    return repo.findById(id.value()).map(userMapper::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return repo.findByUsername(username).map(userMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return repo.findByEmail(email).map(userMapper::toDomain);
  }

  @Override
  public User save(User user) {
    return userMapper.toDomain(repo.save(userMapper.toDocument(user)));
  }

  @Override
  public void delete(UserId id) {
    repo.deleteById(id.value());
  }

  @Override
  public java.util.List<User> findAll() {
    return repo.findAll().stream()
        .map(userMapper::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }
}
