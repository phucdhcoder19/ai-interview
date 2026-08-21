package com.hp.ai_interview.modules.interview.repository;

import com.hp.ai_interview.modules.interview.model.entity.InterviewSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

	Optional<InterviewSession> findBySessionId(String sessionId);
}
