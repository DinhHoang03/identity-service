package com.demo.identity_service.controller;

import com.demo.identity_service.dto.response.APIResponse;
import com.demo.identity_service.dto.request.UserCreationRequest;
import com.demo.identity_service.dto.request.UserUpdateRequest;
import com.demo.identity_service.dto.response.UserResponse;
import com.demo.identity_service.entity.User;
import com.demo.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    APIResponse<User> createUser(@RequestBody @Valid UserCreationRequest request){
        APIResponse<User> apiResponse = new APIResponse<>();

        apiResponse.setResult(userService.createUser(request));
        apiResponse.setMessage("User account is succesfully created!");

        return apiResponse;
    }

    @GetMapping
    List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId){
        return userService.findUserById(userId);
    }

    @PutMapping("/{userId}")
    UserResponse updateUser(@PathVariable("userId") String userId,@RequestBody UserUpdateRequest request){
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUserById(@PathVariable("userId") String userId){
        userService.deleteUserById(userId);
        return "User has deleted!";
    }
}