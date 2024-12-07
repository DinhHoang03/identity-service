package com.demo.identity_service.mapper;

import com.demo.identity_service.dto.request.UserCreationRequest;
import com.demo.identity_service.dto.request.UserUpdateRequest;
import com.demo.identity_service.dto.response.UserResponse;
import com.demo.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
