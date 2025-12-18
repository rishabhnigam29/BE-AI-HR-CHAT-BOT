package ai.rishabh.hrchatbot.controller;

import ai.rishabh.hrchatbot.entity.UploadedFile;
import ai.rishabh.hrchatbot.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public String addBasicTraining(@RequestParam("file") MultipartFile file){
        knowledgeBaseService.addDocuments(file.getResource());
        return "Documents added successfully!";
    }

    @GetMapping
    public List<UploadedFile> getAllFiles(){
        return knowledgeBaseService.getAllFiles();
    }

    @DeleteMapping
    public String deleteFile(@RequestParam("docId") String docId){
        knowledgeBaseService.deleteDocuments(docId);
        return "Document successfully deleted ";
    }
}
