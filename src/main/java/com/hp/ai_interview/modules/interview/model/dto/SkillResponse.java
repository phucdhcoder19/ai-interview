package com.hp.ai_interview.modules.interview.model.dto;

import java.util.List;

public record SkillResponse(
		String id,
		String displayName,
		String description,
		List<String> categories) {
}
