package com.hp.ai_interview.modules.interview.repository;

import com.hp.ai_interview.modules.interview.model.entity.InterviewAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

	Optional<InterviewAnswer> findBySessionIdAndQuestionIndex(Long sessionId, int questionIndex);

	List<InterviewAnswer> findBySessionIdOrderByQuestionIndex(Long sessionId);
}
