package com.sss.post.infrastructure.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollOptionDto {
  
  private String id;
  private String text;
  private int voteCount;
  private String imageUrl;
}
