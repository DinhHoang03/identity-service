package com.demo.identity_service.service;

import com.demo.identity_service.dto.request.AuthenticationRequest;
import com.demo.identity_service.dto.request.IntrospectRequest;
import com.demo.identity_service.dto.request.LogOutRequest;
import com.demo.identity_service.dto.request.RefreshRequest;
import com.demo.identity_service.dto.response.AuthenticationResponse;
import com.demo.identity_service.dto.response.IntrospectResponse;
import com.demo.identity_service.entity.InvalidatedToken;
import com.demo.identity_service.entity.User;
import com.demo.identity_service.exception.AppException;
import com.demo.identity_service.exception.ErrorCode;
import com.demo.identity_service.repository.InvalidatedTokenRepository;
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
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Service xử lý các vấn đề liên quan đến xác thực người dùng
 * - Đăng nhập
 * - Tạo và xác thực JWT token
 * - Làm mới token
 * - Đăng xuất
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    // Repository để truy vấn thông tin user
    UserRepository userRepository;
    // Repository để quản lý các token đã vô hiệu hóa
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal //viết annotation này để tránh bị inject vào constructor
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY; //Khóa bí mật để ký JWT

    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    /**
     * Kiểm tra token có hợp lệ không
     * @param request chứa token cần kiểm tra
     * @return kết quả kiểm tra
     */
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        boolean isValid = true;

        try {
            verifyToken(token, false);
        }catch (AppException e){
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    /**
     * Xác thực JWT token
     * @param token cần xác thực
     * @return JWT đã được ký nếu hợp lệ
     * @throws JOSEException nếu có lỗi xử lý JWT
     * @throws ParseException nếu không parse được token
     */
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        // Tạo verifier với khóa bí mật
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // Parse token thành SignedJWT
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Lấy thời gian hết hạn
        Date expriredTime = (isRefresh) ?
                new Date(
                        signedJWT.getJWTClaimsSet()
                                .getIssueTime()
                                .toInstant()
                                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                                .toEpochMilli()
                )
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        // Xác thực chữ ký
        var verified = signedJWT.verify(verifier);

        // Kiểm tra token có hợp lệ và chưa hết hạn
        if(!verified && expriredTime.after(new Date())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Kiểm tra token có trong danh sách đã vô hiệu hóa không
        if(invalidatedTokenRepository.existsById(signedJWT
                .getJWTClaimsSet()
                .getJWTID())
        ){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    /**
     * Xác thực người dùng và tạo token mới
     * @param request thông tin đăng nhập
     * @return token nếu xác thực thành công
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        // Tìm user theo username
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra mật khẩu
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        // Tạo token mới
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    /**
     * Tạo JWT token cho user
     * @param user cần tạo token
     * @return JWT token dạng string
     */
    private String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); //Build header sử dụng thuật toán HS512 với lớp JWSHeader

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() //Sử dụng Lớp JWTClaimsSet Builder để set các claims bên trong payload
                .subject(user.getUsername()) //Chứa đựng username đang đăng nhập
                .issuer("identity-service.com") //Chứa domain của service
                .issueTime(new Date()) //Chứa thời gian khởi tạo token
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli() //Lấy ở thời điểm bây giờ và cộng thêm một giờ để lấy hạn, tính theo đơn vị mili trên s
                )) //Chứa thời hạn token(ở đây là một tiếng)
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
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

    /**
     * Làm mới token cũ
     * @param request chứa token cũ
     * @return token mới
     */
    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        // Xác thực token cũ
        var signJWT = verifyToken(request.getToken(), true);
        var jwtId = signJWT.getJWTClaimsSet().getJWTID();
        var expirationTime = signJWT.getJWTClaimsSet().getExpirationTime();

        // Vô hiệu hóa token cũ
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder()
                        .id(jwtId)
                        .expirationTime(expirationTime)
                        .build();

        invalidatedTokenRepository.save(invalidatedToken);

        // Tạo token mới
        var username = signJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHENTICATED)
        );

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    /**
     * Đăng xuất người dùng
     * @param request chứa token cần vô hiệu hóa
     */
    public void logout(LogOutRequest request) throws ParseException, JOSEException {
        // Xác thực token

        try {
            var signToken = verifyToken(request.getToken(), true);

            String jwtId = signToken.getJWTClaimsSet().getJWTID();
            Date expirationTime = signToken.getJWTClaimsSet().getExpirationTime();

            // Lưu token vào danh sách đã vô hiệu hóa
            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder()
                            .id(jwtId)
                            .expirationTime(expirationTime)
                            .build();

            invalidatedTokenRepository.save(invalidatedToken);
        }catch (AppException e){
            log.info("Token is not valid");
        }
    }

    /**
     * Xây dựng scope từ roles và permissions của user
     * @param user cần lấy scope
     * @return chuỗi chứa các quyền, phân tách bằng dấu cách
     */
    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");

        // Thêm roles và permissions vào scope
        if(!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(
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
