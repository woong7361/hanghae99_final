package com.hanghae.finalp.entity.dto;

import com.hanghae.finalp.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;

public class NoticeDto {

    @Data
    @AllArgsConstructor
    public static class Res {
        private Long noticeId;
        private String message;
        private Boolean isRead;

        public Res(Notice notice) {
            this.noticeId = notice.getId();
            this.message = notice.getMessage();
            this.isRead = notice.getIsRead();
        }
    }

    @Data
    @AllArgsConstructor
    public static class NonReadCountRes {
        long count;
    }
}
