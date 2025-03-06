package com.demo.identity_service.mapper;

import com.demo.identity_service.dto.request.PermissionRequest;
import com.demo.identity_service.dto.request.UserCreationRequest;
import com.demo.identity_service.dto.request.UserUpdateRequest;
import com.demo.identity_service.dto.response.PermissionResponse;
import com.demo.identity_service.dto.response.UserResponse;
import com.demo.identity_service.entity.Permission;
import com.demo.identity_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
