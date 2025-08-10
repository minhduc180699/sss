package com.sss.post.config;

import com.sss.post.domain.enumeration.PostStatus;
import com.sss.post.domain.enumeration.PostType;
import com.sss.post.domain.model.Post;
import com.sss.post.domain.model.PostId;
import com.sss.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoDataInitializer implements CommandLineRunner {
  
  private final PostRepository postRepository;
  
  @Override
  public void run(String... args) throws Exception {
    log.info("Initializing sample post data...");
    
    // Check if data already exists
    if (postRepository.findAll().isEmpty()) {
      createSamplePosts();
      log.info("Sample post data initialized successfully!");
    } else {
      log.info("Post data already exists, skipping initialization.");
    }
  }
  
  private void createSamplePosts() {
    // Sample post 1: Text post
    Post textPost = Post.builder()
        .id(new PostId(UUID.randomUUID().toString()))
        .authorId("user-1")
        .authorName("Nguyễn Văn A")
        .authorAvatar("https://example.com/avatar1.jpg")
        .title("Chào mừng đến với SSS!")
        .content("Đây là bài đăng đầu tiên của tôi trên nền tảng SSS. Tôi rất vui được tham gia cộng đồng này!")
        .postType(PostType.TEXT)
        .status(PostStatus.PUBLISHED)
        .isPublic(true)
        .tags(Arrays.asList("chào mừng", "giới thiệu"))
        .likeCount(15)
        .commentCount(5)
        .shareCount(2)
        .viewCount(120)
        .createdAt(LocalDateTime.now().minusDays(2))
        .updatedAt(LocalDateTime.now().minusDays(2))
        .publishedAt(LocalDateTime.now().minusDays(2))
        .createdBy("user-1")
        .build();
    
    // Sample post 2: Image post
    Post imagePost = Post.builder()
        .id(new PostId(UUID.randomUUID().toString()))
        .authorId("user-2")
        .authorName("Trần Thị B")
        .authorAvatar("https://example.com/avatar2.jpg")
        .title("Ảnh đẹp cuối tuần")
        .content("Chia sẻ một số ảnh đẹp tôi chụp được trong chuyến đi cuối tuần vừa rồi!")
        .postType(PostType.IMAGE)
        .status(PostStatus.PUBLISHED)
        .imageUrls(Arrays.asList(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg"
        ))
        .thumbnailUrl("https://example.com/thumbnail1.jpg")
        .isPublic(true)
        .tags(Arrays.asList("ảnh đẹp", "du lịch", "cuối tuần"))
        .likeCount(45)
        .commentCount(12)
        .shareCount(8)
        .viewCount(320)
        .createdAt(LocalDateTime.now().minusDays(1))
        .updatedAt(LocalDateTime.now().minusDays(1))
        .publishedAt(LocalDateTime.now().minusDays(1))
        .createdBy("user-2")
        .build();
    
    // Sample post 3: Poll post
    Post pollPost = Post.builder()
        .id(new PostId(UUID.randomUUID().toString()))
        .authorId("user-3")
        .authorName("Lê Văn C")
        .authorAvatar("https://example.com/avatar3.jpg")
        .title("Thăm dò: Bạn thích loại phim nào nhất?")
        .content("Hãy cho tôi biết bạn thích loại phim nào nhất trong các lựa chọn dưới đây!")
        .postType(PostType.POLL)
        .status(PostStatus.PUBLISHED)
        .pollOptions(Arrays.asList(
            com.sss.post.domain.model.PollOption.builder()
                .id(UUID.randomUUID().toString())
                .text("Hành động")
                .voteCount(25)
                .build(),
            com.sss.post.domain.model.PollOption.builder()
                .id(UUID.randomUUID().toString())
                .text("Tình cảm")
                .voteCount(18)
                .build(),
            com.sss.post.domain.model.PollOption.builder()
                .id(UUID.randomUUID().toString())
                .text("Hài hước")
                .voteCount(32)
                .build(),
            com.sss.post.domain.model.PollOption.builder()
                .id(UUID.randomUUID().toString())
                .text("Kinh dị")
                .voteCount(8)
                .build()
        ))
        .pollEndTime(LocalDateTime.now().plusDays(7))
        .isPublic(true)
        .tags(Arrays.asList("thăm dò", "phim ảnh", "sở thích"))
        .likeCount(28)
        .commentCount(15)
        .shareCount(5)
        .viewCount(180)
        .createdAt(LocalDateTime.now().minusHours(6))
        .updatedAt(LocalDateTime.now().minusHours(6))
        .publishedAt(LocalDateTime.now().minusHours(6))
        .createdBy("user-3")
        .build();
    
    // Sample post 4: Event post
    Post eventPost = Post.builder()
        .id(new PostId(UUID.randomUUID().toString()))
        .authorId("user-4")
        .authorName("Phạm Thị D")
        .authorAvatar("https://example.com/avatar4.jpg")
        .title("Sự kiện: Gặp gỡ cộng đồng SSS")
        .content("Chúng ta sẽ có một buổi gặp gỡ cộng đồng SSS vào cuối tuần này. Hãy tham gia cùng chúng tôi!")
        .postType(PostType.EVENT)
        .status(PostStatus.PUBLISHED)
        .eventStartTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0))
        .eventEndTime(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0))
        .eventLocation("Café ABC, 123 Đường XYZ, Quận 1, TP.HCM")
        .isPublic(true)
        .tags(Arrays.asList("sự kiện", "cộng đồng", "gặp gỡ"))
        .likeCount(67)
        .commentCount(23)
        .shareCount(15)
        .viewCount(450)
        .createdAt(LocalDateTime.now().minusHours(12))
        .updatedAt(LocalDateTime.now().minusHours(12))
        .publishedAt(LocalDateTime.now().minusHours(12))
        .createdBy("user-4")
        .build();
    
    // Sample post 5: Story post
    Post storyPost = Post.builder()
        .id(new PostId(UUID.randomUUID().toString()))
        .authorId("user-5")
        .authorName("Hoàng Văn E")
        .authorAvatar("https://example.com/avatar5.jpg")
        .title("Câu chuyện về chú mèo của tôi")
        .content("Hôm nay tôi muốn kể cho các bạn nghe câu chuyện về chú mèo của tôi. Chú ấy tên là Miu và rất thông minh...")
        .postType(PostType.STORY)
        .status(PostStatus.PUBLISHED)
        .imageUrls(Arrays.asList("https://example.com/cat1.jpg"))
        .isPublic(true)
        .tags(Arrays.asList("câu chuyện", "thú cưng", "mèo"))
        .likeCount(89)
        .commentCount(34)
        .shareCount(22)
        .viewCount(670)
        .createdAt(LocalDateTime.now().minusHours(3))
        .updatedAt(LocalDateTime.now().minusHours(3))
        .publishedAt(LocalDateTime.now().minusHours(3))
        .createdBy("user-5")
        .build();
    
    // Save all sample posts
    List<Post> samplePosts = Arrays.asList(textPost, imagePost, pollPost, eventPost, storyPost);
    samplePosts.forEach(postRepository::save);
  }
}
