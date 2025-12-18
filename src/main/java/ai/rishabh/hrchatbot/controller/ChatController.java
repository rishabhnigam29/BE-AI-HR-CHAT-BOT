package ai.rishabh.hrchatbot.controller;

import ai.rishabh.hrchatbot.response.ConversationHistory;
import ai.rishabh.hrchatbot.response.ConversationWithTitle;
import ai.rishabh.hrchatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/new")
    public Map<String, String> createConversation() {
        return Map.of("conversationId", chatService.startConversation());
    }

    @PostMapping
    public Flux<String> chat(@RequestParam String conversationId,
                             @RequestParam String userQuery) {
        if(StringUtils.isEmpty(conversationId)) {
            conversationId = chatService.startConversation();
        }
        return chatService.retrieveAnswer(conversationId, userQuery);
    }

    @GetMapping("/history/{conversationId}")
    public List<ConversationHistory> getConversationHistory(@PathVariable String conversationId) {
        return chatService.getConversationHistory(conversationId);
    }

    @GetMapping
    public List<ConversationWithTitle> getConversationIdsWithTitles() {
        return chatService.getConversationIdsWithTitle();
    }

    @DeleteMapping
    public String deleteConversation(@RequestParam String conversationId) {
        chatService.deleteConversation(conversationId);
        return "Conversation deleted successfully!";
    }
}
