package com.hp.ai_interview.modules.interview.model.dto;

import java.util.List;

public record QuestionResponse(
		int index,
		int total,
		String question,
		String category,
		List<String> followUps) {
}
