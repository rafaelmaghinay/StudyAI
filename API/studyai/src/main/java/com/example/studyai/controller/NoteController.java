package com.example.studyai.controller;

import com.example.studyai.client.FastApiDocumentClient;
import com.example.studyai.dto.NoteDTO;
import com.example.studyai.model.Note;
import com.example.studyai.model.Subject;
import com.example.studyai.model.User;
import com.example.studyai.service.NoteService;
import com.example.studyai.service.SubjectService;
import com.example.studyai.service.UserService;
import com.example.studyai.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @Autowired
    private FastApiDocumentClient fastApiDocumentClient;

    @PostMapping
    public ResponseEntity<?> createNote(@RequestBody Map<String, Object> request) {
        UUID userId = UUID.fromString((String) request.get("userId"));
        UUID subjectId = UUID.fromString((String) request.get("subjectId"));
        String title = (String) request.get("title");
        String s3Key = (String) request.get("s3Key");
        String fileType = (String) request.get("fileType");

        User user = userService.getUserById(userId);
        Optional<Subject> subject = subjectService.getSubjectById(subjectId);

        if (subject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Note.FileType type = Note.FileType.valueOf(fileType);
        Note note = noteService.createNote(user, subject.get(), title, s3Key, type);
        return ResponseEntity.ok(new ApiResponse<>(true, "Note created successfully", noteService.convertToDTO(note)));
    }

    // IMPORTANT: Put /subject/{subjectId} and /user/{userId} BEFORE /{id} to avoid
    // path variable conflict
    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<?> getSubjectNotes(@PathVariable String subjectId) {
        try {
            UUID subjectUuid = UUID.fromString(subjectId);
            List<Note> notes = noteService.getSubjectNotes(subjectUuid);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Notes retrieved successfully", noteService.convertToDTOList(notes)));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for subjectId: {}", subjectId);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid subject ID format", null));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserNotes(@PathVariable String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            List<Note> notes = noteService.getUserNotes(userUuid);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Notes retrieved successfully", noteService.convertToDTOList(notes)));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for userId: {}", userId);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid user ID format", null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNote(@PathVariable String id) {
        try {
            UUID noteId = UUID.fromString(id);
            Optional<Note> note = noteService.getNoteById(noteId);
            if (note.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Note retrieved successfully", noteService.convertToDTO(note.get())));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for note id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid note ID format", null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            UUID noteId = UUID.fromString(id);
            String title = request.get("title");
            Note note = noteService.updateNote(noteId, title);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, "Note updated successfully", noteService.convertToDTO(note)));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for note id: {}", id);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid note ID format", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        try {
            UUID noteId = UUID.fromString(id);
            noteService.deleteNote(noteId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for note id: {}", id);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Upload and create a note from a file
     * Sends file to FastAPI for document processing and S3 storage
     *
     * @param userId    ID of the user uploading
     * @param subjectId ID of the subject
     * @param noteTitle Title for the note
     * @param file      The file to upload
     * @return Created note with S3 metadata
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadNote(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String noteTitle,
            @RequestParam(required = false) MultipartFile file) {
        try {
            logger.info("Upload note request received");
            logger.debug("Parameters - userId: [{}], subjectId: [{}], noteTitle: [{}], file: [{}]",
                    userId, subjectId, noteTitle, file != null ? file.getOriginalFilename() : "null");

            // Validate required parameters with detailed error messages
            if (userId == null || userId.trim().isEmpty()) {
                logger.error("Missing or empty userId parameter");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User ID is required and must not be empty", null));
            }
            if (subjectId == null || subjectId.trim().isEmpty()) {
                logger.error("Missing or empty subjectId parameter");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Subject ID is required and must not be empty", null));
            }
            if (noteTitle == null || noteTitle.trim().isEmpty()) {
                logger.error("Missing or empty noteTitle parameter");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Note title is required and must not be empty", null));
            }
            if (file == null || file.isEmpty()) {
                logger.error("No file provided in request");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "File is required and must not be empty", null));
            }

            // Validate UUID formats
            UUID userUuid;
            UUID subjectUuid;
            try {
                userUuid = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format for userId: {}", userId);
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid user ID format: " + userId, null));
            }

            try {
                subjectUuid = UUID.fromString(subjectId);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid UUID format for subjectId: {}", subjectId);
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Invalid subject ID format: " + subjectId, null));
            }

            User user = userService.getUserById(userUuid);
            if (user == null) {
                logger.error("User not found: {}", userUuid);
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "User not found: " + userUuid, null));
            }

            Optional<Subject> subject = subjectService.getSubjectById(subjectUuid);
            if (subject.isEmpty()) {
                logger.warn("Subject not found: {}", subjectUuid);
                return ResponseEntity.notFound().build();
            }

            // Generate temporary note ID for FastAPI reference
            UUID noteId = UUID.randomUUID();

            // Call FastAPI to upload document and extract text
            logger.info("Calling FastAPI to upload document: noteId={}, fileName={}", noteId,
                    file.getOriginalFilename());
            Map<String, Object> fastApiResponse = fastApiDocumentClient.uploadDocument(
                    file,
                    userId,
                    subjectId,
                    noteId.toString());

            // Extract response data
            String s3Key = (String) fastApiResponse.get("s3_key");
            String fileType = (String) fastApiResponse.get("file_type");
            logger.info("Document processed by FastAPI. S3 Key: {}, File Type: {}", s3Key, fileType);

            // Create note in database
            // Note: Text extraction is handled by FastAPI, not stored in SpringBoot
            Note note = noteService.uploadFile(user, subject.get(), noteTitle, s3Key, fileType);
            logger.info("Note saved to database: {}", note.getId());

            // Get presigned URL for download
            String presignedUrl = fastApiDocumentClient.getPresignedUrl(noteId.toString(), s3Key, 3600);

            // Convert to DTO with download URL
            NoteDTO noteDTO = noteService.convertToDTO(note);
            noteDTO.setDownloadUrl(presignedUrl);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Note uploaded successfully", noteDTO));

        } catch (RestClientException e) {
            logger.error("FastAPI communication error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    new ApiResponse<>(false, "Error communicating with document service: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid UUID format: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Upload failed: " + e.getMessage(), null));
        }
    }

    /**
     * Get the status/details of a note
     * Useful for checking if file processing is complete
     *
     * @param id Note ID
     * @return Note with all processing details
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<NoteDTO> getNoteStatus(@PathVariable UUID id) {
        try {
            Note note = noteService.getNoteStatus(id);
            return ResponseEntity.ok(noteService.convertToDTO(note));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get a presigned URL for downloading a note
     * The URL can be used directly to download the file from S3
     *
     * @param id Note ID
     * @return Presigned URL for downloading
     */
    @GetMapping("/{id}/download-url")
    public ResponseEntity<?> getDownloadUrl(@PathVariable UUID id) {
        try {
            Optional<Note> note = noteService.getNoteById(id);
            if (note.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String s3Key = note.get().getS3Key();
            if (s3Key == null || s3Key.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Note does not have a file attached", null));
            }

            logger.info("Getting download URL for note: {}", id);
            String presignedUrl = fastApiDocumentClient.getPresignedUrl(id.toString(), s3Key, 3600);

            return ResponseEntity.ok(new ApiResponse<>(true, "Download URL generated", Map.of(
                    "noteId", id,
                    "downloadUrl", presignedUrl,
                    "expiresIn", 3600)));

        } catch (RestClientException e) {
            logger.error("Error getting presigned URL from FastAPI: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiResponse<>(false, "Error generating download URL: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error getting download URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error generating download URL", null));
        }
    }

    /**
     * Delete a note and its associated file from S3
     * 
     * @param id Note ID
     * @return Success message
     */
    @DeleteMapping("/{id}/delete-with-file")
    public ResponseEntity<?> deleteNoteWithFile(@PathVariable UUID id) {
        try {
            Optional<Note> note = noteService.getNoteById(id);
            if (note.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String s3Key = note.get().getS3Key();
            if (s3Key != null && !s3Key.isEmpty()) {
                logger.info("Deleting note file from S3: {}", s3Key);
                fastApiDocumentClient.deleteDocument(id.toString(), s3Key);
            }

            noteService.deleteNote(id);
            logger.info("Note deleted: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Note deleted successfully", null));

        } catch (RestClientException e) {
            logger.error("Error deleting note file from S3: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiResponse<>(false, "Error deleting note file: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error deleting note: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting note", null));
        }
    }

    // Helper methods
    private String generateS3Key(String filename) {
        return "notes/" + UUID.randomUUID() + "/" + filename;
    }

    private String getFileType(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "pdf";
    }
}
