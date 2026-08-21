package com.hp.ai_interview.modules.interview.skill;

import com.hp.ai_interview.modules.interview.skill.Skill.SkillCategory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

/**
 * Nạp các hướng phỏng vấn từ classpath và dựng phần "kiến thức" cho prompt.
 *
 * <p>Khác với module knowledgebase, ở đây <b>không có vector hay tìm kiếm ngữ nghĩa</b>.
 * Câu hỏi phỏng vấn cho một hướng là tập biết trước, nên chỉ cần đọc file dàn ý viết sẵn
 * rồi nhét vào prompt — vừa rẻ (không tốn lần gọi embedding nào) vừa dễ kiểm soát.
 */
@Service
public class SkillService {

	private static final Logger log = LoggerFactory.getLogger(SkillService.class);

	/** Chặn trên độ dài phần dàn ý để prompt không phình quá to. */
	private static final int MAX_REFERENCE_CHARS = 12000;

	private static final String SKILLS_ROOT = "classpath:skills/";
	private static final String REFERENCES_PATH = SKILLS_ROOT + "_shared/references/";

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	/** id -> skill, giữ nguyên thứ tự nạp. */
	private final Map<String, Skill> skills = new LinkedHashMap<>();

	@PostConstruct
	void loadSkills() throws IOException {
		for (Resource metaResource : resolver.getResources("classpath*:skills/*/skill.meta.yml")) {
			String id = metaResource.getURL().getPath().replaceAll(".*/skills/([^/]+)/.*", "$1");
			skills.put(id, parseSkill(id, metaResource));
		}
		log.info("Đã nạp {} hướng phỏng vấn: {}", skills.size(), skills.keySet());
	}

	public List<Skill> listSkills() {
		return List.copyOf(skills.values());
	}

	public Skill getSkill(String skillId) {
		Skill skill = skills.get(skillId);
		if (skill == null) {
			throw new IllegalArgumentException("Không tìm thấy hướng phỏng vấn: " + skillId);
		}
		return skill;
	}

	/**
	 * Chia số câu hỏi cho các mảng kiến thức. Ba vòng, theo đúng thứ tự ưu tiên:
	 * ALWAYS_ONE giữ đúng 1 câu, rồi mọi mảng được 1 câu để đảm bảo phủ hết,
	 * phần dư chia vòng tròn cho các mảng CORE.
	 */
	public Map<String, Integer> calculateAllocation(List<SkillCategory> categories, int totalQuestions) {
		List<SkillCategory> alwaysOne = categories.stream().filter(c -> "ALWAYS_ONE".equals(c.priority())).toList();
		List<SkillCategory> core = categories.stream().filter(c -> "CORE".equals(c.priority())).toList();
		List<SkillCategory> normal = categories.stream()
				.filter(c -> !"ALWAYS_ONE".equals(c.priority()) && !"CORE".equals(c.priority()))
				.toList();

		Map<String, Integer> allocation = new LinkedHashMap<>();
		int remaining = totalQuestions;

		for (SkillCategory c : alwaysOne) {
			if (remaining <= 0) {
				break;
			}
			allocation.put(c.key(), 1);
			remaining--;
		}
		for (SkillCategory c : core) {
			if (remaining <= 0) {
				break;
			}
			allocation.put(c.key(), 1);
			remaining--;
		}
		for (SkillCategory c : normal) {
			if (remaining <= 0) {
				break;
			}
			allocation.put(c.key(), 1);
			remaining--;
		}

		// Phần dư chia vòng tròn cho CORE; không có CORE thì chia cho NORMAL.
		List<SkillCategory> spreadTargets = core.isEmpty() ? normal : core;
		int i = 0;
		while (remaining > 0 && !spreadTargets.isEmpty()) {
			String key = spreadTargets.get(i % spreadTargets.size()).key();
			allocation.merge(key, 1, Integer::sum);
			remaining--;
			i++;
		}

		return allocation;
	}

	/** Bảng markdown mô tả phân bổ, đưa thẳng vào prompt cho model dễ bám. */
	public String buildAllocationTable(Map<String, Integer> allocation, List<SkillCategory> categories) {
		StringBuilder table = new StringBuilder("| Mảng kiến thức | Key | Số câu |\n|---|---|---|\n");
		for (SkillCategory c : categories) {
			int count = allocation.getOrDefault(c.key(), 0);
			if (count > 0) {
				table.append("| ").append(c.label())
						.append(" | ").append(c.key())
						.append(" | ").append(count)
						.append(" |\n");
			}
		}
		return table.toString();
	}

	/**
	 * Nối nội dung các file dàn ý của những mảng được phân câu. Đây chính là phần
	 * thay thế cho retrieval: đọc file, ghép chuỗi, cắt ở {@value #MAX_REFERENCE_CHARS} ký tự.
	 */
	public String buildReferenceSection(Skill skill, Map<String, Integer> allocation) {
		StringBuilder section = new StringBuilder();
		for (SkillCategory c : skill.categories()) {
			if (allocation.getOrDefault(c.key(), 0) == 0 || c.ref() == null) {
				continue;
			}
			String content = readReference(c.ref());
			if (content.isBlank()) {
				continue;
			}
			section.append("### ").append(c.label()).append(" (").append(c.key()).append(")\n")
					.append(content).append("\n\n");
		}

		if (section.length() > MAX_REFERENCE_CHARS) {
			log.warn("Phần dàn ý dài {} ký tự, cắt còn {}", section.length(), MAX_REFERENCE_CHARS);
			return section.substring(0, MAX_REFERENCE_CHARS);
		}
		return section.toString();
	}

	private String readReference(String refFileName) {
		Resource resource = resolver.getResource(REFERENCES_PATH + refFileName);
		if (!resource.exists()) {
			log.warn("Không tìm thấy file dàn ý: {}", refFileName);
			return "";
		}
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			log.warn("Không đọc được file dàn ý {}: {}", refFileName, e.getMessage());
			return "";
		}
	}

	@SuppressWarnings("unchecked")
	private Skill parseSkill(String id, Resource metaResource) throws IOException {
		Map<String, Object> meta;
		try (InputStream in = metaResource.getInputStream()) {
			meta = new Yaml().load(in);
		}

		List<SkillCategory> categories = new ArrayList<>();
		Object rawCategories = meta.get("categories");
		if (rawCategories instanceof List<?> list) {
			for (Object item : list) {
				Map<String, Object> c = (Map<String, Object>) item;
				categories.add(new SkillCategory(
						String.valueOf(c.get("key")),
						String.valueOf(c.get("label")),
						String.valueOf(c.getOrDefault("priority", "NORMAL")),
						c.get("ref") == null ? null : String.valueOf(c.get("ref"))));
			}
		}

		return new Skill(
				id,
				String.valueOf(meta.getOrDefault("displayName", id)),
				String.valueOf(meta.getOrDefault("description", "")),
				readPersona(id),
				categories);
	}

	private String readPersona(String skillId) {
		Resource resource = resolver.getResource(SKILLS_ROOT + skillId + "/SKILL.md");
		if (!resource.exists()) {
			return "";
		}
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			log.warn("Không đọc được SKILL.md của {}: {}", skillId, e.getMessage());
			return "";
		}
	}
}
