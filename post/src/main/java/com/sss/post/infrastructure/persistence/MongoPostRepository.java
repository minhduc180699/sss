package com.sss.post.infrastructure.persistence;

import com.sss.post.domain.model.Post;
import com.sss.post.domain.model.PostId;
import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.repository.PostRepository;
import com.sss.post.infrastructure.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoPostRepository implements PostRepository {
  
  private final SpringDataPostRepository springDataPostRepository;
  private final PostMapper postMapper;
  
  @Override
  public Post save(Post post) {
    log.debug("Saving post with ID: {}", post.getId().getValue());
    
    PostDocument postDocument = postMapper.toDocument(post);
    PostDocument savedDocument = springDataPostRepository.save(postDocument);
    
    return postMapper.toDomain(savedDocument);
  }
  
  @Override
  public Optional<Post> findById(PostId id) {
    log.debug("Finding post by ID: {}", id.getValue());
    
    Optional<PostDocument> postDocument = springDataPostRepository.findById(id.getValue());
    return postDocument.map(postMapper::toDomain);
  }
  
  @Override
  public List<Post> findAll() {
    log.debug("Finding all posts");
    
    List<PostDocument> postDocuments = springDataPostRepository.findAll();
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findByAuthorId(String authorId) {
    log.debug("Finding posts by author ID: {}", authorId);
    
    List<PostDocument> postDocuments = springDataPostRepository.findByAuthorId(authorId);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findByStatus(PostStatus status) {
    log.debug("Finding posts by status: {}", status);
    
    List<PostDocument> postDocuments = springDataPostRepository.findByStatus(status);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findByPostType(PostType postType) {
    log.debug("Finding posts by type: {}", postType);
    
    List<PostDocument> postDocuments = springDataPostRepository.findByPostType(postType);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findByAuthorIdAndStatus(String authorId, PostStatus status) {
    log.debug("Finding posts by author ID: {} and status: {}", authorId, status);
    
    List<PostDocument> postDocuments = springDataPostRepository.findByAuthorIdAndStatus(authorId, status);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findPublishedPosts() {
    log.debug("Finding all published posts");
    
    List<PostDocument> postDocuments = springDataPostRepository.findPublishedPosts();
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findPublishedPostsByAuthorId(String authorId) {
    log.debug("Finding published posts by author ID: {}", authorId);
    
    List<PostDocument> postDocuments = springDataPostRepository.findPublishedPostsByAuthorId(authorId);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> findByTags(List<String> tags) {
    log.debug("Finding posts by tags: {}", tags);
    
    List<PostDocument> postDocuments = springDataPostRepository.findByTags(tags);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public List<Post> searchByContent(String keyword) {
    log.debug("Searching posts by keyword: {}", keyword);
    
    List<PostDocument> postDocuments = springDataPostRepository.searchByContent(keyword);
    return postDocuments.stream()
        .map(postMapper::toDomain)
        .collect(Collectors.toList());
  }
  
  @Override
  public void deleteById(PostId id) {
    log.debug("Deleting post by ID: {}", id.getValue());
    springDataPostRepository.deleteById(id.getValue());
  }
  
  @Override
  public boolean existsById(PostId id) {
    return springDataPostRepository.existsById(id.getValue());
  }
  
  @Override
  public long countByAuthorId(String authorId) {
    return springDataPostRepository.countByAuthorId(authorId);
  }
  
  @Override
  public long countByStatus(PostStatus status) {
    return springDataPostRepository.countByStatus(status);
  }
}
