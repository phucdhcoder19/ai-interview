package com.hp.ai_interview.modules.interview.service;

import com.hp.ai_interview.modules.interview.model.InterviewQuestion;
import com.hp.ai_interview.modules.interview.skill.Skill;
import com.hp.ai_interview.modules.interview.skill.SkillService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
 * Sinh cả bộ đề trong một lần gọi model, không hỏi tới đâu sinh tới đó.
 *
 * <p>Nguồn kiến thức là các file dàn ý viết sẵn trong {@code resources/skills/}, được
 * {@link SkillService} đọc rồi nối vào prompt — không dùng vector hay tìm kiếm ngữ nghĩa.
 */
@Service
public class InterviewQuestionService {

	private static final Logger log = LoggerFactory.getLogger(InterviewQuestionService.class);

	private static final int FOLLOW_UP_COUNT = 1;

	private static final Map<String, String> DIFFICULTY_DESCRIPTIONS = Map.of(
			"junior", "Sinh viên mới ra trường hoặc 0-1 năm kinh nghiệm. Kiểm tra khái niệm nền tảng và ứng dụng đơn giản.",
			"mid", "1-3 năm kinh nghiệm. Kiểm tra mức độ hiểu nguyên lý và kinh nghiệm thực chiến.",
			"senior", "Trên 3 năm kinh nghiệm. Kiểm tra tư duy thiết kế kiến trúc và khả năng tối ưu chuyên sâu.");

	/** Dùng khi model lỗi hoặc trả về bộ đề rỗng, để phiên phỏng vấn vẫn chạy được. */
	private static final String[][] FALLBACK_QUESTIONS = {
			{"Hãy kể về một vấn đề kỹ thuật khó mà bạn từng chủ trì xử lý. Cách bạn phân tích ra sao?", "GENERAL"},
			{"Khi chọn giải pháp kỹ thuật, bạn thường cân nhắc những yếu tố nào? Cho một ví dụ cụ thể.", "GENERAL"},
			{"Kể lại một lần bạn xử lý sự cố trên môi trường production, từ lúc phát hiện tới lúc khắc phục.", "GENERAL"},
			{"Bạn làm thế nào để đảm bảo chất lượng code? Nêu những cách bạn đã thực sự áp dụng.", "GENERAL"},
			{"Mô tả một lần bạn tối ưu hiệu năng: động cơ, giải pháp và kết quả đo được.", "GENERAL"},
			{"Bất đồng lớn nhất bạn từng gặp khi làm việc nhóm là gì? Bạn giải quyết thế nào?", "GENERAL"},
	};

	private final ChatClient chatClient;
	private final SkillService skillService;
	private final PromptTemplate systemPromptTemplate;
	private final PromptTemplate userPromptTemplate;

	/** Kiểu trung gian để ép model trả JSON đúng schema. */
	private record QuestionListDto(List<QuestionDto> questions) {
	}

	private record QuestionDto(String question, String category, String topicSummary, List<String> followUps) {
	}

	public InterviewQuestionService(ChatClient.Builder chatClientBuilder,
			SkillService skillService,
			ResourceLoader resourceLoader) throws IOException {
		this.chatClient = chatClientBuilder.build();
		this.skillService = skillService;
		this.systemPromptTemplate = loadTemplate(resourceLoader, "classpath:prompts/interview-question-system.st");
		this.userPromptTemplate = loadTemplate(resourceLoader, "classpath:prompts/interview-question-user.st");
	}

	private static PromptTemplate loadTemplate(ResourceLoader loader, String location) throws IOException {
		return new PromptTemplate(loader.getResource(location).getContentAsString(StandardCharsets.UTF_8));
	}

	public List<InterviewQuestion> generateQuestions(String skillId, String difficulty, int questionCount,
			List<String> previousTopics) {
		Skill skill = skillService.getSkill(skillId);
		Map<String, Integer> allocation = skillService.calculateAllocation(skill.categories(), questionCount);

		log.info("Sinh đề: skill={}, số câu={}, phân bổ={}", skillId, questionCount, allocation);

		Map<String, Object> variables = new HashMap<>();
		variables.put("questionCount", questionCount);
		variables.put("followUpCount", FOLLOW_UP_COUNT);
		variables.put("difficultyDescription", describeDifficulty(difficulty));
		variables.put("skillName", skill.displayName());
		variables.put("skillDescription", skill.description());
		variables.put("allocationTable", skillService.buildAllocationTable(allocation, skill.categories()));
		variables.put("historicalSection", buildHistoricalSection(previousTopics));
		variables.put("referenceSection", skillService.buildReferenceSection(skill, allocation));

		try {
			QuestionListDto dto = chatClient.prompt()
					.system(systemPromptTemplate.render() + "\n\n" + skill.persona())
					.user(userPromptTemplate.render(variables))
					.call()
					.entity(QuestionListDto.class);

			List<InterviewQuestion> questions = convert(dto, questionCount);
			if (questions.isEmpty()) {
				log.warn("Model trả về bộ đề rỗng, dùng câu hỏi dự phòng");
				return fallbackQuestions(questionCount);
			}
			log.info("Sinh đề xong: yêu cầu {} câu, nhận {} câu", questionCount, questions.size());
			return questions;
		}
		catch (Exception e) {
			log.error("Sinh đề thất bại, dùng câu hỏi dự phòng: {}", e.getMessage(), e);
			return fallbackQuestions(questionCount);
		}
	}

	private String describeDifficulty(String difficulty) {
		return DIFFICULTY_DESCRIPTIONS.getOrDefault(difficulty, DIFFICULTY_DESCRIPTIONS.get("mid"));
	}

	private String buildHistoricalSection(List<String> previousTopics) {
		if (previousTopics == null || previousTopics.isEmpty()) {
			return "(chưa có phiên nào trước đó)";
		}
		return previousTopics.stream().map(t -> "- " + t).reduce((a, b) -> a + "\n" + b).orElse("");
	}

	/**
	 * Model đôi khi trả thừa hoặc thiếu câu dù prompt đã ràng buộc, nên phải cắt lại
	 * cho đúng số lượng thay vì tin tưởng hoàn toàn.
	 */
	private List<InterviewQuestion> convert(QuestionListDto dto, int questionCount) {
		if (dto == null || dto.questions() == null) {
			return List.of();
		}

		List<InterviewQuestion> questions = new ArrayList<>();
		for (QuestionDto q : dto.questions()) {
			if (q.question() == null || q.question().isBlank()) {
				continue;
			}
			questions.add(new InterviewQuestion(
					questions.size(),
					q.question(),
					q.category() == null ? "GENERAL" : q.category(),
					q.topicSummary() == null ? "" : q.topicSummary(),
					q.followUps() == null ? List.of() : q.followUps()));
			if (questions.size() == questionCount) {
				break;
			}
		}
		return questions;
	}

	private List<InterviewQuestion> fallbackQuestions(int questionCount) {
		List<InterviewQuestion> questions = new ArrayList<>();
		for (int i = 0; i < Math.min(questionCount, FALLBACK_QUESTIONS.length); i++) {
			questions.add(new InterviewQuestion(
					i, FALLBACK_QUESTIONS[i][0], FALLBACK_QUESTIONS[i][1], "", List.of()));
		}
		return questions;
	}
}
