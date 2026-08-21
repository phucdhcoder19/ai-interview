package com.hp.ai_interview.modules.interview.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "interview_session")
public class InterviewSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "session_id", nullable = false, unique = true)
	private String sessionId;

	@Column(name = "skill_id", nullable = false)
	private String skillId;

	private String difficulty;

	@Column(name = "total_questions", nullable = false)
	private int totalQuestions;

	@Column(name = "current_question_index", nullable = false)
	private int currentQuestionIndex;

	/** Cả bộ đề lưu dạng JSON, giống cách repo tham khảo làm. */
	@Column(name = "questions_json", nullable = false, columnDefinition = "text")
	private String questionsJson;

	@Column(nullable = false)
	private String status;

	/** PENDING -> PROCESSING -> COMPLETED / FAILED. Thêm sẵn để sau này chuyển sang chấm nền. */
	@Column(name = "evaluate_status", nullable = false)
	private String evaluateStatus = "PENDING";

	@Column(name = "overall_score")
	private Integer overallScore;

	@Column(name = "overall_feedback", columnDefinition = "text")
	private String overallFeedback;

	@Column(name = "strengths_json", columnDefinition = "text")
	private String strengthsJson;

	@Column(name = "improvements_json", columnDefinition = "text")
	private String improvementsJson;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	protected InterviewSession() {
	}

	public InterviewSession(String sessionId, String skillId, String difficulty,
			int totalQuestions, String questionsJson) {
		this.sessionId = sessionId;
		this.skillId = skillId;
		this.difficulty = difficulty;
		this.totalQuestions = totalQuestions;
		this.questionsJson = questionsJson;
		this.currentQuestionIndex = 0;
		this.status = "IN_PROGRESS";
	}

	public Long getId() {
		return id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getSkillId() {
		return skillId;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public int getTotalQuestions() {
		return totalQuestions;
	}

	public int getCurrentQuestionIndex() {
		return currentQuestionIndex;
	}

	public String getQuestionsJson() {
		return questionsJson;
	}

	public String getStatus() {
		return status;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public String getEvaluateStatus() {
		return evaluateStatus;
	}

	public Integer getOverallScore() {
		return overallScore;
	}

	public String getOverallFeedback() {
		return overallFeedback;
	}

	public String getStrengthsJson() {
		return strengthsJson;
	}

	public String getImprovementsJson() {
		return improvementsJson;
	}

	public void markEvaluating() {
		this.evaluateStatus = "PROCESSING";
	}

	public void markEvaluationFailed() {
		this.evaluateStatus = "FAILED";
	}

	/** Ghi kết quả tổng kết của cả phiên. */
	public void applyEvaluation(Integer overallScore, String overallFeedback,
			String strengthsJson, String improvementsJson) {
		this.overallScore = overallScore;
		this.overallFeedback = overallFeedback;
		this.strengthsJson = strengthsJson;
		this.improvementsJson = improvementsJson;
		this.evaluateStatus = "COMPLETED";
	}

	public void advanceTo(int questionIndex) {
		this.currentQuestionIndex = questionIndex;
	}

	public void complete() {
		this.status = "COMPLETED";
		this.completedAt = OffsetDateTime.now();
	}
}
