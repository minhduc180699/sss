package com.sss.post.infrastructure.persistence;

import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Repository
public interface SpringDataPostRepository extends MongoRepository<PostDocument, String> {
  
  List<PostDocument> findByAuthorId(String authorId);
  
  List<PostDocument> findByStatus(PostStatus status);
  
  List<PostDocument> findByPostType(PostType postType);
  
  List<PostDocument> findByAuthorIdAndStatus(String authorId, PostStatus status);
  
  @Query("{'status': 'PUBLISHED'}")
  List<PostDocument> findPublishedPosts();
  
  @Query("{'authorId': ?0, 'status': 'PUBLISHED'}")
  List<PostDocument> findPublishedPostsByAuthorId(String authorId);
  
  @Query("{'tags': {$in: ?0}}")
  List<PostDocument> findByTags(List<String> tags);
  
  @Query("{'$or': [{'title': {$regex: ?0, $options: 'i'}}, {'content': {$regex: ?0, $options: 'i'}}]}")
  List<PostDocument> searchByContent(String keyword);
  
  long countByAuthorId(String authorId);
  
  long countByStatus(PostStatus status);
}
