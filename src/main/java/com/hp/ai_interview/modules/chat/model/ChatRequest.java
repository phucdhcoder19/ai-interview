package com.hp.ai_interview.modules.chat.model;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank(message = "Câu hỏi không được để trống") String message) {
}
