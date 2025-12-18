package ai.rishabh.hrchatbot.service;

import ai.rishabh.hrchatbot.response.ConversationHistory;
import ai.rishabh.hrchatbot.response.ConversationWithTitle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final TreeMap<String, ConversationWithTitle> conversationWithTitleMap = new TreeMap<>();
    @Value("classpath:/prompts/rag-system-message.st")
    private Resource ragSystemResource;

    @Value("classpath:/prompts/conversation-title-system-message.st")
    private Resource conversationTitleSystemResource;

    public String startConversation(){
        String conversationId = UUID.randomUUID().toString();
        chatMemoryRepository.saveAll(conversationId, new ArrayList<>());
        return conversationId;
    }

    public Flux<String> retrieveAnswer(String conversationId,
                                       String userQuery){
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor =
                RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory).build();

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(chatMemoryAdvisor, retrievalAugmentationAdvisor)
                .defaultSystem(ragSystemResource)
                .build();

        return chatClient.prompt()
                .advisors(a-> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(userQuery)
                .stream()
                .content();
    }

    public List<ConversationHistory> getConversationHistory(String conversationId){
        List<Message> conversationHistoryById = chatMemoryRepository
                .findByConversationId(conversationId);

        return conversationHistoryById.stream()
                .map(message -> new ConversationHistory(message.getMessageType().getValue(),
                        message.getText()))
                .collect(Collectors.toList());
    }

    public List<ConversationWithTitle> getConversationIdsWithTitle(){
        List<String> conversationIds = chatMemoryRepository.findConversationIds();

        conversationIds.forEach(id-> {
            if(!conversationWithTitleMap.containsKey(id) ||
            conversationWithTitleMap.get(id).title() == null ||
            conversationWithTitleMap.get(id).title().equalsIgnoreCase("New Chat")){
                conversationWithTitleMap.put(id, new ConversationWithTitle(id,
                        generateConversationTitle(chatMemoryRepository.findByConversationId(id))));
            }
        });
        List<ConversationWithTitle> conversationWithTitles = new ArrayList<>(
                conversationWithTitleMap.values());
        Collections.reverse(conversationWithTitles);
        return conversationWithTitles;
    }

    private String generateConversationTitle(List<Message> messages) {

        if (CollectionUtils.isEmpty(messages)) {
            return "New Chat";
        }

        String conversation = messages.stream()
                .map(m -> m.getMessageType().getValue() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        return ChatClient.builder(chatModel).build()
                .prompt()
                .system(conversationTitleSystemResource)
                .user("Conversation: " + conversation)
                .call().content();
    }

    public void deleteConversation(String conversationId){
        chatMemoryRepository.deleteByConversationId(conversationId);
        conversationWithTitleMap.remove(conversationId);
    }
}
