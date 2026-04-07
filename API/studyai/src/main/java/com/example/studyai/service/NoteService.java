package com.example.studyai.service;

import com.example.studyai.dto.NoteDTO;
import com.example.studyai.model.Note;
import com.example.studyai.model.Subject;
import com.example.studyai.model.User;
import com.example.studyai.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public Note createNote(User user, Subject subject, String title, String s3Key,
            Note.FileType fileType) {
        Note note = new Note();
        note.setUser(user);
        note.setSubject(subject);
        note.setTitle(title);
        note.setS3Key(s3Key);
        note.setFileType(fileType);
        return noteRepository.save(note);
    }

    public Optional<Note> getNoteById(UUID id) {
        return noteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Note> getSubjectNotes(UUID subjectId) {
        // Use eager-loading query to avoid N+1 queries and PgBouncer conflicts
        return noteRepository.findBySubjectIdWithUserEager(subjectId);
    }

    @Transactional(readOnly = true)
    public List<Note> getUserNotes(UUID userId) {
        // Use eager-loading query to avoid N+1 queries and PgBouncer conflicts
        return noteRepository.findByUserIdWithUserAndSubjectEager(userId);
    }

    public Note updateNote(UUID id, String title) {
        Optional<Note> note = noteRepository.findById(id);
        if (note.isPresent()) {
            Note n = note.get();
            n.setTitle(title);
            return noteRepository.save(n);
        }
        throw new RuntimeException("Note not found");
    }

    public void deleteNote(UUID id) {
        noteRepository.deleteById(id);
    }

    /**
     * Get status of a note (processing status, extracted text, etc.)
     * 
     * @param id Note ID
     * @return Note with all details
     */
    public Note getNoteStatus(UUID id) {
        Optional<Note> note = noteRepository.findById(id);
        if (note.isEmpty()) {
            throw new RuntimeException("Note not found");
        }
        return note.get();
    }

    /**
     * Upload a file and create a note
     * Called when a file is uploaded to the system
     * Note: Text extraction is handled by FastAPI, not stored in SpringBoot
     * 
     * @param user     User uploading the file
     * @param subject  Subject for the note
     * @param title    Title of the note
     * @param s3Key    S3 storage key
     * @param fileType Type of file (pdf, docx)
     * @return Created note
     */
    public Note uploadFile(User user, Subject subject, String title, String s3Key,
            String fileType) {
        Note.FileType type = Note.FileType.valueOf(fileType.toLowerCase());
        return createNote(user, subject, title, s3Key, type);
    }

    public NoteDTO convertToDTO(Note note) {
        return new NoteDTO(note.getId(), note.getUser().getId(), note.getSubject().getId(),
                note.getTitle(), note.getS3Key(), note.getFileType().toString(),
                note.getCreatedAt());
    }

    public List<NoteDTO> convertToDTOList(List<Note> notes) {
        return notes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
