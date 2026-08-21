package com.hp.ai_interview.modules.chat.service;

import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hp.ai_interview.modules.chat.model.ChatResponse;


@Service
public class ChatService {

	private static final String SYSTEM_PROMPT = """
			Bạn là trợ lý phỏng vấn kỹ thuật. Trả lời ngắn gọn, chính xác, bằng tiếng Việt.
			Nếu không chắc chắn, hãy nói rõ là không chắc thay vì bịa ra câu trả lời.
			""";

	private final ChatClient chatClient;

	public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
		this.chatClient = chatClientBuilder
				.defaultSystem(SYSTEM_PROMPT)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

	public ChatResponse ask(String conversationId, String message) {
		String id = StringUtils.hasText(conversationId)
				? conversationId
				: UUID.randomUUID().toString();

		String answer = chatClient.prompt()
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))
				.user(message)
				.call()
				.content();

		return new ChatResponse(id, answer);
	}
}
