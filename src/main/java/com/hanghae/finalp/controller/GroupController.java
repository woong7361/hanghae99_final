package com.hanghae.finalp.controller;

import com.hanghae.finalp.config.security.PrincipalDetails;
import com.hanghae.finalp.entity.dto.GroupDto;
import com.hanghae.finalp.entity.dto.other.ResultMsg;
import com.hanghae.finalp.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 내 그룹리스트 받아오기
     */
    @Operation(summary = "내 그룹리스트 받아오기", description = "내 그룹리스트 받아오기")
    @GetMapping("/api/groups/my")
    public Slice<GroupDto.SimpleRes> myGroupList(@AuthenticationPrincipal PrincipalDetails principalDetails, Pageable pageable) {
        return groupService.getMyGroupList(principalDetails.getPrincipal().getMemberId(), pageable);
    }

    /**
     * 스터디 그룹 생성
     */
    @Operation(summary = "스터디 그룹 생성", description = "스터디 그룹 생성")
    @PostMapping("/api/groups")
    public GroupDto.SimpleRes createGroup(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid GroupDto.CreateReq createReq,
            @RequestPart(value = "image", required = false) MultipartFile multipartFile
    ) {
        return groupService.createGroup(principalDetails.getMemberId(), createReq, multipartFile);
    }

    /**
     * 스터디 그룹 삭제
     */
    @Operation(summary = "스터디 그룹 삭제", description = "스터디 그룹 삭제")
    @PostMapping("/api/groups/{groupId}/delete")
    public ResultMsg deleteGroup(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable("groupId") Long groupId) {
        groupService.deleteGroup(principalDetails.getMemberId(), groupId);
        return new ResultMsg("success");
    }

    /**
     * 스터디 그룹 수정
     */
    @Operation(summary = "스터디 그룹 수정", description = "스터디 그룹 수정")
    @PostMapping("/api/groups/{groupId}/patch")
    public ResultMsg patchReq(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid GroupDto.CreateReq createReq,
            @RequestPart(value = "image", required = false) MultipartFile multipartFile,
            @PathVariable("groupId") Long groupId
    ) {
        groupService.patchGroup(principalDetails.getMemberId(), groupId, createReq, multipartFile);
        return new ResultMsg("success");
    }


    /**
     * 스터디 그룹 검색
     */
    @Operation(summary = "스터디 그룹 검색", description = "스터디 그룹 검색")
    @GetMapping("/api/groups/list")
    public Slice<GroupDto.SimpleRes> getGroupList(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "roughAddress", required = false) List<String> addressList,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) { //page=1상태로오면 1빼줘야함

        return groupService.getSearchGroupList(title, addressList, pageable);
    }


    /**
    * 특정 그룹
    */
    @Operation(summary = "특정 그룹 보기", description = "특정 그룹 보기")
    @GetMapping("/api/groups/{groupId}")
    public GroupDto.SpecificRes groupView(@PathVariable("groupId") Long groupId){
        return groupService.groupView(groupId);
    }

}
