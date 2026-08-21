package com.hp.ai_interview.modules.interview.model;

import java.util.List;

/**
 * Một câu hỏi trong bộ đề. Toàn bộ bộ đề được sinh một lần lúc tạo phiên rồi
 * lưu dạng JSON trong cột {@code questions_json}.
 *
 * <p>Class này cố ý nằm ở {@code model/} chứ không phải {@code model/dto/} hay
 * {@code model/entity/}: nó không phải kiểu trả về cho client (việc đó là của
 * {@link com.hp.ai_interview.modules.interview.model.dto.QuestionResponse}), cũng không phải
 * một bảng riêng trong database. Đây là mô hình nghiệp vụ nội bộ của module.
 *
 * @param topicSummary tóm tắt điểm kiến thức, dùng để phiên sau không hỏi trùng
 * @param followUps    câu hỏi đào sâu, sinh sẵn cùng câu hỏi chính
 */
public record InterviewQuestion(
		int index,
		String question,
		String category,
		String topicSummary,
		List<String> followUps) {
}
