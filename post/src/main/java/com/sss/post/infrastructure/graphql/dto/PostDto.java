package com.sss.post.infrastructure.graphql.dto;

import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
  
  private String id;
  private String authorId;
  private String authorName;
  private String authorAvatar;
  private String title;
  private String content;
  private PostType postType;
  private PostStatus status;
  
  // Media content
  private List<String> imageUrls;
  private List<String> videoUrls;
  private String thumbnailUrl;
  
  // Engagement metrics
  private int likeCount;
  private int commentCount;
  private int shareCount;
  private int viewCount;
  
  // Poll data
  private List<PollOptionDto> pollOptions;
  private LocalDateTime pollEndTime;
  
  // Event data
  private LocalDateTime eventStartTime;
  private LocalDateTime eventEndTime;
  private String eventLocation;
  
  // Privacy and visibility
  private boolean isPublic;
  private List<String> allowedViewers;
  private List<String> tags;
  
  // Metadata
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime publishedAt;
  private String createdBy;
  private String updatedBy;
  
  // Moderation
  private boolean isModerated;
  private String moderatorId;
  private String moderationNote;
  private LocalDateTime moderatedAt;
}
