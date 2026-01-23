package com.dorandoran.domain.message.controller;

import com.dorandoran.domain.message.dto.request.MessageSendRequest;
import com.dorandoran.domain.message.dto.response.MessageResponse;
import com.dorandoran.domain.message.service.MessageService;
import com.dorandoran.global.response.BaseResponse;
import com.dorandoran.global.response.SuccessCode;
import com.dorandoran.standard.page.dto.PageMessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@Tag(name = "MessageController", description = "메세지 API")
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/{receiverId}")
    @Operation(summary = "메세지 전송", description = "특정 회원에게 메세지를 전송합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> sendMessage(
            @PathVariable Long receiverId,
            @RequestBody MessageSendRequest request,
            Principal principal
    ) {
        log.info("메세지 전송 요청: sender={}, receiver={}, content={}",
                principal.getName(), receiverId, request.getContent());
        messageService.sendMessage(receiverId, principal.getName(), request);
        return BaseResponse.ok(SuccessCode.MESSAGE_SEND_SUCCESS);
    }

    @GetMapping("/{messageId}")
    @Operation(summary = "메세지 조회", description = "특정 메세지를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<MessageResponse> getMessage(
            @PathVariable Long messageId,
            Principal principal
    ) {
        MessageResponse response = messageService.getMessage(messageId, principal.getName());
        return BaseResponse.ok(SuccessCode.MESSAGE_DETAIL_SUCCESS, response);
    }

    @GetMapping
    @Operation(summary = "메세지 목록 조회", description = "회원이 주고받은 메세지 목록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<PageMessageDto<MessageResponse>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String type,
            Principal principal
    ) {
        PageMessageDto<MessageResponse> response = messageService.getMessagesByType(principal.getName(), type, page, size);
        return BaseResponse.ok(SuccessCode.MESSAGE_LIST_SUCCESS, response);
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "메세지 삭제", description = "특정 메세지를 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public BaseResponse<Void> deleteMessage(
            @PathVariable Long messageId,
            Principal principal
    ) {
        log.info("메세지 삭제 요청: messageId={}, user={}", messageId, principal.getName());
        messageService.deleteMessage(messageId, principal.getName());
        return BaseResponse.ok(SuccessCode.MESSAGE_DELETE_SUCCESS);
    }
}
