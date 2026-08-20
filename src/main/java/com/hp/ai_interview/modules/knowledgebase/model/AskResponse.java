package com.hp.ai_interview.modules.knowledgebase.model;

import java.util.List;

public record AskResponse(String answer, List<Source> sources) {

	public record Source(String title, Double score) {
	}
}
