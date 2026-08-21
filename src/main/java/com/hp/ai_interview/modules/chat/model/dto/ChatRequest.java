package com.hp.ai_interview.modules.chat.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
  String conversationId,
  @NotBlank(message = "Câu hỏi không được để trống") String message) {
}
