package com.demo.identity_service.mapper;

import com.demo.identity_service.dto.request.PermissionRequest;
import com.demo.identity_service.dto.request.RoleRequest;
import com.demo.identity_service.dto.response.PermissionResponse;
import com.demo.identity_service.dto.response.RoleResponse;
import com.demo.identity_service.entity.Permission;
import com.demo.identity_service.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request); //Xem ở đoạn 42:08
    RoleResponse toRoleResponse(Role role);
}
