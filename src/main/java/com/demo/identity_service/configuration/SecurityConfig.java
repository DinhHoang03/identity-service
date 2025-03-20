package com.demo.identity_service.configuration;

import com.demo.identity_service.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

/**
 * Cấu hình bảo mật cho ứng dụng
 * - Định nghĩa các endpoint công khai/riêng tư
 * - Cấu hình xác thực bằng JWT
 * - Cấu hình mã hóa mật khẩu
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    /**
     * Danh sách các endpoint không yêu cầu xác thực
     * - /users: đăng ký user mới
     * - /auth/token: đăng nhập
     * - /auth/introspect: kiểm tra token
     * - /auth/logout: đăng xuất
     * - /auth/refresh: làm mới token
     */
    private final String[] PUBLIC_ENDPOINTS =
            {"/users",
            "/auth/token",
            "/auth/introspect",
            "/auth/logout",
            "/auth/refresh"};

    /**
     * Cấu hình bộ lọc bảo mật
     * @param httpSecurity đối tượng cấu hình
     * @return chuỗi bộ lọc đã cấu hình
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // Cấu hình quyền truy cập các endpoint
        httpSecurity.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                authorizationManagerRequestMatcherRegistry
                        // Cho phép truy cập tự do các PUBLIC_ENDPOINTS với method POST
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        // Các request khác yêu cầu xác thực
                        .anyRequest().authenticated());

        // Cấu hình xác thực bằng JWT
        httpSecurity.oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer ->
                httpSecurityOAuth2ResourceServerConfigurer.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(customJWTDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JWTAuthenticationEntryPoint())
        );

        // Tắt CSRF vì dùng JWT
        httpSecurity.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());

        return httpSecurity.build();
    }

    // Khóa bí mật để verify JWT
    @Value("${jwt.signerKey}")
    private String signerKey;

    // JWT Decoder tùy chỉnh để verify token
    @Autowired
    private CustomJWTDecoder customJWTDecoder;

    /**
     * Cấu hình chuyển đổi JWT thành Authentication object
     * @return converter đã cấu hình
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        // Cấu hình cách trích xuất quyền từ JWT
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Bỏ prefix mặc định "SCOPE_" khỏi tên quyền
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * Cấu hình encoder để mã hóa mật khẩu
     * @return BCryptPasswordEncoder với độ mạnh = 10
     */
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}