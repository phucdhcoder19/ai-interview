package com.hp.ai_interview.modules.interview.skill;

import java.util.List;

/**
 * Một hướng phỏng vấn, nạp từ thư mục {@code resources/skills/<id>/}.
 *
 * @param persona nội dung SKILL.md — chỉ thị vai trò cho AI, nối thẳng vào system prompt
 */
public record Skill(
		String id,
		String displayName,
		String description,
		String persona,
		List<SkillCategory> categories) {

	/**
	 * Một mảng kiến thức trong hướng phỏng vấn.
	 *
	 * @param priority ALWAYS_ONE (luôn giữ đúng 1 câu), CORE (ưu tiên chia thêm), NORMAL
	 * @param ref      tên file dàn ý trong _shared/references, null nghĩa là không có dàn ý
	 */
	public record SkillCategory(String key, String label, String priority, String ref) {
	}
}
