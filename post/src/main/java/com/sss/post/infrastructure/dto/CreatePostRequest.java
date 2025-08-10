package com.sss.post.infrastructure.dto;

import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.model.PollOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePostRequest {
  
  @NotBlank(message = "Author ID is required")
  private String authorId;
  
  private String authorName;
  private String authorAvatar;
  
  @NotBlank(message = "Title is required")
  private String title;
  
  @NotBlank(message = "Content is required")
  private String content;
  
  @NotNull(message = "Post type is required")
  private PostType postType;
  
  // Media content
  private List<String> imageUrls;
  private List<String> videoUrls;
  private String thumbnailUrl;
  
  // Poll data
  private List<PollOption> pollOptions;
  private LocalDateTime pollEndTime;
  
  // Event data
  private LocalDateTime eventStartTime;
  private LocalDateTime eventEndTime;
  private String eventLocation;
  
  // Privacy and visibility
  private boolean isPublic = true;
  private List<String> allowedViewers;
  private List<String> tags;
}
