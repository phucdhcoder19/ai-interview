package com.hp.ai_interview.modules.interview.repository;

import com.hp.ai_interview.modules.interview.model.entity.InterviewSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

	Optional<InterviewSession> findBySessionId(String sessionId);

	/** Các phiên gần nhất cùng hướng phỏng vấn, dùng để khử trùng lặp câu hỏi. */
	List<InterviewSession> findTop5BySkillIdOrderByIdDesc(String skillId);
}
