package com.demo.identity_service.service;

import com.demo.identity_service.dto.request.AuthenticationRequest;
import com.demo.identity_service.dto.request.IntrospectRequest;
import com.demo.identity_service.dto.response.AuthenticationResponse;
import com.demo.identity_service.dto.response.IntrospectResponse;
import com.demo.identity_service.entity.User;
import com.demo.identity_service.exception.AppException;
import com.demo.identity_service.exception.ErrorCode;
import com.demo.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;

    @NonFinal //viết annotation này để tránh bị inject vào constructor
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY; //Khóa bảo mật

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expriredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified =  signedJWT.verify(verifier);

        var result = verified && expriredTime.after(new Date());

        return IntrospectResponse.builder()
                .valid(result)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); //Build header sử dụng thuật toán HS512 với lớp JWSHeader

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() //Sử dụng Lớp JWTClaimsSet Builder để set các claims bên trong payload
                .subject(user.getUsername()) //Chứa đựng username đang đăng nhập
                .issuer("identity-service.com") //Chứa domain của service
                .issueTime(new Date()) //Chứa thời gian khởi tạo token
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli() //Lấy ở thời điểm bây giờ và cộng thêm một giờ để lấy hạn, tính theo đơn vị mili trên s
                )) //Chứa thời hạn token(ở đây là một tiếng)
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); //Lưu trữ các claims trong biến payload

        JWSObject jwsObject = new JWSObject(header, payload); //Tổng hợp vào một jwsObject

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes())); //Kí token bằng khóa bí mật
            return jwsObject.serialize(); //Chuyển đổi đối tượng jwsObject thành một String sử dụng hàm serialize
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");

       if(!CollectionUtils.isEmpty(user.getRoles())) user.getRoles().forEach(
               role -> {
                   stringJoiner.add("ROLE_" + role.getName());
                   if(!CollectionUtils.isEmpty(role.getPermissions())){
                       role.getPermissions().forEach(permission ->
                               stringJoiner.add(permission.getName()));
                   }
               }
       );
        return stringJoiner.toString();
    }
}
