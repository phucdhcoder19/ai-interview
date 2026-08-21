package com.hp.ai_interview.modules.knowledgebase.service;

import com.hp.ai_interview.modules.knowledgebase.model.dto.AskResponse;
import com.hp.ai_interview.modules.knowledgebase.model.dto.IngestResponse;
import com.hp.ai_interview.modules.knowledgebase.model.entity.KnowledgeDocument;
import com.hp.ai_interview.modules.knowledgebase.repository.KnowledgeDocumentRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.reader.tika.TikaDocumentReader;

@Service
public class KnowledgeBaseService {

	private static final int TOP_K = 4;

	/**
	 * Ngưỡng đo được từ dữ liệu thật: đoạn đúng nằm trong khoảng 0.64–0.88, còn câu hỏi
	 * hoàn toàn lạc đề chỉ đạt tối đa 0.50. Lấy 0.6 để giữ được đoạn đúng có score thấp
	 * mà vẫn chặn được câu lạc đề.
	 */
	private static final double SIMILARITY_THRESHOLD = 0.6;

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
		return save(title, "manual", text);
	}

	/** Phần dùng chung: lưu metadata, cắt đoạn, vector hóa. */
	private IngestResponse save(String title, String source, String text) {
		KnowledgeDocument saved = documentRepository.save(new KnowledgeDocument(title, source));

		Document raw = new Document(text, Map.of(
				"documentId", saved.getId(),
				"title", title));
		List<Document> chunks = splitter.split(List.of(raw));
		vectorStore.add(chunks);

		return new IngestResponse(saved.getId(), chunks.size());
	}

	/**
	 * Nạp một file tài liệu (PDF, DOCX, DOC, TXT...) vào knowledge base.
	 * Tika tự nhận dạng định dạng nên không cần kiểm tra đuôi file.
	 */
	public IngestResponse ingestFile(MultipartFile file, String title) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File rỗng, không có gì để nạp");
		}

		String documentTitle = StringUtils.hasText(title)
				? title
				: StringUtils.getFilename(file.getOriginalFilename());

		String text;
		try (InputStream in = file.getInputStream()) {
			List<Document> parsed = new TikaDocumentReader(new InputStreamResource(in)).read();
			text = parsed.stream()
					.map(Document::getText)
					.collect(Collectors.joining(System.lineSeparator()));
		}

		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException("Không đọc được nội dung văn bản nào từ file này");
		}

		return save(documentTitle, file.getOriginalFilename(), text);
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

		ChatClientResponse response = chatClient.prompt()
				.advisors(advisor)
				.user(question)
				.call()
				.chatClientResponse();

		String answer = response.chatResponse().getResult().getOutput().getText();

		return new AskResponse(answer, toSources(retrievedDocuments(response)));
	}

	/**
	 * Lấy đúng những đoạn mà advisor đã thực sự đưa vào prompt. Trước đây phần này chạy một
	 * similaritySearch riêng — vừa tốn thêm một lần gọi API embedding cho mỗi câu hỏi,
	 * vừa trả về cả những đoạn mà câu trả lời không hề dùng tới.
	 */
	@SuppressWarnings("unchecked")
	private List<Document> retrievedDocuments(ChatClientResponse response) {
		Object documents = response.context().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
		return documents instanceof List<?> list ? (List<Document>) list : List.of();
	}

	/** Gộp nhiều đoạn của cùng một tài liệu thành một dòng nguồn duy nhất. */
	private List<AskResponse.Source> toSources(List<Document> used) {
		Map<String, Double> bestScoreByTitle = new LinkedHashMap<>();
		for (Document d : used) {
			String title = String.valueOf(d.getMetadata().get("title"));
			bestScoreByTitle.merge(title, d.getScore(), Math::max);
		}

		return bestScoreByTitle.entrySet().stream()
				.map(e -> new AskResponse.Source(e.getKey(), e.getValue()))
				.toList();
	}
}
