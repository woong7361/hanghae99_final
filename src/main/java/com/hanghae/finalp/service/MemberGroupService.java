package com.hanghae.finalp.service;

import com.hanghae.finalp.config.exception.customexception.DuplicationRequestException;
import com.hanghae.finalp.config.exception.customexception.authority.AuthorJoinException;
import com.hanghae.finalp.config.exception.customexception.authority.AuthorOwnerException;
import com.hanghae.finalp.config.exception.customexception.authority.AuthorWaitException;
import com.hanghae.finalp.config.exception.customexception.authority.AuthorityException;
import com.hanghae.finalp.config.exception.customexception.entity.EntityNotExistException;
import com.hanghae.finalp.config.exception.customexception.MaxNumberException;
import com.hanghae.finalp.config.exception.customexception.entity.GroupNotExistException;
import com.hanghae.finalp.config.exception.customexception.entity.MemberGroupNotExistException;
import com.hanghae.finalp.entity.ChatMember;
import com.hanghae.finalp.entity.Chatroom;
import com.hanghae.finalp.entity.Group;
import com.hanghae.finalp.entity.MemberGroup;
import com.hanghae.finalp.entity.mappedsuperclass.Authority;
import com.hanghae.finalp.repository.ChatMemberRepository;
import com.hanghae.finalp.repository.ChatRoomRepository;
import com.hanghae.finalp.repository.GroupRepository;
import com.hanghae.finalp.repository.MemberGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MemberGroupService {

    private final ChatMemberRepository chatMemberRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final GroupRepository groupRepository;
    private final ChatRoomRepository chatRoomRepository;

    //그룹 참가 신청
    @Transactional
    public void applyGroup(Long memberId, Long groupId) {
        //멤버가 이미 해당 그룹에 속해있는지 확인하기 -> memberGroup에 memberId, groupId 동시에 있는지 확인하면됨

        //중복된 요청입니다
        memberGroupRepository.findByMemberIdAndGroupId(memberId, groupId)
                .ifPresent((mg) -> {throw new DuplicationRequestException("apply group");});

        //WAIT으로 memberGroup을 생성 -chatroodId는 승인시 따로 넣어줄 예정
        MemberGroup newMemberGroup = MemberGroup.createMemberGroup(Authority.WAIT, memberId, groupId, null);
        memberGroupRepository.save(newMemberGroup);
//        Group group= groupRepository.findById(groupId).orElseThrow(
//                GroupNotExistException::new);
//        group.getMemberGroups().add(newMemberGroup);
    }



    //그룹 참가자 승인
    @Transactional
    public void approveGroup(Long myMemberId, Long groupId, Long memberId) {

        log.debug("custom log:: owner's memberGroup 확인");
        //내가 속했으며, 승인을 요청한 멤버그룹을 찾는다
        MemberGroup myMemberGroup = memberGroupRepository.findByMemberIdAndGroupId(myMemberId, groupId)
                .orElseThrow(MemberGroupNotExistException::new);

        //그리고 내 auth를 확인 -> 만약 내가 그 멤버그룹의 오너가 아니라면
        if(!Authority.OWNER.equals(myMemberGroup.getAuthority())) throw new AuthorOwnerException();

        log.debug("custom log:: target's memberGroup 확인");
        //그사람도 같은 멤버그룹에서 대기중인지 확인 & 권한 확인
        MemberGroup yourMemberGroup= memberGroupRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(MemberGroupNotExistException::new);

        if(!Authority.WAIT.equals(yourMemberGroup.getAuthority())) throw new AuthorOwnerException();

        log.debug("custom log:: 그룹의 최대 인원수 확인");
        if (!(yourMemberGroup.getGroup().getMemberCount() < yourMemberGroup.getGroup().getMaxMemberCount())) {
            throw new MaxNumberException();
        }

        yourMemberGroup.setAuthority(Authority.JOIN); //wait일 경우 join으로 바꿔줌
        yourMemberGroup.getGroup().plusMemberCount();

        log.debug("custom log:: chatroom 관련 logic");
        //승인 전에 안넣어줬던 챗룸아이디를 멤버그룹에 넣어준 후
        yourMemberGroup.setChatroomId(myMemberGroup.getChatroomId());
        //조인이 되는 순간 채팅방도 가입시켜줘야 된다 => 챗멤버 생성필요
        Chatroom chatroom = chatRoomRepository.findById(myMemberGroup.getChatroomId()).orElseThrow(
                EntityNotExistException::new);
        ChatMember chatMember = ChatMember.createChatMember(yourMemberGroup.getMember(), chatroom);
        chatroom.getChatMembers().add(chatMember);


    }

    //그룹 참가자 거절
    @Transactional
    public void denyGroup(Long myMemberId, Long groupId, Long memberId) {

        log.debug("custom log:: owner's memberGroup 확인");
        //내가 속했으며, 승인을 요청한 멤버그룹을 찾는다
        MemberGroup myMemberGroup = memberGroupRepository.findByMemberIdAndGroupId(myMemberId, groupId).orElseThrow(
                MemberGroupNotExistException::new);

        //그리고 내 auth를 확인 -> 만약 내가 그 멤버그룹의 오너가 아니라면
        if(!Authority.OWNER.equals(myMemberGroup.getAuthority())){
            throw new AuthorOwnerException();
        }

        log.debug("custom log:: target's memberGroup 확인");
        MemberGroup yourMemberGroup= memberGroupRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(
                MemberGroupNotExistException::new);

        //그사람의 auth 확인 ->그사람의 권한이 wait일 경우
        if(Authority.WAIT.equals(yourMemberGroup.getAuthority())) {
            throw new AuthorOwnerException();
        }

        memberGroupRepository.delete(yourMemberGroup);
//        Group group = groupRepository.findById(groupId).orElseThrow(
//                GroupNotExistException::new);
//        group.getMemberGroups().remove(yourMemberGroup);
    }



    //그룹 참가자 추방
    @Transactional
    public void banGroup(Long myMemberId, Long groupId, Long memberId) {
        log.debug("custom log:: owner's memberGroup 확인");
        //내가 속했으며, 추방할 멤버그룹을 찾는다
        MemberGroup myMemberGroup = memberGroupRepository.findByMemberIdAndGroupId(myMemberId, groupId)
                .orElseThrow(MemberGroupNotExistException::new);

        //그리고 내 auth를 확인 -> 만약 내가 그 멤버그룹의 오너가 아니라면
        if (!Authority.OWNER.equals(myMemberGroup.getAuthority())) {
            throw new AuthorOwnerException();
        }

        log.debug("custom log:: target's memberGroup 확인");
        //그사람도 같은 멤버그룹에 속했는지 확인
        MemberGroup yourMemberGroup = memberGroupRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(MemberGroupNotExistException::new);

        //그사람의 auth 확인 ->그사람의 권한이 join일 경우
        if (Authority.JOIN.equals(yourMemberGroup.getAuthority())) {
            throw new AuthorOwnerException();
        }

        log.debug("custom log:: chatroom 관련 logic");
        //1.채팅룸에서 드랍 => 1-1.챗멤버를 없애줘야함
        ChatMember chatMember = chatMemberRepository.findByMemberIdAndChatroomId(memberId, yourMemberGroup.getChatroomId())
                .orElseThrow(EntityNotExistException::new);
        chatMemberRepository.delete(chatMember);
        //1-2. 채팅룸에서도 챗멤버를 없애 줘야함.
        Chatroom chatroom = chatRoomRepository.findById(yourMemberGroup.getChatroomId())
                .orElseThrow(EntityNotExistException::new);
        chatroom.getChatMembers().remove(chatMember);

        //2.멤버그룹 삭제
        memberGroupRepository.delete(yourMemberGroup);

//        Group group = groupRepository.findById(groupId).orElseThrow(
//                GroupNotExistException::new);
//        group.getMemberGroups().remove(yourMemberGroup);
        yourMemberGroup.getGroup().minusMemberCount();

    }


    @Transactional
    public void cancelApplyGroup(Long memberId, Long groupId) {

        log.debug("custom log:: target's memberGroup 확인");
        MemberGroup memberGroup = memberGroupRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(MemberGroupNotExistException::new);

        if (!memberGroup.getAuthority().equals(Authority.WAIT)) {
            throw new AuthorWaitException();
        }

        memberGroupRepository.delete(memberGroup);
    }

    @Transactional
    public void exitGroup(Long memberId, Long groupId) {
        log.debug("custom log:: target's memberGroup 확인");
        MemberGroup memberGroup = memberGroupRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(MemberGroupNotExistException::new);

        if (!memberGroup.getAuthority().equals(Authority.JOIN)) {
            throw new AuthorJoinException();
        }

        log.debug("custom log:: chatroom 관련 logic");
        ChatMember chatMember = chatMemberRepository.findByMemberIdAndChatroomId(memberId, memberGroup.getChatroomId())
                .orElseThrow(EntityNotExistException::new);
        chatMemberRepository.delete(chatMember);
        memberGroup.getGroup().minusMemberCount();

        memberGroupRepository.delete(memberGroup);
    }



}
