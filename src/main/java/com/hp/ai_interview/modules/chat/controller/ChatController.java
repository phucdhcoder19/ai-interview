package com.hp.ai_interview.modules.chat.controller;

import com.hp.ai_interview.common.result.Result;
import com.hp.ai_interview.modules.chat.model.ChatRequest;
import com.hp.ai_interview.modules.chat.model.ChatResponse;
import com.hp.ai_interview.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping
	public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
		return Result.ok(chatService.ask(request.conversationId(), request.message()));
	}
}
