package com.demo.identity_service.controller;

import com.demo.identity_service.dto.request.PermissionRequest;
import com.demo.identity_service.dto.response.APIResponse;
import com.demo.identity_service.dto.response.PermissionResponse;
import com.demo.identity_service.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    APIResponse<PermissionResponse> create(@RequestBody PermissionRequest request){
        return APIResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    APIResponse<List<PermissionResponse>> getAll(){
        return APIResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permissionId}")
    APIResponse delete(@PathVariable("permissionId") String permissionId){
        permissionService.delete(permissionId);
        return APIResponse.builder()
                .message("Delete success!")
                .build();
    }
}
