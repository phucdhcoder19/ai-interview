package com.hp.ai_interview.modules.interview.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.hp.ai_interview.modules.interview.model.entity.InterviewAnswer;
import com.hp.ai_interview.modules.interview.model.InterviewQuestion;
import com.hp.ai_interview.modules.interview.model.entity.InterviewSession;
import com.hp.ai_interview.modules.interview.model.dto.QuestionResponse;
import com.hp.ai_interview.modules.interview.model.dto.SessionResponse;
import com.hp.ai_interview.modules.interview.model.dto.SkillResponse;
import com.hp.ai_interview.modules.interview.repository.InterviewAnswerRepository;
import com.hp.ai_interview.modules.interview.repository.InterviewSessionRepository;
import com.hp.ai_interview.modules.interview.skill.Skill;
import com.hp.ai_interview.modules.interview.skill.SkillService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InterviewSessionService {

	private final InterviewQuestionService questionService;
	private final InterviewSessionRepository sessionRepository;
	private final InterviewAnswerRepository answerRepository;
	private final SkillService skillService;
	private final ObjectMapper objectMapper;

	public InterviewSessionService(InterviewQuestionService questionService,
			InterviewSessionRepository sessionRepository,
			InterviewAnswerRepository answerRepository,
			SkillService skillService,
			ObjectMapper objectMapper) {
		this.questionService = questionService;
		this.sessionRepository = sessionRepository;
		this.answerRepository = answerRepository;
		this.skillService = skillService;
		this.objectMapper = objectMapper;
	}

	public List<SkillResponse> listSkills() {
		return skillService.listSkills().stream()
				.map(s -> new SkillResponse(s.id(), s.displayName(), s.description(),
						s.categories().stream().map(Skill.SkillCategory::label).toList()))
				.toList();
	}

	@Transactional
	public SessionResponse createSession(String skillId, String difficulty, int questionCount) {
		Skill skill = skillService.getSkill(skillId);
		String resolvedDifficulty = StringUtils.hasText(difficulty) ? difficulty : "mid";

		List<InterviewQuestion> questions =
				questionService.generateQuestions(skillId, resolvedDifficulty, questionCount, List.of());

		InterviewSession session = new InterviewSession(
				UUID.randomUUID().toString(),
				skillId,
				resolvedDifficulty,
				questions.size(),
				writeJson(questions));

		return toSessionResponse(sessionRepository.save(session), skill);
	}

	@Transactional(readOnly = true)
	public SessionResponse getSession(String sessionId) {
		InterviewSession session = requireSession(sessionId);
		return toSessionResponse(session, skillService.getSkill(session.getSkillId()));
	}

	@Transactional(readOnly = true)
	public QuestionResponse getCurrentQuestion(String sessionId) {
		InterviewSession session = requireSession(sessionId);
		List<InterviewQuestion> questions = readQuestions(session);
		int index = session.getCurrentQuestionIndex();

		if (index >= questions.size()) {
			throw new IllegalArgumentException("Phiên phỏng vấn đã hết câu hỏi");
		}

		InterviewQuestion q = questions.get(index);
		return new QuestionResponse(q.index(), questions.size(), q.question(), q.category(), q.followUps());
	}

	@Transactional
	public SessionResponse submitAnswer(String sessionId, int questionIndex, String answer) {
		InterviewSession session = requireSession(sessionId);
		List<InterviewQuestion> questions = readQuestions(session);

		if (questionIndex < 0 || questionIndex >= questions.size()) {
			throw new IllegalArgumentException("Chỉ số câu hỏi không hợp lệ: " + questionIndex);
		}

		InterviewQuestion question = questions.get(questionIndex);

		// Nộp lại cùng một câu thì ghi đè, nhờ ràng buộc unique (session_id, question_index).
		answerRepository.findBySessionIdAndQuestionIndex(session.getId(), questionIndex)
				.ifPresentOrElse(
						existing -> existing.updateAnswer(answer),
						() -> answerRepository.save(new InterviewAnswer(
								session.getId(), questionIndex, question.question(), question.category(), answer)));

		if (questionIndex >= session.getCurrentQuestionIndex()) {
			session.advanceTo(questionIndex + 1);
		}

		return toSessionResponse(session, skillService.getSkill(session.getSkillId()));
	}

	@Transactional
	public SessionResponse complete(String sessionId) {
		InterviewSession session = requireSession(sessionId);
		session.complete();
		return toSessionResponse(session, skillService.getSkill(session.getSkillId()));
	}

	private InterviewSession requireSession(String sessionId) {
		return sessionRepository.findBySessionId(sessionId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên phỏng vấn: " + sessionId));
	}

	private SessionResponse toSessionResponse(InterviewSession session, Skill skill) {
		return new SessionResponse(
				session.getSessionId(),
				session.getSkillId(),
				skill.displayName(),
				session.getDifficulty(),
				session.getTotalQuestions(),
				session.getCurrentQuestionIndex(),
				session.getStatus());
	}

	private String writeJson(List<InterviewQuestion> questions) {
		try {
			return objectMapper.writeValueAsString(questions);
		}
		catch (Exception e) {
			throw new IllegalStateException("Không serialize được bộ đề", e);
		}
	}

	private List<InterviewQuestion> readQuestions(InterviewSession session) {
		try {
			return objectMapper.readValue(session.getQuestionsJson(), new TypeReference<>() {
			});
		}
		catch (Exception e) {
			throw new IllegalStateException("Không đọc được bộ đề đã lưu", e);
		}
	}
}
