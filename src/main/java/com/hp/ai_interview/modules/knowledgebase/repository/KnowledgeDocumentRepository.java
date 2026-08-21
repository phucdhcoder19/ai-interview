package com.hp.ai_interview.modules.knowledgebase.repository;
import com.hp.ai_interview.modules.knowledgebase.model.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
  
}
