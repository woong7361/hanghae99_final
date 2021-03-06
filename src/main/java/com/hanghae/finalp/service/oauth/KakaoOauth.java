package com.hanghae.finalp.service.oauth;


import com.hanghae.finalp.config.security.PrincipalDetails;
import com.hanghae.finalp.config.security.kakao.KakaoProfile;
import com.hanghae.finalp.config.security.kakao.OAuthToken;
import com.hanghae.finalp.entity.Member;
import com.hanghae.finalp.entity.dto.MemberDto;
import com.hanghae.finalp.repository.MemberRepository;
import com.hanghae.finalp.util.JwtTokenUtils;
import com.hanghae.finalp.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class KakaoOauth {

    private final MemberRepository memberRepository;
    private final RedisUtils redisUtils;
    private final JwtTokenUtils jwtTokenUtils;

    @Value("${spring.security.oauth2.client.registration.Kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.Kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.Kakao.authorization-grant-type}")
    private String authorizationGrantType;
    @Value("${spring.security.oauth2.client.registration.Kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.Kakao.authorization-uri}")
    private String authorizationUri;
    @Value("${spring.security.oauth2.client.provider.Kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.Kakao.user-info-uri}")
    private String userInfoUri;

    @Transactional
    public MemberDto.LoginRes login(String providerName, String code) {

        OAuthToken tokenResponse = getTokenFromKakao(code); //provider??? ???????????? ???????????? OAuthToken ?????????
        KakaoProfile kakaoProfile = getUserProfileFromKakao(providerName, tokenResponse); //????????? ????????? ???????????? ??????

        MemberDto.LoginRes response = saveMember(providerName, kakaoProfile);//?????? ??????

        String accessToken = jwtTokenUtils.
                createAccessToken(response.getMember().getMemberId(), response.getMember().getUsername());
        String refreshToken = jwtTokenUtils.createRefreshToken(response.getMember().getMemberId());

        //hardcoding need refactoring
        redisUtils.setRefreshTokenDataExpire(
                String.valueOf(response.getMember().getMemberId()),
                refreshToken, jwtTokenUtils.getRefreshTokenExpireTime(refreshToken)
        );

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }



    //==============================================================================================//

    private OAuthToken getTokenFromKakao(String code) {
        OAuthToken oAuthToken = WebClient.create()
                .post()
                .uri(tokenUri)
                .headers(header -> {
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(tokenReqeust(code))
                .retrieve()
                .onStatus(
                        HttpStatus.BAD_REQUEST::equals,
                        response -> response.bodyToMono(String.class).map(Exception::new))
                .bodyToMono(OAuthToken.class)
                .block();

        return oAuthToken;
    }

    private MultiValueMap<String, String> tokenReqeust(String code) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", authorizationGrantType);
        formData.add("redirect_uri", redirectUri);
//        formData.add("client_secret", provider.getClientSecret());
        formData.add("client_id", clientId);
        return formData;
    }

    //????????? ????????? ??????
    private KakaoProfile getUserProfileFromKakao(String providerName, OAuthToken tokenResponse) {

        return WebClient.create()
                .get()
                .uri(userInfoUri)
                .headers(header -> header.setBearerAuth(tokenResponse.getAccess_token()))
                .retrieve()
                .bodyToMono(KakaoProfile.class)
                .block();
    }

    private MemberDto.LoginRes saveMember(String providerName, KakaoProfile kakaoProfile) {
        String kakaoId = kakaoProfile.getId() + "_" + providerName; //??????????????? ????????? ???????????? kakaoId???.
        String username = kakaoProfile.getProperties().getNickname();  //??????????????? ????????? nickname??? username??????.
        String imageUrl = kakaoProfile.getProperties().getProfile_image(); //??????????????? ????????? Profile_image??? imageUrl???.


        //????????? db??? ?????? ?????? ?????? ???????????? ??????, ?????? ?????? db??? ???????????? ????????????.
        Long memberId = null;
        Boolean isFirst = null;
        Optional<Member> member = memberRepository.findByKakaoId(kakaoId);
        if(member.isPresent()){ //db??? ????????????????????? ????????? ????????????????????? ??????, memberId??? ?????????.
            memberId = member.get().getId();
            isFirst = false;
        } else{ //db??? ????????????????????? ????????? member??? ???????????????.
            Member newMember = memberRepository.save(Member.createMember(kakaoId, username, imageUrl));
            memberId = newMember.getId();
            isFirst = true;
        }
        PrincipalDetails principalDetails = new PrincipalDetails(memberId, username); //principalDetails??? ????????????
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new MemberDto.LoginRes(new MemberDto.MemberRes(memberId, username, imageUrl), isFirst);
    }

}
