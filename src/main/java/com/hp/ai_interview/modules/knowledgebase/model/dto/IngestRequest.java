package com.hp.ai_interview.modules.knowledgebase.model.dto;

import jakarta.validation.constraints.NotBlank;

public record IngestRequest(
		@NotBlank(message = "Tiêu đề không được để trống") String title,
		@NotBlank(message = "Nội dung không được để trống") String text) {
}
