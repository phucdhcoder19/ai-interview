package com.hp.ai_interview.modules.interview.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
		@NotBlank(message = "Hướng phỏng vấn không được để trống") String skillId,
		String difficulty,
		@Min(value = 3, message = "Số câu hỏi tối thiểu là 3")
		@Max(value = 20, message = "Số câu hỏi tối đa là 20")
		int questionCount) {
}
