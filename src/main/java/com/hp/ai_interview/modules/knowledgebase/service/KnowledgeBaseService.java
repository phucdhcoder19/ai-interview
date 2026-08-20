package com.hp.ai_interview.modules.knowledgebase.service;

import com.hp.ai_interview.modules.knowledgebase.model.AskResponse;
import com.hp.ai_interview.modules.knowledgebase.model.IngestResponse;
import com.hp.ai_interview.modules.knowledgebase.model.KnowledgeDocument;
import com.hp.ai_interview.modules.knowledgebase.repository.KnowledgeDocumentRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseService {

	private static final int TOP_K = 4;

	/**
	 * Ngưỡng đo được từ dữ liệu thật: đoạn đúng nằm trong khoảng 0.64–0.88, còn câu hỏi
	 * hoàn toàn lạc đề chỉ đạt tối đa 0.50. Lấy 0.6 để giữ được đoạn đúng có score thấp
	 * mà vẫn chặn được câu lạc đề.
	 */
	private static final double SIMILARITY_THRESHOLD = 0.6;

	/**
	 * Chỉ giữ lại nguồn có score đạt ít nhất 85% so với nguồn tốt nhất. Một ngưỡng tuyệt đối
	 * không tách được nhiễu, vì đoạn nhiễu của câu hỏi này (0.68) còn cao hơn đoạn đúng của
	 * câu hỏi khác (0.64) — nên phải so tương đối trong cùng một lần truy vấn.
	 */
	private static final double SOURCE_RELATIVE_CUTOFF = 0.85;

	private static final PromptTemplate EMPTY_CONTEXT_PROMPT = new PromptTemplate("""
			Người dùng hỏi một câu nằm ngoài phạm vi kiến thức bạn được cung cấp.
			Hãy trả lời bằng tiếng Việt, lịch sự, rằng bạn không tìm thấy thông tin về nội dung này
			trong cơ sở kiến thức, và mời họ hỏi chủ đề khác.
			Tuyệt đối không tự suy đoán hay bịa ra câu trả lời.
			""");

	private final VectorStore vectorStore;
	private final KnowledgeDocumentRepository documentRepository;
	private final ChatClient chatClient;
	private final TokenTextSplitter splitter = TokenTextSplitter.builder()
			.withChunkSize(400)
			.build();

	public KnowledgeBaseService(VectorStore vectorStore,
			KnowledgeDocumentRepository documentRepository,
			ChatClient.Builder chatClientBuilder) {
		this.vectorStore = vectorStore;
		this.documentRepository = documentRepository;
		this.chatClient = chatClientBuilder.build();
	}

	public IngestResponse ingest(String title, String text) {
		KnowledgeDocument saved = documentRepository.save(new KnowledgeDocument(title, "manual"));

		Document raw = new Document(text, Map.of(
				"documentId", saved.getId(),
				"title", title));
		List<Document> chunks = splitter.split(List.of(raw));
		vectorStore.add(chunks);

		return new IngestResponse(saved.getId(), chunks.size());
	}

	public AskResponse ask(String question) {
		var advisor = RetrievalAugmentationAdvisor.builder()
				.documentRetriever(VectorStoreDocumentRetriever.builder()
						.vectorStore(vectorStore)
						.topK(TOP_K)
						.similarityThreshold(SIMILARITY_THRESHOLD)
						.build())
				.queryAugmenter(ContextualQueryAugmenter.builder()
						.allowEmptyContext(false)
						.emptyContextPromptTemplate(EMPTY_CONTEXT_PROMPT)
						.build())
				.build();

		String answer = chatClient.prompt()
				.advisors(advisor)
				.user(question)
				.call()
				.content();

		List<Document> matched = vectorStore.similaritySearch(SearchRequest.builder()
				.query(question)
				.topK(TOP_K)
				.similarityThreshold(SIMILARITY_THRESHOLD)
				.build());

		return new AskResponse(answer, toSources(matched));
	}

	/**
	 * Gom các đoạn tìm được thành danh sách nguồn để trả cho client: bỏ đoạn quá yếu so với
	 * đoạn tốt nhất, và gộp nhiều đoạn của cùng một tài liệu thành một dòng duy nhất.
	 */
	private List<AskResponse.Source> toSources(List<Document> matched) {
		if (matched.isEmpty()) {
			return List.of();
		}

		double bestScore = matched.stream()
				.mapToDouble(Document::getScore)
				.max()
				.orElse(0);
		double cutoff = bestScore * SOURCE_RELATIVE_CUTOFF;

		Map<String, Double> bestScoreByTitle = new LinkedHashMap<>();
		for (Document d : matched) {
			if (d.getScore() < cutoff) {
				continue;
			}
			String title = String.valueOf(d.getMetadata().get("title"));
			bestScoreByTitle.merge(title, d.getScore(), Math::max);
		}

		return bestScoreByTitle.entrySet().stream()
				.map(e -> new AskResponse.Source(e.getKey(), e.getValue()))
				.toList();
	}
}
