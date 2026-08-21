package com.hp.ai_interview.modules.interview.service;

import com.hp.ai_interview.modules.interview.model.entity.InterviewAnswer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Chấm cả phiên phỏng vấn trong một lần gọi model.
 *
 * <p>Chấm từng câu ngay lúc nộp sẽ khiến mỗi lần nộp đáp án phải chờ model, nên ở đây gom
 * lại chấm một lượt lúc kết thúc. Đổi lại, lần gọi này rất nặng — nhận vào toàn bộ hỏi đáp,
 * trả ra điểm, nhận xét và đáp án tham khảo cho từng câu — nên có thể mất khá lâu.
 */
@Service
public class AnswerEvaluationService {

	private static final Logger log = LoggerFactory.getLogger(AnswerEvaluationService.class);

	private final ChatClient chatClient;
	private final PromptTemplate systemPromptTemplate;
	private final PromptTemplate userPromptTemplate;

	/** Kết quả chấm cho một câu. */
	public record AnswerEvaluation(
			int questionIndex,
			int score,
			String feedback,
			List<String> keyPoints,
			String referenceAnswer) {
	}

	/** Kết quả chấm cho cả phiên. */
	public record SessionEvaluation(
			List<AnswerEvaluation> answers,
			int overallScore,
			String overallFeedback,
			List<String> strengths,
			List<String> improvements) {
	}

	public AnswerEvaluationService(ChatClient.Builder chatClientBuilder,
			ResourceLoader resourceLoader) throws IOException {
		this.chatClient = chatClientBuilder.build();
		this.systemPromptTemplate = loadTemplate(resourceLoader, "classpath:prompts/interview-evaluation-system.st");
		this.userPromptTemplate = loadTemplate(resourceLoader, "classpath:prompts/interview-evaluation-user.st");
	}

	private static PromptTemplate loadTemplate(ResourceLoader loader, String location) throws IOException {
		return new PromptTemplate(loader.getResource(location).getContentAsString(StandardCharsets.UTF_8));
	}

	public SessionEvaluation evaluate(String skillName, String difficultyDescription,
			List<InterviewAnswer> answers) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("skillName", skillName);
		variables.put("difficultyDescription", difficultyDescription);
		variables.put("answerCount", answers.size());
		variables.put("transcript", buildTranscript(answers));

		log.info("Bắt đầu chấm phiên: {} câu trả lời", answers.size());

		SessionEvaluation evaluation = chatClient.prompt()
				.system(systemPromptTemplate.render())
				.user(userPromptTemplate.render(variables))
				.call()
				.entity(SessionEvaluation.class);

		if (evaluation == null) {
			throw new IllegalStateException("Model không trả về kết quả chấm điểm");
		}

		log.info("Chấm xong: điểm chung {}, {} câu được chấm",
				evaluation.overallScore(), evaluation.answers().size());
		return evaluation;
	}

	private String buildTranscript(List<InterviewAnswer> answers) {
		StringBuilder transcript = new StringBuilder();
		for (InterviewAnswer a : answers) {
			transcript.append("### Câu ").append(a.getQuestionIndex())
					.append(" (mảng kiến thức: ").append(a.getCategory()).append(")\n")
					.append("**Câu hỏi:** ").append(a.getQuestion()).append("\n")
					.append("**Trả lời:** ").append(a.getUserAnswer()).append("\n\n");
		}
		return transcript.toString();
	}
}
