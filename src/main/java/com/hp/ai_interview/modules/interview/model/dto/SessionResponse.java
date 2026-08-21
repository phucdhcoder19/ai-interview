package com.hp.ai_interview.modules.interview.model.dto;

public record SessionResponse(
		String sessionId,
		String skillId,
		String skillName,
		String difficulty,
		int totalQuestions,
		int currentQuestionIndex,
		String status) {
}
