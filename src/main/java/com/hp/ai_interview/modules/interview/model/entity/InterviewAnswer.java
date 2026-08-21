package com.hp.ai_interview.modules.interview.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "interview_answer")
public class InterviewAnswer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "session_id", nullable = false)
	private Long sessionId;

	@Column(name = "question_index", nullable = false)
	private int questionIndex;

	@Column(columnDefinition = "text")
	private String question;

	private String category;

	@Column(name = "user_answer", columnDefinition = "text")
	private String userAnswer;

	private Integer score;

	@Column(columnDefinition = "text")
	private String feedback;

	@Column(name = "key_points_json", columnDefinition = "text")
	private String keyPointsJson;

	@Column(name = "reference_answer", columnDefinition = "text")
	private String referenceAnswer;

	@Column(name = "answered_at", insertable = false, updatable = false)
	private OffsetDateTime answeredAt;

	protected InterviewAnswer() {
	}

	public InterviewAnswer(Long sessionId, int questionIndex, String question,
			String category, String userAnswer) {
		this.sessionId = sessionId;
		this.questionIndex = questionIndex;
		this.question = question;
		this.category = category;
		this.userAnswer = userAnswer;
	}

	public Long getId() {
		return id;
	}

	public int getQuestionIndex() {
		return questionIndex;
	}

	public String getQuestion() {
		return question;
	}

	public String getCategory() {
		return category;
	}

	public String getUserAnswer() {
		return userAnswer;
	}

	public Integer getScore() {
		return score;
	}

	public String getFeedback() {
		return feedback;
	}

	public String getKeyPointsJson() {
		return keyPointsJson;
	}

	public String getReferenceAnswer() {
		return referenceAnswer;
	}

	/** Ghi kết quả chấm điểm cho câu trả lời này. */
	public void applyEvaluation(Integer score, String feedback, String keyPointsJson, String referenceAnswer) {
		this.score = score;
		this.feedback = feedback;
		this.keyPointsJson = keyPointsJson;
		this.referenceAnswer = referenceAnswer;
	}

	public void updateAnswer(String userAnswer) {
		this.userAnswer = userAnswer;
	}
}
