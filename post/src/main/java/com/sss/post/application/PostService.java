package com.sss.post.application;

import com.sss.post.domain.model.Post;
import com.sss.post.domain.model.PostId;
import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.exception.PostNotFoundException;
import com.sss.post.domain.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
  
  private final PostRepository postRepository;
  
  public Post createPost(Post post) {
    log.info("Creating new post for author: {}", post.getAuthorId());
    
    // Generate new ID if not provided
    if (post.getId() == null || post.getId().getValue() == null) {
      post.setId(new PostId(UUID.randomUUID().toString()));
    }
    
    // Set default values
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    post.setLikeCount(0);
    post.setCommentCount(0);
    post.setShareCount(0);
    post.setViewCount(0);
    
    if (post.getStatus() == null) {
      post.setStatus(PostStatus.DRAFT);
    }
    
    Post savedPost = postRepository.save(post);
    log.info("Post created successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  @Transactional(readOnly = true)
  public Post getPostById(String postId) {
    log.info("Fetching post with ID: {}", postId);
    
    PostId id = new PostId(postId);
    Optional<Post> post = postRepository.findById(id);
    
    if (post.isEmpty()) {
      throw new PostNotFoundException("Post not found with ID: " + postId);
    }
    
    return post.get();
  }
  
  @Transactional(readOnly = true)
  public List<Post> getAllPosts() {
    log.info("Fetching all posts");
    return postRepository.findAll();
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPostsByAuthorId(String authorId) {
    log.info("Fetching posts for author: {}", authorId);
    return postRepository.findByAuthorId(authorId);
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPublishedPosts() {
    log.info("Fetching all published posts");
    return postRepository.findPublishedPosts();
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPublishedPostsByAuthorId(String authorId) {
    log.info("Fetching published posts for author: {}", authorId);
    return postRepository.findPublishedPostsByAuthorId(authorId);
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPostsByStatus(PostStatus status) {
    log.info("Fetching posts with status: {}", status);
    return postRepository.findByStatus(status);
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPostsByType(PostType postType) {
    log.info("Fetching posts with type: {}", postType);
    return postRepository.findByPostType(postType);
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPostsByAuthorIdAndStatus(String authorId, PostStatus status) {
    log.info("Fetching posts for author: {} with status: {}", authorId, status);
    return postRepository.findByAuthorIdAndStatus(authorId, status);
  }
  
  @Transactional(readOnly = true)
  public List<Post> searchPosts(String keyword) {
    log.info("Searching posts with keyword: {}", keyword);
    return postRepository.searchByContent(keyword);
  }
  
  @Transactional(readOnly = true)
  public List<Post> getPostsByTags(List<String> tags) {
    log.info("Fetching posts with tags: {}", tags);
    return postRepository.findByTags(tags);
  }
  
  public Post updatePost(String postId, Post updatedPost) {
    log.info("Updating post with ID: {}", postId);
    
    Post existingPost = getPostById(postId);
    
    // Update fields
    if (updatedPost.getTitle() != null) {
      existingPost.setTitle(updatedPost.getTitle());
    }
    if (updatedPost.getContent() != null) {
      existingPost.setContent(updatedPost.getContent());
    }
    if (updatedPost.getPostType() != null) {
      existingPost.setPostType(updatedPost.getPostType());
    }
    if (updatedPost.getImageUrls() != null) {
      existingPost.setImageUrls(updatedPost.getImageUrls());
    }
    if (updatedPost.getVideoUrls() != null) {
      existingPost.setVideoUrls(updatedPost.getVideoUrls());
    }
    if (updatedPost.getTags() != null) {
      existingPost.setTags(updatedPost.getTags());
    }
    if (updatedPost.getPollOptions() != null) {
      existingPost.setPollOptions(updatedPost.getPollOptions());
    }
    if (updatedPost.getPollEndTime() != null) {
      existingPost.setPollEndTime(updatedPost.getPollEndTime());
    }
    if (updatedPost.getEventStartTime() != null) {
      existingPost.setEventStartTime(updatedPost.getEventStartTime());
    }
    if (updatedPost.getEventEndTime() != null) {
      existingPost.setEventEndTime(updatedPost.getEventEndTime());
    }
    if (updatedPost.getEventLocation() != null) {
      existingPost.setEventLocation(updatedPost.getEventLocation());
    }
    
    existingPost.setUpdatedAt(LocalDateTime.now());
    existingPost.setUpdatedBy(updatedPost.getAuthorId());
    
    Post savedPost = postRepository.save(existingPost);
    log.info("Post updated successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public Post publishPost(String postId) {
    log.info("Publishing post with ID: {}", postId);
    
    Post post = getPostById(postId);
    post.publish();
    post.setUpdatedAt(LocalDateTime.now());
    
    Post savedPost = postRepository.save(post);
    log.info("Post published successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public Post archivePost(String postId) {
    log.info("Archiving post with ID: {}", postId);
    
    Post post = getPostById(postId);
    post.archive();
    
    Post savedPost = postRepository.save(post);
    log.info("Post archived successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public Post deletePost(String postId) {
    log.info("Deleting post with ID: {}", postId);
    
    Post post = getPostById(postId);
    post.delete();
    
    Post savedPost = postRepository.save(post);
    log.info("Post deleted successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public Post likePost(String postId) {
    log.info("Liking post with ID: {}", postId);
    
    Post post = getPostById(postId);
    post.incrementLikeCount();
    post.setUpdatedAt(LocalDateTime.now());
    
    Post savedPost = postRepository.save(post);
    log.info("Post liked successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public Post unlikePost(String postId) {
    log.info("Unliking post with ID: {}", postId);
    
    Post post = getPostById(postId);
    post.decrementLikeCount();
    post.setUpdatedAt(LocalDateTime.now());
    
    Post savedPost = postRepository.save(post);
    log.info("Post unliked successfully with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public Post incrementViewCount(String postId) {
    log.info("Incrementing view count for post with ID: {}", postId);
    
    Post post = getPostById(postId);
    post.incrementViewCount();
    
    Post savedPost = postRepository.save(post);
    log.info("View count incremented successfully for post with ID: {}", savedPost.getId().getValue());
    
    return savedPost;
  }
  
  public boolean existsById(String postId) {
    return postRepository.existsById(new PostId(postId));
  }
  
  public long getPostCountByAuthorId(String authorId) {
    return postRepository.countByAuthorId(authorId);
  }
  
  public long getPostCountByStatus(PostStatus status) {
    return postRepository.countByStatus(status);
  }
}
