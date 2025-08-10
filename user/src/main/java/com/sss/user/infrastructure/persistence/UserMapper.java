package com.sss.user.infrastructure.persistence;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
import com.sss.user.domain.enumeration.UserType;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface UserMapper {

  // Map từ domain → document
  @Mapping(target = "id", source = "id", qualifiedByName = "mapUserIdToString")
  @Mapping(target = "userType", source = "userType", qualifiedByName = "mapUserTypeToString")
  UserDocument toDocument(User user);

  // Map từ document → domain
  @Mapping(target = "id", source = "id", qualifiedByName = "mapStringToUserId")
  @Mapping(target = "userType", source = "userType", qualifiedByName = "mapStringToUserType")
  User toDomain(UserDocument doc);

  // ----------- Custom Mapping Methods -----------

  @Named("mapUserIdToString")
  static String mapUserIdToString(UserId id) {
    return id == null ? null : id.value();
  }

  @Named("mapStringToUserId")
  static UserId mapStringToUserId(String id) {
    return id == null ? null : UserId.of(id);
  }

  @Named("mapUserTypeToString")
  static String mapUserTypeToString(UserType userType) {
    return userType == null ? null : userType.name();
  }

  @Named("mapStringToUserType")
  static UserType mapStringToUserType(String userType) {
    return userType == null ? null : UserType.valueOf(userType);
  }
}