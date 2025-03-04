package com.demo.identity_service.service;

import com.demo.identity_service.dto.request.UserCreationRequest;
import com.demo.identity_service.dto.request.UserUpdateRequest;
import com.demo.identity_service.dto.response.UserResponse;
import com.demo.identity_service.entity.User;
import com.demo.identity_service.enums.Role;
import com.demo.identity_service.exception.AppException;
import com.demo.identity_service.exception.ErrorCode;
import com.demo.identity_service.mapper.UserMapper;
import com.demo.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public User createUser(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        //Pass data from dto to entity using mapper
        User user = userMapper.toUser(request);

        /**Truyền độ mạnh của thuật toán mã hóa từ 4->31 (Mặc định là 10), vì nó sẽ liên quan đến performance của hệ thống */
        //PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        //Bắt đầu tiến hành mã hóa password của user từ request của dto về entity sử dụng hàm encode
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        //Save the object into database through repository
        return userRepository.save(user);
    }

    /**Create user using Builder annotation (@Builder)*/
    public User createUser2(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserCreationRequest request1 = new UserCreationRequest().builder()
                .username("Long")
                .password("12345")
                .firstName("Long")
                .lastName("Trinh")
                .dob(LocalDate.of(2003, 04, 21))
                .build();

        return userMapper.toUser(request1);
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers(){
        log.info("In method get Users");
        return userRepository.findAll();
    }

    @PostAuthorize("returnObject.username == authentication.name")
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
