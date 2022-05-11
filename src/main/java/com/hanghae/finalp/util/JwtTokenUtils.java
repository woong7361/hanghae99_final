package com.hanghae.finalp.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hanghae.finalp.dto.LoginDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtils {

    public static final int SEC = 1000;
    public static final int MINUTE = 60 * SEC;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    public static final String TOKEN_HEADER_NAME = "Authorization";
    public static final String TOKEN_NAME_WITH_SPACE = "Bearer ";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ID = "id";

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    public DecodedJWT verifyToken(String jwtToken) {
        try {
            return JWT
                    .require(Algorithm.HMAC512(JWT_SECRET))
                    .build()
                    .verify(jwtToken);
        } catch (AlgorithmMismatchException algorithmMismatchException){
            throw new IllegalArgumentException("토큰 알고리즘 미스매칭");
        } catch (SignatureVerificationException signatureVerificationException){
            throw new IllegalArgumentException("signature verifying 에러");
        } catch (TokenExpiredException tokenExpiredException) {
            throw new TokenExpiredException("토큰 만료됨");
        } catch (InvalidClaimException invalidClaimException) {
            throw new IllegalArgumentException("토큰 클레임 에러");
        }
    }

    public String getTokenFromHeader(HttpServletRequest request) throws IllegalArgumentException {
        try {
            return request.
                    getHeader(TOKEN_HEADER_NAME).
                    replace(TOKEN_NAME_WITH_SPACE, "");
        } catch (Exception e) {
            throw new IllegalArgumentException("헤더 추출 에러");
        }
    }

    public String createAccessToken(Long memberId, String username) {
        String token = JWT.create()
                .withSubject("accessToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + (30 * MINUTE) ))
                .withClaim(CLAIM_ID, memberId)
                .withClaim(CLAIM_USERNAME, username)
                .sign(Algorithm.HMAC512(JWT_SECRET));   //secretkey
        return token;
    }

    public String createRefreshToken(Long memberId) {
        String token = JWT.create()
                .withSubject("refreshToken")
                .withExpiresAt(new Date(System.currentTimeMillis() + (14 * DAY) ))
                .withClaim(CLAIM_ID, memberId)
                .sign(Algorithm.HMAC512(JWT_SECRET));   //secretkey
        return token;
    }

    public ResponseEntity<LoginDto.Response> makeTokenResponse(String accessToken, String refreshToken) {
        return ResponseEntity.ok()
                .body(new LoginDto.Response(
                        TOKEN_NAME_WITH_SPACE + accessToken,
                        TOKEN_NAME_WITH_SPACE + refreshToken)
                );
    }

}