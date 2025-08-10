package com.sss.post.infrastructure.dto;

import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.model.PollOption;
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
public class UpdatePostRequest {
  
  private String title;
  private String content;
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
  private Boolean isPublic;
  private List<String> allowedViewers;
  private List<String> tags;
}
