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

	public String getUserAnswer() {
		return userAnswer;
	}

	public void updateAnswer(String userAnswer) {
		this.userAnswer = userAnswer;
	}
}
