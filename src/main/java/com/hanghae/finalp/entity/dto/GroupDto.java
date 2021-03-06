package com.hanghae.finalp.entity.dto;

import com.hanghae.finalp.entity.Group;
import com.hanghae.finalp.entity.MemberGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;


public class GroupDto {

    @Data
    @NoArgsConstructor
    public static class SimpleRes {
        private Long groupId;
        private String imageUrl;
        private String groupTitle;
        private String roughAddress;
        private int memberCount;
        private int maxMemberCount;
        private String firstDay;
        private String lastDay;
        private String description;

        public SimpleRes(MemberGroup gm) {
            this.groupId = gm.getGroup().getId();
            this.imageUrl = gm.getGroup().getImageUrl();
            this.groupTitle = gm.getGroup().getGroupTitle();
            this.roughAddress = gm.getGroup().getRoughAddress();
            this.memberCount = gm.getGroup().getMemberCount();
            this.maxMemberCount = gm.getGroup().getMaxMemberCount();
            this.firstDay = gm.getGroup().getFirstDay();
            this.lastDay = gm.getGroup().getLastDay();
            this.description = gm.getGroup().getDescription();
        }

        public SimpleRes(Group group) {
            this.groupId = group.getId();
            this.imageUrl = group.getImageUrl();
            this.groupTitle = group.getGroupTitle();
            this.roughAddress = group.getRoughAddress();
            this.memberCount = group.getMemberCount();
            this.maxMemberCount = group.getMaxMemberCount();
            this.firstDay = group.getFirstDay();
            this.lastDay = group.getLastDay();
            this.description = group.getDescription();
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecificRes {
        private Long groupId;
        private String imageUrl;
        private String groupTitle;
        private String roughAddress;
        private int memberCount;
        private int maxMemberCount;
        private String firstDay;
        private String lastDay;
        private String description;
        private List<MemberDto.SpecificRes> memberList;

        public SpecificRes(Group group, List<MemberDto.SpecificRes> memberList) {
            this.groupId = group.getId();
            this.imageUrl = group.getImageUrl();
            this.groupTitle = group.getGroupTitle();
            this.roughAddress = group.getRoughAddress();
            this.memberCount = group.getMemberCount();
            this.maxMemberCount = group.getMaxMemberCount();
            this.firstDay = group.getFirstDay();
            this.lastDay = group.getLastDay();
            this.description = group.getDescription();
            this.memberList = memberList;
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReq {
        @NotBlank(message = "????????? ????????? ????????? ?????????.")
        private String groupTitle;

        private String description;

//        @ColumnDefault("30") ?????? ????????? ??????
        @NotNull
        @Max(value = 100, message = "????????? ?????? ???????????? 100??? ?????????.")
        @Min(value = 2, message = "????????? ?????? ???????????? 2??? ?????????.")
        private int maxMemberCount;

        private String roughAddress;

        //???????????? ????????? ?????????????????? ??????????????? ?????? ???????????? ?????? ???????????? patchReq???????????????
        @NotBlank(message = "YYYY.MM.DD ????????? ?????? ????????? ?????????.")
        @Pattern(regexp = "^([12]\\d{3}.(0[1-9]|1[0-2]).(0[1-9]|[12]\\d|3[01]))$")
        private String firstDay;

        @NotBlank(message = "YYYY.MM.DD ????????? ?????? ????????? ?????????.")
        @Pattern(regexp = "^([12]\\d{3}.(0[1-9]|1[0-2]).(0[1-9]|[12]\\d|3[01]))$")
        private String lastDay;
    }
}
