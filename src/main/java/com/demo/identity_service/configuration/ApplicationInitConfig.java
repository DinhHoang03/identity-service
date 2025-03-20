package com.demo.identity_service.configuration;

import com.demo.identity_service.entity.InvalidatedToken;
import com.demo.identity_service.entity.User;
import com.demo.identity_service.enums.Role;
import com.demo.identity_service.repository.InvalidatedTokenRepository;
import com.demo.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Bean
    ApplicationRunner createAdmin(UserRepository userRepository){
        return args -> {
          if(userRepository.findByUsername("admin").isEmpty()){
              var roles = new HashSet<String>();
              roles.add(Role.ADMIN.name());
              User user = User.builder()
                      .username("admin")
                      .password(passwordEncoder.encode("admin"))
                      //.roles(roles)
                      .build();

              userRepository.save(user);
              log.warn("Admin user has been created with default password: admin, please change it");
          }
        };
    }

//    @Scheduled(cron = "0 0 * * * ?")
//    public void cleanExpiredTokens(){
//        long oneHourInMillis = 60 * 60 * 1000; //60 min * 60s * 1000ms
//        Date expiredDate = new Date(System.currentTimeMillis() - oneHourInMillis);
//        invalidatedTokenRepository.deleteByExpirationTimeBefore(expiredDate);
//    }
}
