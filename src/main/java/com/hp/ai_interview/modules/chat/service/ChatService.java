package com.hp.ai_interview.modules.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

	private static final String SYSTEM_PROMPT = """
			Bạn là trợ lý phỏng vấn kỹ thuật. Trả lời ngắn gọn, chính xác, bằng tiếng Việt.
			Nếu không chắc chắn, hãy nói rõ là không chắc thay vì bịa ra câu trả lời.
			""";

	private final ChatClient chatClient;

	public ChatService(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder
				.defaultSystem(SYSTEM_PROMPT)
				.build();
	}

	public String ask(String message) {
		return chatClient.prompt()
				.user(message)
				.call()
				.content();
	}
}
