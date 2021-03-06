package com.hanghae.finalp.controller;

import com.hanghae.finalp.entity.dto.MessageDto;
import com.hanghae.finalp.entity.mappedsuperclass.MessageType;
import com.hanghae.finalp.service.ChatService;
import com.hanghae.finalp.service.MessageService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Hidden
public class MessageController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;
    private final ChatService chatService;
    private final MessageService messageService;

    /**
     * webSocket  --  /pub/channel 으로 들어온 메시징 처리
     * 메시지 보내기
     */
    @MessageMapping("/channel")
    public void redisMessage(MessageDto.SendReq message) {
        message.setMessageType(MessageType.TALK);

        chatService.saveMessage(message.getChatroomId(), message.getSenderId(),message.getSenderName(),
                message.getContent(), message.getMessageType());

        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }


    /**
     * 채팅기록 가져오기
     */
    @GetMapping("/api/messages/{chatroomId}")
    public Slice<MessageDto.SendRes> getPreviousMessage(@PathVariable("chatroomId") Long chatroomId, Pageable pageable) {
        return messageService.getPreviousMessage(chatroomId, pageable);
    }
}
