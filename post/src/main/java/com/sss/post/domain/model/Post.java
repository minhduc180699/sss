package com.sss.post.domain.model;

import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import java.time.LocalDateTime;
import java.util.List;
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
public class Post {
  private PostId id;
  private String authorId;           // ID của người tạo bài đăng
  private String authorName;         // Tên người tạo bài đăng
  private String authorAvatar;       // Avatar người tạo bài đăng
  private String title;              // Tiêu đề bài đăng
  private String content;            // Nội dung bài đăng
  private PostType postType;         // Loại bài đăng
  private PostStatus status;         // Trạng thái bài đăng
  
  // Media content
  private List<String> imageUrls;    // Danh sách URL hình ảnh
  private List<String> videoUrls;    // Danh sách URL video
  private String thumbnailUrl;       // URL thumbnail
  
  // Engagement metrics
  private int likeCount;             // Số lượt thích
  private int commentCount;          // Số lượt bình luận
  private int shareCount;            // Số lượt chia sẻ
  private int viewCount;             // Số lượt xem
  
  // Poll data (nếu là bài đăng thăm dò)
  private List<PollOption> pollOptions;  // Các lựa chọn thăm dò
  private LocalDateTime pollEndTime;     // Thời gian kết thúc thăm dò
  
  // Event data (nếu là bài đăng sự kiện)
  private LocalDateTime eventStartTime;  // Thời gian bắt đầu sự kiện
  private LocalDateTime eventEndTime;    // Thời gian kết thúc sự kiện
  private String eventLocation;          // Địa điểm sự kiện
  
  // Privacy and visibility
  private boolean isPublic;          // Công khai hay riêng tư
  private List<String> allowedViewers;   // Danh sách người được phép xem (nếu riêng tư)
  private List<String> tags;         // Tags của bài đăng
  
  // Metadata
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime publishedAt;
  private String createdBy;
  private String updatedBy;
  
  // Moderation
  private boolean isModerated;       // Đã được kiểm duyệt chưa
  private String moderatorId;        // ID người kiểm duyệt
  private String moderationNote;     // Ghi chú kiểm duyệt
  private LocalDateTime moderatedAt; // Thời gian kiểm duyệt
  
  public void incrementLikeCount() {
    this.likeCount++;
  }
  
  public void decrementLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }
  
  public void incrementCommentCount() {
    this.commentCount++;
  }
  
  public void decrementCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }
  
  public void incrementShareCount() {
    this.shareCount++;
  }
  
  public void incrementViewCount() {
    this.viewCount++;
  }
  
  public void publish() {
    this.status = PostStatus.PUBLISHED;
    this.publishedAt = LocalDateTime.now();
  }
  
  public void archive() {
    this.status = PostStatus.ARCHIVED;
    this.updatedAt = LocalDateTime.now();
  }
  
  public void delete() {
    this.status = PostStatus.DELETED;
    this.updatedAt = LocalDateTime.now();
  }
  
  public boolean isPublished() {
    return this.status == PostStatus.PUBLISHED;
  }
  
  public boolean isDraft() {
    return this.status == PostStatus.DRAFT;
  }
  
  public boolean isDeleted() {
    return this.status == PostStatus.DELETED;
  }
  
  public boolean isPoll() {
    return this.postType == PostType.POLL;
  }
  
  public boolean isEvent() {
    return this.postType == PostType.EVENT;
  }
  
  public boolean isExpired() {
    if (isPoll() && pollEndTime != null) {
      return LocalDateTime.now().isAfter(pollEndTime);
    }
    return false;
  }
}
