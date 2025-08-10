package com.sss.post.domain.model;

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
public class PollOption {
  private String id;
  private String text;           // Nội dung lựa chọn
  private int voteCount;         // Số lượt bình chọn
  private String imageUrl;       // Hình ảnh cho lựa chọn (nếu có)
}
