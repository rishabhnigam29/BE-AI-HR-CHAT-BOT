package ai.rishabh.hrchatbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadedFile {
    @Id
    private String docId;
    private String fileName;
    private LocalDateTime uploadedAt;
}
