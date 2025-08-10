package com.sss.post.infrastructure.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.sss.post.application.PostService;
import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.model.Post;
import com.sss.post.domain.model.PostId;
import com.sss.post.infrastructure.graphql.dto.CreatePostInput;
import com.sss.post.infrastructure.graphql.dto.PostDto;
import com.sss.post.infrastructure.graphql.dto.PollOptionDto;
import com.sss.post.infrastructure.graphql.dto.UpdatePostInput;
import com.sss.post.infrastructure.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class PostDataFetcher {
  
  private final PostService postService;
  private final PostMapper postMapper;
  
  @DgsQuery
  public PostDto post(@InputArgument String id) {
    log.info("GraphQL Query: Fetching post with ID: {}", id);
    
    Post post = postService.getPostById(id);
    return convertToDto(post);
  }
  
  @DgsQuery
  public PostConnection posts(
      @InputArgument String authorId,
      @InputArgument PostStatus status,
      @InputArgument PostType postType,
      @InputArgument List<String> tags,
      @InputArgument String keyword,
      @InputArgument Integer limit,
      @InputArgument Integer offset) {
    
    log.info("GraphQL Query: Fetching posts with filters - authorId: {}, status: {}, postType: {}, tags: {}, keyword: {}", 
        authorId, status, postType, tags, keyword);
    
    List<Post> posts;
    
    if (keyword != null && !keyword.trim().isEmpty()) {
      posts = postService.searchPosts(keyword);
    } else if (tags != null && !tags.isEmpty()) {
      posts = postService.getPostsByTags(tags);
    } else if (authorId != null) {
      if (status != null) {
        posts = postService.getPostsByAuthorIdAndStatus(authorId, status);
      } else {
        posts = postService.getPostsByAuthorId(authorId);
      }
    } else if (status != null) {
      posts = postService.getPostsByStatus(status);
    } else if (postType != null) {
      posts = postService.getPostsByType(postType);
    } else {
      posts = postService.getAllPosts();
    }
    
    // Apply pagination
    int actualLimit = limit != null ? limit : 20;
    int actualOffset = offset != null ? offset : 0;
    
    List<Post> paginatedPosts = posts.stream()
        .skip(actualOffset)
        .limit(actualLimit)
        .collect(Collectors.toList());
    
    List<PostDto> postDtos = paginatedPosts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
    
    return PostConnection.builder()
        .posts(postDtos)
        .totalCount(posts.size())
        .hasNextPage(actualOffset + actualLimit < posts.size())
        .hasPreviousPage(actualOffset > 0)
        .build();
  }
  
  @DgsQuery
  public PostConnection publishedPosts(
      @InputArgument String authorId,
      @InputArgument Integer limit,
      @InputArgument Integer offset) {
    
    log.info("GraphQL Query: Fetching published posts - authorId: {}", authorId);
    
    List<Post> posts;
    if (authorId != null) {
      posts = postService.getPublishedPostsByAuthorId(authorId);
    } else {
      posts = postService.getPublishedPosts();
    }
    
    // Apply pagination
    int actualLimit = limit != null ? limit : 20;
    int actualOffset = offset != null ? offset : 0;
    
    List<Post> paginatedPosts = posts.stream()
        .skip(actualOffset)
        .limit(actualLimit)
        .collect(Collectors.toList());
    
    List<PostDto> postDtos = paginatedPosts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
    
    return PostConnection.builder()
        .posts(postDtos)
        .totalCount(posts.size())
        .hasNextPage(actualOffset + actualLimit < posts.size())
        .hasPreviousPage(actualOffset > 0)
        .build();
  }
  
  @DgsQuery
  public PostConnection searchPosts(
      @InputArgument String keyword,
      @InputArgument Integer limit,
      @InputArgument Integer offset) {
    
    log.info("GraphQL Query: Searching posts with keyword: {}", keyword);
    
    List<Post> posts = postService.searchPosts(keyword);
    
    // Apply pagination
    int actualLimit = limit != null ? limit : 20;
    int actualOffset = offset != null ? offset : 0;
    
    List<Post> paginatedPosts = posts.stream()
        .skip(actualOffset)
        .limit(actualLimit)
        .collect(Collectors.toList());
    
    List<PostDto> postDtos = paginatedPosts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
    
    return PostConnection.builder()
        .posts(postDtos)
        .totalCount(posts.size())
        .hasNextPage(actualOffset + actualLimit < posts.size())
        .hasPreviousPage(actualOffset > 0)
        .build();
  }
  
  @DgsQuery
  public PostConnection postsByTags(
      @InputArgument List<String> tags,
      @InputArgument Integer limit,
      @InputArgument Integer offset) {
    
    log.info("GraphQL Query: Fetching posts by tags: {}", tags);
    
    List<Post> posts = postService.getPostsByTags(tags);
    
    // Apply pagination
    int actualLimit = limit != null ? limit : 20;
    int actualOffset = offset != null ? offset : 0;
    
    List<Post> paginatedPosts = posts.stream()
        .skip(actualOffset)
        .limit(actualLimit)
        .collect(Collectors.toList());
    
    List<PostDto> postDtos = paginatedPosts.stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
    
    return PostConnection.builder()
        .posts(postDtos)
        .totalCount(posts.size())
        .hasNextPage(actualOffset + actualLimit < posts.size())
        .hasPreviousPage(actualOffset > 0)
        .build();
  }
  
  @DgsQuery
  public Integer postCount(@InputArgument String authorId, @InputArgument PostStatus status) {
    log.info("GraphQL Query: Getting post count - authorId: {}, status: {}", authorId, status);
    
    if (authorId != null && status != null) {
      return (int) postService.getPostCountByAuthorId(authorId);
    } else if (authorId != null) {
      return (int) postService.getPostCountByAuthorId(authorId);
    } else if (status != null) {
      return (int) postService.getPostCountByStatus(status);
    } else {
      return (int) postService.getPostCountByStatus(PostStatus.PUBLISHED);
    }
  }
  
  @DgsMutation
  public PostDto createPost(@InputArgument CreatePostInput input) {
    log.info("GraphQL Mutation: Creating post - title: {}, authorId: {}", input.getTitle(), input.getAuthorId());
    
    Post post = Post.builder()
        .id(new PostId(UUID.randomUUID().toString()))
        .authorId(input.getAuthorId())
        .authorName(input.getAuthorName())
        .authorAvatar(input.getAuthorAvatar())
        .title(input.getTitle())
        .content(input.getContent())
        .postType(input.getPostType())
        .imageUrls(input.getImageUrls())
        .videoUrls(input.getVideoUrls())
        .thumbnailUrl(input.getThumbnailUrl())
        .pollOptions(convertPollOptions(input.getPollOptions()))
        .pollEndTime(input.getPollEndTime())
        .eventStartTime(input.getEventStartTime())
        .eventEndTime(input.getEventEndTime())
        .eventLocation(input.getEventLocation())
        .isPublic(input.getIsPublic() != null ? input.getIsPublic() : true)
        .allowedViewers(input.getAllowedViewers())
        .tags(input.getTags())
        .build();
    
    Post savedPost = postService.createPost(post);
    return convertToDto(savedPost);
  }
  
  @DgsMutation
  public PostDto updatePost(@InputArgument String id, @InputArgument UpdatePostInput input) {
    log.info("GraphQL Mutation: Updating post with ID: {}", id);
    
    Post updatedPost = Post.builder()
        .id(new PostId(id))
        .title(input.getTitle())
        .content(input.getContent())
        .postType(input.getPostType())
        .imageUrls(input.getImageUrls())
        .videoUrls(input.getVideoUrls())
        .thumbnailUrl(input.getThumbnailUrl())
        .pollOptions(convertPollOptions(input.getPollOptions()))
        .pollEndTime(input.getPollEndTime())
        .eventStartTime(input.getEventStartTime())
        .eventEndTime(input.getEventEndTime())
        .eventLocation(input.getEventLocation())
        .isPublic(input.getIsPublic() != null ? input.getIsPublic() : true)
        .allowedViewers(input.getAllowedViewers())
        .tags(input.getTags())
        .build();
    
    Post savedPost = postService.updatePost(id, updatedPost);
    return convertToDto(savedPost);
  }
  
  @DgsMutation
  public PostDto publishPost(@InputArgument String id) {
    log.info("GraphQL Mutation: Publishing post with ID: {}", id);
    
    Post post = postService.publishPost(id);
    return convertToDto(post);
  }
  
  @DgsMutation
  public PostDto archivePost(@InputArgument String id) {
    log.info("GraphQL Mutation: Archiving post with ID: {}", id);
    
    Post post = postService.archivePost(id);
    return convertToDto(post);
  }
  
  @DgsMutation
  public PostDto deletePost(@InputArgument String id) {
    log.info("GraphQL Mutation: Deleting post with ID: {}", id);
    
    Post post = postService.deletePost(id);
    return convertToDto(post);
  }
  
  @DgsMutation
  public PostDto likePost(@InputArgument String id) {
    log.info("GraphQL Mutation: Liking post with ID: {}", id);
    
    Post post = postService.likePost(id);
    return convertToDto(post);
  }
  
  @DgsMutation
  public PostDto unlikePost(@InputArgument String id) {
    log.info("GraphQL Mutation: Unliking post with ID: {}", id);
    
    Post post = postService.unlikePost(id);
    return convertToDto(post);
  }
  
  @DgsMutation
  public PostDto incrementViewCount(@InputArgument String id) {
    log.info("GraphQL Mutation: Incrementing view count for post with ID: {}", id);
    
    Post post = postService.incrementViewCount(id);
    return convertToDto(post);
  }
  
  private PostDto convertToDto(Post post) {
    return PostDto.builder()
        .id(post.getId().getValue())
        .authorId(post.getAuthorId())
        .authorName(post.getAuthorName())
        .authorAvatar(post.getAuthorAvatar())
        .title(post.getTitle())
        .content(post.getContent())
        .postType(post.getPostType())
        .status(post.getStatus())
        .imageUrls(post.getImageUrls())
        .videoUrls(post.getVideoUrls())
        .thumbnailUrl(post.getThumbnailUrl())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .shareCount(post.getShareCount())
        .viewCount(post.getViewCount())
        .pollOptions(convertPollOptionDtos(post.getPollOptions()))
        .pollEndTime(post.getPollEndTime())
        .eventStartTime(post.getEventStartTime())
        .eventEndTime(post.getEventEndTime())
        .eventLocation(post.getEventLocation())
        .isPublic(post.isPublic())
        .allowedViewers(post.getAllowedViewers())
        .tags(post.getTags())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .publishedAt(post.getPublishedAt())
        .createdBy(post.getCreatedBy())
        .updatedBy(post.getUpdatedBy())
        .isModerated(post.isModerated())
        .moderatorId(post.getModeratorId())
        .moderationNote(post.getModerationNote())
        .moderatedAt(post.getModeratedAt())
        .build();
  }
  
  private List<com.sss.post.domain.model.PollOption> convertPollOptions(List<com.sss.post.infrastructure.graphql.dto.PollOptionInput> pollOptionInputs) {
    if (pollOptionInputs == null) {
      return null;
    }
    
    return pollOptionInputs.stream()
        .map(input -> com.sss.post.domain.model.PollOption.builder()
            .id(UUID.randomUUID().toString())
            .text(input.getText())
            .imageUrl(input.getImageUrl())
            .voteCount(0)
            .build())
        .collect(Collectors.toList());
  }
  
  private List<PollOptionDto> convertPollOptionDtos(List<com.sss.post.domain.model.PollOption> pollOptions) {
    if (pollOptions == null) {
      return null;
    }
    
    return pollOptions.stream()
        .map(option -> PollOptionDto.builder()
            .id(option.getId())
            .text(option.getText())
            .voteCount(option.getVoteCount())
            .imageUrl(option.getImageUrl())
            .build())
        .collect(Collectors.toList());
  }
  
  // Inner class for PostConnection
  @lombok.Data
  @lombok.Builder
  public static class PostConnection {
    private List<PostDto> posts;
    private int totalCount;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
  }
}
