package com.demo.identity_service.service;

import com.demo.identity_service.dto.request.UserCreationRequest;
import com.demo.identity_service.dto.request.UserUpdateRequest;
import com.demo.identity_service.dto.response.UserResponse;
import com.demo.identity_service.entity.User;
import com.demo.identity_service.exception.AppException;
import com.demo.identity_service.exception.ErrorCode;
import com.demo.identity_service.mapper.UserMapper;
import com.demo.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public User createUser(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        //Pass data from dto to entity using mapper
        User user = userMapper.toUser(request);

        //Save the object into database through repository
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public UserResponse findUserById(String id){
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUserById(String userId){
        userRepository.deleteById(userId);
    }
}
