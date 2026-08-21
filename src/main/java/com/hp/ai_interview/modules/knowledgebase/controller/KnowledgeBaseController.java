package com.hp.ai_interview.modules.knowledgebase.controller;

import com.hp.ai_interview.common.result.Result;
import com.hp.ai_interview.modules.knowledgebase.model.AskRequest;
import com.hp.ai_interview.modules.knowledgebase.model.AskResponse;
import com.hp.ai_interview.modules.knowledgebase.model.IngestRequest;
import com.hp.ai_interview.modules.knowledgebase.model.IngestResponse;
import com.hp.ai_interview.modules.knowledgebase.service.KnowledgeBaseService;
import jakarta.validation.Valid;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag")
public class KnowledgeBaseController {

	private final KnowledgeBaseService knowledgeBaseService;

	public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
		this.knowledgeBaseService = knowledgeBaseService;
	}

	@PostMapping("/ingest")
	public Result<IngestResponse> ingest(@Valid @RequestBody IngestRequest request) {
		return Result.ok(knowledgeBaseService.ingest(request.title(), request.text()));
	}
	@PostMapping("/upload")
	public Result<IngestResponse> upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "title", required = false) String title) throws IOException {
		return Result.ok(knowledgeBaseService.ingestFile(file, title));
	}
	@PostMapping("/ask")
	public Result<AskResponse> ask(@Valid @RequestBody AskRequest request) {
		return Result.ok(knowledgeBaseService.ask(request.question()));
	}
}
