package com.sss.post.infrastructure.persistence;

import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.model.PollOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
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
@Document(collection = "posts")
public class PostDocument {
  
  @Id
  private String id;
  
  @Field("author_id")
  private String authorId;
  
  @Field("author_name")
  private String authorName;
  
  @Field("author_avatar")
  private String authorAvatar;
  
  private String title;
  private String content;
  
  @Field("post_type")
  private PostType postType;
  
  private PostStatus status;
  
  // Media content
  @Field("image_urls")
  private List<String> imageUrls;
  
  @Field("video_urls")
  private List<String> videoUrls;
  
  @Field("thumbnail_url")
  private String thumbnailUrl;
  
  // Engagement metrics
  @Field("like_count")
  private int likeCount;
  
  @Field("comment_count")
  private int commentCount;
  
  @Field("share_count")
  private int shareCount;
  
  @Field("view_count")
  private int viewCount;
  
  // Poll data
  @Field("poll_options")
  private List<PollOption> pollOptions;
  
  @Field("poll_end_time")
  private LocalDateTime pollEndTime;
  
  // Event data
  @Field("event_start_time")
  private LocalDateTime eventStartTime;
  
  @Field("event_end_time")
  private LocalDateTime eventEndTime;
  
  @Field("event_location")
  private String eventLocation;
  
  // Privacy and visibility
  @Field("is_public")
  private boolean isPublic;
  
  @Field("allowed_viewers")
  private List<String> allowedViewers;
  
  private List<String> tags;
  
  // Metadata
  @Field("created_at")
  private LocalDateTime createdAt;
  
  @Field("updated_at")
  private LocalDateTime updatedAt;
  
  @Field("published_at")
  private LocalDateTime publishedAt;
  
  @Field("created_by")
  private String createdBy;
  
  @Field("updated_by")
  private String updatedBy;
  
  // Moderation
  @Field("is_moderated")
  private boolean isModerated;
  
  @Field("moderator_id")
  private String moderatorId;
  
  @Field("moderation_note")
  private String moderationNote;
  
  @Field("moderated_at")
  private LocalDateTime moderatedAt;
}
