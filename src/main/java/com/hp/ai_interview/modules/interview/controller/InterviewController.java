package com.hp.ai_interview.modules.interview.controller;

import com.hp.ai_interview.common.result.Result;
import com.hp.ai_interview.modules.interview.model.dto.CreateSessionRequest;
import com.hp.ai_interview.modules.interview.model.dto.QuestionResponse;
import com.hp.ai_interview.modules.interview.model.dto.SessionResponse;
import com.hp.ai_interview.modules.interview.model.dto.SkillResponse;
import com.hp.ai_interview.modules.interview.model.dto.SubmitAnswerRequest;
import com.hp.ai_interview.modules.interview.service.InterviewSessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

	private final InterviewSessionService sessionService;

	public InterviewController(InterviewSessionService sessionService) {
		this.sessionService = sessionService;
	}

	@GetMapping("/skills")
	public Result<List<SkillResponse>> listSkills() {
		return Result.ok(sessionService.listSkills());
	}

	@PostMapping("/sessions")
	public Result<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
		return Result.ok(sessionService.createSession(
				request.skillId(), request.difficulty(), request.questionCount()));
	}

	@GetMapping("/sessions/{sessionId}")
	public Result<SessionResponse> getSession(@PathVariable String sessionId) {
		return Result.ok(sessionService.getSession(sessionId));
	}

	@GetMapping("/sessions/{sessionId}/question")
	public Result<QuestionResponse> getCurrentQuestion(@PathVariable String sessionId) {
		return Result.ok(sessionService.getCurrentQuestion(sessionId));
	}

	@PostMapping("/sessions/{sessionId}/answers")
	public Result<SessionResponse> submitAnswer(@PathVariable String sessionId,
			@Valid @RequestBody SubmitAnswerRequest request) {
		return Result.ok(sessionService.submitAnswer(sessionId, request.questionIndex(), request.answer()));
	}

	@PostMapping("/sessions/{sessionId}/complete")
	public Result<SessionResponse> complete(@PathVariable String sessionId) {
		return Result.ok(sessionService.complete(sessionId));
	}
}
