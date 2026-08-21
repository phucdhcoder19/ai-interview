package com.hp.ai_interview.modules.knowledgebase.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String source;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected KnowledgeDocument() {
	}

	public KnowledgeDocument(String title, String source) {
		this.title = title;
		this.source = source;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getSource() {
		return source;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
