package com.demo.identity_service.controller;

import com.demo.identity_service.dto.request.RoleRequest;
import com.demo.identity_service.dto.response.APIResponse;
import com.demo.identity_service.dto.response.RoleResponse;
import com.demo.identity_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    APIResponse<RoleResponse> create(@RequestBody RoleRequest request){
        return APIResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    APIResponse<List<RoleResponse>> getAll(){
        return APIResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{roleId}")
    APIResponse delete(@PathVariable("roleId") String roleId){
        roleService.delete(roleId);
        return APIResponse.builder()
                .message("Delete success!")
                .build();
    }
}
