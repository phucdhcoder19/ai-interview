package com.hp.ai_interview.modules.interview.model.dto;

import java.util.List;

/**
 * Báo cáo kết quả cả phiên phỏng vấn.
 *
 * @param evaluateStatus PENDING / PROCESSING / COMPLETED / FAILED — client dựa vào đây để biết
 *                       báo cáo đã sẵn sàng chưa
 */
public record ReportResponse(
		String sessionId,
		String skillName,
		String difficulty,
		String evaluateStatus,
		Integer overallScore,
		String overallFeedback,
		List<String> strengths,
		List<String> improvements,
		List<AnswerReport> answers) {

	public record AnswerReport(
			int questionIndex,
			String category,
			String question,
			String userAnswer,
			Integer score,
			String feedback,
			List<String> keyPoints,
			String referenceAnswer) {
	}
}
