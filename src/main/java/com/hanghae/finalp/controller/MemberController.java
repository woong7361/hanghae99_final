package com.hanghae.finalp.controller;

import com.hanghae.finalp.config.security.PrincipalDetails;
import com.hanghae.finalp.entity.dto.MemberDto;
import com.hanghae.finalp.entity.dto.other.ResultMsg;
import com.hanghae.finalp.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //내 프로필 생성 ->로그인때 카카오아이디 받아와서, db에 멤버가 없을 경우 만들어주기때문에 프로필은 생성이 필요없다

    /**
     * 내 프로필 조회
     */
    @Operation(summary = "내 프로필 조회", description = "내 프로필 조회")
    @GetMapping("/api/profile")
    public MemberDto.ProfileRes memberGet(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return memberService.getMyProfile(principalDetails.getMemberId());
    }

    /**
     * 내 프로필 수정
     */
    @Operation(summary = "내 프로필 수정", description = "내 프로필 수정")
    @PostMapping("/api/profile/edit")
    public MemberDto.ProfileRes memberEdit(@NotBlank @RequestPart("username") String username,
                             @RequestPart(value = "image", required = false) MultipartFile file,
                             @AuthenticationPrincipal PrincipalDetails principalDetails) {

        return memberService.editMyProfile(username, file, principalDetails.getMemberId());
    }


    /**
     * 회원 탈퇴
     */
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴")
    @GetMapping("/api/withdraw")
    public ResultMsg memberDelete(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        memberService.deleteMember(principalDetails.getMemberId());
        return new ResultMsg("success");
    }

}


