package com.hp.ai_interview.modules.interview.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.hp.ai_interview.modules.interview.model.entity.InterviewAnswer;
import com.hp.ai_interview.modules.interview.model.InterviewQuestion;
import com.hp.ai_interview.modules.interview.model.entity.InterviewSession;
import com.hp.ai_interview.modules.interview.model.dto.QuestionResponse;
import com.hp.ai_interview.modules.interview.model.dto.ReportResponse;
import com.hp.ai_interview.modules.interview.model.dto.SessionResponse;
import com.hp.ai_interview.modules.interview.model.dto.SkillResponse;
import com.hp.ai_interview.modules.interview.repository.InterviewAnswerRepository;
import com.hp.ai_interview.modules.interview.repository.InterviewSessionRepository;
import com.hp.ai_interview.modules.interview.skill.Skill;
import com.hp.ai_interview.modules.interview.skill.SkillService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class InterviewSessionService {

	private static final Logger log = LoggerFactory.getLogger(InterviewSessionService.class);

	private final InterviewQuestionService questionService;
	private final AnswerEvaluationService evaluationService;
	private final InterviewSessionRepository sessionRepository;
	private final InterviewAnswerRepository answerRepository;
	private final SkillService skillService;
	private final ObjectMapper objectMapper;

	public InterviewSessionService(InterviewQuestionService questionService,
			AnswerEvaluationService evaluationService,
			InterviewSessionRepository sessionRepository,
			InterviewAnswerRepository answerRepository,
			SkillService skillService,
			ObjectMapper objectMapper) {
		this.questionService = questionService;
		this.evaluationService = evaluationService;
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

		List<InterviewQuestion> questions = questionService.generateQuestions(
				skillId, resolvedDifficulty, questionCount, recentTopics(skillId));

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

	/**
	 * Kết thúc phiên và chấm điểm ngay. Lần gọi model ở đây rất nặng nên endpoint sẽ chờ lâu;
	 * nếu chấm thất bại thì phiên vẫn được đánh dấu kết thúc, chỉ đặt evaluateStatus = FAILED.
	 */
	@Transactional
	public SessionResponse complete(String sessionId) {
		InterviewSession session = requireSession(sessionId);
		session.complete();

		List<InterviewAnswer> answers = answerRepository.findBySessionIdOrderByQuestionIndex(session.getId());
		if (answers.isEmpty()) {
			log.info("Phiên {} không có câu trả lời nào, bỏ qua chấm điểm", sessionId);
			return toSessionResponse(session, skillService.getSkill(session.getSkillId()));
		}

		Skill skill = skillService.getSkill(session.getSkillId());
		session.markEvaluating();

		try {
			var evaluation = evaluationService.evaluate(
					skill.displayName(),
					InterviewQuestionService.describeDifficulty(session.getDifficulty()),
					answers);
			applyEvaluation(session, answers, evaluation);
		}
		catch (Exception e) {
			log.error("Chấm điểm phiên {} thất bại: {}", sessionId, e.getMessage(), e);
			session.markEvaluationFailed();
		}

		return toSessionResponse(session, skill);
	}

	private void applyEvaluation(InterviewSession session, List<InterviewAnswer> answers,
			AnswerEvaluationService.SessionEvaluation evaluation) {
		Map<Integer, AnswerEvaluationService.AnswerEvaluation> byIndex = evaluation.answers().stream()
				.collect(Collectors.toMap(
						AnswerEvaluationService.AnswerEvaluation::questionIndex,
						a -> a,
						(first, second) -> first));

		for (InterviewAnswer answer : answers) {
			var result = byIndex.get(answer.getQuestionIndex());
			if (result == null) {
				log.warn("Model không chấm câu {} của phiên {}", answer.getQuestionIndex(), session.getSessionId());
				continue;
			}
			answer.applyEvaluation(
					result.score(),
					result.feedback(),
					writeJson(result.keyPoints()),
					result.referenceAnswer());
		}

		session.applyEvaluation(
				evaluation.overallScore(),
				evaluation.overallFeedback(),
				writeJson(evaluation.strengths()),
				writeJson(evaluation.improvements()));
	}

	@Transactional(readOnly = true)
	public ReportResponse getReport(String sessionId) {
		InterviewSession session = requireSession(sessionId);
		Skill skill = skillService.getSkill(session.getSkillId());

		List<ReportResponse.AnswerReport> answerReports =
				answerRepository.findBySessionIdOrderByQuestionIndex(session.getId()).stream()
						.map(a -> new ReportResponse.AnswerReport(
								a.getQuestionIndex(),
								a.getCategory(),
								a.getQuestion(),
								a.getUserAnswer(),
								a.getScore(),
								a.getFeedback(),
								readStringList(a.getKeyPointsJson()),
								a.getReferenceAnswer()))
						.toList();

		return new ReportResponse(
				session.getSessionId(),
				skill.displayName(),
				session.getDifficulty(),
				session.getEvaluateStatus(),
				session.getOverallScore(),
				session.getOverallFeedback(),
				readStringList(session.getStrengthsJson()),
				readStringList(session.getImprovementsJson()),
				answerReports);
	}

	/**
	 * Gom {@code topicSummary} của các phiên gần đây cùng hướng phỏng vấn, để lần ra đề sau
	 * không hỏi lại đúng những điểm kiến thức đã hỏi.
	 */
	private List<String> recentTopics(String skillId) {
		return sessionRepository.findTop5BySkillIdOrderByIdDesc(skillId).stream()
				.flatMap(s -> readQuestions(s).stream())
				.map(InterviewQuestion::topicSummary)
				.filter(StringUtils::hasText)
				.distinct()
				.limit(30)
				.toList();
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

	/** Ghi một object bất kỳ thành JSON để lưu vào cột dạng text. */
	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value == null ? List.of() : value);
		}
		catch (Exception e) {
			throw new IllegalStateException("Không serialize được dữ liệu sang JSON", e);
		}
	}

	private List<String> readStringList(String json) {
		if (!StringUtils.hasText(json)) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<List<String>>() {
			});
		}
		catch (Exception e) {
			return List.of();
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
