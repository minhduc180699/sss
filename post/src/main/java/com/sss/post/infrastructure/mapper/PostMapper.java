package com.sss.post.infrastructure.mapper;

import com.sss.post.domain.model.Post;
import com.sss.post.domain.model.PostId;
import com.sss.post.infrastructure.dto.PostResponseDto;
import com.sss.post.infrastructure.persistence.PostDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
@Mapper(componentModel = "spring")
public interface PostMapper {
  
  PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);
  
  @Mapping(target = "id.value", source = "id")
  Post toDomain(PostDocument postDocument);
  
  @Mapping(target = "id", source = "id.value")
  PostDocument toDocument(Post post);
  
  @Mapping(target = "id", source = "id.value")
  PostResponseDto toResponseDto(Post post);
}
