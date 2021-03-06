package com.hanghae.finalp.entity;

import com.hanghae.finalp.entity.mappedsuperclass.TimeStamped;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Member extends TimeStamped {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String kakaoId;
    private String username;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ChatMember> chatMembers = new ArrayList<>();


    //========================================생성자=============================================//
    private Member(String kakaoId, String username, String imageUrl) {
        this.kakaoId = kakaoId;
        this.username = username;
        this.imageUrl = imageUrl;
    }

    private Member(Long id) {
        this.id = id;
    }

    //========================================생성 편의자=============================================//

    public static Member createMember(String kakaoId, String username, String imageUrl) {
        return new Member(kakaoId, username, imageUrl);
    }

    public static Member createMappingMember(Long memberId) {
        return new Member(memberId);
    }

    //========================================비즈니스 로직==============================================//

    public void patchMember(String username, String imageUrl) {
        this.username = username;
        this.imageUrl = imageUrl;
    }

}
