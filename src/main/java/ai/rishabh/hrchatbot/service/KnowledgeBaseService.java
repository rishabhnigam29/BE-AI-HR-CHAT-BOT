package ai.rishabh.hrchatbot.service;

import ai.rishabh.hrchatbot.entity.UploadedFile;
import ai.rishabh.hrchatbot.repository.UploadedFileRepository;
import ai.rishabh.hrchatbot.util.DocumentUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {
    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final UploadedFileRepository uploadedFileRepository;

    private void addDocuments(List<Document> documents){
        log.info("Adding {} documents to the vector store", documents.size());
        vectorStore.add(documents);
    }

    public void addDocuments(Resource resource){
        String filename = resource.getFilename();
        String docId = UUID.randomUUID().toString();

        //Add doc in vector store
        List<Document> documents = DocumentUtils
                .getDocsFromPdf(resource, chatModel, docId, filename);
        addDocuments(documents);

        //Add doc metadata in postgresql
        uploadedFileRepository.save(UploadedFile
                .builder()
                .fileName(filename)
                .docId(docId)
                .uploadedAt(LocalDateTime.now())
                .build());
    }

    public List<UploadedFile> getAllFiles(){
        return uploadedFileRepository.findAll();
    }

    @Transactional
    public void deleteDocuments(String docId){
        vectorStore.delete(new Filter.Expression(Filter.ExpressionType.EQ,
                new Filter.Key("docId"), new Filter.Value(docId)));
        log.info("Deleted documents with docId: {} from vector store", docId);

        uploadedFileRepository.deleteByDocId(docId);
        log.info("Deleted documents with docId: {} from relation db", docId);
    }
}
