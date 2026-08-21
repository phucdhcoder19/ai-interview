package com.hp.ai_interview.modules.interview.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(
		@NotNull(message = "Thiếu chỉ số câu hỏi")
		@jakarta.validation.constraints.Min(value = 0, message = "Chỉ số câu hỏi không hợp lệ")
		Integer questionIndex,
		@NotBlank(message = "Câu trả lời không được để trống") String answer) {
}
