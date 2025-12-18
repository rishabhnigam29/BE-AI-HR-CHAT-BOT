package ai.rishabh.hrchatbot.util;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentUtils {
    public static List<Document> getDocsFromPdf(Resource pdfResource,
                                                ChatModel chatModel,
                                                String docId,
                                                String fileName){

        // Step 1: Extract raw text
        TikaDocumentReader tikaReader = new TikaDocumentReader(pdfResource);
        List<Document> rawDocs = tikaReader.read();

        //Step 2: Split by tokens
        List<Document> docsAfterSplitting= splitDocs(rawDocs);

        //Step 3: Add metadata (docId + filename)
        List<Document> docsWithMetaData = enrichMetaDataWithDocDetails(docId,
                fileName, docsAfterSplitting);

        //Step 4: Enrich with keyword
        List<Document> keyWordEnriched = enrichMetaDataWithKeyWords(chatModel,
                docsWithMetaData);

        //Step 5: Enrich with Summaries
        return enrichMetaDataWithSummaries(chatModel, keyWordEnriched);
    }

    private static List<Document> enrichMetaDataWithDocDetails(String docId, String fileName, List<Document> docsAfterSplitting) {
        return docsAfterSplitting.stream()
                .peek( docs -> {
                    docs.getMetadata().put("docId", docId);
                    docs.getMetadata().put("source", fileName);
                }).collect(Collectors.toList());
    }

    private static List<Document> enrichMetaDataWithSummaries(ChatModel chatModel,
                                                              List<Document> keyWordEnriched) {
        return new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.CURRENT))
                .apply(keyWordEnriched);
    }

    private static List<Document> enrichMetaDataWithKeyWords(ChatModel chatModel,
                                                             List<Document> docsAfterSplitting) {
        return KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build()
                .apply(docsAfterSplitting);
    }

    private static List<Document> splitDocs(List<Document> rawDocs) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(rawDocs);
    }
}
