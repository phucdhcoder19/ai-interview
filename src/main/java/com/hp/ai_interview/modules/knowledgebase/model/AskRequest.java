package com.hp.ai_interview.modules.knowledgebase.model;

import jakarta.validation.constraints.NotBlank;

public record AskRequest(@NotBlank(message = "Câu hỏi không được để trống") String question) {
}
