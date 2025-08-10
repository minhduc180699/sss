package com.sss.post.domain.repository;

import com.sss.post.domain.model.Post;
import com.sss.post.domain.model.PostId;
import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import java.util.List;
import java.util.Optional;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
public interface PostRepository {
  
  Post save(Post post);
  
  Optional<Post> findById(PostId id);
  
  List<Post> findAll();
  
  List<Post> findByAuthorId(String authorId);
  
  List<Post> findByStatus(PostStatus status);
  
  List<Post> findByPostType(PostType postType);
  
  List<Post> findByAuthorIdAndStatus(String authorId, PostStatus status);
  
  List<Post> findPublishedPosts();
  
  List<Post> findPublishedPostsByAuthorId(String authorId);
  
  List<Post> findByTags(List<String> tags);
  
  List<Post> searchByContent(String keyword);
  
  void deleteById(PostId id);
  
  boolean existsById(PostId id);
  
  long countByAuthorId(String authorId);
  
  long countByStatus(PostStatus status);
}
